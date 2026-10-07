package app.trollfoss.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.trollfoss.audio.MusicPlayer
import app.trollfoss.audio.MusicTheme
import app.trollfoss.audio.Soundscape
import app.trollfoss.audio.SoundscapePlayer
import app.trollfoss.audio.Sfx
import app.trollfoss.audio.SoundFx
import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import app.trollfoss.data.WorldHistory
import app.trollfoss.domain.Progression
import app.trollfoss.domain.ToyReward
import app.trollfoss.domain.ThingType
import app.trollfoss.domain.edit
import app.trollfoss.domain.Festival
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.HouseKeys
import app.trollfoss.domain.Look
import app.trollfoss.domain.Maalform
import app.trollfoss.domain.Mode
import app.trollfoss.domain.Passage
import app.trollfoss.domain.Person
import app.trollfoss.domain.Players
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Season
import app.trollfoss.domain.SeasonChoice
import app.trollfoss.domain.Seasons
import app.trollfoss.domain.Secrets
import app.trollfoss.domain.Sim
import app.trollfoss.domain.Species
import app.trollfoss.domain.Weather
import app.trollfoss.domain.World
import app.trollfoss.domain.WorldFactory
import app.trollfoss.ui.components.Feedback
import app.trollfoss.ui.play.Engine
import app.trollfoss.ui.play.EngineHost
import app.trollfoss.update.AppUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import app.trollfoss.domain.Task
import java.io.File
import java.time.LocalDate
import java.util.EnumMap

/**
 * Holds the village, saves it, plays sound and music, and connects the engine to the screens. One
 * instance lives for the whole app.
 */
class TrollfossViewModel(application: Application) : AndroidViewModel(application), EngineHost {
    private val store = WorldStore(File(application.filesDir, "trollfoss.json"))
    private val photoDir = File(application.filesDir, "photos").apply { mkdirs() }

    var world: World
        private set
    var settings by mutableStateOf(Settings())
        private set
    var sim: Sim
        private set

    val sfx = SoundFx(application)

    /** The builder panel of Mitt hus (see docs/BYGG.md): open or not, and which tab. */
    val mineUi = app.trollfoss.ui.screens.MineUi()
    private val music = MusicPlayer(application.cacheDir)
    /** The soft place sounds under the music; they follow the music switch. */
    private val ambient = SoundscapePlayer()
    val updater = AppUpdater(application, viewModelScope)

    private val navigation = ScreenHistory()
    var screen by mutableStateOf<Screen>(navigation.current)
        private set
    var place by mutableStateOf(PlaceId.HOME)
        private set
    /** The season and the feast showing now: the calendar's, unless the grown-ups chose otherwise. */
    var season by mutableStateOf(Season.SUMMER)
        private set
    var festival by mutableStateOf(Festival.NONE)
        private set
    var night by mutableStateOf(false)
        private set
    var weather by mutableStateOf(Weather.SUN)
        private set
    var found by mutableIntStateOf(0)
        private set
    /** Golden keys of the big house found so far (of five). */
    var houseKeys by mutableIntStateOf(0)
        private set
    var discoveries by mutableIntStateOf(0)
        private set
    var telescopeOpen by mutableStateOf(false)
    var splash by mutableStateOf(true)
    var photos by mutableStateOf(listPhotos())
        private set
    /** Bumps when something new lands in the discovery book, so the book button can wiggle. */
    var bookPulse by mutableIntStateOf(0)
        private set
    /** The task just finished, for its celebration on the play screen; null when none. */
    var taskDone by mutableStateOf<Task?>(null)
    /** Tasks left on the board and stickers earned, for the buttons. */
    var tasksLeft by mutableIntStateOf(3)
        private set
    var stickers by mutableIntStateOf(0)
        private set
    var undoVersion by mutableIntStateOf(0)
        private set
    var tasksVersion by mutableIntStateOf(0)
        private set
    private val history=WorldHistory({ world }) { undoVersion++ }
    val canUndo: Boolean get() { undoVersion; return history.available && !sim.mine.busy && engine?.touching != true }
    var levelGift by mutableIntStateOf(0)
        private set
    private var seenLevel=1

    /** Stickers flying to the book right now, fed one at a time from [firstQueue]. */
    val firstPops = androidx.compose.runtime.mutableStateListOf<FirstPop>()
    private val firstQueue = FirstQueue()
    /** Where the book and furniture buttons are on screen, so stickers and gifts know where to fly. */
    var bookAnchor by mutableStateOf<androidx.compose.ui.geometry.Offset?>(null)
    var furnishAnchor by mutableStateOf<androidx.compose.ui.geometry.Offset?>(null)
    /** True while stickers wait to fly, so the overlay only asks for frames when it has something to show. */
    var firstsPending by mutableStateOf(false)
        private set
    override fun first(first: app.trollfoss.domain.First, x: Float, y: Float) { firstQueue.push(first, x, y); firstsPending = true; bookPulse++; refreshTasks(); scheduleSave() }
    fun tickFirsts(now: Float) { firstQueue.due(now)?.let { firstPops += it }; firstsPending = firstQueue.size > 0 }
    override fun busy(): Boolean = levelGift > 0 || parcelOpening > 0 || screen != Screen.Play

    /** Bumps when the world is replaced, so engines are rebuilt. */
    var generation by mutableIntStateOf(0)
        private set

    var playersVersion by mutableIntStateOf(0)
        private set
    private var visiblePlayerIds: List<Int> = emptyList()

    var engine: Engine? = null
        private set
    private val cams = EnumMap<PlaceId, Float>(PlaceId::class.java)
    private var radioOn = false
    private var saveJob: Job? = null

    val feedback = object : Feedback {
        override fun sfx(effect: Sfx, volume: Float, rate: Float) = this@TrollfossViewModel.sfx(effect, volume, rate)
    }

    init {
        val saved = store.load()
        world = saved?.world ?: WorldFactory.create()
        // A brand-new world starts on the empty plot of Mitt hus, with the builder panel open (an old save is left as it is).
        if (saved == null) startOnPlot()
        world.visited += world.place
        settings = saved?.settings ?: Settings()
        sim = Sim(world)
        sim.journal=history
        Progression.remember(world)
        seenLevel=Progression.level(world)
        if(seenLevel>=2 && "gift:level:seen:$seenLevel" !in world.flags) levelGift=seenLevel
        wireTasks()
        syncFromWorld()
        applySettings()
        if (Players.CHOSEN !in world.flags) screen = navigation.open(Screen.Players)
        if (Players.activeTeam(world).any { it.place != place || it.mode == Mode.BAG }) {
            Players.arrive(world, place, arrivalCenter(place))
        }
    }

    /** A new world begins on the plot: the family comes along, and the builder panel is open. */
    private fun startOnPlot() {
        world.place = PlaceId.MINE_YARD
        for ((i, name) in listOf("Hedda", "Øyvind").withIndex()) {
            val p = world.people().firstOrNull { it.name == name } ?: continue
            app.trollfoss.domain.House.moveTo(world, p, PlaceId.MINE_YARD, 3.35f + i * 0.4f, 0.9f - i * 0.03f)
        }
        mineUi.open = true
    }

    private fun syncFromWorld() {
        sim.today = LocalDate.now().toEpochDay()
        place = world.place
        night = world.night
        weather = world.weather
        found = world.found.size
        houseKeys = HouseKeys.found(world)
        discoveries = world.discoveries.size
    }

    /** The task board tells the screens when a task is done. */
    private fun wireTasks() {
        sim.tasks.onDone = { task ->
            taskDone = task
            sfx(Sfx.FANFARE, 0.9f)
            bookPulse++
            refreshTasks()
            scheduleSave()
        }
        refreshTasks()
    }

    fun refreshTasks() {
        tasksVersion++
        Progression.remember(world)
        tasksLeft = sim.tasks.board().count { !sim.tasks.done(it) }
        stickers = world.stickers.size
        val level=Progression.level(world)
        if(level>seenLevel) levelGift=level
        seenLevel=level
    }

    /** Deals three new tasks once the board is done. */
    fun newTasks() {
        sim.tasks.deal()
        sfx(Sfx.MAGIC, 0.8f)
        refreshTasks()
        scheduleSave()
    }

    fun swapTask(task:Task) { sim.tasks.swap(task);refreshTasks();scheduleSave() }
    /** Opens the parcel of [levelGift]: while playing in a built place, its first toy is set out on the spot. Returns true when one was. */
    fun openParcel(): Boolean {
        val level=levelGift;if(level<=0) return false
        val unbuilt=place==PlaceId.MINE_UPPER && !world.mine.upperBuilt || place==PlaceId.MINE_GROUND && !world.mine.started
        val placed=screen==Screen.Play && !unbuilt && run {
            val x=engine?.takeIf { it.place==place }?.let { it.cam+it.visibleViewport/2 } ?: 1.15f
            sim.toys.openGift(level,place,x)!=null
        }
        if(placed) changed()
        parcelOpening=level
        sfx(Sfx.FANFARE,0.9f);haptic();dismissLevelGift()
        return placed
    }
    /** The level whose parcel is popping open right now; the parcel stays on screen until its toys have flown. */
    var parcelOpening by mutableIntStateOf(0)
        private set
    fun parcelDone() { parcelOpening=0 }
    fun dismissLevelGift() { if(levelGift>0) world.flags+="gift:level:seen:$levelGift";levelGift=0;scheduleSave() }
    fun undoEdit() {
        if(!canUndo) return
        engine?.cancel()
        val restored=history.undo() ?: return
        world=restored;sim=Sim(world);sim.journal=history;wireTasks()
        if(world.place==PlaceId.MINE_GROUND && !world.mine.started || world.place==PlaceId.MINE_UPPER && !world.mine.upperBuilt) world.place=PlaceId.MINE_YARD
        if(Players.activeTeam(world).any { it.place!=world.place }) Players.arrive(world,world.place,arrivalCenter(world.place))
        engine=null;generation++;syncFromWorld();playersVersion++;changed()
        sfx(Sfx.MAGIC,0.65f)
    }
    fun tryToy(reward:ToyReward):Boolean {
        val to=if(place==PlaceId.MINE_UPPER && !world.mine.upperBuilt || place==PlaceId.MINE_GROUND && !world.mine.started) PlaceId.MINE_YARD else place
        val x=engine?.takeIf { it.place==to }?.let { it.cam+it.visibleViewport/2 } ?: 1.15f
        val f=sim.toys.claim(reward,to,x) ?: return false
        sim.toys.supply(f);changed();travelPlayCard(to,f.x);return true
    }
    var guidedTask by mutableStateOf<Task?>(null)
    var bookTab by mutableStateOf(BookTab.FIRSTS)
    fun openGifts() { open(Screen.Tasks); bookTab = BookTab.GIFTS }
    fun openFirsts() { open(Screen.Tasks); bookTab = BookTab.FIRSTS }
    fun goRecipe(recipe: app.trollfoss.domain.Recipe, fixture: app.trollfoss.domain.Fixture) {
        mineUi.open=false;engine?.designMode=false
        sim.edit {
            recipe.inputs.forEachIndexed { i, type ->
                val key="recipe:${recipe.key}:$i"
                val old=world.bodies[world.toyInputs[key]] as? app.trollfoss.domain.Thing
                val thing=old?.takeIf { it.type==type && it.used==0 }
                    ?: world.addThing(type,0,fixture.place,fixture.x,PlaceId.FRONT-0.03f).also { world.toyInputs[key]=it.id }
                if(!thing.held && thing.mode in listOf(Mode.FREE,Mode.BAG)) {
                    thing.place=fixture.place;thing.mode=Mode.FREE;thing.holder=-1;thing.inside=-1
                    thing.restOwner=-2;thing.resting=false;thing.vx=0f;thing.vy=0f
                    thing.x=(fixture.x-0.35f+i*0.15f).coerceIn(0.1f,fixture.place.width-0.1f)
                    thing.y=PlaceId.FRONT-0.03f;thing.ground=thing.y
                }
            }
        }
        changed();travelPlayCard(fixture.place,fixture.x)
    }

    fun goTask(task:Task) {
        mineUi.open = false
        engine?.designMode = false
        guidedTask = task
        val to=task.place ?: place
        val fixture=world.fixturesIn(to).firstOrNull { it.type==task.fixture }
        val x=fixture?.x ?: world.people().firstOrNull { it.place==to && it.species==task.species }?.x ?: 1.15f
        // Only these introductory raw materials are supplied. An owned tool is never taken from a friend.
        val type=when(task.id) { "feed_horse" -> ThingType.CARROT;"feed_dog" -> ThingType.SAUSAGE;"crown" -> ThingType.CROWN;else -> null }
        if(type!=null) sim.edit {
            val key="task:${task.id}"
            val old=world.bodies[world.toyInputs[key]] as? app.trollfoss.domain.Thing
            val t=old?.takeIf { it.type==type && it.used==0 } ?: world.addThing(type,0,to,x-0.22f,to.floor).also { world.toyInputs[key]=it.id }
            if(!t.held && t.mode in listOf(Mode.FREE,Mode.BAG)) { t.place=to;t.mode=Mode.FREE;t.holder=-1;t.x=(x-0.22f).coerceAtLeast(0.1f);t.y=to.floor;t.ground=t.y;t.resting=false;t.restOwner=-2 }
        }
        changed();travelPlayCard(to,x)
        // The helper gives a moving animal a calm pause while the child tries the pictured action.
        world.people().filter { it.place==to && it.species==task.species && it.mode==Mode.FREE && !it.held }.forEach {
            it.anim.walkTo=Float.NaN;it.anim.nextWalk=30f;it.anim.pose=app.trollfoss.domain.Pose.STAND;it.anim.auto=0
        }
    }

    /** The device was shaken: the world shakes, but only while the play screen is showing. */
    fun shake() {
        if (screen != Screen.Play || splash) return
        sim.quake(place)
    }

    /** A small sticker-shaped «task» that only exists to celebrate an Easter egg in the banner. */
    private val eggTask = Task("egg", null, 1, icon = "egg") { _, _, _, _, _ -> false }

    override fun egg(id: String) {
        taskDone = eggTask
        sfx(Sfx.FANFARE, 0.9f)
        bookPulse++
        refreshTasks()
        scheduleSave()
    }

    private fun applySettings() {
        sfx.enabled = settings.sound
        music.enabled = settings.music
        ambient.enabled = settings.music
        val today = java.time.LocalDate.now().toEpochDay()
        season = debugSeason ?: Seasons.resolve(settings.season, today)
        festival = debugFestival ?: if (settings.festive) Seasons.festival(today) else Festival.NONE
    }

    /** Debug builds only: a season and a feast forced by the debug intent (never saved). */
    private var debugSeason: Season? = null
    private var debugFestival: Festival? = null

    private var debugSkip = 0

    /** The engine for the place on screen. A new one is made when the child travels. */
    fun engineFor(place: PlaceId, motion: Boolean): Engine {
        engine?.let { cams[it.place] = it.cam }
        val start = cams[place] ?: defaultCam(place)
        app.trollfoss.ui.art.MineView.house = world.mine
        app.trollfoss.ui.art.ToyArt.photos=world.toyPhotos
        app.trollfoss.ui.art.CreativeArt.state=world.community
        app.trollfoss.ui.art.CreativeArt.bodies=world.bodies
        return Engine(world, place, sim, this, motion, start).also {
            pendingFocus?.let { x -> it.focusOn(x); pendingFocus = null }
            it.season = season
            it.festival = festival
            it.skip = debugSkip
            engine = it
            radioOn = sim.musicOn(place)
            updateMusic()
        }
    }

    private fun defaultCam(place: PlaceId): Float = when (place) {
        PlaceId.HOME -> 0.9f
        PlaceId.BEACH -> 0.5f
        PlaceId.FOREST -> 0.8f
        PlaceId.MOUNTAIN -> 0.6f
        PlaceId.FARM -> 0.1f
        PlaceId.MANOR_GROUND, PlaceId.MANOR_UPPER -> 0.4f
        PlaceId.MINE_YARD -> 1.0f
        PlaceId.MINE_GROUND, PlaceId.MINE_UPPER -> 0f
        else -> 0.3f
    }

    // ---------------------------------------------------------------------------------- navigation

    /** Bumps with every passage taken, so the play screen can play its curtain. */
    var passageStamp by mutableIntStateOf(0)
        private set
    private var pendingFocus: Float? = null
    var balloonDestination: PlaceId? = null
    fun flyTo(to: PlaceId) {
        balloonDestination = to
        open(Screen.Map)
    }
    fun landBalloon(to: PlaceId) {
        sim.firstTime(app.trollfoss.domain.First.BALLOON)
        if(to==PlaceId.CLOUD_ISLAND) sim.firstTime(app.trollfoss.domain.First.SKY_ISLAND)
        if(place==PlaceId.CLOUD_ISLAND && to==world.community.returnPlace) {
            val x=world.community.returnX
            world.community.returnPlace=null
            travelPlayCard(to,x)
            return
        }
        if(to==PlaceId.CLOUD_ISLAND && place!=to) {
            world.community.returnPlace=place
            world.community.returnX=engine?.let { it.cam+it.visibleViewport/2 } ?: 1f
        }
        travel(to)
    }


    fun travelPlayCard(to: PlaceId, x: Float = 1.15f) {
        pendingFocus = x
        travel(to)
        engine?.takeIf { it.place == to }?.focusOn(x)
    }

    override fun passage(passage: Passage, arrivalX: Float) {
        pendingFocus = arrivalX
        travel(passage.to)
        passageStamp++
    }

    fun travel(to: PlaceId) {
        engine?.let { cams[it.place] = it.cam }
        // Whatever the builder is busy with is finished before the child goes; a housewarming ends when they leave the house.
        sim.mine.finishJob()
        if (!to.mine) sim.mine.endParty()
        engine?.cancel()
        Players.arrive(world, to, pendingFocus ?: arrivalCenter(to))
        sim.community.follow(to)
        if (place != to) radioOn = false
        world.place = to
        place = to
        sim.visit(to)
        screen = navigation.arrive(Screen.Play)
        // A trip within the same place reuses its engine; consume the focus here too.
        engine?.takeIf { it.place == to }?.let { current ->
            pendingFocus?.let(current::focusOn)
            pendingFocus = null
        }
        sfx(Sfx.WHOOSH, 0.7f)
        updateMusic()
        scheduleSave()
    }

    fun travelMineRoom(to: PlaceId, slot: Int) {
        if (to != PlaceId.MINE_GROUND && to != PlaceId.MINE_UPPER) return
        pendingFocus = slot.coerceIn(0, app.trollfoss.domain.Mine.SLOTS - 1) * app.trollfoss.domain.Mine.SLOT_W + 1f
        travel(to)
        engine?.takeIf { it.place==to }?.selectRoom(slot)
    }

    fun open(target: Screen) {
        if (target == Screen.Tasks) bookTab = BookTab.FIRSTS
        if (target != Screen.Play) engine?.cancel()
        screen = navigation.open(target)
        updateMusic()
    }

    fun back() {
        if (screen == Screen.Players) world.flags.add(Players.CHOSEN)
        screen = navigation.back()
        updateMusic()
        scheduleSave()
    }

    private fun arrivalCenter(to: PlaceId): Float = when {
        to == PlaceId.MINE_GROUND || to == PlaceId.MINE_UPPER -> 1f
        to == PlaceId.BEACH -> 0.85f
        else -> ((cams[to] ?: defaultCam(to)) + (engine?.viewport ?: 2f).coerceAtMost(2f) / 2f)
            .coerceIn(0.3f, to.width - 0.3f)
    }

    fun togglePlayer(person: Person) {
        history.clear()
        Players.toggle(world, person)
        if (person.id in world.playerIds) engine?.invite(person)
        playersVersion++
        scheduleSave()
    }

    fun recallPlayers() {
        engine?.cancel()
        val x = engine?.let { it.cam + it.visibleViewport / 2f } ?: arrivalCenter(place)
        Players.arrive(world, place, x)
        scheduleSave()
    }

    fun recallPlayer(person: Person) {
        if (person.id !in world.playerIds) return
        Players.resume(world, person)
        engine?.invite(person)
        playersVersion++
        scheduleSave()
    }

    // ---------------------------------------------------------------------------------- world controls

    fun toggleNight() {
        world.night = !world.night
        night = world.night
        sim.lampsFollowNight(world.night)
        sfx(if (night) Sfx.CHIME else Sfx.MAGIC, 0.7f, if (night) 0.8f else 1.1f)
        updateMusic()
        scheduleSave()
    }

    fun cycleWeather() {
        val old = world.weather
        world.weather = Weather.entries[(old.ordinal + 1) % Weather.entries.size]
        weather = world.weather
        if (old == Weather.RAIN) engine?.rainbow = 1f
        sfx(when (weather) {
            Weather.RAIN -> Sfx.SPLASH
            Weather.SNOW -> Sfx.SPARKLE
            Weather.SUN -> Sfx.CHIME
        }, 0.6f)
        scheduleSave()
    }

    /** Makes a new figure (or updates one) from the workshop and lets it pop into the current place. */
    fun saveFigure(editId: Int?, look: Look, name: String) {
        history.clear()
        val existing = editId?.let { world.bodies[it] as? Person }
        if (existing != null) {
            existing.look = look.safe()
            existing.name = name.trim().take(24)
            existing.anim.sparkle = 1f
        } else {
            val e = engine
            val x = if (e != null) e.cam + e.viewport / 2 else place.width / 2
            val p = world.addPerson(Species.FOLK, look, Look.voiceFor(look), place, x, 0.4f, name.trim().take(24).ifBlank { freeName(look) })
            p.age = 0f
            p.anim.sparkle = 1f
            p.vy = -0.5f
            if (navigation.previousIsPlayers()) world.playerIds.add(p.id)
        }
        playersVersion++
        sfx(Sfx.FANFARE, 0.7f)
        screen = navigation.back()
        updateMusic()
        scheduleSave()
    }

    /** A name from the family list that no figure has yet. */
    fun freeName(look: Look): String {
        val taken = world.people().map { it.name }.toSet()
        val list = if (look.height < 0.95f) CHILD_NAMES else GROWN_NAMES
        return (list + CHILD_NAMES + GROWN_NAMES).firstOrNull { it !in taken } ?: "Venn"
    }

    fun folk(): List<Person> = world.people().filter { it.species == Species.FOLK }.sortedBy { it.id }

    fun resetWorld() {
        history.clear()
        world = WorldFactory.create()
        startOnPlot()
        sim = Sim(world)
        sim.journal=history
        wireTasks()
        cams.clear()
        engine = null
        syncFromWorld()
        generation++
        screen = navigation.arrive(Screen.Map)
        screen = navigation.open(Screen.Players)
        scheduleSave(immediate = true)
    }

    fun updateSettings(transform: (Settings) -> Settings) {
        settings = transform(settings)
        applySettings()
        if (settings.music) updateMusic()
        scheduleSave()
    }

    fun setMaalform(m: Maalform) = updateSettings { it.copy(maalform = m) }
    fun setSeason(s: SeasonChoice) = updateSettings { it.copy(season = s) }

    // ---------------------------------------------------------------------------------- photos

    fun savePhoto(image: ImageBitmap) {
        sfx(Sfx.SHUTTER, 0.9f)
        sim.tasks.record(app.trollfoss.domain.Deed.PHOTO, place)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val file = File(photoDir, "trollfoss-${System.currentTimeMillis()}.png")
                file.outputStream().use { image.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 92, it) }
                // Keep the album small: the 30 newest photos.
                photoDir.listFiles()?.sortedByDescending { it.lastModified() }?.drop(30)?.forEach { it.delete() }
            }
            withContext(Dispatchers.Main) { photos = listPhotos() }
        }
    }

    private fun listPhotos(): List<File> =
        (File(getApplication<Application>().filesDir, "photos").listFiles()?.filter { it.extension == "png" } ?: emptyList()).sortedByDescending { it.lastModified() }

    fun deletePhoto(file: File) {
        file.delete()
        photos = listPhotos()
    }

    // ---------------------------------------------------------------------------------- engine host

    override fun sfx(effect: Sfx, volume: Float, rate: Float) {
        sfx.play(effect, volume, rate)
    }

    override fun haptic() {
        if (!settings.haptics) return
        runCatching {
            val context = getApplication<Application>()
            val vibrator = if (Build.VERSION.SDK_INT >= 31) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            vibrator.vibrate(VibrationEffect.createOneShot(12, 70))
        }
    }

    /** Bumps with every change of Mitt hus; the panel and its buttons follow it. */
    var mineVersion by mutableIntStateOf(0)
        private set
    private var partyActive = false

    override fun changed() {
        weather = world.weather
        val ids = world.playerIds.toList()
        if (ids != visiblePlayerIds) {
            visiblePlayerIds = ids
            playersVersion++
        }
        mineVersion = world.mine.version
        mineUi.version = mineVersion
        val party = world.mine.party != null || world.community.partyPlace == place
        if (party != partyActive) {
            partyActive = party
            updateMusic()
        }
        houseKeys = HouseKeys.found(world)
        refreshTasks()
        scheduleSave()
    }

    override fun secretFound(id: String) {
        found = world.found.size
        if (world.allSecretsFound()) sfx(Sfx.FANFARE, 0.9f)
    }

    override fun discovered(key: String) {
        discoveries = world.discoveries.size
        bookPulse++
        scheduleSave()
    }

    override fun telescope() {
        telescopeOpen = true
    }

    override fun radio(on: Boolean) {
        radioOn = on
        updateMusic()
    }

    private fun updateMusic() {
        val theme = when {
            screen == Screen.Map -> MusicTheme.MAP
            world.community.partyPlace==place && screen==Screen.Play -> when(world.community.partyMusic) {
                0 -> MusicTheme.CAFE
                1 -> MusicTheme.RADIO
                else -> MusicTheme.STAGE
            }
            (radioOn || world.mine.party != null && place.mine) && screen == Screen.Play -> MusicTheme.RADIO
            night && place != PlaceId.SPACE && place != PlaceId.UNDERWATER && place != PlaceId.STAGE -> MusicTheme.NIGHT
            else -> when (place) {
                PlaceId.HOME -> MusicTheme.HOME
                PlaceId.CAFE -> MusicTheme.CAFE
                PlaceId.SALON -> MusicTheme.SALON
                PlaceId.BEACH -> MusicTheme.BEACH
                PlaceId.FOREST -> MusicTheme.FOREST
                PlaceId.LAB -> MusicTheme.LAB
                PlaceId.MOUNTAIN -> MusicTheme.PARK
                PlaceId.FARM -> MusicTheme.FARM
                PlaceId.SPACE -> MusicTheme.SPACE
                PlaceId.CLOUD_ISLAND -> MusicTheme.SPACE
                PlaceId.TIVOLI -> MusicTheme.TIVOLI
                PlaceId.SHOP -> MusicTheme.SHOP
                PlaceId.DOCTOR -> MusicTheme.DOCTOR
                PlaceId.STAGE -> MusicTheme.STAGE
                PlaceId.UNDERWATER -> MusicTheme.SEA
                PlaceId.HEILEBERGET -> MusicTheme.BERG
                PlaceId.MANOR_GROUND, PlaceId.MANOR_UPPER -> MusicTheme.MANOR
                PlaceId.MANOR_ATTIC -> MusicTheme.ATTIC
                PlaceId.MANOR_CELLAR -> MusicTheme.CELLAR
                PlaceId.MANOR_GARDEN -> MusicTheme.GARDEN
                PlaceId.MINE_YARD -> MusicTheme.PARK
                PlaceId.MINE_GROUND, PlaceId.MINE_UPPER -> MusicTheme.HOME
                PlaceId.VAGSTADDALEN -> MusicTheme.FOREST
            }
        }
        music.play(theme)
        val bed = Soundscape.bedFor(place, night, onMap = screen == Screen.Map)
        ambient.play(bed, Soundscape.gain(bed, Progression.level(world)))
    }

    // ---------------------------------------------------------------------------------- saving and lifecycle

    fun scheduleSave(immediate: Boolean = false) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            if (!immediate) delay(1500)
            saveNow()
        }
    }

    private suspend fun saveNow() {
        val json = WorldStore.encode(world, settings).toString()
        withContext(Dispatchers.IO) { runCatching { store.write(json) } }
    }

    fun onForeground() {
        sim.today = LocalDate.now().toEpochDay()
        music.resume()
        ambient.resume()
        updateMusic()
    }

    fun onBackground() {
        engine?.cancel()
        sim.mine.leave()
        music.pause()
        ambient.pause()
        saveJob?.cancel()
        val json = WorldStore.encode(world, settings).toString()
        runCatching { store.write(json) }
    }

    override fun onCleared() {
        sfx.release()
        music.release()
        ambient.release()
    }

    /** Debug builds only: jump straight to a place or screen for screenshots. */
    fun debug(placeName: String?, screenName: String?, nightOn: String?, weatherName: String?, secrets: Int, wishes: Boolean = false, skip: Int = 0, task: String? = null, seasonName: String? = null, festivalName: String? = null, mine: String? = null, shape: Int = 0, build: String? = null, cam: Float = Float.NaN, layers: String? = null, toys: String? = null) {
        layers?.let { app.trollfoss.ui.play.SpriteCache.useLayers = it != "off" }
        if (seasonName != null || festivalName != null) {
            if (seasonName != null) debugSeason = Season.entries.firstOrNull { it.name.equals(seasonName, true) }
            if (festivalName != null) debugFestival = Festival.entries.firstOrNull { it.name.equals(festivalName, true) }
            applySettings()
        }
        // Mitt hus: `--es mine demo|demo2` fills the house, `--ei shape 0..3` picks the template, `--es build on|off` opens or closes the builder.
        mine?.let { m ->
            if (m.startsWith("demo")) app.trollfoss.domain.MineDemo.fill(sim, if (m == "demo2") 2 else 1, shape)
            world.mine.version++
        }
        build?.let { b ->
            mineUi.open = b == "on"
            sim.mine.setBuildMode(b == "on", PlaceId.entries.firstOrNull { it.name.equals(placeName, true) } ?: world.place)
        }
        placeName?.let { name -> PlaceId.entries.firstOrNull { it.name.equals(name, true) }?.let { travel(it) } }
        // `--es toys show|inventor` (or fixture type names joined by commas) sets those toys out in a row in the current place.
        toys?.let { list ->
            val types = if (list == "show") app.trollfoss.domain.ShowPlay.TYPES.toList()
            else if (list == "inventor") app.trollfoss.domain.InventorPlay.TYPES.toList()
            else list.split(',').mapNotNull { n -> FixtureType.entries.firstOrNull { it.name.equals(n.trim(), true) } }
            types.forEachIndexed { i, type -> sim.designer.add(place, type, 0, 0.5f + i * 0.6f, place.floor) }
            // Two loose things on the floor, so the helper robot has something to fetch.
            if (list == "inventor") listOf(ThingType.BALL to 0.88f).forEach { (type, x) -> world.addThing(type, 0, place, x, place.floor - 0.1f).also { it.ground = place.floor } }
        }
        when (screenName?.lowercase()) {
            "map" -> open(Screen.Map)
            "creator" -> open(Screen.Creator(null))
            "book" -> open(Screen.Book)
            "tasks" -> open(Screen.Tasks)
            "quake" -> sim.quake(place)
            "lightning" -> engine?.strike()
            "parent" -> open(Screen.Parent)
            "gate" -> open(Screen.ParentGate)
            "play" -> open(Screen.Play)
        }
        nightOn?.let { if ((it == "on") != world.night) toggleNight() }
        weatherName?.let { name -> Weather.entries.firstOrNull { it.name.equals(name, true) }?.let { world.weather = it; weather = it } }
        if (secrets > 0) {
            Secrets.all.take(secrets).forEach { world.found += it.id }
            found = world.found.size
        }
        debugSkip = skip
        task?.let { id ->
            sim.tasks.board()
            if (world.taskSet.isNotEmpty()) world.taskSet[0] = id
            refreshTasks()
        }
        engine?.skip = skip
        // Everyone in the place wishes for something right away.
        if (wishes) world.people().filter { it.place == place }.forEach { it.anim.nextWish = 0.2f + it.id % 5 * 0.4f }
        if (!cam.isNaN()) {
            pendingFocus = cam
            engine?.focusOn(cam)
        }
        splash = false
    }

    companion object {
        /** The names the family chose for the figures (see docs/DESIGN.md). */
        val CHILD_NAMES = listOf("Hedda", "Alva", "Frida", "Velte", "Eilev", "Olve", "Eira", "Iver", "Olvar")
        val GROWN_NAMES = listOf("Tuva", "Øyvind", "Sondre", "Elise", "Sander", "Hilde", "Berit", "Sølve")
        val ELDER_NAMES = listOf("BesteSonja", "Besten")

        @Suppress("unused")
        private fun Person.isInBag() = mode == Mode.BAG
    }
}

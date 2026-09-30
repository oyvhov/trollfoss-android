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
import app.trollfoss.audio.Sfx
import app.trollfoss.audio.SoundFx
import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.Look
import app.trollfoss.domain.Maalform
import app.trollfoss.domain.Mode
import app.trollfoss.domain.Person
import app.trollfoss.domain.PlaceId
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

/** Where the child is. The play screen is home; the others open over it. */
sealed interface Screen {
    object Play : Screen
    object Map : Screen
    class Creator(val editId: Int?) : Screen
    object Book : Screen
    object Tasks : Screen
    object ParentGate : Screen
    object Parent : Screen
}

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
    private val music = MusicPlayer(application.cacheDir)
    val updater = AppUpdater(application, viewModelScope)

    var screen by mutableStateOf<Screen>(Screen.Play)
    var place by mutableStateOf(PlaceId.HOME)
        private set
    var night by mutableStateOf(false)
        private set
    var weather by mutableStateOf(Weather.SUN)
        private set
    var found by mutableIntStateOf(0)
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

    /** Bumps when the world is replaced, so engines are rebuilt. */
    var generation by mutableIntStateOf(0)
        private set

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
        settings = saved?.settings ?: Settings()
        sim = Sim(world)
        wireTasks()
        syncFromWorld()
        applySettings()
    }

    private fun syncFromWorld() {
        sim.today = LocalDate.now().toEpochDay()
        place = world.place
        night = world.night
        weather = world.weather
        found = world.found.size
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
        tasksLeft = sim.tasks.board().count { !sim.tasks.done(it) }
        stickers = world.stickers.size
    }

    /** Deals three new tasks once the board is done. */
    fun newTasks() {
        sim.tasks.deal()
        sfx(Sfx.MAGIC, 0.8f)
        refreshTasks()
        scheduleSave()
    }

    private fun applySettings() {
        sfx.enabled = settings.sound
        music.enabled = settings.music
    }

    private var debugSkip = 0

    /** The engine for the place on screen. A new one is made when the child travels. */
    fun engineFor(place: PlaceId, motion: Boolean): Engine {
        engine?.let { cams[it.place] = it.cam }
        val start = cams[place] ?: defaultCam(place)
        return Engine(world, place, sim, this, motion, start).also {
            it.skip = debugSkip
            engine = it
            radioOn = world.fixturesIn(place).any { f -> f.type == FixtureType.RADIO && f.on }
            updateMusic()
        }
    }

    private fun defaultCam(place: PlaceId): Float = when (place) {
        PlaceId.HOME -> 0.9f
        PlaceId.BEACH -> 0.5f
        PlaceId.FOREST -> 0.8f
        PlaceId.MOUNTAIN -> 0.6f
        PlaceId.FARM -> 0.1f
        else -> 0.3f
    }

    // ---------------------------------------------------------------------------------- navigation

    fun travel(to: PlaceId) {
        engine?.let { cams[it.place] = it.cam }
        world.place = to
        place = to
        screen = Screen.Play
        sfx(Sfx.WHOOSH, 0.7f)
        scheduleSave()
    }

    fun open(target: Screen) {
        screen = target
        updateMusic()
    }

    fun back() {
        screen = when (screen) {
            Screen.Parent, Screen.ParentGate -> Screen.Map
            else -> Screen.Play
        }
        updateMusic()
    }

    // ---------------------------------------------------------------------------------- world controls

    fun toggleNight() {
        world.night = !world.night
        night = world.night
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
        }
        sfx(Sfx.FANFARE, 0.7f)
        screen = Screen.Play
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
        world = WorldFactory.create()
        sim = Sim(world)
        wireTasks()
        cams.clear()
        engine = null
        syncFromWorld()
        generation++
        screen = Screen.Play
        scheduleSave(immediate = true)
    }

    fun updateSettings(transform: (Settings) -> Settings) {
        settings = transform(settings)
        applySettings()
        if (settings.music) updateMusic()
        scheduleSave()
    }

    fun setMaalform(m: Maalform) = updateSettings { it.copy(maalform = m) }

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

    override fun changed() = scheduleSave()

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
            radioOn && screen == Screen.Play -> MusicTheme.RADIO
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
                PlaceId.TIVOLI -> MusicTheme.TIVOLI
                PlaceId.SHOP -> MusicTheme.SHOP
                PlaceId.DOCTOR -> MusicTheme.DOCTOR
                PlaceId.STAGE -> MusicTheme.STAGE
                PlaceId.UNDERWATER -> MusicTheme.SEA
            }
        }
        music.play(theme)
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
        updateMusic()
    }

    fun onBackground() {
        engine?.cancel()
        music.pause()
        saveJob?.cancel()
        val json = WorldStore.encode(world, settings).toString()
        runCatching { store.write(json) }
    }

    override fun onCleared() {
        sfx.release()
        music.release()
    }

    /** Debug builds only: jump straight to a place or screen for screenshots. */
    fun debug(placeName: String?, screenName: String?, nightOn: String?, weatherName: String?, secrets: Int, wishes: Boolean = false, skip: Int = 0) {
        placeName?.let { name -> PlaceId.entries.firstOrNull { it.name.equals(name, true) }?.let { travel(it) } }
        when (screenName?.lowercase()) {
            "map" -> open(Screen.Map)
            "creator" -> open(Screen.Creator(null))
            "book" -> open(Screen.Book)
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
        engine?.skip = skip
        // Everyone in the place wishes for something right away.
        if (wishes) world.people().filter { it.place == place }.forEach { it.anim.nextWish = 0.2f + it.id % 5 * 0.4f }
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

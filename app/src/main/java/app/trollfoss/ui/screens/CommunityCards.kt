package app.trollfoss.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.domain.*
import app.trollfoss.ui.*
import app.trollfoss.ui.art.*
import app.trollfoss.ui.components.*
import app.trollfoss.ui.play.Engine
import app.trollfoss.ui.theme.T

@Composable
private fun PeopleChoices(people:List<Person>,selected:Set<Int>,onClick:(Person)->Unit) {
    for(row in people.chunked(4)) Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
        for(p in row) Column(Modifier.weight(1f).background(if(p.id in selected) T.Mint else T.CreamDeep,RoundedCornerShape(14.dp))
            .clickable { onClick(p) }.padding(6.dp),horizontalAlignment=Alignment.CenterHorizontally) {
            Canvas(Modifier.size(48.dp)) { drawSpeciesThumb(p.species,Rect(0f,0f,size.width,size.height),p.look) }
            GameText(p.name.ifBlank { S.place(p.place ?: PlaceId.HOME).str() },fontSize=12.sp,color=T.Ink,maxLines=1)
            Box(Modifier.size(20.dp)) { if(p.id in selected) IconCanvas(Icons.Check,Modifier.size(20.dp)) }
        }
        repeat(4-row.size) { Spacer(Modifier.weight(1f)) }
    }
}

/** Picture-first workshop: personal choices and repeatable scenes, available during ordinary play. */
@Composable
fun CommunityCards(vm:TrollfossViewModel,engine:Engine,onClose:()->Unit) {
    var mode by remember { mutableIntStateOf(0) }
    var redraw by remember { mutableIntStateOf(0) }
    var failed by remember { mutableStateOf(false) }
    var chosen by remember { mutableStateOf(Players.activeTeam(vm.world).map { it.id }.take(2).toSet()) }
    var music by remember { mutableIntStateOf(0) };var lights by remember { mutableIntStateOf(0) }
    redraw
    val here=vm.world.people().filter { it.place==vm.place && it.species==Species.FOLK && it.mode!=Mode.BAG }
    val first=here.firstOrNull { it.id in chosen }
    fun after() { vm.changed();redraw++ }
    fun kit(key:String,types:List<ThingType>) {
        for((i,type) in types.withIndex()) {
            val idKey="community:$key:$i"
            val old=vm.world.bodies[vm.world.toyInputs[idKey]] as? Thing
            val t=old?.takeIf { it.type==type } ?: vm.world.addThing(type,0,vm.place,engine.cam+0.5f+i*0.12f,PlaceId.FRONT-0.04f).also { vm.world.toyInputs[idKey]=it.id }
            if(!t.held && t.mode in listOf(Mode.FREE,Mode.BAG)) { t.mode=Mode.FREE;t.holder=-1;t.inside=-1;t.place=vm.place;t.x=(engine.cam+0.5f+i*0.12f).coerceIn(0.2f,vm.place.width-0.2f);t.y=PlaceId.FRONT-0.04f;t.ground=t.y;t.vx=0f;t.vy=0f;t.resting=false;t.restOwner=-2 }
        };after();onClose()
    }
    if(mode==0) {
        if(failed) GameText(SP.noSpace.str(),color=T.Ink,fontSize=16.sp)
        for(row in listOf(SC.choose to 1,SC.pets to 2,SC.band to 3,SC.party to 4,SC.art to 5,SC.weather to 6,SC.sky to 7,SC.rescue to 8,SC.secretThings to 9).chunked(2)) Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            for((label,id) in row) Row(Modifier.weight(1f).background(T.CreamDeep,RoundedCornerShape(18.dp)).clickable { mode=id;failed=false }.padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                IconCanvas(when(id) { 3 -> CreativeIcons.Music;5 -> CreativeIcons.Paint;6 -> CreativeIcons.Weather;7 -> Icons.Map;9 -> Icons.Star;else -> Icons.Friends },Modifier.size(40.dp))
                GameText(label.str(),fontSize=16.sp,color=T.Ink,maxLines=2)
            }
            if(row.size==1) Spacer(Modifier.weight(1f))
        }
        if(vm.world.community.returnPlace!=null) Row(verticalAlignment=Alignment.CenterVertically) {
            RoundButton(SC.returnTo.str(),onClick={ val state=vm.world.community;val to=state.returnPlace ?: return@RoundButton;val x=state.returnX;state.returnPlace=null;after();onClose();vm.travelPlayCard(to,x) },tone=Tones.Sea,icon=Icons.Map)
            GameText(SC.returnTo.str(),color=T.Ink)
        }
        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            for(reward in listOf(ToyReward.TUNNEL,ToyReward.JUMP,ToyReward.DOOR,ToyReward.TREE)) Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.weight(1f)) {
                ToyPicture(reward,48.dp)
                RoundButton(SP.name(reward).str(),onClick={ failed=!vm.tryToy(reward);if(!failed) onClose() },size=48.dp,tone=Tones.Mint,icon=Icons.Check)
                GameText(SP.name(reward).str(),fontSize=12.sp,color=T.Ink,maxLines=2)
            }
        }
    } else {
        RoundButton(SC.back.str(),onClick={ mode=0;failed=false },size=44.dp,tone=Tones.Sea,icon=Icons.Map)
        if(mode in 1..3) PeopleChoices(here,chosen) { p -> chosen=if(p.id in chosen) chosen-p.id else (chosen+p.id).takeLastSet(3);failed=false }
        when(mode) {
            1 -> {
                Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) { for(a in FriendAction.entries) Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally) {
                    RoundButton(SC.friend(a).str(),onClick={ val pair=here.filter { it.id in chosen }.take(2);failed=pair.size<2 || !vm.sim.community.friend(pair[0],pair[1],a);after();if(!failed) onClose() },tone=Tones.Mint,icon={ drawFriendAction(a) })
                    GameText(SC.friend(a).str(),fontSize=14.sp,color=T.Ink)
                } }
                GameText(SC.personalities.str(),fontSize=18.sp,color=T.Ink)
                first?.let { p -> Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) { for(t in Temperament.entries) Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.weight(1f)) {
                    RoundButton(SC.trait(t).str(),onClick={ vm.sim.community.setTrait(p,t);after() },tone=if(vm.sim.community.trait(p)==t) Tones.Mint else Tones.Sun,icon={ drawTemperament(t) })
                    GameText(SC.trait(t).str(),fontSize=14.sp,color=T.Ink)
                } } }
                if(failed) GameText(SC.needTwo.str(),color=T.Ink)
            }
            2 -> {
                GameText(SC.petHint.str(),color=T.Ink)
                val owner=first ?: Players.activeTeam(vm.world).firstOrNull()
                val pets=vm.world.people().filter { it.species in Community.PETS }
                PeopleChoices(pets,setOfNotNull(owner?.let { vm.world.community.pets[it.id] })) { p -> owner?.let { failed=!vm.sim.community.choosePet(it,p);after();if(!failed) onClose() } }
                RoundButton(SC.petHome.str(),onClick={ owner?.let { vm.sim.community.choosePet(it,null);after() } },tone=Tones.Berry,icon=CreativeIcons.Home)
                GameText(SC.petHome.str(),color=T.Ink)
            }
            3 -> {
                GameText(SC.bandHint.str(),color=T.Ink)
                Row(horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                    RoundButton(SC.instruments.str(),onClick={ kit("band",Community.INSTRUMENTS.toList()) },tone=Tones.Sea,icon=Icons.Bag)
                    RoundButton(SC.start.str(),onClick={ failed=!vm.sim.community.startBand(chosen.toList());after();if(!failed) onClose() },tone=Tones.Mint,icon=CreativeIcons.Music)
                    RoundButton(SC.stop.str(),onClick={ vm.sim.community.stopBand();after();onClose() },tone=Tones.Berry,icon=Icons.Close)
                }
                if(failed) GameText(SC.bandHint.str(),color=T.Ink)
            }
            4 -> {
                GameText(SC.guests.str(),color=T.Ink)
                PeopleChoices(vm.world.people().filter { it.species==Species.FOLK },chosen) { p -> chosen=if(p.id in chosen) chosen-p.id else (chosen+p.id).takeLastSet(8) }
                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) { for(i in 0..2) {
                    RoundButton(SC.music.str()+" ${i+1}",onClick={ music=i;redraw++ },size=48.dp,tone=if(music==i) Tones.Mint else Tones.Sun,icon=CreativeIcons.Music)
                    RoundButton(SC.lights.str()+" ${i+1}",onClick={ lights=i;redraw++ },size=48.dp,tone=if(lights==i) Tones.Mint else Tones.Sun,icon=Icons.Star)
                } }
                Row(horizontalArrangement=Arrangement.spacedBy(15.dp)) {
                    RoundButton(SC.food.str(),onClick={ kit("food",listOf(ThingType.APPLE,ThingType.CAKE,ThingType.CUP)) },tone=Tones.Sea,icon=Icons.Bag)
                    RoundButton(SC.start.str(),onClick={ engine.cancel();failed=!vm.sim.community.startParty(vm.place,engine.cam+engine.visibleViewport/2,chosen.toList(),music,lights);after();if(!failed) onClose() },tone=Tones.Mint,icon=Icons.Friends)
                    RoundButton(SC.endParty.str(),onClick={ engine.cancel();vm.sim.community.endParty();after();onClose() },tone=Tones.Berry,icon=CreativeIcons.Home)
                }
                if(failed) GameText(SC.noGuests.str(),color=T.Ink)
            }
            5 -> ArtEditor(vm,engine,onClose)
            6 -> {
                Row(horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                    RoundButton(SC.rain.str(),onClick={ vm.world.weather=if(vm.world.weather==Weather.RAIN) Weather.SUN else Weather.RAIN;after();onClose() },tone=Tones.Sea,icon=CreativeIcons.Weather)
                    RoundButton(SC.wind.str(),onClick={ if(!vm.world.flags.remove("play:wind")) vm.world.flags.add("play:wind");after();onClose() },tone=Tones.Sun,icon=CreativeIcons.Weather)
                    RoundButton(SC.snow.str(),onClick={ vm.world.weather=Weather.SNOW
                        val old=vm.world.fixtures[vm.world.toyInputs["weather:snowman"]]
                        if(old?.place!=vm.place) vm.sim.designer.add(vm.place,FixtureType.SNOWMAN,0,engine.cam+engine.visibleViewport/2,vm.place.floor)?.also { it.mode=0;vm.world.toyInputs["weather:snowman"]=it.id }
                        kit("snow",listOf(ThingType.SNOWBALL,ThingType.SNOWBALL,ThingType.CARROT)) },tone=Tones.Mint,icon=Icons.Bag)
                    RoundButton(SC.stop.str(),onClick={ vm.world.weather=Weather.SUN;vm.world.flags.remove("play:wind");after();onClose() },tone=Tones.Berry,icon=Icons.Close)
                }
            }
            7 -> {
                GameText(SC.skyHint.str(),color=T.Ink)
                RoundButton(SC.sky.str(),onClick={ after();onClose();vm.flyTo(PlaceId.CLOUD_ISLAND) },tone=Tones.Sea,icon=Icons.Map)
            }
            8 -> {
                GameText(SC.rescueHint.str(),color=T.Ink)
                val f=vm.world.fixturesIn(vm.place).firstOrNull { it.type==FixtureType.PLAY_RESCUE }
                RoundButton(SC.rescue.str(),onClick={ failed=!vm.tryToy(ToyReward.RESCUE);if(!failed) onClose() },tone=Tones.Mint,icon=Icons.Map)
                if(f!=null) Row(horizontalArrangement=Arrangement.spacedBy(15.dp)) {
                    RoundButton(SC.bridge.str(),onClick={ vm.sim.toys.supply(f);after();onClose() },tone=Tones.Sea,icon=BuildIcons.Hammer)
                    RoundButton(SC.boat.str(),onClick={
                        val key="rescue:boat:${f.id}";val old=vm.world.fixtures[vm.world.toyInputs[key]]
                        val boat=old?.takeIf { it.type==FixtureType.BOAT } ?: vm.sim.designer.add(vm.place,FixtureType.BOAT,0,(f.x-0.6f).coerceAtLeast(0.3f),f.y)?.also { vm.world.toyInputs[key]=it.id }
                        if(boat!=null) vm.world.flags += "rescue:boat:start:${f.id}"
                        after();onClose()
                    },tone=Tones.Sea,icon=Icons.Map)
                }
            }
            9 -> {
                GameText(SC.secretThingsHint.str(),color=T.Ink,fontSize=17.sp)
                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    for(type in listOf(ThingType.MAGNET,ThingType.GR_UMBRELLA,ThingType.SPOON))
                        CachedThumb("secret-kit:$type",72.dp) { drawThingThumb(type,0,Rect(0f,0f,size.width,size.height)) }
                }
                BigButton(S.playKit.str(),onClick={ kit("secrets",listOf(ThingType.MAGNET,ThingType.GR_UMBRELLA,ThingType.SPOON,ThingType.PLAY_GEAR)) },tone=Tones.Mint,icon=Icons.Bag)
            }
        }
    }
}
private fun Set<Int>.takeLastSet(n:Int)=toList().takeLast(n).toSet()

@Composable
fun ArtEditor(vm:TrollfossViewModel,engine:Engine,onClose:()->Unit) {
    val id=remember { vm.sim.community.newArt() }
    var committed by remember(id) { mutableStateOf(false) }
    DisposableEffect(id) {
        onDispose {
            if(!committed && vm.world.community.art.remove(id)!=null) vm.changed()
        }
    }
    var shape by remember { mutableIntStateOf(0) };var color by remember { mutableIntStateOf(0) };var revision by remember { mutableIntStateOf(0) }
    var full by remember { mutableStateOf(false) }
    GameText(SC.artHint.str(),color=T.Ink)
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { repeat(6) { i -> RoundButton(SP.color.str()+" ${i+1}",onClick={ color=i },size=40.dp,tone=Tone(ToyColors[i],ToyColors[i],T.Ink),icon={ if(color==i) Icons.Check(this) }) } }
    Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) { repeat(3) { i -> RoundButton(SC.shape.str()+" ${i+1}",onClick={ shape=i },size=42.dp,tone=if(shape==i) Tones.Mint else Tones.Sun,icon={ drawArtwork(listOf(ArtMark(i,color,0.5f,0.5f)),Rect(-size.width*2,-size.height*2,size.width*3,size.height*3),Pen(size.width*0.02f)) }) } }
    val marks=vm.world.community.art[id].orEmpty()
    Canvas(Modifier.fillMaxWidth().height(if(engine.compact) 100.dp else 210.dp).background(T.Cream).border(2.dp,T.Ink).pointerInput(id,shape,color) {
        detectTapGestures { p -> vm.sim.edit { vm.sim.community.addMark(id,shape,color,p.x/size.width,p.y/size.height) };vm.changed();revision++ }
    }.pointerInput(id,shape,color) {
        var last=androidx.compose.ui.geometry.Offset.Zero
        fun stamp(p:androidx.compose.ui.geometry.Offset) {
            if(p.x !in 0f..size.width.toFloat() || p.y !in 0f..size.height.toFloat()) return
            vm.sim.edit { vm.sim.community.addMark(id,shape,color,p.x/size.width,p.y/size.height) }
            vm.changed();revision++;last=p
        }
        detectDragGestures(onDragStart={ stamp(it) },onDrag={ change,_ ->
            change.consume()
            if((change.position-last).getDistance()>size.width*0.045f) stamp(change.position)
        })
    }) { revision;drawArtwork(marks,Rect(0f,0f,size.width,size.height),Pen(2f)) }
    Row(horizontalArrangement=Arrangement.spacedBy(14.dp)) {
        RoundButton(SC.artUndo.str(),onClick={ vm.sim.edit { vm.world.community.art[id]?.removeLastOrNull() };vm.changed();revision++ },enabled=marks.isNotEmpty(),tone=Tones.Berry,icon=Icons.Bag)
        RoundButton(SC.hang.str(),onClick={ full=vm.sim.community.hangArt(id,vm.place,engine.cam+engine.visibleViewport/2)==null;committed=!full;vm.changed();if(!full) onClose() },enabled=marks.isNotEmpty(),tone=Tones.Mint,icon=Icons.Check)
        if(Decor.decoratable(vm.place)) RoundButton(SC.wallpaper.str(),onClick={ vm.sim.edit { val key=Decor.key(vm.place,Decor.roomAt(vm.place,engine.cam+engine.visibleViewport/2));vm.world.community.wallArt[key]=id;vm.world.styles[key]=(vm.world.styles[key] ?: RoomStyle()).copy(wall=1) };committed=true;vm.changed();onClose() },enabled=marks.isNotEmpty(),tone=Tones.Sea,icon=CreativeIcons.Paint)
    }
    if(full) GameText(SP.noSpace.str(),color=T.Ink)
}

@Composable
fun DoorControl(vm:TrollfossViewModel,engine:Engine,f:Fixture) {
    var revision by remember { mutableIntStateOf(0) }
    TrollDialog(onClose={ engine.toyFixtureId=-1 }) {
        GameText(SC.door.str(),fontSize=24.sp,color=T.Ink)
        val links=listOf(PlaceId.MINE_GROUND,PlaceId.MINE_UPPER).flatMap { place -> (0 until Mine.SLOTS).filter { vm.world.mine.standing(place,it) }.map { RoomLink(place,it) } }
        if(links.isEmpty()) GameText(SC.buildRoom.str(),color=T.Ink)
        for(row in links.chunked(3)) Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) { for(link in row) Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.weight(1f)) {
            revision
            val label=(if(link.place==PlaceId.MINE_UPPER) app.trollfoss.ui.SM.upperFloor else app.trollfoss.ui.SM.groundFloor).str()+" · "+roomLabel(vm.world,link.place,link.slot).str()+" ${link.slot+1}"
            RoundButton(label,onClick={ vm.sim.community.linkDoor(f,link.place,link.slot);vm.changed();revision++ },tone=if(vm.sim.community.door(f)==link) Tones.Mint else Tones.Sun,icon=CreativeIcons.Home)
            GameText(label,fontSize=14.sp,color=T.Ink,maxLines=3)
        } }
        vm.sim.community.door(f)?.let { to -> RoundButton(SC.enter.str(),onClick={ vm.world.community.returnPlace=f.place;vm.world.community.returnX=f.x;engine.toyFixtureId=-1;vm.changed();vm.travelMineRoom(to.place,to.slot) },tone=Tones.Sea,icon=Icons.Map) }
    }
}

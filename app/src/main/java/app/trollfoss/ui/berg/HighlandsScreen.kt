package app.trollfoss.ui.berg

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLSurfaceView
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.*
import app.trollfoss.ui.*
import app.trollfoss.ui.components.*
import app.trollfoss.ui.screens.drawSpeciesThumb
import app.trollfoss.ui.theme.LocalMotion
import app.trollfoss.ui.theme.T
import kotlinx.coroutines.delay
import kotlin.math.*

/** Native 3D surface plus the same accessible Compose controls used by the rest of Trollfoss. */
@Composable
fun HighlandsScreen(vm:TrollfossViewModel) {
    val state=vm.world.highlands
    val walk=remember(state) { HighlandsWalk(state) }
    val caveAudio=remember(walk) { BergCaveAudio() }
    val context=LocalContext.current
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    val motion=LocalMotion.current
    val compact=LocalConfiguration.current.screenHeightDp<520
    val button=if(compact) 48.dp else 64.dp
    val edge=if(compact) 10.dp else 18.dp
    val lowMemory=remember { (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).isLowRamDevice }
    val renderer=remember(state,compact,lowMemory) { BergRenderer(context.assets,compact || lowMemory) }
    var surface by remember { mutableStateOf<GLSurfaceView?>(null) }
    var bagOpen by remember { mutableStateOf(false) }
    var echoOpen by remember { mutableStateOf(false) }
    var replying by remember { mutableStateOf(false) }
    var tune by remember(state) { mutableStateOf(state.song.toList()) }
    var bagVersion by remember { mutableIntStateOf(0) }
    var leader by remember { mutableIntStateOf(state.leader) }
    var cave by remember { mutableStateOf(state.cave) }
    var shake by remember { mutableStateOf(state.shake) }
    var spot by remember { mutableStateOf(walk.nearby()) }
    var message by remember { mutableStateOf<Txt?>(SBerg.hint) }
    var failed by remember { mutableStateOf(false) }
    var loading by remember(renderer) { mutableStateOf(true) }
    vm.playersVersion
    val team=Players.activeTeam(vm.world).filter { it.place==PlaceId.HEILEBERGET }
    val appearances=remember(team.map { it.id to it.look },vm.playersVersion) {
        team.map { person -> BergAppearance(person.id,person.look,vm.world.carried(person).map { it.type to it.variant },person.name) }
    }
    val currentAppearances by rememberUpdatedState(appearances)
    val currentBagOpen by rememberUpdatedState(bagOpen)
    fun event(event:BergEvent) {
        val sound=when(event) {
            BergEvent.MOUSE -> Sfx.BERG_MOUSE
            BergEvent.SPRING -> Sfx.BERG_DRIP
            BergEvent.STONE_NOTE -> Sfx.BERG_STONE
            BergEvent.FLUTE_NOTE -> Sfx.BERG_OCARINA
            BergEvent.BERRIES -> Sfx.PICK
            BergEvent.FILL -> Sfx.BLOOP
            BergEvent.STIR -> Sfx.SWISH
            BergEvent.POUR -> Sfx.BUBBLE
            BergEvent.TOAST -> Sfx.GR_CLINK
            BergEvent.SIP -> Sfx.GULP
            BergEvent.PICK -> Sfx.PICK
            BergEvent.LIGHT -> Sfx.CHIME
            BergEvent.GULP -> Sfx.GULP
            BergEvent.BURP -> Sfx.BURP
            BergEvent.FART -> Sfx.PRRT
            BergEvent.CAKE -> Sfx.CHOMP
            BergEvent.TRAVEL -> Sfx.WHOOSH
            BergEvent.LOOKOUT -> Sfx.WHOOSH
            BergEvent.WAVE -> Sfx.GIGGLE
            BergEvent.HARE -> Sfx.CHIME
            BergEvent.BUTTERFLY -> Sfx.CHIME
            BergEvent.SKIP -> Sfx.WHOOSH
            BergEvent.SPLASH -> Sfx.SPLASH
            BergEvent.NOTE -> Sfx.CHIME
            BergEvent.WASH -> Sfx.SWISH
            else -> null
        }
        val pitch=when(event) {
            BergEvent.SPRING -> .94f+.085f*walk.springKey
            BergEvent.STONE_NOTE,BergEvent.FLUTE_NOTE -> BergEcho.pitches.getOrElse(walk.echo.tone){1f}
            BergEvent.BURP,BergEvent.FART -> .65f
            BergEvent.NOTE -> Highlands.crystalPitches.getOrElse(walk.crystalIndex){1f}
            else -> 1f
        }
        sound?.let { vm.sfx(it,when(event){BergEvent.MOUSE->.38f;BergEvent.SPRING->.48f;else->.65f},pitch) }
        message=when(event) {
            BergEvent.SPRING -> SBerg.springLit
            BergEvent.FRIEND -> SBerg.familiar
            BergEvent.LISTEN -> SBerg.echoHint
            BergEvent.NEED_DIAMOND -> SBerg.needDiamond
            BergEvent.NEED_GOLD -> SBerg.needGold
            BergEvent.GOLD_ELSEWHERE -> SBerg.goldElsewhere
            BergEvent.WAVE -> if (state.sharedJuice) SBerg.hello else SBerg.invitation
            BergEvent.BERRIES, BergEvent.BASKET_FULL -> SBerg.picked
            BergEvent.NEED_BERRIES -> SBerg.needBerries
            BergEvent.FILL -> SBerg.steep
            BergEvent.STIR -> SBerg.pour
            BergEvent.POUR, BergEvent.CUP_FULL -> SBerg.takeCup
            BergEvent.TOAST -> SBerg.cheers
            BergEvent.HARE -> SBerg.hareHello
            BergEvent.FART -> SBerg.rumble
            else -> null
        }
        echoOpen=walk.echo.active;replying=walk.echo.replyLength>0f
        if(event==BergEvent.STONE_NOTE) tune=state.song.toList()
        if(event!=BergEvent.NONE) {
            bagVersion++;cave=state.cave
            if(event!=BergEvent.FLUTE_NOTE && event!=BergEvent.REPLY && event!=BergEvent.LISTEN) vm.changed()
        }
        if(event==BergEvent.TRAVEL) vm.refreshHighlandsAmbience()
    }
    val onEvent by rememberUpdatedState(::event)
    fun snapshot():BergFrame {
        val ordered=currentAppearances.sortedBy { if(it.id==state.leader) 0 else 1 }
        return BergFrame(x=state.x,z=state.z,cave=state.cave,yaw=state.yaw,pitch=walk.pitch,distance=walk.distance,
            time=if(motion) walk.time else 0f,moving=walk.moving,motion=motion,shake=state.shake,
            light=state.light,taken=state.taken,held=state.held,walkers=ordered.mapIndexed { index,appearance ->
                val p=if(index==0) BergPoint(state.x,state.z) else walk.follower(index)
                BergWalker(appearance,p.x,p.z,walk.facing)
            },destination=walk.destination,rumle=walk.rumle,visitFocus=if(motion) walk.visitFocus else 0f,treasure=state.treasure,
            harePlay=walk.harePlay,hareIndex=walk.hareIndex,treasureSlot=state.treasureSlot,
            skipPlay=walk.skipPlay,skipOrigin=walk.skipOrigin,crystalPlay=walk.crystalPlay,crystalIndex=walk.crystalIndex,
            panPlay=walk.panPlay,panFocus=walk.panFocus,night=vm.night,butterflyPlay=walk.butterflyPlay,
            christmas=vm.festival==Festival.CHRISTMAS,pot=state.pot,sharedJuice=state.sharedJuice,lastBottle=state.lastBottle,
            pickedBerries=BergItem.BERRIES in state.bag,kitchenEvent=walk.kitchenEvent,kitchenPlay=walk.kitchenPlay,
            mothFriend=state.mothFriend,friendReach=if(motion) walk.friendReach else if(walk.friendHand) 1f else 0f,
            roomFocus=if(motion) walk.roomFocus else 0f,roomTarget=walk.roomTarget,
            caveMouseAge=if(motion || walk.caveMouseAge<0f) walk.caveMouseAge else 2.8f,
            springAwake=state.springAwake,springAge=if(motion) walk.springAge else -1f,springKey=walk.springKey,
            echo=EchoFrame(walk.echo.active,walk.echo.tone,walk.echo.glow,walk.echo.replyAge,walk.echo.replyLength,walk.echo.guide))
    }
    LaunchedEffect(message) { if(message!=null) { delay(6500);message=null } }

    DisposableEffect(surface,lifecycle,vm.screen) {
        val view=surface
        val visible=vm.screen==Screen.Play
        val observer=LifecycleEventObserver { _,event ->
            when(event) {
                Lifecycle.Event.ON_START -> if(visible) view?.onResume()
                Lifecycle.Event.ON_STOP -> { walk.stop();view?.onPause();vm.changed() }
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        if(visible && lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) view?.onResume() else view?.onPause()
        onDispose { lifecycle.removeObserver(observer);walk.stop();view?.onPause();vm.changed() }
    }
    LaunchedEffect(surface,motion,vm.screen) {
        val view=surface ?: return@LaunchedEffect
        if(vm.screen!=Screen.Play) { walk.stop();return@LaunchedEffect }
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            var previous=withFrameNanos { it };var rendered=0L;var saved=previous
            var savedX=state.x;var savedZ=state.z;var savedYaw=state.yaw
            while(true) {
                withFrameNanos { now ->
                    val dt=((now-previous)/1_000_000_000f).coerceIn(0f,.05f);previous=now
                    if(!currentBagOpen) {
                        val result=walk.step(dt)
                        if(result!=BergEvent.NONE) onEvent(result)
                        val cue=caveAudio.step(dt,state.cave,state.x,state.z,walk.moving,walk.echo.active,walk.springHit,renderer.soundYaw(state.cave,state.yaw))
                        if(cue!=null) vm.sfx(cue.effect,cue.volume,cue.pitch,cue.pan)
                    }
                    spot=walk.nearby();echoOpen=walk.echo.active;replying=walk.echo.replyLength>0f
                    failed=renderer.failure!=null
                    loading=renderer.frames==0
                    if(now-rendered >= if(lowMemory) 32_000_000L else 15_000_000L) {
                        val next=snapshot()
                        // Opening the bag pauses the world. Reduced motion also
                        // leaves a stationary scene unchanged. Keep input polling,
                        // but don't submit identical scenes to the GPU every tick.
                        if(next!=renderer.frame || renderer.frames==0) {
                            renderer.frame=next
                            view.requestRender()
                        }
                        rendered=now
                    }
                    if(now-saved>2_000_000_000L) {
                        // Check the state, including after a thumb is released. Checking
                        // only moving lost the last short step and camera-only turns.
                        if(state.x!=savedX || state.z!=savedZ || state.yaw!=savedYaw) {
                            vm.changed();savedX=state.x;savedZ=state.z;savedYaw=state.yaw
                        }
                        saved=now
                    }
                }
            }
        }
    }

    val sceneLabel=(if(cave) SBerg.cave else SBerg.title).str()
    fun callMoth() {
        walk.echo.close()
        walk.callFriend()
        echoOpen=false;replying=false;message=null
    }
    val mothLabel=SBerg.callMoth.str()
    val guideMothLabel=SBerg.guideMoth.str()
    val mouseLabel=SBerg.mouse.str()
    val waveLabel=SBerg.waveOutside.str()
    val zoomInLabel=SBerg.zoomIn.str();val zoomOutLabel=SBerg.zoomOut.str()
    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFFA6C4E6)).testTag("highlands-scene")) {
        // Leave the map button reachable even in a narrow window; the team row can scroll.
        val teamWidth=minOf(if(compact) 240.dp else 420.dp,(maxWidth-button-edge*2-16.dp).coerceAtLeast(button))
        key(renderer) { AndroidView(factory={ ctx -> GLSurfaceView(ctx).apply {
            setEGLContextClientVersion(2)
            setEGLConfigChooser(BergEglConfig())
            preserveEGLContextOnPause=true
            renderer.frame=snapshot()
            setRenderer(renderer)
            renderMode=GLSurfaceView.RENDERMODE_WHEN_DIRTY
            surface=this
        } },modifier=Modifier.fillMaxSize()) }
        // Scene gestures and the movement stick have independent pointer targets, allowing two thumbs.
        Box(Modifier.fillMaxSize().semantics {
            contentDescription=sceneLabel
            customActions=listOfNotNull(
                if(echoOpen) CustomAccessibilityAction(guideMothLabel) {
                    walk.leadEcho(EchoLight(state.x,Highlands.floor(state.x,state.z,state.cave)+1.55f,state.z-.55f));true
                } else null,
                if(state.mothFriend) CustomAccessibilityAction(mothLabel){callMoth();true} else null,
                if(state.cave && CaveMouse.canCall(state.x,state.z)) CustomAccessibilityAction(mouseLabel){event(walk.callMouse());true} else null,
                if(spot?.action==BergAction.ENTER) CustomAccessibilityAction(waveLabel){event(walk.waveOutside());true} else null,
                CustomAccessibilityAction(zoomInLabel) {walk.zoomBy(1.25f);true},
                CustomAccessibilityAction(zoomOutLabel) {walk.zoomBy(.8f);true},
            )
        }.pointerInput(walk) {
            awaitEachGesture {
                val down=awaitFirstDown();val start=down.position
                var last=start;var moved=false;var pinching=false
                val onInstrument=walk.echo.active || (walk.nearby()?.action==BergAction.ECHO &&
                    renderer.echoKey(start.x,start.y,state.cave)!=null)
                var lastKey:Int?=null
                do {
                    val e=awaitPointerEvent();val p=e.changes.firstOrNull { it.id==down.id } ?: break
                    val fingers=e.changes.filter {it.pressed}
                    if(fingers.size>=2) {
                        moved=true;pinching=true
                        val a=fingers[0];val b=fingers[1]
                        if(a.previousPressed && b.previousPressed) {
                            val before=(a.previousPosition-b.previousPosition).getDistance()
                            val after=(a.position-b.position).getDistance()
                            if(before>viewConfiguration.touchSlop) walk.zoomBy(after/before)
                        }
                        fingers.forEach {it.consume()};last=p.position
                        continue
                    }
                    // Lifting one finger after a pinch must not turn into a tap
                    // or an unintended camera turn while the other is still down.
                    if(pinching) {p.consume();last=p.position;continue}
                    val delta=p.position-last
                    if((p.position-start).getDistance()>viewConfiguration.touchSlop) moved=true
                    if(onInstrument && !moved && !p.pressed && walk.echo.replyLength<=0f && renderer.hitsSatchel(p.position.x,p.position.y)) {
                        callMoth();p.consume();continue
                    }
                    if(onInstrument && (moved || !p.pressed)) {
                        if(!walk.echo.active && walk.nearby()?.action==BergAction.ECHO) onEvent(walk.use())
                        val note=renderer.echoKey(p.position.x,p.position.y,state.cave)
                        if(note!=null && note!=lastKey) { walk.leadEcho(null);onEvent(walk.playEcho(note)) }
                        else if(note==null) walk.leadEcho(renderer.mothTarget(p.position.x,p.position.y,state.yaw,state.cave))
                        lastKey=note;p.consume();last=p.position
                        continue
                    }
                    if(moved) {
                        walk.cancelPanFocus()
                        state.yaw-=delta.x*.007f
                        walk.pitch=(walk.pitch+delta.y*.003f).coerceIn(.15f,1.12f)
                        p.consume()
                    }
                    last=p.position
                } while(p.pressed)
                if(!moved && !onInstrument) {
                    val here=walk.nearby()
                    val cap=if(state.cave) renderer.springTarget(last.x,last.y) else null
                    if(renderer.hitsSatchel(last.x,last.y)) callMoth()
                    else if(state.cave && renderer.hitsMouse(last.x,last.y)) {
                        if(CaveMouse.canCall(state.x,state.z)) onEvent(walk.callMouse())
                        else walk.walkTo(CaveMouse.calling)
                    } else if(cap!=null) {
                        if(BergSpring.inReach(state.x,state.z)) onEvent(walk.touchSpring(cap.takeIf {it<3}))
                        else walk.walkTo(BergPoint(BergSpring.bank.x,BergSpring.bank.z))
                    } else if(here!=null && renderer.hitsActivity(last.x,last.y,here)) {
                        onEvent(if(here.action==BergAction.ENTER) walk.waveOutside() else walk.use())
                    } else renderer.pick(last.x,last.y)?.let { walk.walkTo(it);message=null }
                }
            }
        })

        Row(Modifier.align(Alignment.TopStart).padding(edge),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            RoundButton(S.map.str(),{ walk.stop();vm.open(Screen.Map) },size=button,tone=Tones.Sea,icon=Icons.Map)
            Text(sceneLabel,Modifier.background(T.Cream,RoundedCornerShape(18.dp)).padding(horizontal=14.dp,vertical=8.dp),color=T.Ink,style=MaterialTheme.typography.headlineSmall,fontSize=if(compact) 18.sp else 24.sp)
        }
        Row(Modifier.align(Alignment.TopEnd).padding(edge).widthIn(max=teamWidth).horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            team.forEach { person ->
                val chosen=leader==person.id || (leader !in team.map { it.id } && person==team.firstOrNull())
                RoundButton(person.name,{ state.leader=person.id;leader=person.id;walk.stop();walk.resetTrail();vm.changed() },
                    modifier=Modifier.border(if(chosen) 3.dp else 0.dp,if(chosen) T.Sun else Color.Transparent,CircleShape),
                    size=button,tone=Tones.Cream) { drawSpeciesThumb(person.species,Rect(0f,0f,size.width,size.height),person.look) }
            }
            app.trollfoss.ui.screens.ChristmasButton(vm,button)
            RoundButton(S.choosePlayers.str(),{ walk.stop();vm.open(Screen.Players) },size=button,tone=Tones.Cream,icon=Icons.Friends)
        }
        BergJoystick(walk,Modifier.align(Alignment.BottomStart).padding(edge),compact)
        val narrow=maxWidth<560.dp
        val instrumentModifier=if(narrow) Modifier.align(Alignment.TopCenter).padding(top=button+edge+8.dp)
            else Modifier.align(Alignment.BottomCenter).padding(bottom=edge)
        if(echoOpen) EchoControls(tune,if(narrow) 48.dp else button,instrumentModifier,
            play={event(walk.playEcho(it))},
            clear={walk.echo.clearSong();tune=emptyList();replying=false;vm.changed()},
            close={walk.echo.close();echoOpen=false;message=null})
        else Row(Modifier.align(Alignment.BottomCenter).padding(bottom=edge),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            RoundButton(SBerg.resetCamera.str(),{ walk.resetCamera() },size=button,tone=Tones.Cream,icon=Icons.Refresh)
            val shakeState=(if(shake) SBerg.shake else SBerg.noShake).str()
            RoundButton((if(shake) SBerg.turnShakeOff else SBerg.turnShakeOn).str(),{ state.shake=!state.shake;shake=state.shake;vm.changed() },
                modifier=Modifier.semantics {stateDescription=shakeState},size=button,tone=if(shake) Tones.Cream else Tones.Mint) {BergIcons.camera(this,shake)}
        }
        bagVersion // Held-item labels also update while the bag is closed.
        val actionText=when {
            echoOpen -> SBerg.answer
            spot?.action==BergAction.TROLL && state.mothFriend && state.held==BergItem.NONE && tune.isNotEmpty() -> SBerg.ourTune
            else -> SBerg.action(spot?.action,state.held,state.pot)
        }.str()
        Column(Modifier.align(Alignment.BottomEnd).padding(edge),horizontalAlignment=Alignment.End,verticalArrangement=Arrangement.spacedBy(8.dp)) {
            if(spot!=null) Text(actionText,Modifier.background(T.Cream,RoundedCornerShape(12.dp)).padding(8.dp),color=T.Ink,fontSize=if(compact) 14.sp else 18.sp)
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp),verticalAlignment=Alignment.CenterVertically) {
                RoundButton(SBerg.bag.str(),{ walk.stop();bagOpen=true },size=button,tone=Tones.Sun,icon=Icons.Bag)
                RoundButton(actionText,{event(if(echoOpen) walk.echo.answer() else walk.use());spot=walk.nearby()},
                    modifier=Modifier.testTag("highlands-action"),size=button*1.2f,tone=Tones.Mint,
                    enabled=if(echoOpen) tune.isNotEmpty() && !replying else spot!=null) {
                    BergIcons.action(if(echoOpen) BergAction.ECHO else spot?.action,state.held,this)
                }
            }
            BergJoystick(walk,Modifier,compact,camera=true)
        }
        // Keep reactions and faces clear; this space sits above the middle controls.
        val hintModifier=if(narrow && echoOpen) Modifier.align(Alignment.TopCenter).padding(top=button+edge+99.dp)
            else Modifier.align(Alignment.BottomCenter).padding(bottom=if(compact) 80.dp else 112.dp)
        message?.let { msg -> Text(msg.str(),hintModifier.widthIn(max=if(compact) 360.dp else 540.dp).background(T.Cream.copy(alpha=.96f),RoundedCornerShape(14.dp)).padding(12.dp).semantics {liveRegion=LiveRegionMode.Polite},color=T.Ink,fontSize=if(compact) 13.sp else 17.sp) }
        if(loading && !failed) Text(SBerg.loading.str(),Modifier.align(Alignment.Center).background(T.Cream,RoundedCornerShape(20.dp)).padding(24.dp),color=T.Ink,fontSize=20.sp)
        else if(team.isEmpty()) RoundButton(S.choosePlayers.str(),{vm.open(Screen.Players)},Modifier.align(Alignment.Center),size=84.dp,tone=Tones.Sun,icon=Icons.Friends)
        if(failed) TrollDialog({vm.open(Screen.Map)}) { Text(SBerg.error.str(),color=T.Ink) }
    }
    if(bagOpen) {
        bagVersion
        TrollDialog({bagOpen=false}) {
            Text(SBerg.bag.str(),color=T.Ink,fontSize=24.sp)
            val next=BergKitchen.next(state)
            TrailButton(SBerg.recipe.str(),{
                walk.followRecipe();bagVersion++;bagOpen=false;vm.changed()
                message=SBerg.action(next,state.held,state.pot)
            },Modifier.fillMaxWidth(),subtitle=SBerg.trail.str()) {
                BergIcons.action(next,if(next==BergAction.TROLL) BergItem.BERRY_CUP else state.held,this)
            }
            fun guideTo(target:BergPoint,hint:Txt) {
                walk.walkTo(target);bagOpen=false;message=hint
            }
            TrailButton(SBerg.findEcho.str(),{
                val site=BergEcho.site(state.cave)
                guideTo(BergPoint(site.x,site.z),SBerg.echo)
            },Modifier.fillMaxWidth()) { echoSign(2) }
            if(cave) Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                TrailButton(SBerg.findMouse.str(),{guideTo(CaveMouse.calling,SBerg.mouse)},Modifier.weight(1f)) {CaveIcons.mouse(this)}
                TrailButton(SBerg.findSpring.str(),{
                    guideTo(BergPoint(BergSpring.bank.x,BergSpring.bank.z),SBerg.spring)
                },Modifier.weight(1f)) {CaveIcons.spring(this)}
            }
            if(state.bag.isEmpty()) Text(SBerg.empty.str(),color=T.Ink)
            Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                (listOf(BergItem.NONE)+state.bag.toList()).forEach { item ->
                    Column(horizontalAlignment=Alignment.CenterHorizontally) {
                        RoundButton(SBerg.item(item).str(),{state.held=item;bagVersion++;bagOpen=false;vm.changed()},size=64.dp,tone=if(state.held==item) Tones.Mint else Tones.Cream) { BergIcons.item(item,this) }
                        Text(SBerg.item(item).str(),color=T.Ink,fontSize=14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun BergJoystick(walk:HighlandsWalk,modifier:Modifier,compact:Boolean,camera:Boolean=false) {
    var knob by remember { mutableStateOf(Offset.Zero) }
    val label=(if(camera) SBerg.look else SBerg.walk).str()
    val left=(if(camera) SBerg.lookLeft else SBerg.left).str();val right=(if(camera) SBerg.lookRight else SBerg.right).str()
    val ahead=(if(camera) SBerg.lookUp else SBerg.ahead).str();val behind=(if(camera) SBerg.lookDown else SBerg.behind).str()
    fun nudge(x:Float,z:Float):Boolean {
        if(camera) { walk.state.yaw-=x*.3f;walk.pitch=(walk.pitch-z*.15f).coerceIn(.15f,1.12f);return true }
        val nx=walk.state.x+x*cos(walk.state.yaw)-z*sin(walk.state.yaw)
        val nz=walk.state.z-x*sin(walk.state.yaw)-z*cos(walk.state.yaw)
        walk.walkTo(BergPoint(nx,nz));return true
    }
    Canvas(modifier.size(if(camera) { if(compact) 96.dp else 132.dp } else { if(compact) 112.dp else 152.dp }).testTag(if(camera) "highlands-look-stick" else "highlands-stick").semantics {
        contentDescription=label
        customActions=listOf(CustomAccessibilityAction(left){nudge(-1f,0f)},CustomAccessibilityAction(right){nudge(1f,0f)},CustomAccessibilityAction(ahead){nudge(0f,1f)},CustomAccessibilityAction(behind){nudge(0f,-1f)})
    }.pointerInput(walk) {
        awaitEachGesture {
            val down=awaitFirstDown();down.consume()
            fun update(position:Offset) {
                val center=Offset(size.width/2f,size.height/2f)
                val offset=(position-center)/(size.width*.35f)
                val length=offset.getDistance().coerceAtLeast(1f)
                knob=offset/length
                if(camera) {walk.lookSide=knob.x;walk.lookUp=-knob.y}
                else {walk.side=knob.x;walk.forward=-knob.y}
            }
            try {
                update(down.position)
                do { val e=awaitPointerEvent();val p=e.changes.firstOrNull { it.id==down.id } ?: break;update(p.position);p.consume() } while(p.pressed)
            } finally {
                knob=Offset.Zero
                if(camera) {walk.lookSide=0f;walk.lookUp=0f} else {walk.side=0f;walk.forward=0f}
            }
        }
    }) {
        drawCircle(Color(0x997D8890));drawCircle(Color.White.copy(alpha=.65f),style=Stroke(3.dp.toPx()))
        val center=Offset(size.width/2,size.height/2)
        for(i in 0..3) { val angle=i*PI.toFloat()/2;val p=center+Offset(cos(angle),sin(angle))*size.width*.39f;drawCircle(T.Cream,size.width*.035f,p) }
        drawCircle(T.Cream,size.width*.22f,center+knob*size.width*.25f)
        drawCircle(T.CreamDeep,size.width*.22f,center+knob*size.width*.25f,style=Stroke(2.dp.toPx()))
        if(camera) {
            val eye=center+knob*size.width*.25f
            drawOval(T.Ink,eye-Offset(size.width*.12f,size.width*.075f),Size(size.width*.24f,size.width*.15f),style=Stroke(2.dp.toPx()))
            drawCircle(T.Ink,size.width*.037f,eye)
        }
    }
}

internal object BergIcons {
    /** The picture follows the nearby activity, so a child need not read its label. */
    fun action(action:BergAction?,held:BergItem,s:DrawScope)=with(s) {
        val w=size.width;val h=size.height;val line=2.dp.toPx()
        when(action) {
            BergAction.DIAMOND,BergAction.SOCKET -> item(BergItem.DIAMOND,this)
            BergAction.TREASURE -> item(BergItem.GOLD,this)
            BergAction.GOLD -> {
                val bowl=Path().apply {moveTo(w*.08f,h*.44f);quadraticTo(w*.18f,h*.90f,w*.50f,h*.91f);quadraticTo(w*.82f,h*.90f,w*.92f,h*.44f);close()}
                drawPath(bowl,Color(0xFF527F79));drawPath(bowl,T.Ink,style=Stroke(line))
                drawOval(Color(0xFFACC4B0),Offset(w*.08f,h*.28f),Size(w*.84f,h*.33f))
                drawOval(T.Ink,Offset(w*.08f,h*.28f),Size(w*.84f,h*.33f),style=Stroke(line))
                drawOval(T.Sea,Offset(w*.17f,h*.34f),Size(w*.66f,h*.21f))
                for(i in 0..2) drawCircle(T.Sun,w*.05f,Offset(w*(.33f+i*.17f),h*(.43f+(i%2)*.03f)))
            }
            BergAction.PEPSI -> item(BergItem.PEPSI,this)
            BergAction.URGE -> item(BergItem.URGE,this)
            BergAction.CAKE -> item(BergItem.CAKE,this)
            BergAction.MOUSE -> CaveIcons.mouse(this)
            BergAction.SPRING -> CaveIcons.spring(this)
            BergAction.ECHO -> echoSign(2)
            BergAction.BERRIES -> item(BergItem.BERRIES,this)
            BergAction.KETTLE -> {
                drawRoundRect(Color(0xFFC77C54),Offset(w*.13f,h*.45f),Size(w*.65f,h*.38f),androidx.compose.ui.geometry.CornerRadius(w*.13f))
                drawOval(T.Ink,Offset(w*.12f,h*.36f),Size(w*.68f,h*.22f))
                drawLine(Color(0xFFB59269),Offset(w*.50f,h*.49f),Offset(w*.8f,h*.12f),line*1.5f,StrokeCap.Round)
            }
            BergAction.TROLL -> if(held in listOf(BergItem.PEPSI,BergItem.URGE,BergItem.CAKE,BergItem.BERRY_CUP)) item(held,this) else Hand(this)
            BergAction.ENTER,BergAction.EXIT -> {
                val arch=Path().apply {moveTo(w*.18f,h*.88f);lineTo(w*.18f,h*.42f);cubicTo(w*.18f,-h*.02f,w*.82f,-h*.02f,w*.82f,h*.42f);lineTo(w*.82f,h*.88f);close()}
                drawPath(arch,Color(0xFF8594AA));drawPath(arch,T.Ink,style=Stroke(line))
                val opening=Path().apply {moveTo(w*.33f,h*.88f);lineTo(w*.33f,h*.43f);cubicTo(w*.33f,h*.19f,w*.67f,h*.19f,w*.67f,h*.43f);lineTo(w*.67f,h*.88f);close()}
                drawPath(opening,T.Ink)
                val end=if(action==BergAction.ENTER) .63f else .25f
                val start=if(action==BergAction.ENTER) .14f else .78f
                val direction=if(action==BergAction.ENTER) 1f else -1f
                drawLine(T.Cream,Offset(w*start,h*.63f),Offset(w*end,h*.63f),line*1.5f,StrokeCap.Round)
                for(dy in listOf(-.12f,.12f)) drawLine(T.Cream,Offset(w*(end-direction*.13f),h*(.63f+dy)),Offset(w*end,h*.63f),line*1.5f,StrokeCap.Round)
            }
            BergAction.HARE -> {
                for(x in listOf(.34f,.65f)) {
                    drawOval(T.Ink,Offset(w*(x-.10f),h*.02f),Size(w*.20f,h*.52f))
                    drawOval(T.Cream,Offset(w*(x-.067f),h*.055f),Size(w*.134f,h*.42f))
                    drawOval(Color(0xFFE6B3AC),Offset(w*(x-.035f),h*.10f),Size(w*.07f,h*.30f))
                }
                drawOval(T.Cream,Offset(w*.17f,h*.37f),Size(w*.66f,h*.55f))
                drawOval(T.Ink,Offset(w*.17f,h*.37f),Size(w*.66f,h*.55f),style=Stroke(line))
                for(x in listOf(.36f,.64f)) drawCircle(T.Ink,w*.035f,Offset(w*x,h*.61f))
                drawOval(Color(0xFFBA8C89),Offset(w*.45f,h*.71f),Size(w*.10f,h*.065f))
            }
            BergAction.BUTTERFLY -> {
                for(x in listOf(.13f,.51f)) {
                    drawOval(Color(0xFF81BDDB),Offset(w*x,h*.16f),Size(w*.36f,h*.42f))
                    drawOval(T.Ink,Offset(w*x,h*.16f),Size(w*.36f,h*.42f),style=Stroke(line*.75f))
                    drawOval(Color(0xFFB8DDE9),Offset(w*(x+.04f),h*.51f),Size(w*.28f,h*.31f))
                    drawOval(T.Ink,Offset(w*(x+.04f),h*.51f),Size(w*.28f,h*.31f),style=Stroke(line*.75f))
                    drawCircle(T.Cream,w*.04f,Offset(w*(x+.16f),h*.32f))
                }
                drawLine(T.Ink,Offset(w*.5f,h*.3f),Offset(w*.5f,h*.77f),line*1.4f,StrokeCap.Round)
                for(x in listOf(.37f,.63f)) drawLine(T.Ink,Offset(w*.5f,h*.34f),Offset(w*x,h*.12f),line*.7f,StrokeCap.Round)
            }
            BergAction.SKIP -> {
                for(i in 0..2) drawOval(T.Sea,Offset(w*(.12f+i*.25f),h*(.63f-i*.035f)),Size(w*.27f,h*.11f),style=Stroke(line*.65f))
                val arc=Path().apply {moveTo(w*.17f,h*.43f);quadraticTo(w*.38f,h*.10f,w*.55f,h*.52f);quadraticTo(w*.68f,h*.29f,w*.83f,h*.53f)}
                drawPath(arc,T.Cream,style=Stroke(line,cap=StrokeCap.Round))
                drawOval(Color(0xFF929BAC),Offset(w*.07f,h*.32f),Size(w*.27f,h*.13f))
                drawOval(T.Ink,Offset(w*.07f,h*.32f),Size(w*.27f,h*.13f),style=Stroke(line*.75f))
            }
            BergAction.LOOKOUT -> {
                drawOval(T.Cream,Offset(w*.07f,h*.26f),Size(w*.86f,h*.48f))
                drawOval(T.Ink,Offset(w*.07f,h*.26f),Size(w*.86f,h*.48f),style=Stroke(line))
                drawCircle(T.Sea,w*.16f,Offset(w*.5f,h*.5f));drawCircle(T.Ink,w*.08f,Offset(w*.5f,h*.5f))
                drawCircle(T.Cream,w*.035f,Offset(w*.455f,h*.45f))
            }
            BergAction.CRYSTAL -> {
                val colours=listOf(Color(0xFF78CCDD),Color(0xFFB2A0DB),Color(0xFFE8C27D))
                for(i in 0..2) {
                    val x=w*(.23f+i*.27f);val top=h*(if(i==1) .18f else .33f)
                    val shape=Path().apply {moveTo(x,top);lineTo(x+w*.11f,top+h*.16f);lineTo(x+w*.09f,h*.84f);lineTo(x-w*.09f,h*.84f);lineTo(x-w*.11f,top+h*.16f);close()}
                    drawPath(shape,colours[i]);drawPath(shape,T.Ink,style=Stroke(line*.7f))
                    drawLine(T.Cream,Offset(x,top+h*.18f),Offset(x,h*.69f),line,StrokeCap.Round)
                }
            }
            null -> Hand(this)
        }
    }
    fun camera(s:DrawScope,shake:Boolean)=with(s) {
        val w=size.width;val h=size.height;val stroke=Stroke(2.dp.toPx())
        drawRoundRect(T.Sea.copy(alpha=.22f),Offset(w*.22f,h*.28f),Size(w*.56f,h*.46f),androidx.compose.ui.geometry.CornerRadius(w*.07f))
        drawRoundRect(T.Ink,Offset(w*.22f,h*.28f),Size(w*.56f,h*.46f),androidx.compose.ui.geometry.CornerRadius(w*.07f),style=stroke)
        drawCircle(T.Ink,w*.13f,Offset(w*.5f,h*.51f),style=stroke)
        drawLine(T.Ink,Offset(w*.34f,h*.25f),Offset(w*.48f,h*.25f),2.dp.toPx(),StrokeCap.Round)
        if(shake) for(side in listOf(-1f,1f)) {
            val x=w*(.5f+side*.38f)
            val p=Path().apply {moveTo(x,h*.28f);quadraticTo(x-side*w*.08f,h*.4f,x,h*.5f);quadraticTo(x+side*w*.08f,h*.6f,x,h*.74f)}
            drawPath(p,T.Ink,style=stroke)
        } else {
            drawLine(T.MintDeep,Offset(w*.62f,h*.75f),Offset(w*.72f,h*.84f),3.dp.toPx(),StrokeCap.Round)
            drawLine(T.MintDeep,Offset(w*.72f,h*.84f),Offset(w*.9f,h*.61f),3.dp.toPx(),StrokeCap.Round)
        }
    }
    val Hand:DrawScope.()->Unit={
        val w=size.width;val h=size.height
        drawRoundRect(T.Ink,Offset(w*.28f,h*.38f),Size(w*.49f,h*.5f),androidx.compose.ui.geometry.CornerRadius(w*.2f))
        for(i in 0..3) drawLine(T.Ink,Offset(w*(.32f+i*.13f),h*.54f),Offset(w*(.29f+i*.15f),h*(.18f+abs(i-1.5f)*.08f)),w*.14f,StrokeCap.Round)
        drawLine(T.Ink,Offset(w*.42f,h*.7f),Offset(w*.12f,h*.46f),w*.17f,StrokeCap.Round)
        drawRoundRect(Color.White,Offset(w*.32f,h*.4f),Size(w*.41f,h*.43f),androidx.compose.ui.geometry.CornerRadius(w*.18f))
        for(i in 0..3) drawLine(Color.White,Offset(w*(.32f+i*.13f),h*.54f),Offset(w*(.29f+i*.15f),h*(.18f+abs(i-1.5f)*.08f)),w*.09f,StrokeCap.Round)
    }
    fun item(item:BergItem,s:DrawScope)=with(s) {
        val w=size.width;val h=size.height
        when(item) {
            BergItem.NONE -> Hand(this)
            BergItem.DIAMOND -> { val p=Path().apply { moveTo(w*.2f,h*.15f);lineTo(w*.8f,h*.15f);lineTo(w*.98f,h*.4f);lineTo(w*.5f,h*.95f);lineTo(w*.02f,h*.4f);close() };drawPath(p,Color(0xFF67D9F1));drawPath(p,T.Ink,style=Stroke(2.dp.toPx())) }
            BergItem.GOLD -> { drawOval(T.Sun,Offset(w*.08f,h*.3f),Size(w*.84f,h*.5f));drawOval(T.Ink,Offset(w*.08f,h*.3f),Size(w*.84f,h*.5f),style=Stroke(2.dp.toPx())) }
            BergItem.URGE,BergItem.PEPSI -> { drawRoundRect(if(item==BergItem.URGE) Color(0xFFABC958) else T.Ink,Offset(w*.25f,h*.2f),Size(w*.5f,h*.7f),androidx.compose.ui.geometry.CornerRadius(w*.12f));drawRect(if(item==BergItem.URGE) T.Sun else T.Sea,Offset(w*.25f,h*.45f),Size(w*.5f,h*.2f));drawRect(if(item==BergItem.URGE) T.MintDeep else T.Berry,Offset(w*.36f,h*.04f),Size(w*.28f,h*.18f)) }
            BergItem.BERRIES -> {
                drawOval(T.MintDeep,Offset(w*.40f,h*.12f),Size(w*.38f,h*.20f))
                for(p in listOf(.35f to .48f,.65f to .48f,.50f to .70f)) {
                    drawCircle(T.Ink,w*.19f,Offset(w*p.first,h*p.second))
                    drawCircle(Color(0xFF7684B6),w*.145f,Offset(w*p.first,h*p.second))
                    drawCircle(T.Ink,w*.035f,Offset(w*(p.first+.04f),h*(p.second-.045f)))
                }
            }
            BergItem.BERRY_CUP -> {
                drawOval(T.Ink,Offset(w*.64f,h*.34f),Size(w*.26f,h*.31f),style=Stroke(w*.075f))
                drawRoundRect(Color(0xFFE1A266),Offset(w*.14f,h*.30f),Size(w*.56f,h*.48f),androidx.compose.ui.geometry.CornerRadius(w*.12f))
                drawOval(T.Cream,Offset(w*.14f,h*.22f),Size(w*.56f,h*.22f))
                drawOval(Color(0xFF784164),Offset(w*.19f,h*.26f),Size(w*.46f,h*.14f))
            }
            BergItem.CAKE -> { drawRect(Color(0xFFBC895F),Offset(w*.1f,h*.35f),Size(w*.8f,h*.5f));drawOval(T.Cream,Offset(w*.1f,h*.2f),Size(w*.8f,h*.3f));drawCircle(T.Berry,w*.1f,Offset(w*.5f,h*.22f)) }
        }
    }
}

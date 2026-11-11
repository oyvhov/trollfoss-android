package app.trollfoss.ui.berg

import android.app.Application
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.test.platform.app.InstrumentationRegistry
import app.trollfoss.domain.*
import app.trollfoss.ui.*
import app.trollfoss.ui.theme.TrollfossTheme
import app.trollfoss.ui.theme.LocalMotion
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class HighlandsUiTest {
    @get:Rule val rule=createAndroidComposeRule<ComponentActivity>()

    @Test fun mobileAssetsDecodeAndKeepTheGeometryBudget() {
        val assets=BergAssets(rule.activity.assets,true)
        val outside=assets.mesh("outside")
        assertTrue(outside.triangles in 10_000..140_000)
        assertEquals(outside.triangles*3*20,outside.gpuBytes)
        assertTrue(assets.mesh("troll").triangles in 5_000..30_000)
        assertTrue(assets.mesh("cave").triangles in 10_000..85_000)
        assertTrue(assets.mesh("arm").triangles>1000)
        val host=listOf("troll","rumle-eyes","rumle-gaze","arm","rumle-forearm","rumle-hand-grip")
        assertTrue(host.sumOf { assets.mesh(it).triangles } in 16000..34000)
        assertTrue(assets.mesh("gold-pan").triangles in 500..5000)
        assertTrue(assets.mesh("hare").triangles in 1000..10_000)
        for(name in listOf("blue","lilac","honey")) assertTrue(assets.mesh("crystal-$name").triangles in 50..2000)
        for(name in listOf("eira","eilev","oyvind")) {
            val total=listOf("body","arm-l","arm-r","leg-l","leg-r","eyes-open","eyes-closed").sumOf {assets.mesh("$name-$it").triangles}
            assertTrue("$name topology: $total",total in 10_000..30_000)
            assertTrue(assets.mesh("$name-eyes-open").triangles>1000)
            assertTrue(assets.mesh("$name-eyes-closed").triangles in 100..800)
        }
    }

    @Test fun berryTrailUsesRealBagWalkingStationsAndTheServingButton() {
        lateinit var vm:TrollfossViewModel
        rule.runOnUiThread {
            vm=TrollfossViewModel(rule.activity.application as Application)
            vm.splash=false
            val guest=vm.world.people().first { it.name=="Eira" }
            vm.world.playerIds.clear();vm.world.playerIds.add(guest.id);vm.world.flags.add(Players.CHOSEN)
            vm.world.highlands.apply {
                cave=false;x=0f;z=18f;yaw=0f;leader=guest.id;bag.clear();held=BergItem.NONE
                pot=BerryPot.EMPTY;sharedJuice=false
            }
            vm.travel(PlaceId.HEILEBERGET)
        }
        rule.mainClock.autoAdvance=false
        rule.setContent { TrollfossTheme { TrollfossApp(vm) } }
        rule.mainClock.advanceTimeBy(800)
        fun visit(label:String, ms:Long) {
            rule.onNodeWithContentDescription("Tursekk").performClick()
            rule.mainClock.advanceTimeBy(600)
            rule.onNodeWithContentDescription("Vis vegen").performClick()
            rule.mainClock.advanceTimeBy(ms)
            rule.onNodeWithContentDescription(label).performClick()
            rule.mainClock.advanceTimeBy(160)
        }
        visit("Plukk blåbær",4300)
        rule.runOnIdle { assertEquals(BergItem.BERRIES,vm.world.highlands.held) }
        visit("Gå inn",14500)
        visit("Legg bær i gryta",13500)
        rule.onNodeWithContentDescription("Rør i gryta").performClick()
        rule.mainClock.advanceTimeBy(1600)
        rule.onNodeWithContentDescription("Aus opp saft").performClick()
        rule.mainClock.advanceTimeBy(1700)
        visit("Skål med Rumle",10500)
        rule.mainClock.advanceTimeBy(1250)
        rule.onNodeWithText("Skål! Rumle har sett fram ein kopp til deg òg.").assertExists()
        rule.runOnIdle { assertTrue(vm.world.highlands.sharedJuice);assertFalse(BergItem.BERRY_CUP in vm.world.highlands.bag) }
    }

    @Test fun touchARealEchoStoneAnswerTheTuneAndReturnToWalking() {
        lateinit var vm:TrollfossViewModel
        val site=BergEcho.outside
        rule.runOnUiThread {
            vm=TrollfossViewModel(rule.activity.application as Application)
            val guest=vm.world.people().first {it.name=="Eira"}
            vm.splash=false;vm.world.playerIds.clear();vm.world.playerIds.add(guest.id);vm.world.flags.add(Players.CHOSEN)
            vm.world.highlands.apply {x=site.x;z=site.z;cave=false;yaw=0f;leader=guest.id;song.clear()}
            vm.travel(PlaceId.HEILEBERGET)
        }
        rule.mainClock.autoAdvance=false
        rule.setContent {TrollfossTheme {TrollfossApp(vm)}}
        rule.mainClock.advanceTimeBy(800)
        rule.onNodeWithContentDescription("Spel på ekkosteinane").performClick()
        rule.mainClock.advanceTimeBy(400)
        rule.onNodeWithContentDescription("Soltonen").performClick()
        rule.mainClock.advanceTimeBy(560)
        val scene=rule.onNodeWithTag("highlands-scene")
        // The Compose clock can advance before the native surface has finished uploading its meshes.
        rule.waitUntil(30_000) {
            rule.mainClock.advanceTimeBy(48)
            rule.onAllNodesWithText("På veg til fjellet …").fetchSemanticsNodes().isEmpty()
        }
        // Project the centre of the actual moon stone in the opening camera, in window pixels.
        val p=BergEcho.stone(false,2)
        val size=scene.fetchSemanticsNode().size
        val projection=FloatArray(16);val view=FloatArray(16);val vp=FloatArray(16);val clip=FloatArray(4)
        val y=Highlands.floor(site.x,site.z,false)
        val eyeZ=site.z+kotlin.math.cos(.25f)*10f
        val eyeY=maxOf(y+1.1f+kotlin.math.sin(.25f)*10f,Highlands.floor(site.x,eyeZ,false)+1.2f)
        android.opengl.Matrix.perspectiveM(projection,0,56f,size.width.toFloat()/size.height,.2f,260f)
        android.opengl.Matrix.setLookAtM(view,0,site.x,eyeY,eyeZ,site.x,y+1.55f,site.z-2.5f,0f,1f,0f)
        android.opengl.Matrix.multiplyMM(vp,0,projection,0,view,0)
        android.opengl.Matrix.multiplyMV(clip,0,vp,0,floatArrayOf(p.x,Highlands.floor(p.x,p.z,false)+.6f,p.z,1f),0)
        scene.performTouchInput {click(Offset((clip[0]/clip[3]+1f)*width/2,(1f-clip[1]/clip[3])*height/2))}
        rule.mainClock.advanceTimeBy(350)
        rule.runOnIdle {assertEquals(listOf(0,2),vm.world.highlands.song.map {it.tone})}
        scene.performTouchInput {swipe(Offset(width*.39f,height*.24f),Offset(width*.57f,height*.34f),450)}
        rule.mainClock.advanceTimeBy(150)
        rule.runOnIdle {assertEquals(site.x,vm.world.highlands.x,.001f);assertEquals(0f,vm.world.highlands.yaw,.001f)}
        rule.onNodeWithContentDescription("La Rumle svare").performClick()
        rule.mainClock.advanceTimeBy(250)
        rule.onNodeWithContentDescription("La Rumle svare").assertIsNotEnabled()
        rule.mainClock.advanceTimeBy(3800)
        rule.onNodeWithContentDescription("La Rumle svare").assertIsEnabled()
        // The first invitation to tap the satchel arrives while the instrument is still open.
        // Touch that actual bag in the 3D scene, then reopen and close the instrument normally.
        val facing=kotlin.math.atan2(p.x-site.x,p.z-site.z)
        val bodyScale=Residents.eiraLook().height*1.25f
        val satchel=floatArrayOf(site.x+(-.32f*kotlin.math.cos(facing)+.03f*kotlin.math.sin(facing))*bodyScale,
            y+.73f*bodyScale,site.z+(.32f*kotlin.math.sin(facing)+.03f*kotlin.math.cos(facing))*bodyScale,1f)
        android.opengl.Matrix.multiplyMV(clip,0,vp,0,satchel,0)
        scene.performTouchInput {click(Offset((clip[0]/clip[3]+1f)*width/2,(1f-clip[1]/clip[3])*height/2))}
        rule.mainClock.advanceTimeBy(350)
        rule.onNodeWithContentDescription("Spel på ekkosteinane").performClick()
        rule.mainClock.advanceTimeBy(350)
        rule.onNodeWithContentDescription("Lukk").performClick()
        rule.mainClock.advanceTimeBy(200)
        rule.onNodeWithContentDescription("Spel på ekkosteinane").assertIsDisplayed()
        rule.runOnIdle {assertTrue(vm.world.highlands.mothFriend)}
    }

    @Test fun travelThroughTheCaveToTheMouseAndTheLivingSpring() {
        lateinit var vm:TrollfossViewModel
        rule.runOnUiThread {
            vm=TrollfossViewModel(rule.activity.application as Application)
            val guest=vm.world.people().first {it.name=="Eira"}
            vm.splash=false;vm.world.playerIds.clear();vm.world.playerIds.add(guest.id);vm.world.flags.add(Players.CHOSEN)
            vm.world.highlands.apply {
                x=0f;z=10f;cave=true;yaw=0f;leader=guest.id;springAwake=false
                bag.add(BergItem.CAKE);held=BergItem.CAKE
            }
            vm.travel(PlaceId.HEILEBERGET)
        }
        rule.mainClock.autoAdvance=false
        rule.setContent {TrollfossTheme {TrollfossApp(vm)}}
        rule.mainClock.advanceTimeBy(800)
        fun walkTo(label:String,action:String) {
            rule.onNodeWithContentDescription("Tursekk").performClick()
            rule.mainClock.advanceTimeBy(700)
            rule.onNodeWithContentDescription(label).performScrollTo().performClick()
            rule.mainClock.advanceTimeBy(16000)
            rule.onNodeWithContentDescription(action).performClick()
            rule.mainClock.advanceTimeBy(650)
        }
        walkTo("Finn musereiret","Lokk på hòlemusa")
        rule.runOnIdle {assertTrue(CaveMouse.canCall(vm.world.highlands.x,vm.world.highlands.z))}
        walkTo("Finn dryppkjelda","Rør ved kjelda")
        rule.onNodeWithText("Sjå under hatten på soppen!").assertIsDisplayed()
        rule.runOnIdle {assertTrue(vm.world.highlands.springAwake);assertEquals(BergItem.CAKE,vm.world.highlands.held)}
    }

    @Test fun recordRumlesPosesAndTheKitchenFromTheNativeSurface() {
        val handheld=rule.activity.resources.configuration.smallestScreenWidthDp<600
        val renderer=BergRenderer(rule.activity.assets,handheld)
        lateinit var surface:GLSurfaceView
        val guest=BergAppearance(42,Residents.eiraLook(),emptyList(),"Eira")
        val home=BergFrame(x=-2.2f,z=-1.35f,cave=true,distance=6.0f,pitch=.20f,visitFocus=.9f,
            walkers=listOf(BergWalker(guest,BergKitchen.serving.x,BergKitchen.serving.z,3.14f)))
        renderer.frame=home
        rule.setContent { AndroidView(factory={ctx -> GLSurfaceView(ctx).apply {
            setEGLContextClientVersion(2);setEGLConfigChooser(BergEglConfig())
            setRenderer(renderer);renderMode=GLSurfaceView.RENDERMODE_WHEN_DIRTY;surface=this
        } }) }
        rule.waitUntil(30000){renderer.frames>0 || renderer.failure!=null}
        fun record(frame:BergFrame,file:String) {
            renderer.frame=frame
            repeat(2) {
                val before=renderer.frames
                rule.runOnUiThread {surface.requestRender()}
                rule.waitUntil(20000){renderer.frames>before || renderer.failure!=null}
                assertNull(renderer.failure)
            }
            surfaceBrightness(surface,file,checkVisible=true)
        }
        record(home.copy(time=6.35f),"rumle-kvardag.png")
        val mouse=CaveMouse.calling
        record(BergFrame(x=mouse.x,z=mouse.z,cave=true,time=8f,caveMouseAge=3.05f,roomFocus=1f,roomTarget=BergAction.MOUSE,
            walkers=listOf(BergWalker(guest,mouse.x,mouse.z,3.14f))),"rumle-musa.png")
        val bank=BergSpring.bank
        record(BergFrame(x=bank.x,z=bank.z,cave=true,time=4.4f,springAwake=true,springAge=.66f,roomFocus=1f,roomTarget=BergAction.SPRING,
            walkers=listOf(BergWalker(guest,bank.x,bank.z,3.14f))),"rumle-dryppkjelda.png")
        val music=EchoFrame(active=true,tone=2,glow=.75f,replyAge=1.45f,replyLength=3.20f)
        record(home.copy(time=5.2f,echo=music),"rumle-spelmann.png")
        val echo=BergEcho.outside
        record(BergFrame(x=echo.x,z=echo.z,night=true,time=7.1f,distance=7.9f,echo=music,
            walkers=listOf(BergWalker(guest,echo.x,echo.z,3.14f))),"rumle-lysvatn.png")
        record(BergFrame(x=4.0f,z=14.1f,yaw=2.81f,pitch=.22f,distance=4f,time=2f,
            mothFriend=true,friendReach=1f,walkers=listOf(BergWalker(guest,4.0f,14.1f,2.81f))),"rumle-turvenn.png")
        record(home.copy(time=1f,rumle=RumleMoment(RumleGesture.WAVE,1.1f)),"rumle-wave.png")
        record(home.copy(time=2f,rumle=RumleMoment(RumleGesture.DRINK,1.77f,BergItem.URGE,42)),"rumle-drink.png")
        record(home.copy(time=3f,sharedJuice=true,rumle=RumleMoment(RumleGesture.TOAST,1.16f,BergItem.BERRY_CUP,42)),"rumle-toast.png")
        record(home.copy(time=4f,sharedJuice=true,motion=false),"rumle-table.png")
        record(BergFrame(x=4.2f,z=12.9f,pitch=.36f,distance=4.2f),"rumle-berries.png")
        record(BergFrame(x=-16.6f,z=-3.65f,cave=true,distance=4.3f,pitch=.28f,pot=BerryPot.STEEPED),"rumle-hearth.png")
        rule.runOnUiThread {surface.onPause()}
    }

    @Test fun realControlsTravelCarryDrinkAndReturnWithoutChangingPeople() {
        lateinit var vm:TrollfossViewModel
        rule.runOnUiThread {
            vm=TrollfossViewModel(rule.activity.application as Application)
            vm.splash=false
            vm.world.playerIds.clear()
            val people=vm.world.people().filter { it.species==Species.FOLK }
            val chosen=listOf("Eira","Eilev","Øyvind").mapNotNull { name -> people.find { it.name==name } }.ifEmpty { people.take(3) }
            chosen.forEach { vm.world.playerIds.add(it.id) }
            vm.world.flags.add(Players.CHOSEN)
            vm.world.highlands.apply { x=0f;z=18f;yaw=0f;cave=false;bag.clear();held=BergItem.NONE }
            vm.travel(PlaceId.HEILEBERGET)
        }
        val ids=vm.world.playerIds.toList();val looks=Players.team(vm.world).map { it.look }
        rule.mainClock.autoAdvance=false
        rule.setContent { TrollfossTheme { TrollfossApp(vm) } }
        rule.mainClock.advanceTimeBy(1200)
        rule.onNodeWithTag("highlands-scene").assertExists()
        rule.onNodeWithTag("highlands-action").assertIsNotEnabled()
        rule.onNodeWithTag("highlands-stick").performTouchInput { down(center);moveTo(center-Offset(0f,height*.25f)) }
        rule.mainClock.advanceTimeBy(1000)
        rule.onNodeWithTag("highlands-stick").performTouchInput { up() }
        rule.runOnIdle { assertTrue(vm.world.highlands.z<17f) }
        val moveBounds=rule.onNodeWithTag("highlands-stick").fetchSemanticsNode().boundsInRoot
        val lookBounds=rule.onNodeWithTag("highlands-look-stick").fetchSemanticsNode().boundsInRoot
        val beforeZ=vm.world.highlands.z
        rule.onRoot().performTouchInput {
            down(0,moveBounds.center);down(1,lookBounds.center)
            moveTo(0,moveBounds.center-Offset(0f,moveBounds.height*.25f))
            moveTo(1,lookBounds.center+Offset(lookBounds.width*.25f,0f))
        }
        rule.mainClock.advanceTimeBy(600)
        rule.onRoot().performTouchInput {up(0);up(1)}
        rule.runOnIdle {assertTrue(vm.world.highlands.z<beforeZ);assertTrue(vm.world.highlands.yaw<-.3f)}
        val stoppedYaw=vm.world.highlands.yaw
        rule.mainClock.advanceTimeBy(300)
        rule.runOnIdle {assertEquals(stoppedYaw,vm.world.highlands.yaw,.001f)}
        // A camera-only gesture must be saved while the family stands still,
        // without relying on leaving the screen or backgrounding the app.
        rule.onNodeWithTag("highlands-look-stick").performTouchInput {down(center);moveTo(center+Offset(width*.25f,0f))}
        rule.mainClock.advanceTimeBy(500)
        rule.onNodeWithTag("highlands-look-stick").performTouchInput {up()}
        val stationaryYaw=vm.world.highlands.yaw
        assertTrue(stationaryYaw<stoppedYaw-.3f)
        rule.mainClock.advanceTimeBy(4200)
        rule.waitUntil(5000) {
            runCatching {
                val save=org.json.JSONObject(File(rule.activity.filesDir,"trollfoss.json").readText())
                kotlin.math.abs(save.getJSONObject("highlands").getDouble("yaw").toFloat()-stationaryYaw)<.001f
            }.getOrDefault(false)
        }
        rule.onNodeWithContentDescription("Sjå framover").performClick()
        rule.onNodeWithContentDescription("Tursekk").performClick()
        rule.mainClock.advanceTimeBy(600)
        rule.onNodeWithText("Finn skattar eller besøk matlageret i hola.").assertExists()
        rule.onNodeWithContentDescription("Lukk").performClick()
        rule.mainClock.advanceTimeBy(400)
        rule.onNodeWithText("Tursekk").assertDoesNotExist()

        rule.runOnIdle {vm.world.highlands.x=17f;vm.world.highlands.z=-5f}
        rule.mainClock.advanceTimeBy(300)
        rule.onNodeWithContentDescription("Hels på haren").performClick()
        rule.mainClock.advanceTimeBy(100)
        rule.onNodeWithText("Haren helsar med eit lite hopp!").assertExists()

        rule.runOnIdle {vm.world.highlands.x=-14f;vm.world.highlands.z=4.5f;vm.world.highlands.taken=0}
        rule.mainClock.advanceTimeBy(300)
        rule.onNodeWithContentDescription("Vask gull").performClick()
        rule.mainClock.advanceTimeBy(3500)
        rule.runOnIdle {assertEquals(BergItem.GOLD,vm.world.highlands.held);assertTrue(vm.world.highlands.taken and 2!=0)}

        rule.runOnIdle {vm.world.highlands.x=-3f;vm.world.highlands.z=19f}
        rule.mainClock.advanceTimeBy(300)
        rule.onNodeWithContentDescription("Hels på sommarfuglen").performClick()
        rule.mainClock.advanceTimeBy(3000)
        rule.runOnIdle {assertEquals(BergItem.GOLD,vm.world.highlands.held);assertEquals(-3f,vm.world.highlands.x,.001f)}

        rule.runOnIdle {vm.world.highlands.x=5f;vm.world.highlands.z=7f}
        rule.mainClock.advanceTimeBy(300)
        rule.onNodeWithContentDescription("Kast smutt").performClick()
        rule.mainClock.advanceTimeBy(3800)
        rule.runOnIdle {assertEquals(5f,vm.world.highlands.x,.001f);assertEquals(7f,vm.world.highlands.z,.001f)}

        rule.runOnIdle { vm.world.highlands.x=13f;vm.world.highlands.z=-11f }
        rule.mainClock.advanceTimeBy(300)
        rule.onNodeWithContentDescription("Gå inn").performClick()
        rule.mainClock.advanceTimeBy(900)
        rule.runOnIdle { assertTrue(vm.world.highlands.cave) }
        rule.onNodeWithText("Inni Heileberget").assertExists()
        rule.runOnIdle {vm.world.highlands.x=9f;vm.world.highlands.z=-5f}
        rule.mainClock.advanceTimeBy(300)
        rule.onNodeWithContentDescription("Ta Urge").performClick()
        rule.runOnIdle {assertEquals(BergItem.URGE,vm.world.highlands.held)}
        rule.runOnIdle {vm.world.highlands.x=-3f;vm.world.highlands.z=-1f}
        rule.mainClock.advanceTimeBy(300)
        rule.onNodeWithContentDescription("Gi Urge").performClick()
        rule.mainClock.advanceTimeBy(3200)
        rule.runOnIdle {assertEquals(BergItem.NONE,vm.world.highlands.held)}

        Highlands.crystals.forEach {p ->
            rule.runOnIdle {vm.world.highlands.x=p.x;vm.world.highlands.z=p.z+2f}
            rule.mainClock.advanceTimeBy(300)
            rule.onNodeWithContentDescription("Spel på krystallane").performClick()
            rule.mainClock.advanceTimeBy(150)
        }

        rule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        rule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        rule.mainClock.advanceTimeBy(600)
        rule.runOnIdle {assertTrue(vm.world.highlands.cave);assertEquals(ids,vm.world.playerIds.toList());assertEquals(looks,Players.team(vm.world).map{it.look})}
        rule.onNodeWithContentDescription("Kart").performClick()
        rule.mainClock.advanceTimeBy(600)
        rule.runOnIdle {assertEquals(Screen.Map,vm.screen)}
    }

    @Test fun pinchShowsTheFacesCloserWithoutWalkingOrTurningWhenOneFingerLifts() {
        lateinit var vm:TrollfossViewModel
        rule.runOnUiThread {
            vm=TrollfossViewModel(rule.activity.application as Application);vm.splash=false
            if(vm.night) vm.toggleNight()
            val people=vm.world.people().filter {it.species==Species.FOLK}
            val team=listOf("Eilev","Eira","Øyvind").map {name -> people.first {it.name==name}}
            vm.world.playerIds.clear();team.forEach {vm.world.playerIds.add(it.id)}
            vm.world.flags.add(Players.CHOSEN)
            vm.world.highlands.apply {x=0f;z=15f;yaw=3.14159f;cave=false;leader=team.first().id}
            vm.travel(PlaceId.HEILEBERGET)
        }
        rule.mainClock.autoAdvance=false
        rule.setContent {TrollfossTheme {CompositionLocalProvider(LocalMotion provides false) {TrollfossApp(vm)}}}
        rule.mainClock.advanceTimeBy(1200)
        fun findSurface(view:View):GLSurfaceView? {
            if(view is GLSurfaceView) return view
            if(view is ViewGroup) for(index in 0 until view.childCount) findSurface(view.getChildAt(index))?.let {return it}
            return null
        }
        lateinit var surface:GLSurfaceView
        rule.runOnIdle {surface=checkNotNull(findSurface(rule.activity.window.decorView))}
        android.os.SystemClock.sleep(700)
        val before=surfaceBitmap(surface)
        outFile("pinch-before.png").outputStream().use {before.compress(Bitmap.CompressFormat.PNG,100,it)}
        val yaw=vm.world.highlands.yaw
        rule.onNodeWithTag("highlands-scene").performTouchInput {
            val y=height*.37f
            down(0,Offset(width*.425f,y));down(1,Offset(width*.575f,y))
            for(i in 1..12) {
                val expansion=width*.105f*i/12f
                moveTo(0,Offset(width*.425f-expansion,y))
                moveTo(1,Offset(width*.575f+expansion,y))
            }
            up(0);moveTo(1,Offset(width*.8f,y+height*.1f));up(1)
        }
        rule.mainClock.advanceTimeBy(600)
        rule.runOnIdle {
            assertEquals(0f,vm.world.highlands.x,.001f);assertEquals(15f,vm.world.highlands.z,.001f)
            assertEquals(yaw,vm.world.highlands.yaw,.001f)
        }
        fun difference(a:Bitmap,b:Bitmap):Float {
            var sum=0f;var samples=0
            for(y in a.height/3 until a.height*9/10 step 16) for(x in a.width/4 until a.width*3/4 step 16) {
                val pa=a.getPixel(x,y);val pb=b.getPixel(x,y)
                for(shift in listOf(0,8,16)) {sum+=kotlin.math.abs((pa shr shift and 255)-(pb shr shift and 255));samples++}
            }
            return sum/samples
        }
        fun shirtArea(bitmap:Bitmap):Int {
            // Eilev's teal shirt is isolated from sky/grass in this fixed view.
            // Its visible area must grow, not merely shift by a few pixels.
            var area=0
            for(y in bitmap.height/3 until bitmap.height*9/10 step 2) {
                for(x in bitmap.width*2/5 until bitmap.width*3/5 step 2) {
                    val p=bitmap.getPixel(x,y)
                    val r=p shr 16 and 255;val g=p shr 8 and 255;val b=p and 255
                    if(r<75 && g>70 && b>60 && g>r*1.4f) area++
                }
            }
            return area
        }
        val beforeArea=shirtArea(before)
        assertTrue("The initial view must contain Eilev's shirt",beforeArea>100)
        var after=surfaceBitmap(surface)
        var settled=0
        repeat(30) {
            if(settled<2) {
                android.os.SystemClock.sleep(150)
                val next=surfaceBitmap(surface)
                settled=if(difference(after,next)<.05f && shirtArea(next)>beforeArea*2) settled+1 else 0
                after.recycle();after=next
            }
        }
        outFile("pinch-after.png").outputStream().use {after.compress(Bitmap.CompressFormat.PNG,100,it)}
        assertTrue("Wait for the fully zoomed GL buffer, not an intermediate frame",settled>=2)
        assertTrue("Zoom must make Eilev clearly larger",shirtArea(after)>beforeArea*2)
        assertTrue("Spreading two fingers must visibly zoom the actual GL scene",difference(before,after)>4f)
        before.recycle();after.recycle()
        rule.onNodeWithContentDescription("Sjå framover").performClick()
        rule.mainClock.advanceTimeBy(300)
    }

    @Test fun realGpuRendersBothAreasAndRecreatesTheContext() {
        val renderer=BergRenderer(rule.activity.assets)
        lateinit var surface:GLSurfaceView
        val appearance=BergAppearance(1,Residents.eiraLook(),emptyList(),"Eira")
        val eilev=BergAppearance(2,Residents.eilevLook(),emptyList(),"Eilev")
        assertEquals("eira",BergCharacters.model(appearance));assertEquals("eilev",BergCharacters.model(eilev))
        val adult=BergAppearance(3,Residents.oyvindLook(),emptyList(),"Øyvind")
        assertEquals("oyvind",BergCharacters.model(adult))
        assertNull(BergCharacters.model(adult.copy(look=adult.look.copy(hairColor=3))))
        assertNull(BergCharacters.model(appearance.copy(look=appearance.look.copy(topColor=3))))
        renderer.frame=BergFrame(walkers=listOf(BergWalker(appearance,0f,18f,0f),BergWalker(eilev,1f,18f,0f)),moving=true,time=.7f)
        rule.setContent { AndroidView(factory={context -> GLSurfaceView(context).apply {
            setEGLContextClientVersion(2);setEGLConfigChooser(BergEglConfig())
            setRenderer(renderer);renderMode=GLSurfaceView.RENDERMODE_WHEN_DIRTY;surface=this
        } }) }
        rule.waitUntil(30000) { renderer.failure!=null || renderer.frames>0 }
        assertNull(renderer.failure);assertTrue(renderer.triangleCount>10000)
        val draws=ArrayList<Long>()
        repeat(15) {
            val before=renderer.frames
            rule.runOnUiThread {surface.requestRender()}
            rule.waitUntil(10000) {renderer.frames>before || renderer.failure!=null}
            assertNull(renderer.failure);draws.add(renderer.drawNanos)
        }
        val dayBrightness=surfaceBrightness(surface,"gpu-day.png")
        val dayTriangles=renderer.triangleCount
        val daylight=renderer.frame
        // Eira closes her eyes briefly, independently of Eilev. Reduced motion
        // restores the original eyes without discarding or rebuilding the avatar.
        renderer.frame=daylight.copy(time=3.67f)
        repeat(2) {
            val before=renderer.frames
            rule.runOnUiThread {surface.requestRender()}
            rule.waitUntil(10000) {renderer.frames>before || renderer.failure!=null}
            assertNull(renderer.failure)
        }
        assertTrue(renderer.triangleCount<dayTriangles-1000)
        surfaceBrightness(surface,"gpu-blink.png")
        renderer.frame=renderer.frame.copy(motion=false)
        val beforeStill=renderer.frames
        rule.runOnUiThread {surface.requestRender()}
        rule.waitUntil(10000) {renderer.frames>beforeStill || renderer.failure!=null}
        assertEquals(dayTriangles,renderer.triangleCount)
        renderer.frame=daylight
        renderer.frame=renderer.frame.copy(night=true)
        // onDrawFrame finishes before GLSurfaceView swaps its buffers. A second
        // completed draw proves that the first night frame has been presented.
        repeat(2) {
            val dayFrames=renderer.frames
            rule.runOnUiThread {surface.requestRender()}
            rule.waitUntil(10000) {renderer.frames>dayFrames || renderer.failure!=null}
            assertNull(renderer.failure)
        }
        val nightBrightness=surfaceBrightness(surface,"gpu-night.png")
        assertTrue("Night luminance $nightBrightness must be below daylight $dayBrightness",nightBrightness<dayBrightness*.85f)
        assertTrue(renderer.triangleCount in (dayTriangles+1)..(dayTriangles+2000))
        renderer.frame=BergFrame(x=-3f,z=19f,yaw=3.14f,distance=5f,motion=false,
            butterflyPlay=4f,walkers=listOf(BergWalker(appearance,-3f,19f,3.14f)))
        repeat(2) {
            val previous=renderer.frames
            rule.runOnUiThread {surface.requestRender()}
            rule.waitUntil(10000) {renderer.frames>previous || renderer.failure!=null}
            assertNull(renderer.failure)
        }
        surfaceBrightness(surface,"gpu-butterfly.png",checkVisible=true)
        renderer.frame=BergFrame(x=8f,z=7f,cave=true,walkers=listOf(BergWalker(appearance,8f,7f,3.14f)))
        repeat(2) {
            val previous=renderer.frames
            rule.runOnUiThread {surface.requestRender()}
            rule.waitUntil(10000) {renderer.frames>previous || renderer.failure!=null}
            assertNull(renderer.failure)
        }
        surfaceBrightness(surface,"gpu-ramp.png",checkVisible=true)
        renderer.frame=BergFrame(x=0f,z=10f,cave=true,walkers=listOf(BergWalker(appearance,0f,10f,3.14f)))
        var before=renderer.frames
        rule.runOnUiThread {surface.requestRender()}
        rule.waitUntil(20000) {renderer.frames>before || renderer.failure!=null}
        assertNull(renderer.failure)
        val contexts=renderer.contexts
        rule.runOnUiThread {surface.onPause();surface.onResume();surface.requestRender()}
        rule.waitUntil(20000) {(renderer.contexts>contexts && renderer.frames>0) || renderer.failure!=null}
        assertNull(renderer.failure)
        outFile("gpu-timings.txt").writeText("CPU submit milliseconds (emulator, not device FPS): ${draws.map {it/1e6}}\nTriangles: ${renderer.triangleCount}\n")
        rule.runOnUiThread {surface.onPause()}
    }

    private fun outFile(name:String):File {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        return File(context.getExternalFilesDir(null),"heileberget").apply{mkdirs()}.resolve(name)
    }
    private fun surfaceBitmap(surface:GLSurfaceView):Bitmap {
        // Copy the GL buffer itself: the instrumentation window can be covered by
        // another activity even though this SurfaceView has rendered successfully.
        val bitmap=Bitmap.createBitmap(surface.width,surface.height,Bitmap.Config.ARGB_8888)
        val deadline=android.os.SystemClock.elapsedRealtime()+15_000L
        var result:Int
        do {
            val copied=CountDownLatch(1)
            result=-1
            PixelCopy.request(surface,bitmap,{result=it;copied.countDown()},Handler(Looper.getMainLooper()))
            assertTrue("GL pixel copy timed out",copied.await(10,TimeUnit.SECONDS))
            if(result==PixelCopy.SUCCESS) return bitmap
            if(result!=PixelCopy.ERROR_SOURCE_NO_DATA && result!=PixelCopy.ERROR_TIMEOUT) break
            // A newly created surface has no front buffer until EGL presents its first frame.
            surface.requestRender()
            android.os.SystemClock.sleep(75)
        } while(android.os.SystemClock.elapsedRealtime()<deadline)
        bitmap.recycle()
        throw AssertionError("The native surface never supplied a frame (PixelCopy result $result)")
    }
    private fun surfaceBrightness(surface:GLSurfaceView,name:String,checkVisible:Boolean=false):Float {
        val bitmap=surfaceBitmap(surface)
        outFile(name).outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
        var sum=0f;var squares=0f;var count=0
        for(y in bitmap.height/10 until bitmap.height*9/10 step 24) {
            for(x in bitmap.width/10 until bitmap.width*9/10 step 24) {
                val pixel=bitmap.getPixel(x,y)
                val value=(pixel shr 16 and 255)*.2126f+(pixel shr 8 and 255)*.7152f+(pixel and 255)*.0722f
                sum+=value;squares+=value*value
                count++
            }
        }
        val mean=sum/count
        if(checkVisible) assertTrue("The camera must show the room, not one flat wall",squares/count-mean*mean>100f)
        bitmap.recycle();return mean
    }
}

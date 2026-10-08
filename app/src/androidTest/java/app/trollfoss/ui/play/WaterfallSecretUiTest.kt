package app.trollfoss.ui.play

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.lifecycle.ViewModelStore
import app.trollfoss.audio.Sfx
import app.trollfoss.data.Settings
import app.trollfoss.data.WorldStore
import app.trollfoss.domain.*
import app.trollfoss.ui.Screen
import app.trollfoss.ui.TrollfossViewModel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WaterfallSecretUiTest {
    private var journey: Passage? = null
    private var arrival = 0f
    private val host = object : EngineHost {
        override fun sfx(effect: Sfx, volume: Float, rate: Float) {}
        override fun haptic() {}
        override fun changed() {}
        override fun secretFound(id: String) {}
        override fun discovered(key: String) {}
        override fun telescope() {}
        override fun radio(on: Boolean) {}
        override fun egg(id: String) {}
        override fun passage(passage: Passage, arrivalX: Float) { journey=passage; arrival=arrivalX }
    }

    private fun scenes(check: (Engine, (Float,Float)->Offset, String, Int, Int, Float)->Unit) {
        for (phone in listOf(false,true)) for (place in listOf(PlaceId.FOREST,PlaceId.LAB)) {
            val width=if(phone) 2400 else 1920; val height=if(phone) 1080 else 1200
            val density=if(phone) 2.625f else 1.5f
            val world=WorldFactory.create().apply { this.place=place }
            val engine=Engine(world,place,Sim(world),host,false,0f).apply {
                compact=phone; setSize(width.toFloat(),height.toFloat(),density)
                focusOn(if(place==PlaceId.FOREST) 3.02f else 2.735f)
                repeat(15) { update(0.016f) }
            }
            fun at(x:Float,y:Float)=Offset((x-engine.cam)*engine.u,height-engine.u+y*engine.u)
            journey=null
            try { check(engine,::at,"${if(phone) "phone" else "tablet"}-${place.name}",width,height,density) }
            finally { engine.cancel() }
        }
    }

    @Test fun tappingTheRealWaterEntersAndLeavesWhileDraggingStillPans() = scenes { e,at,_,_,_,_ ->
        val p=if(e.place==PlaceId.FOREST) at(3.02f,0.59f) else at(2.735f,0.235f)
        e.down(1,p,1000); e.move(1,p+Offset(-110f,0f),1150); e.up(1,p+Offset(-110f,0f),1300)
        assertNull("swiping water does not travel",journey)
        e.focusOn(if(e.place==PlaceId.FOREST) 3.02f else 2.735f)
        val tap=if(e.place==PlaceId.FOREST) at(3.02f,0.59f) else at(2.735f,0.235f)
        e.down(2,tap,2000); e.up(2,tap,2080)
        assertEquals(WaterfallSecret.passage(e.place),journey)
        assertEquals(WaterfallSecret.arrival(journey!!),arrival,0.001f)
    }

    @Test fun aSecondFingerAndOpenPanelsDoNotCauseAnAccidentalTrip() = scenes { e,at,_,_,_,_ ->
        val p=if(e.place==PlaceId.FOREST) at(3.02f,0.59f) else at(2.735f,0.235f)
        e.down(3,at(1.6f,0.18f),3000)
        e.down(4,p,3100); e.up(4,p,3180)
        assertNull(journey); assertFalse(e.throughWaterfall())
        e.cancel()
        e.bagOpen=true; assertFalse(e.throughWaterfall())
        e.bagOpen=false; e.designMode=true; assertFalse(e.throughWaterfall())
        e.designMode=false; assertTrue(e.throughWaterfall())
    }

    @Test fun captureBothEntrancesAmongExistingFurniture() = scenes { e,_,label,width,height,d ->
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val out=java.io.File(context.filesDir,"waterfall-scenes").apply { mkdirs() }
        val density=Density(d)
        val text=TextMeasurer(createFontFamilyResolver(context),density,LayoutDirection.Ltr)
        val bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
        try {
            e.photoMode=true
            CanvasDrawScope().draw(density,LayoutDirection.Ltr,Canvas(bitmap.asImageBitmap()),Size(width.toFloat(),height.toFloat())) { e.draw(this,text) }
            java.io.File(out,"$label.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
        } finally { bitmap.recycle() }
    }

    @Test fun theMapAndSceneUseTheRealNavigationAndKeepTheSamePlayersAndBelongings() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val store=ViewModelStore()
            val vm=TrollfossViewModel(instrumentation.targetContext.applicationContext as android.app.Application)
            store.put("waterfall-test",vm)
            try {
                vm.updateSettings { it.copy(sound=false,music=false,haptics=false) }
                vm.travel(PlaceId.HOME)
                vm.world.visited.remove(PlaceId.LAB)
                val team=vm.world.people().filter { it.name in listOf("Hedda","Alva") }
                vm.world.playerIds.clear(); vm.world.playerIds.addAll(team.map { it.id })
                val hat=vm.world.addThing(ThingType.CROWN,1,PlaceId.HOME,1f,0.9f)
                vm.sim.give(team.first(),hat,Part.HAT)
                val before=vm.world.bodies.size

                vm.travelPlayCard(PlaceId.LAB,2.13f)
                assertEquals("a hint cannot bypass the hidden entrance",PlaceId.FOREST,vm.place)
                assertFalse(WaterfallSecret.known(vm.world))
                assertTrue(team.all { it.x < Places.spec(PlaceId.FOREST).water!!.x1 })

                vm.open(Screen.Map); vm.waterfallFromMap()
                assertEquals(Screen.Play,vm.screen); assertEquals(PlaceId.LAB,vm.place)
                assertTrue(WaterfallSecret.known(vm.world))
                assertTrue(team.all { it.place==PlaceId.LAB })
                assertEquals(team.first().id,hat.holder); assertEquals(PlaceId.LAB,hat.place)

                val engine=vm.engineFor(PlaceId.LAB,false)
                engine.setSize(1920f,1200f,1.5f)
                assertTrue(engine.throughWaterfall())
                assertEquals(PlaceId.FOREST,vm.place)
                assertTrue(team.all { it.place==PlaceId.FOREST && it.x<2.55f })
                assertEquals(before,vm.world.bodies.size)
                val reloaded=WorldStore.decode(WorldStore.encode(vm.world,Settings())).world
                assertTrue(WaterfallSecret.known(reloaded))
                assertEquals(vm.world.playerIds,reloaded.playerIds)
                assertEquals(team.first().id,reloaded.bodies.getValue(hat.id).holder)
            } finally { vm.engine?.cancel(); store.clear() }
        }
    }
}

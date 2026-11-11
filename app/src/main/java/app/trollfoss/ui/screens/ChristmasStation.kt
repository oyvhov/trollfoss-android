package app.trollfoss.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import app.trollfoss.domain.*
import app.trollfoss.ui.*
import app.trollfoss.ui.components.*
import app.trollfoss.ui.theme.T

/** Picture recipes keep making, changing the recipe and retrieving unfinished work explicit. */
@Composable
fun ChristmasStation(vm:TrollfossViewModel,f:Fixture,onClose:()->Unit) {
    if(f.type==FixtureType.XM_SLEIGH) {ChristmasSleigh(vm,f,onClose);return}
    if(f.type in setOf(FixtureType.XM_CALENDAR,FixtureType.XM_MARKET,FixtureType.XM_SLEIGH)) {
        ChristmasPanel(vm,onClose,initialTab=if(f.type==FixtureType.XM_CALENDAR) 2 else if(f.type==FixtureType.XM_SLEIGH) 3 else 0)
        return
    }
    var revision by remember(f.id) { mutableIntStateOf(0) }
    val outputs=when(f.type) {
        FixtureType.XM_BAKE -> listOf(ThingType.XM_GINGER_MAN,ThingType.XM_GINGER_HEART,ThingType.XM_GINGER_STAR,ThingType.XM_LUSSEKATT,ThingType.XM_PORRIDGE)
        FixtureType.XM_HOUSE_BENCH -> listOf(ThingType.XM_GINGER_HOUSE)
        FixtureType.XM_CRAFT -> listOf(ThingType.XM_PAPER_HEART,ThingType.XM_CHAIN,ThingType.XM_SNOWFLAKE)
        FixtureType.XM_TOY_BENCH -> listOf(ThingType.XM_TRAIN,ThingType.XM_TOP,ThingType.XM_ROBOT)
        else -> listOf(ThingType.XM_PARCEL)
    }
    val materials=when(f.type) {
        FixtureType.XM_BAKE -> listOf(if(f.mode==4) ThingType.MILK else ThingType.XM_DOUGH,ThingType.XM_ROLLING_PIN,ThingType.XM_CUTTER,ThingType.XM_ICING)
        FixtureType.XM_HOUSE_BENCH -> listOf(ThingType.XM_GINGER_HEART,ThingType.XM_ICING)
        FixtureType.XM_CRAFT -> listOf(ThingType.XM_PAPER,ThingType.XM_BRUSH,ThingType.SNOWBALL)
        FixtureType.XM_TOY_BENCH -> listOf(ThingType.PLANK,ThingType.STICK,ThingType.HAMMER)
        else -> listOf(ThingType.XM_TRAIN,ThingType.XM_BEAR,ThingType.XM_ROBOT)
    }
    TrollDialog(onClose,maxWidth=650.dp) {
        revision
        GameText(SX.ideas.str(),fontSize=22.sp,color=T.Ink)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            outputs.forEachIndexed { i,type -> Column(Modifier.background(if(f.mode==i) T.SunTop else Color.White,RoundedCornerShape(12.dp)).clickable { f.mode=i;revision++;vm.changed() }.padding(8.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                ChristmasPicture(type)
                GameText(SX.thing(type).str(),fontSize=12.sp,color=T.Ink)
            } }
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            materials.forEach {type -> Column(Modifier.clickable {vm.sim.christmas.supply(type,place=f.place,x=f.x+0.35f);vm.changed();onClose()}.padding(8.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                ChristmasPicture(type)
                GameText(SX.take.str(),fontSize=14.sp,color=T.Ink)
            } }
        }
        if(f.type==FixtureType.XM_BAKE) {
            val hint=if(f.mode==4) { if(f.on) SX.porridgeCooking else SX.porridgeReady } else when(f.count) {
                0 -> SX.recipeStep0
                1 -> SX.recipeStep1
                2 -> SX.recipeStep2
                3 -> SX.recipeStep3
                else -> SX.recipeStep4
            }
            GameText(hint.str(),fontSize=16.sp,color=T.Ink)
            if(f.count==3) RoundButton((if(f.mode==4) SX.boil else SX.recipeStep5).str(),onClick={vm.sim.christmas.tap(f,0f,0f);vm.changed();onClose()},icon=Icons.Sun,tone=Tones.Sun)
        } else if(f.type==FixtureType.XM_HOUSE_BENCH) GameText(SX.recipeStep6.str(),fontSize=16.sp,color=T.Ink)
        else if(f.type==FixtureType.XM_TOY_BENCH) GameText(SX.recipeStep7.str(),fontSize=16.sp,color=T.Ink)
        else GameText(SX.hint.str(),fontSize=14.sp,color=T.Ink)
        if(f.type==FixtureType.XM_WRAP) Row(horizontalArrangement=Arrangement.spacedBy(16.dp)) {
            for(v in 0..2) Column(Modifier.clickable {f.variant=v;revision++;vm.changed()}.padding(8.dp)) {ChristmasPicture(ThingType.XM_PARCEL,v);if(f.variant==v) IconCanvas(Icons.Check,Modifier.size(22.dp))}
        }
        if(vm.world.inMachine(f).isNotEmpty()) RoundButton(SP.release.str(),onClick={
            vm.world.inMachine(f).forEach { t->t.mode=Mode.FREE;t.holder=-1;t.inside=-1;t.x=f.x+0.35f;t.y=f.top;t.resting=false };f.on=false;f.count=0;vm.changed();onClose()
        },icon=Icons.Bag,tone=Tones.Cream)
    }
}

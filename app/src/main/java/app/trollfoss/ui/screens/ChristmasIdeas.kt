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

private data class ChristmasIdea(val label:Txt,val input:ThingType,val fixture:FixtureType,val output:ThingType,val place:PlaceId)
private val ideas=listOf(
    ChristmasIdea(SX.idea0,ThingType.XM_BAUBLE,FixtureType.XM_TREE,ThingType.XM_STAR,PlaceId.CHRISTMAS_SQUARE),
    ChristmasIdea(SX.idea1,ThingType.XM_PAPER,FixtureType.XM_CRAFT,ThingType.XM_PAPER_HEART,PlaceId.CHRISTMAS_WORKSHOP),
    ChristmasIdea(SX.idea2,ThingType.XM_DOUGH,FixtureType.XM_BAKE,ThingType.XM_GINGER_MAN,PlaceId.CHRISTMAS_HOME),
    ChristmasIdea(SX.idea3,ThingType.XM_GINGER_HEART,FixtureType.XM_HOUSE_BENCH,ThingType.XM_GINGER_HOUSE,PlaceId.CHRISTMAS_WORKSHOP),
    ChristmasIdea(SX.idea4,ThingType.XM_DOUGH,FixtureType.XM_BAKE,ThingType.XM_LUSSEKATT,PlaceId.CHRISTMAS_HOME),
    ChristmasIdea(SX.idea5,ThingType.MILK,FixtureType.XM_BAKE,ThingType.XM_PORRIDGE,PlaceId.CHRISTMAS_HOME),
    ChristmasIdea(SX.idea6,ThingType.XM_COCOA,FixtureType.XM_COCOA,ThingType.XM_COCOA,PlaceId.CHRISTMAS_SQUARE),
    ChristmasIdea(SX.idea7,ThingType.PLANK,FixtureType.XM_TOY_BENCH,ThingType.XM_ROBOT,PlaceId.CHRISTMAS_WORKSHOP),
    ChristmasIdea(SX.idea8,ThingType.XM_TRAIN,FixtureType.XM_WRAP,ThingType.XM_PARCEL,PlaceId.CHRISTMAS_WORKSHOP),
    ChristmasIdea(SX.idea9,ThingType.XM_PARCEL,FixtureType.XM_BELT,ThingType.XM_PARCEL,PlaceId.CHRISTMAS_WORKSHOP),
    ChristmasIdea(SX.idea10,ThingType.XM_WISH,FixtureType.XM_POST,ThingType.XM_WISH,PlaceId.CHRISTMAS_SQUARE),
    ChristmasIdea(SX.idea11,ThingType.XM_BEAR,FixtureType.XM_FIREPLACE,ThingType.XM_STOCKING,PlaceId.CHRISTMAS_HOME),
    ChristmasIdea(SX.idea12,ThingType.CARROT,FixtureType.XM_TROUGH,ThingType.XM_BRUSH,PlaceId.CHRISTMAS_STABLE),
    ChristmasIdea(SX.idea13,ThingType.XM_PARCEL,FixtureType.XM_SLEIGH,ThingType.XM_PARCEL,PlaceId.CHRISTMAS_STABLE),
    ChristmasIdea(SX.idea14,ThingType.SNOWBALL,FixtureType.SNOWMAN,ThingType.XM_HAT,PlaceId.CHRISTMAS_STABLE),
    ChristmasIdea(SX.snowLantern,ThingType.SNOWBALL,FixtureType.XM_CRAFT,ThingType.XM_SNOWFLAKE,PlaceId.CHRISTMAS_WORKSHOP),
    ChristmasIdea(SX.idea15,ThingType.XM_BELL,FixtureType.XM_MUSICBOX,ThingType.XM_BELL,PlaceId.CHRISTMAS_HOME),
    ChristmasIdea(SX.party,ThingType.XM_PLATTER,FixtureType.XM_TABLE,ThingType.XM_PORRIDGE,PlaceId.CHRISTMAS_HOME),
    ChristmasIdea(SX.ramp,ThingType.XM_WISH,FixtureType.XM_POST,ThingType.XM_HAT,PlaceId.CHRISTMAS_SQUARE),
    ChristmasIdea(SX.calendar,ThingType.XM_PARCEL,FixtureType.XM_CALENDAR,ThingType.XM_STAR,PlaceId.CHRISTMAS_SQUARE),
)
@Composable
internal fun ChristmasIdeas(vm:TrollfossViewModel,onClose:()->Unit) {
    for(idea in ideas) Row(Modifier.fillMaxWidth().background(Color.White,RoundedCornerShape(14.dp)).clickable {
        if(idea.label==SX.ramp) { onClose();vm.travelPlayCard(vm.sim.christmas.rampLocation,vm.world.people().firstOrNull {it.name=="Rampenissen"}?.x ?: 1f);return@clickable }
        onClose();vm.travelPlayCard(idea.place,vm.world.fixturesIn(idea.place).firstOrNull {it.type==idea.fixture}?.x ?: 1f)
        vm.sim.christmas.supply(idea.input,place=idea.place,x=vm.world.fixturesIn(idea.place).firstOrNull {it.type==idea.fixture}?.x ?: 1f);vm.changed()
    }.padding(10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        ChristmasPicture(idea.input,side=40.dp)
        GameText("→",fontSize=18.sp,color=T.Ink)
        ChristmasPicture(idea.output,side=40.dp)
        GameText(idea.label.str(),fontSize=15.sp,color=T.Ink,modifier=Modifier.weight(1f))
        IconCanvas(Icons.Map,Modifier.size(26.dp))
    }
}

@Composable
internal fun ChristmasStoryCards(vm:TrollfossViewModel,onClose:()->Unit) {
    val deeds=vm.world.christmas.deeds
    val stories=listOf(
        Triple(SX.bellStory,listOf("bell:post","bell:found","bell:returned"),listOf(PlaceId.CHRISTMAS_SQUARE,PlaceId.CHRISTMAS_WORKSHOP,PlaceId.CHRISTMAS_HOME)),
        Triple(SX.partyStory,listOf("tree","bake","party"),listOf(PlaceId.CHRISTMAS_HOME,PlaceId.CHRISTMAS_HOME,PlaceId.CHRISTMAS_HOME)),
        Triple(SX.starStory,listOf("star:CHRISTMAS_SQUARE","star:MANOR_GROUND","star:HOME"),listOf(PlaceId.CHRISTMAS_SQUARE,PlaceId.MANOR_GROUND,PlaceId.HOME)),
    )
    for((title,steps,places) in stories) {
        val done=steps.count { it in deeds }
        val next=steps.indexOfFirst {it !in deeds}.coerceAtLeast(0)
        Row(Modifier.fillMaxWidth().background(Color(0xFFF3E3BC),RoundedCornerShape(14.dp)).clickable {
            val at=places[next]
            if(title==SX.bellStory && next==1) vm.sim.christmas.supply(ThingType.XM_BELL,place=at,x=7.6f)
            if(title==SX.starStory) {
                if(vm.world.fixturesIn(at).none {it.type in Christmas.trees}) vm.sim.designer.add(at,FixtureType.XM_SMALL_TREE,0,1.5f,at.floor)
                vm.sim.christmas.supply(ThingType.XM_STAR,place=at,x=1.4f)
            }
            onClose();vm.travelPlayCard(at,if(title==SX.bellStory && next==1) 7.6f else 1.6f);vm.changed()
        }.padding(12.dp),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically) {
            ChristmasPicture(if(title==SX.bellStory) ThingType.XM_BELL else if(title==SX.partyStory) ThingType.XM_PORRIDGE else ThingType.XM_STAR)
            Column(Modifier.weight(1f)) { GameText(title.str(),fontSize=16.sp,color=T.Ink);GameText("$done / 3",fontSize=14.sp,color=T.Ink) }
            IconCanvas(if(done==3) Icons.Check else Icons.Map,Modifier.size(30.dp))
        }
    }
}

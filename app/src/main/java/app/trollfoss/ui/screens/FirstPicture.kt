package app.trollfoss.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import app.trollfoss.domain.First
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.ThingType
import app.trollfoss.ui.art.Pen
import app.trollfoss.ui.art.drawBalloon
import app.trollfoss.ui.theme.T

/** What the picture of a discovery shows. */
sealed interface FirstSubject {
    data class Fixture(val type: FixtureType) : FirstSubject
    data class Thing(val type: ThingType) : FirstSubject
    data object Trip : FirstSubject
    data object Troll : FirstSubject
    data object Friends : FirstSubject
}

fun firstSubject(f: First): FirstSubject = when (f) {
    First.TRACTOR -> FirstSubject.Fixture(FixtureType.TRACTOR)
    First.BOAT -> FirstSubject.Fixture(FixtureType.BOAT)
    First.SUBMARINE -> FirstSubject.Fixture(FixtureType.SUBMARINE)
    First.BUMPER_CAR -> FirstSubject.Fixture(FixtureType.BUMPER_CAR)
    First.BUS -> FirstSubject.Fixture(FixtureType.PLAY_BUS)
    First.TANDEM -> FirstSubject.Fixture(FixtureType.PLAY_TANDEM)
    First.TOY_TRAIN -> FirstSubject.Fixture(FixtureType.PLAY_TRAIN)
    First.VACUUM_RIDE -> FirstSubject.Fixture(FixtureType.ROBOT_VACUUM)
    First.BALLOON, First.SKY_ISLAND, First.FIRST_TRIP, First.TRIP_5, First.TRIP_12 -> FirstSubject.Trip
    First.BUBBLE_POP -> FirstSubject.Fixture(FixtureType.PLAY_BUBBLES)
    First.PILLOW_LAUNCH -> FirstSubject.Fixture(FixtureType.PLAY_LAUNCHER)
    First.MARBLES -> FirstSubject.Fixture(FixtureType.PLAY_MARBLES)
    First.COLOUR_SPRAY -> FirstSubject.Fixture(FixtureType.PLAY_COLORS)
    First.POPCORN -> FirstSubject.Fixture(FixtureType.PLAY_POPCORN)
    First.PUMP -> FirstSubject.Fixture(FixtureType.PLAY_PUMP)
    First.PHOTO -> FirstSubject.Fixture(FixtureType.PLAY_CAMERA)
    First.MINI_LIFT -> FirstSubject.Fixture(FixtureType.PLAY_LIFT)
    First.WINDMILL -> FirstSubject.Fixture(FixtureType.PLAY_WINDMILL)
    First.SEESAW -> FirstSubject.Fixture(FixtureType.PLAY_SEESAW)
    First.PUPPETS -> FirstSubject.Fixture(FixtureType.PLAY_PUPPETS)
    First.PICNIC -> FirstSubject.Fixture(FixtureType.PLAY_PICNIC)
    First.CRANE -> FirstSubject.Fixture(FixtureType.PLAY_CRANE)
    First.CONVEYOR -> FirstSubject.Fixture(FixtureType.PLAY_CONVEYOR)
    First.BUILD_TABLE -> FirstSubject.Fixture(FixtureType.PLAY_BUILD)
    First.WATER_CHANNEL -> FirstSubject.Fixture(FixtureType.PLAY_CHANNEL)
    First.WATER_WHEEL -> FirstSubject.Fixture(FixtureType.PLAY_WATER_WHEEL)
    First.RAIN_CLOUD -> FirstSubject.Fixture(FixtureType.PLAY_CLOUD)
    First.MIRROR -> FirstSubject.Fixture(FixtureType.PLAY_MIRROR)
    First.HOVER -> FirstSubject.Fixture(FixtureType.PLAY_HOVER)
    First.PORTAL -> FirstSubject.Fixture(FixtureType.PLAY_PORTAL)
    First.OBSTACLE -> FirstSubject.Fixture(FixtureType.PLAY_JUMP)
    First.APPLE_HARVEST -> FirstSubject.Fixture(FixtureType.PLAY_TREE)
    First.REPAIR_LIGHT -> FirstSubject.Fixture(FixtureType.PLAY_REPAIR)
    First.SECRET_DOOR -> FirstSubject.Fixture(FixtureType.PLAY_DOOR)
    First.STAMP_ART, First.HANG_ART -> FirstSubject.Fixture(FixtureType.PLAY_ART)
    First.CABLE_CAR -> FirstSubject.Fixture(FixtureType.PLAY_CABLE_CAR)
    First.DIVING_BELL -> FirstSubject.Fixture(FixtureType.PLAY_DIVING_BELL)
    First.DIGGER -> FirstSubject.Fixture(FixtureType.PLAY_DIGGER)
    First.TREASURE_TABLE -> FirstSubject.Fixture(FixtureType.PLAY_TREASURE_TABLE)
    First.ECHO_BOX -> FirstSubject.Fixture(FixtureType.PLAY_ECHO_BOX)
    First.DANCE_FLOOR -> FirstSubject.Fixture(FixtureType.PLAY_DANCE_FLOOR)
    First.CONFETTI -> FirstSubject.Fixture(FixtureType.PLAY_CONFETTI)
    First.LIGHT_RIG -> FirstSubject.Fixture(FixtureType.PLAY_LIGHT_RIG)
    First.ROBOT_WORKSHOP -> FirstSubject.Fixture(FixtureType.PLAY_ROBOT_WORKSHOP)
    First.HELPER_ROBOT -> FirstSubject.Fixture(FixtureType.PLAY_HELPER_ROBOT)
    First.ROCKET_KIT -> FirstSubject.Fixture(FixtureType.PLAY_ROCKET_KIT)
    First.REACTION_COURSE -> FirstSubject.Fixture(FixtureType.PLAY_REACTION_COURSE)
    First.HUG, First.HIGH_FIVE, First.HOLD_HANDS, First.PET -> FirstSubject.Friends
    First.LIVING_TEDDY -> FirstSubject.Thing(ThingType.TEDDY)
    First.BAND -> FirstSubject.Thing(ThingType.GUITAR)
    First.PARTY -> FirstSubject.Thing(ThingType.PARTY_HAT)
    First.SNOWMAN -> FirstSubject.Fixture(FixtureType.SNOWMAN)
    First.SANDCASTLE -> FirstSubject.Fixture(FixtureType.SANDCASTLE)
    First.COOK -> FirstSubject.Fixture(FixtureType.STOVE)
    First.EAT -> FirstSubject.Thing(ThingType.APPLE)
    First.BATH -> FirstSubject.Fixture(FixtureType.BATH)
    First.SLEEP -> FirstSubject.Fixture(FixtureType.BED)
    First.READ -> FirstSubject.Thing(ThingType.BOOK)
    First.PHONE -> FirstSubject.Thing(ThingType.PHONE)
    First.BRUSH_TEETH -> FirstSubject.Thing(ThingType.TOOTHBRUSH)
    First.THROW_BALL -> FirstSubject.Thing(ThingType.BALL)
    First.CATCH_BALL -> FirstSubject.Thing(ThingType.BEACH_BALL)
    First.TIDY -> FirstSubject.Fixture(FixtureType.ROBOT_VACUUM)
    First.PAINT_ROOM -> FirstSubject.Fixture(FixtureType.PICTURE)
    First.BUILD_ROOM -> FirstSubject.Thing(ThingType.HAMMER)
    First.TREASURE_BOX -> FirstSubject.Fixture(FixtureType.TREASURE_BOX)
    First.ATTIC_CHEST -> FirstSubject.Thing(ThingType.COIN)
    First.PEEK_TROLL -> FirstSubject.Troll
}

private val SILHOUETTE = ColorFilter.tint(Color(0xFF241A3C).copy(alpha = 0.92f), BlendMode.SrcIn)

/** The picture of a discovery; found ones in full colour, the others as a dark silhouette of the same picture. */
@Composable
fun FirstPicture(first: First, modifier: Modifier = Modifier, found: Boolean) {
    BoxWithConstraints(modifier) {
        val density = LocalDensity.current
        val px = constraints.maxWidth.coerceAtMost(constraints.maxHeight).coerceAtLeast(1)
        val subject = firstSubject(first)
        val image = remember(subject, px) { ThumbCache.get("first:$subject", px, density.density) { drawSubject(subject) } }
        Image(image, contentDescription = null, modifier = Modifier.fillMaxSize(), colorFilter = if (found) null else SILHOUETTE)
    }
}

private fun DrawScope.drawSubject(s: FirstSubject) {
    val box = Rect(Offset.Zero, size)
    when (s) {
        is FirstSubject.Fixture -> drawFixtureThumb(s.type, 0, box)
        is FirstSubject.Thing -> drawThingThumb(s.type, 0, box)
        FirstSubject.Trip -> drawBalloon(Offset(size.width / 2, size.height * 0.42f), size.minDimension * 0.3f, Pen(size.minDimension * 0.025f))
        FirstSubject.Troll -> drawTrollFace(size.minDimension)
        FirstSubject.Friends -> drawTwoFriends(size.minDimension)
    }
}

/** A small green troll peeking: round head, big ears, two eyes and a grin. */
fun DrawScope.drawTrollFace(d: Float) {
    val c = Offset(size.width / 2, size.height * 0.55f); val r = d * 0.3f; val lw = d * 0.035f
    val green = Color(0xFF6AA86A)
    for (side in intArrayOf(-1, 1)) {
        val e = Offset(c.x + side * r * 0.95f, c.y - r * 0.35f)
        drawOval(green, Offset(e.x - r * 0.28f, e.y - r * 0.42f), Size(r * 0.56f, r * 0.84f))
        drawOval(T.Ink, Offset(e.x - r * 0.28f, e.y - r * 0.42f), Size(r * 0.56f, r * 0.84f), style = Stroke(lw))
    }
    drawCircle(green, r, c); drawCircle(T.Ink, r, c, style = Stroke(lw))
    for (side in intArrayOf(-1, 1)) drawCircle(T.Ink, r * 0.12f, Offset(c.x + side * r * 0.35f, c.y - r * 0.12f))
    drawArc(T.Ink, 20f, 140f, false, Offset(c.x - r * 0.45f, c.y - r * 0.05f), Size(r * 0.9f, r * 0.6f), style = Stroke(lw))
}

/** Two little friends side by side, hand in hand. */
fun DrawScope.drawTwoFriends(d: Float) {
    val lw = d * 0.035f
    val colours = listOf(Color(0xFF4F8FD9), Color(0xFFD9654B))
    for ((i, side) in intArrayOf(-1, 1).withIndex()) {
        val x = size.width / 2 + side * d * 0.18f
        val head = Offset(x, size.height * 0.36f)
        drawRoundRect(colours[i], Offset(x - d * 0.12f, size.height * 0.5f), Size(d * 0.24f, d * 0.3f), androidx.compose.ui.geometry.CornerRadius(d * 0.08f))
        drawRoundRect(T.Ink, Offset(x - d * 0.12f, size.height * 0.5f), Size(d * 0.24f, d * 0.3f), androidx.compose.ui.geometry.CornerRadius(d * 0.08f), style = Stroke(lw))
        drawCircle(Color(0xFFF1C6A8), d * 0.12f, head); drawCircle(T.Ink, d * 0.12f, head, style = Stroke(lw))
    }
    drawLine(T.Ink, Offset(size.width / 2 - d * 0.08f, size.height * 0.62f), Offset(size.width / 2 + d * 0.08f, size.height * 0.62f), strokeWidth = lw * 1.6f)
}

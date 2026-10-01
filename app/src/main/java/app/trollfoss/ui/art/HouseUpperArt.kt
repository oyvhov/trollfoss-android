package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.RoomStyle
import app.trollfoss.domain.Thing
import app.trollfoss.domain.ThingType

/*
 * Storhuset, upper floor (Andre høgda): the art. Entry points for the floor (docs/HUSET.md); the drawing itself is split
 * over the files with the same prefix:
 *   HouseUpperRoomsArt.kt     the six rooms (walls, floors, dividers, the balcony view)
 *   HouseUpperPassageArt.kt   stairs, lift, slides, pole, laundry chute, dumbwaiter
 *   HouseUpperKidsArt.kt      landing and children's room
 *   HouseUpperPlayArt.kt      playroom
 *   HouseUpperBathArt.kt      bathroom and bedroom
 *   HouseUpperBalconyArt.kt   balcony
 *   HouseUpperThingArt.kt     things
 * Origin of a fixture is the bottom centre of its front face; depth recedes up and to the right. Anything that moves by
 * itself reads the fixture's `angle`, `timer` and `anim` (the rules keep it drawn live) or the clock `pen.t`.
 */

/** The place's back layer: walls, floor, windows, far scenery. */
internal fun DrawScope.upperBack(st: Stage, pen: Pen, styles: List<RoomStyle>) = upperRooms(st, pen, styles)

/** What lies in front of everything. */
internal fun DrawScope.upperFront(st: Stage, pen: Pen) = upperRoomsFront(st, pen)

/** The back layer of a fixture of this floor; true when drawn (the shared passage types included). */
internal fun DrawScope.drawUpperFixtureBack(f: Fixture, u: Float, pen: Pen, contents: List<Thing>): Boolean {
    when (f.type) {
        FixtureType.STAIRCASE -> if (f.variant == 1) upStairsUp(f, u, pen) else upStairsDown(f, u, pen)
        FixtureType.LIFT -> upLift(f, u, pen)
        FixtureType.SLIDE -> if (f.variant == 1) upSlideBalcony(f, u, pen) else upSlideIndoor(f, u, pen)
        FixtureType.FIRE_POLE -> upPole(f, u, pen)
        FixtureType.HATCH -> upHatch(f, u, pen)
        FixtureType.DUMBWAITER -> upDumbwaiter(f, u, pen)
        FixtureType.BED -> if (f.variant >= 1) upCanopyBed(f, u, pen) else return false
        FixtureType.UP_PORTRAIT -> upPortrait(f, u, pen)
        FixtureType.UP_WINDOW -> upWindow(f, u, pen)
        FixtureType.UP_WINDOW_SEAT -> upWindowSeat(f, u, pen)
        FixtureType.UP_TOY_TRAIN -> upTrain(f, u, pen)
        FixtureType.UP_BLOCKS -> upBlocks(f, u, pen)
        FixtureType.UP_DOLLHOUSE -> upDollhouse(f, u, pen)
        FixtureType.UP_PUPPET_THEATER -> upPuppets(f, u, pen)
        FixtureType.UP_NIGHT_LAMP -> upNightLamp(f, u, pen)
        FixtureType.UP_POSTER -> upPoster(f, u, pen)
        FixtureType.UP_MOBILE -> upMobile(f, u, pen)
        FixtureType.UP_FORT -> upFort(f, u, pen)
        FixtureType.UP_BALL_PIT -> upBallPit(f, u, pen)
        FixtureType.UP_CLIMBING_WALL -> upClimbingWall(f, u, pen)
        FixtureType.UP_TRAMPOLINE -> upTrampoline(f, u, pen)
        FixtureType.UP_EASEL -> upEasel(f, u, pen)
        FixtureType.UP_KARAOKE -> upKaraoke(f, u, pen)
        FixtureType.UP_SHOWER -> upShower(f, u, pen)
        FixtureType.UP_BATH_MIRROR -> upBathMirror(f, u, pen)
        FixtureType.UP_TOWELS -> upTowels(f, u, pen)
        FixtureType.UP_WARDROBE -> upWardrobe(f, u, pen)
        FixtureType.UP_VANITY -> upVanity(f, u, pen)
        FixtureType.UP_JEWEL_BOX -> upJewelBox(f, u, pen)
        FixtureType.UP_ROCKING_CHAIR -> upRockingChair(f, u, pen)
        FixtureType.UP_BIRD_FEEDER -> upBirdFeeder(f, u, pen)
        FixtureType.UP_HANGING_CHAIR -> upHangingChair(f, u, pen)
        FixtureType.UP_RAILING -> upRailing(f, u, pen)
        else -> return false
    }
    return true
}

/** The front layer of a fixture of this floor (what stands in front of whoever sits in it). */
internal fun DrawScope.drawUpperFixtureFront(f: Fixture, u: Float, pen: Pen): Boolean {
    when (f.type) {
        FixtureType.BED -> if (f.variant >= 1) upCanopyBedFront(f, u, pen) else return false
        FixtureType.UP_BALL_PIT -> upBallPitFront(f, u, pen)
        FixtureType.UP_SHOWER -> upShowerFront(f, u, pen)
        FixtureType.UP_TOY_TRAIN -> upTrainFront(f, u, pen)
        else -> return false
    }
    return true
}

/** A thing that belongs to this floor; true when drawn. */
internal fun DrawScope.drawUpperThing(type: ThingType, variant: Int, used: Int, w: Float, h: Float, pen: Pen, cook: Float): Boolean {
    when (type) {
        ThingType.UP_BLOCK -> thUpBlock(variant, w, h, pen)
        ThingType.UP_SOCK -> thUpSock(variant, w, h, pen)
        ThingType.UP_PAINTBRUSH -> thUpBrush(variant, w, h, pen)
        ThingType.UP_SLIPPER -> thUpSlipper(variant, w, h, pen)
        ThingType.UP_PAPER_PLANE -> thUpPlane(variant, w, h, pen)
        else -> return false
    }
    return true
}

// ---------------------------------------------------------------------------------------------- the floor's colours

/** Everyday and friendly: warm wood, pastel toys, brass. Shared by every file of the floor. */
internal object UpC {
    val oak = Color(0xFFE2BE8A)
    val oakDark = Color(0xFFC49A62)
    val wood = Color(0xFFC98A55)
    val walnut = Color(0xFF6E4630)
    val walnutDark = Color(0xFF4B2F20)
    val cream = Color(0xFFF6EEDC)
    val paper = Color(0xFFFFFBF2)
    val brass = Color(0xFFE0B04A)
    val brassDark = Color(0xFFB88A2F)
    val steel = Color(0xFFBAC4D4)
    val steelDark = Color(0xFF8A93AA)
    val red = Color(0xFFD2443A)
    val falun = Color(0xFFB8342B)
    val yellow = Color(0xFFFFC83D)
    val orange = Color(0xFFFF9A3D)
    val green = Color(0xFF3BC46B)
    val leaf = Color(0xFF5DB35B)
    val teal = Color(0xFF3FA79A)
    val mint = Color(0xFF8FD9C0)
    val sky = Color(0xFF5AA9E6)
    val skyLight = Color(0xFFBFE3FA)
    val blue = Color(0xFF3D7FD6)
    val lilac = Color(0xFFB9A2F0)
    val purple = Color(0xFF8B5CF6)
    val pink = Color(0xFFF08CB8)
    val blush = Color(0xFFF7C6D4)
    val skin = Color(0xFFF2C29B)
    val glow = Color(0xFFFFD27A)
    val dark = Color(0xFF2B2140)
}

/** The soft shadow a piece of furniture throws on the floor behind and beside it. */
internal fun DrawScope.upShadow(u: Float, w: Float, d: Float = 0.13f, alpha: Float = 1f) {
    val dx = FX_DX * d * u
    val dy = FX_DY * d * u
    drawOval(Ink.shadow.copy(alpha = Ink.shadow.alpha * alpha), Offset(-w * u / 2f + 0.01f * u, dy - 0.012f * u), Size(w * u + dx, -dy + 0.03f * u))
}

/** The ramp of an effect that was set off by a tap: 1 at the start, down to 0 (the engine counts [Fixture.anim] down). */
internal fun upBeat(f: Fixture): Float = f.anim.coerceIn(0f, 1f)

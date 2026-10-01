package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import app.trollfoss.domain.CellarFloor
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.RoomStyle
import app.trollfoss.domain.Thing
import app.trollfoss.domain.ThingType
import kotlin.math.sin

/*
 * Storhuset, cellar (Kjellaren): the art. The background (five rooms with their own looks) is in
 * HouseCellarRoomsArt.kt; the furniture is drawn room by room in HouseCellar*Art.kt and routed from here.
 * The same fixture type can look different here than elsewhere: the cellar's own staircase, sauna and
 * furnace, and the mine door that stands in Trollhola (a SECRET_DOOR of the lab).
 */

/** The place's back layer: walls, floors, pillars, pipes and the pool basin. */
internal fun DrawScope.cellarBack(st: Stage, pen: Pen, styles: List<RoomStyle>) = cellarBackground(st, pen, styles)

/** What lies in front of everything: the water of the pool, which swimmers and floating things sit inside. */
internal fun DrawScope.cellarFront(st: Stage, pen: Pen) {
    val x1 = CellarFloor.POOL_X1
    val x2 = CellarFloor.POOL_X2
    if (!st.sees(x1 - 0.05f, x2 + 0.05f)) return
    val u = st.u
    drawWaterFront(st, pen, x1, x2, CellarFloor.POOL_LINE, CellarFloor.POOL_BED, Color(0xFF7BE8F0), Color(0xFF1C74B8))
    // Bubbles rise from the bottom and light flickers across the surface.
    val t = pen.t
    for (k in 0 until 6) {
        val ph = wrap(t * (0.12f + 0.05f * hash01(k, 931)) + hash01(k, 932), 1f)
        val bx = x1 + 0.08f + (x2 - x1 - 0.16f) * hash01(k, 933) + sin(t * 1.3f + k) * 0.01f
        val by = CellarFloor.POOL_BED - ph * (CellarFloor.POOL_BED - CellarFloor.POOL_LINE)
        drawCircle(Color.White, (0.004f + 0.004f * hash01(k, 934)) * u, st.o(bx, by), alpha = 0.7f * (1f - ph * 0.5f))
    }
}

/** The back layer of a fixture of this floor; true when drawn (the shared passage types included). */
internal fun DrawScope.drawCellarFixtureBack(f: Fixture, u: Float, pen: Pen, contents: List<Thing>): Boolean {
    val here = f.place == PlaceId.MANOR_CELLAR
    when (f.type) {
        FixtureType.SECRET_DOOR -> when (f.place) {
            PlaceId.MANOR_CELLAR -> ceTunnelDoor(f, u, pen)
            PlaceId.LAB -> ceLabDoor(f, u, pen)
            else -> return false
        }
        FixtureType.STAIRCASE -> if (here) ceStairs(f, u, pen) else return false

        FixtureType.CE_WASHER -> ceWasher(f, u, pen)
        FixtureType.CE_DRYER -> ceDryer(f, u, pen)
        FixtureType.CE_SOCK_MONSTER -> ceSockMonster(f, u, pen)
        FixtureType.CE_BASKET -> ceBasket(f, u, pen)
        FixtureType.CE_CLOTHESLINE -> ceClothesline(f, u, pen)
        FixtureType.CE_IRON_BOARD -> ceIronBoard(f, u, pen)
        FixtureType.CE_CHUTE -> ceChute(f, u, pen)

        FixtureType.CE_CHARGER -> ceCharger(f, u, pen)
        FixtureType.CE_BULB -> ceBulb(f, u, pen)
        FixtureType.CE_CRATES -> ceCrates(f, u, pen)
        FixtureType.CE_MOUSE_HOLE -> ceMouseHole(f, u, pen)
        FixtureType.CE_SAW -> ceSaw(f, u, pen)

        FixtureType.CE_BOILER -> ceBoiler(f, u, pen)
        FixtureType.CE_VALVE -> ceValve(f, u, pen)

        FixtureType.SAUNA -> if (here) ceSauna(f, u, pen) else return false
        FixtureType.CE_SAUNA_BUCKET -> ceSaunaBucket(f, u, pen)
        FixtureType.CE_SHOWER -> ceShower(f, u, pen)
        FixtureType.CE_LIFEBUOY -> ceLifebuoy(f, u, pen)
        FixtureType.CE_DIVING_BOARD -> ceDivingBoard(f, u, pen)
        FixtureType.CE_POOL_SLIDE -> ceSlide(f, u, pen)
        FixtureType.CE_POOL_FLOAT -> ceDuck(f, u, pen)
        // The water is only there to be tapped: the pool itself is part of the background.
        FixtureType.CE_POOL_WATER -> Unit

        FixtureType.CE_DANCE_FLOOR -> ceDanceFloor(f, u, pen)
        FixtureType.CE_JUKEBOX -> ceJukebox(f, u, pen)
        FixtureType.CE_KARAOKE -> ceKaraoke(f, u, pen)
        FixtureType.CE_SNACK_BAR -> ceSnackBar(f, u, pen)
        FixtureType.CE_BAR_STOOL -> ceBarStool(f, u, pen)
        FixtureType.CE_NEON -> ceNeon(f, u, pen)
        FixtureType.CE_CONFETTI -> ceConfetti(f, u, pen)

        FixtureType.CE_MINE_CART -> ceMineCart(f, u, pen)
        else -> return false
    }
    return true
}

/** The front layer of a fixture of this floor (what stands in front of whoever sits in it). */
internal fun DrawScope.drawCellarFixtureFront(f: Fixture, u: Float, pen: Pen): Boolean {
    when (f.type) {
        FixtureType.CE_BASKET -> ceBasketFront(f, u, pen)
        FixtureType.CE_POOL_FLOAT -> ceDuckFront(f, u, pen)
        FixtureType.CE_MINE_CART -> ceMineCartFront(f, u, pen)
        else -> return false
    }
    return true
}

/** A thing that belongs to this floor; true when drawn. */
internal fun DrawScope.drawCellarThing(type: ThingType, variant: Int, used: Int, w: Float, h: Float, pen: Pen, cook: Float): Boolean {
    when (type) {
        ThingType.CE_SOCK -> translate(-0.03f * w, 0f) { ceSock(w * 0.92f, h, variant, pen) }
        else -> return false
    }
    return true
}

/** A small pointer used while the art is being written: keeps [Offset] imported for the others. */
@Suppress("unused")
private val ceOrigin = Offset.Zero

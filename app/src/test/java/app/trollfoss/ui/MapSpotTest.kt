package app.trollfoss.ui

import app.trollfoss.domain.PlaceId
import app.trollfoss.ui.art.MANOR_SCALE
import app.trollfoss.ui.art.manorGate
import app.trollfoss.ui.art.manorUnit
import app.trollfoss.ui.art.mapLabel
import app.trollfoss.ui.art.mapSpot
import app.trollfoss.ui.art.MAP_WIDTH_FACTOR
import app.trollfoss.ui.screens.mapControls
import app.trollfoss.ui.screens.mapMarkerBounds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max

/**
 * The map's places must keep their words (and the buttons in the corners) clear of each other on every
 * landscape screen: the phone at 2400 × 1080 px (2.625 px per dp) and the tablet at 1920 × 1200 px (1.5).
 * The word ends at the marker's lower edge; floating controls can move that edge above the coast.
 */
class MapSpotTest {
    private class Screen(val name: String, val wPx: Int, val hPx: Int, val density: Float) {
        val w get() = wPx / density
        val h get() = hPx / density
        val controls get() = mapControls(h)
        // The gift card is 58 dp at the default text size; the tablet buttons are taller.
        val controlsHeight get() = max(controls.button, 58f) + controls.edge * 2
    }

    private class Box(val l: Float, val t: Float, val r: Float, val b: Float) {
        fun overlaps(o: Box, gap: Float = 0f) = l < o.r + gap && r > o.l - gap && t < o.b + gap && b > o.t - gap
    }

    private val screens = listOf(Screen("phone", 2400, 1080, 2.625f), Screen("tablet", 1920, 1200, 1.5f), Screen("wide phone", 2400, 1080, 2.2f))
    private val places = PlaceId.entries.filter { it.onMap }

    private fun word(place: PlaceId, s: Screen): Box {
        val spot = mapSpot(place)
        val label = mapLabel(place)
        val chars = max(label.nn.length, label.nb.length)
        val half = (chars * 10.5f + 6f) / 2f
        val cx = spot.x * s.w * MAP_WIDTH_FACTOR
        val bottom = mapMarkerBounds(place, s.w * MAP_WIDTH_FACTOR, s.h, s.controlsHeight).bottom
        return Box(cx - half, bottom - 34f, cx + half, bottom)
    }

    private fun corners(s: Screen) = listOf(
        Box(16f, 16f, 76f, 76f), // close
        Box(s.w - 68f, 16f, s.w - 16f, 68f), // parents
        Box(s.controls.edge, s.h - s.controls.edge - s.controls.button,
            s.controls.edge + s.controls.groupWidth, s.h - s.controls.edge), // all three buttons
        Box(s.w - s.controls.edge - 240f, s.h - s.controls.edge - 58f,
            s.w - s.controls.edge, s.h - s.controls.edge), // progress and next gift
    )

    @Test
    fun storhusetIsOnTheMapWithItsOwnName() {
        assertTrue(PlaceId.MANOR_GROUND in places)
        assertEquals(1, places.count { it.manor })
        assertEquals("Storhuset", mapLabel(PlaceId.MANOR_GROUND).nn)
        assertEquals("Storhuset", mapLabel(PlaceId.MANOR_GROUND).nb)
    }

    @Test
    fun wordsDoNotCollide() {
        for (s in screens) {
            for (i in places.indices) for (j in i + 1 until places.size) {
                val a = word(places[i], s)
                val b = word(places[j], s)
                assertFalse("${places[i]} and ${places[j]} collide on the ${s.name}", a.overlaps(b, 2f))
            }
        }
    }

    @Test
    fun wordsAndSpotsStayClearOfTheCornerButtons() {
        for (s in screens) for (p in places) {
            val w = word(p, s)
            val pan = (mapSpot(p).x * s.w * MAP_WIDTH_FACTOR - s.w / 2f).coerceIn(0f, s.w * (MAP_WIDTH_FACTOR - 1f))
            val visible = Box(w.l - pan, w.t, w.r - pan, w.b)
            for (c in corners(s)) assertFalse("$p touches a corner button on the ${s.name}", visible.overlaps(c, 4f))
            val spot = mapSpot(p)
            assertTrue("$p is inside the map", spot.x in 0.04f..0.96f && spot.y in 0.05f..0.9f)
        }
    }

    @Test
    fun everyFloorUsesItsHouseLandmarkWhenLeavingForTheMap() {
        for (p in PlaceId.entries) {
            assertTrue("$p must have a map landmark", p.mapPlace.onMap)
            assertEquals(mapSpot(p.mapPlace), mapSpot(p))
        }
        assertEquals(PlaceId.MINE_YARD, PlaceId.MINE_UPPER.mapPlace)
        assertEquals(PlaceId.MANOR_GROUND, PlaceId.MANOR_CELLAR.mapPlace)
    }

    @Test
    fun spotsAreFarEnoughApartForTheirTouchAreas() {
        // Two places may never share a spot, and the big house keeps clear of the neighbours that carry buttons too.
        for (i in places.indices) for (j in i + 1 until places.size) {
            val a = mapSpot(places[i])
            val b = mapSpot(places[j])
            val dx = kotlin.math.abs(a.x - b.x)
            val dy = kotlin.math.abs(a.y - b.y)
            assertTrue("${places[i]} and ${places[j]} are on top of each other", dx > 0.05f || dy > 0.12f)
        }
    }

    @Test
    fun coastalTargetsStayAboveControlsAndDoNotStealValleyLabelTaps() {
        for (s in screens) for (p in places.filter { mapSpot(it).y > 0.75f }) {
            val target = mapMarkerBounds(p, s.w * MAP_WIDTH_FACTOR, s.h, s.controlsHeight)
            assertEquals("Coastal target keeps a usable touch height", 48f, target.height, 0.01f)
            assertTrue("$p clears both bottom control groups on ${s.name}", target.bottom <= s.h - s.controlsHeight)
            val hit = Box(target.left, target.top, target.right, target.bottom)
            for (other in places.filter { mapSpot(it).y in 0.45f..0.6f }) {
                assertFalse("$p steals $other label taps on ${s.name}", hit.overlaps(word(other, s)))
            }
        }
    }

    @Test
    fun theGateOfStorhusetIsInsideItsLandAndLeftOfTheHouse() {
        for (s in screens) {
            val w = s.wPx.toFloat()
            val h = s.hPx.toFloat()
            val gate = manorGate(w, h)
            val spot = mapSpot(PlaceId.MANOR_GROUND)
            val u = manorUnit(w, h)
            assertTrue(gate.x < spot.x * w)
            assertTrue(gate.x > spot.x * w - 2.5f * u)
            assertTrue(gate.y > (spot.y + 0.045f) * h)
            assertTrue(u < 0.15f * h * MANOR_SCALE * 1.05f)
        }
    }
}

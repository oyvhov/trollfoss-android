package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.Thing
import app.trollfoss.domain.ThingType
import kotlin.math.cos
import kotlin.math.sin

/*
 * Storhuset, garden (Hagen): the art. The background (sky, hills, the back of the house, the lawn, the pond)
 * is in HouseGardenSceneArt.kt, HouseGardenHouseArt.kt and HouseGardenPondArt.kt; the furniture is in
 * HouseGardenPlantArt.kt (greenhouse, beds, flowers, compost, barrel, bird house), HouseGardenPlayArt.kt
 * (treehouse, ladder, swing, zip line, trampoline, sandbox, hammock, bridge, frogs) and HouseGardenSmallArt.kt
 * (gnomes, mower, sprinkler, grill, table, shed, gate, snowman and the door). This file routes, and draws the
 * things (the vegetables and the sand cake).
 */

/** The back layer of a fixture of this floor; true when drawn (the shared passage types included). */
internal fun DrawScope.drawGardenFixtureBack(f: Fixture, u: Float, pen: Pen, contents: List<Thing>): Boolean {
    when (f.type) {
        FixtureType.DOOR -> gaDoor(f, u, pen)
        FixtureType.GA_GREENHOUSE -> gaGreenhouse(f, u, pen)
        FixtureType.GA_PLANTER -> gaPlanter(f, u, pen)
        FixtureType.GA_FLOWER_BED -> gaFlowerBed(f, u, pen)
        FixtureType.GA_PINWHEEL -> gaPinwheel(f, u, pen)
        FixtureType.GA_COMPOST -> gaCompost(f, u, pen)
        FixtureType.GA_BARREL -> gaBarrel(f, u, pen)
        FixtureType.GA_BIRDHOUSE -> gaBirdhouse(f, u, pen)
        FixtureType.GA_TREEHOUSE -> gaTreehouse(f, u, pen)
        FixtureType.GA_LADDER -> gaLadder(f, u, pen)
        FixtureType.GA_SWING -> gaSwing(f, u, pen)
        FixtureType.GA_ZIP -> gaZip(f, u, pen)
        FixtureType.GA_ZIP_POLE -> gaZipPole(f, u, pen)
        FixtureType.GA_TRAMPOLINE -> gaTrampoline(f, u, pen)
        FixtureType.GA_SANDBOX -> gaSandbox(f, u, pen)
        FixtureType.GA_HAMMOCK -> gaHammock(f, u, pen)
        FixtureType.GA_BRIDGE -> gaBridge(f, u, pen)
        FixtureType.GA_FROG -> gaFrog(f, u, pen)
        FixtureType.GA_GNOME -> gaGnome(f, u, pen)
        FixtureType.GA_MOWER -> gaMower(f, u, pen)
        FixtureType.GA_SPRINKLER -> gaSprinkler(f, u, pen)
        FixtureType.GA_GRILL -> gaGrill(f, u, pen)
        FixtureType.GA_PATIO -> gaPatio(f, u, pen)
        FixtureType.GA_SHED -> gaShed(f, u, pen)
        FixtureType.GA_GATE -> gaGate(f, u, pen)
        FixtureType.GA_SNOWMAN -> gaSnowman(f, u, pen)
        else -> return false
    }
    return true
}

/** The front layer of a fixture of this floor (what stands in front of whoever sits in it). */
internal fun DrawScope.drawGardenFixtureFront(f: Fixture, u: Float, pen: Pen): Boolean {
    when (f.type) {
        FixtureType.GA_PLANTER -> gaPlanterFront(f, u, pen)
        FixtureType.GA_SWING -> gaSwingFront(f, u, pen)
        FixtureType.GA_HAMMOCK -> gaHammockFront(f, u, pen)
        FixtureType.DOOR, FixtureType.GA_GREENHOUSE, FixtureType.GA_FLOWER_BED, FixtureType.GA_PINWHEEL, FixtureType.GA_COMPOST,
        FixtureType.GA_BARREL, FixtureType.GA_BIRDHOUSE, FixtureType.GA_TREEHOUSE, FixtureType.GA_LADDER, FixtureType.GA_ZIP,
        FixtureType.GA_ZIP_POLE, FixtureType.GA_TRAMPOLINE, FixtureType.GA_SANDBOX, FixtureType.GA_BRIDGE, FixtureType.GA_FROG,
        FixtureType.GA_GNOME, FixtureType.GA_MOWER, FixtureType.GA_SPRINKLER, FixtureType.GA_GRILL, FixtureType.GA_PATIO,
        FixtureType.GA_SHED, FixtureType.GA_GATE, FixtureType.GA_SNOWMAN -> Unit
        else -> return false
    }
    return true
}

/** A thing that belongs to this floor; true when drawn. Origin bottom centre, the box is [w] by [h] pixels. */
internal fun DrawScope.drawGardenThing(type: ThingType, variant: Int, used: Int, w: Float, h: Float, pen: Pen, cook: Float): Boolean {
    when (type) {
        ThingType.GA_VEGGIE -> gaVeggie(variant, used, w, h, pen)
        ThingType.GA_SAND_CAKE -> gaSandCake(used, w, h, pen)
        else -> return false
    }
    return true
}

/** Bites taken out of a round thing: circles cut away on its right side. */
private fun DrawScope.gaBite(used: Int, cx: Float, cy: Float, r: Float) {
    for (k in 0 until used) {
        val a = -0.5f + k * 0.55f
        drawCircle(Color.Transparent, r * 0.3f, Offset(cx + cos(a) * r * 0.95f, cy + sin(a) * r * 0.8f), blendMode = BlendMode.Clear)
    }
}

/** A vegetable from the greenhouse: 0 a pumpkin, 1 a tomato, 2 a pea pod. */
private fun DrawScope.gaVeggie(variant: Int, used: Int, w: Float, h: Float, pen: Pen) {
    val cx = 0f
    val cy = -h / 2f
    when (variant % 3) {
        0 -> {
            val body = Rect(-w * 0.46f, -h * 0.82f, w * 0.46f, 0f)
            inkedOval(body, GaK.pumpkin, pen)
            for (k in -1..1) {
                val rib = Path().apply {
                    moveTo(cx + k * w * 0.16f, body.top + h * 0.06f)
                    quadraticTo(cx + k * w * 0.3f, cy, cx + k * w * 0.18f, body.bottom - h * 0.04f)
                }
                drawPath(rib, GaK.pumpkinDark, alpha = 0.7f, style = Stroke(pen.lw * 0.7f))
            }
            inkedRound(Rect(cx - w * 0.07f, -h, cx + w * 0.07f, -h * 0.76f), w * 0.03f, GaK.leafDark, pen, shade = false)
            drawPath(fxLeaf(cx + w * 0.07f, -h * 0.84f, cx + w * 0.3f, -h * 0.98f, 0.5f), GaK.leaf)
            drawPath(fxLeaf(cx + w * 0.07f, -h * 0.84f, cx + w * 0.3f, -h * 0.98f, 0.5f), Ink.line, style = pen.thin)
            shine(Offset(-w * 0.2f, -h * 0.58f), w * 0.18f, h * 0.1f, 0.6f)
            gaBite(used, cx, cy, w * 0.46f)
        }
        1 -> {
            val body = Rect(-w * 0.44f, -h * 0.84f, w * 0.44f, 0f)
            inkedOval(body, GaK.tomato, pen)
            for (k in 0 until 5) {
                val a = -2.4f + k * 0.9f
                drawPath(fxLeaf(cx, -h * 0.82f, cx + sin(a) * w * 0.28f, -h * 0.82f - cos(a) * h * 0.16f, 0.5f), GaK.leaf)
                drawPath(fxLeaf(cx, -h * 0.82f, cx + sin(a) * w * 0.28f, -h * 0.82f - cos(a) * h * 0.16f, 0.5f), Ink.line, style = pen.thin)
            }
            drawLine(GaK.leafDark, Offset(cx, -h * 0.84f), Offset(cx + w * 0.04f, -h * 0.98f), strokeWidth = pen.lw * 1.4f)
            shine(Offset(-w * 0.2f, -h * 0.58f), w * 0.16f, h * 0.1f, 0.8f)
            gaBite(used, cx, cy, w * 0.44f)
        }
        else -> {
            val pod = Path().apply {
                moveTo(-w * 0.48f, -h * 0.4f)
                cubicTo(-w * 0.3f, h * 0.02f, w * 0.3f, h * 0.02f, w * 0.5f, -h * 0.55f)
                cubicTo(w * 0.3f, -h * 0.95f, -w * 0.3f, -h * 0.95f, -w * 0.48f, -h * 0.4f)
                close()
            }
            inked(pod, GaK.peaPod, pen)
            for (k in 0 until 4) {
                val px = -w * 0.3f + k * w * 0.2f
                inkedCircle(Offset(px, -h * (0.45f + 0.08f * sin(k / 3f * 3.1416f))), h * 0.16f, GaK.pea, pen, shade = true)
            }
            drawPath(
                Path().apply {
                    moveTo(w * 0.5f, -h * 0.55f)
                    cubicTo(w * 0.62f, -h * 0.8f, w * 0.7f, -h * 0.9f, w * 0.58f, -h * 0.95f)
                },
                GaK.leafDark, style = Stroke(pen.lw * 1.2f),
            )
        }
    }
}

/** A cake of sand: a bucket's worth turned over, with ridges and a small daisy on top. */
private fun DrawScope.gaSandCake(used: Int, w: Float, h: Float, pen: Pen) {
    val body = Path().apply {
        moveTo(-w * 0.42f, 0f)
        lineTo(-w * 0.3f, -h * 0.72f)
        quadraticTo(0f, -h * 0.82f, w * 0.3f, -h * 0.72f)
        lineTo(w * 0.42f, 0f)
        quadraticTo(0f, h * 0.1f, -w * 0.42f, 0f)
        close()
    }
    inked(body, GaK.sand, pen)
    for (k in 1..3) {
        val y = -h * 0.2f * k
        drawLine(GaK.sand.darken(0.25f), Offset(-w * (0.4f - 0.04f * k), y), Offset(w * (0.4f - 0.04f * k), y), strokeWidth = pen.lw * 0.7f, alpha = 0.7f)
    }
    drawOval(GaK.sand.lighten(0.1f), Offset(-w * 0.3f, -h * 0.82f), Size(w * 0.6f, h * 0.2f))
    drawOval(Ink.line, Offset(-w * 0.3f, -h * 0.82f), Size(w * 0.6f, h * 0.2f), style = pen.thin)
    val c = Offset(w * 0.02f, -h * 0.9f)
    for (p in 0 until 6) {
        val a = p * 1.047f
        drawCircle(Color.White, h * 0.07f, Offset(c.x + cos(a) * h * 0.09f, c.y + sin(a) * h * 0.06f))
    }
    drawCircle(Color(0xFFFFC83D), h * 0.06f, c)
    gaBite(used, 0f, -h * 0.4f, w * 0.4f)
}

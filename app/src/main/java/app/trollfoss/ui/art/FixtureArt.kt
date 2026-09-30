package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.Thing
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

/**
 * Draws the back layer of a fixture — its body, and its inside when it is open — with the origin at
 * the bottom centre of its front face. [u] is pixels per scene unit, so the front face is spec.w × spec.h
 * units; depth recedes up and to the right in oblique 3D ([Oblique]). [contents] are the things a machine
 * has taken (blender, cauldron, workbench), for machines that show them.
 *
 * The art for each type lives in FixtureArtHome.kt, FixtureArtLiving.kt, FixtureArtShop.kt,
 * FixtureArtOutdoor.kt and FixtureArtFarm.kt; this file holds the entry points and the 3D drawing kit.
 * The engine squashes the back layer on a tap, so the art does not bounce by itself.
 */
fun DrawScope.drawFixtureBack(f: Fixture, u: Float, pen: Pen, contents: List<Thing> = emptyList()) {
    // The newer places draw their own furniture, shadows included.
    if (drawRoomsBack(f, u, pen, contents) || drawRidesBack(f, u, pen, contents) || drawDecorBack(f, u, pen, contents)) return
    val spec = f.spec
    if (!spec.wall && f.type !in FX_NO_SHADOW) fxFootShadow(u, spec.w, fxDepthOf(f.type))
    fxBobbed(f, u) {
        when (f.type) {
            FixtureType.BED -> fxBed(f, u, pen)
            FixtureType.SOFA -> fxSofa(f, u, pen)
            FixtureType.CHAIR -> fxChair(f, u, pen)
            FixtureType.STOOL -> fxStool(f, u, pen)
            FixtureType.TABLE -> fxTable(f, u, pen)
            FixtureType.ROUND_TABLE -> fxRoundTable(f, u, pen)
            FixtureType.COUNTER -> fxCounter(f, u, pen)
            FixtureType.SHELF -> fxShelf(f, u, pen)
            FixtureType.BOOKCASE -> fxBookcase(f, u, pen)
            FixtureType.FRIDGE -> fxFridge(f, u, pen)
            FixtureType.WARDROBE -> fxWardrobe(f, u, pen)
            FixtureType.CHEST -> fxChest(f, u, pen)
            FixtureType.STOVE -> fxStove(f, u, pen)
            FixtureType.SINK -> fxSink(f, u, pen)
            FixtureType.BATH -> fxBath(f, u, pen)
            FixtureType.TOILET -> fxToilet(f, u, pen)
            FixtureType.MIRROR -> fxMirror(f, u, pen)
            FixtureType.LAMP -> fxLamp(f, u, pen)
            FixtureType.TV -> fxTv(f, u, pen)
            FixtureType.RADIO -> fxRadio(f, u, pen)
            FixtureType.PIANO -> fxPiano(f, u, pen)
            FixtureType.WINDOW -> fxWindow(f, u, pen)
            FixtureType.MAILBOX -> fxMailbox(f, u, pen)
            FixtureType.CLOCK -> fxClock(f, u, pen)
            FixtureType.PLANT_BIG -> fxPlant(f, u, pen)
            FixtureType.WOOD_STOVE -> fxWoodStove(f, u, pen)

            FixtureType.CASH_REGISTER -> fxRegister(f, u, pen)
            FixtureType.OVEN -> fxOven(f, u, pen)
            FixtureType.BLENDER -> fxBlender(f, u, pen, contents)
            FixtureType.FRUIT_CRATE -> fxFruitCrate(f, u, pen)
            FixtureType.ICE_CREAM_MACHINE -> fxIceCream(f, u, pen)
            FixtureType.DISPLAY_CASE -> fxDisplayCase(f, u, pen)
            FixtureType.FLOUR_SACK -> fxFlourSack(f, u, pen)

            FixtureType.SALON_CHAIR -> fxSalonChair(f, u, pen)
            FixtureType.DRYER_HOOD -> fxDryer(f, u, pen)
            FixtureType.HAIR_WASH -> fxHairWash(f, u, pen)
            FixtureType.CLOTHES_RACK -> fxClothesRack(f, u, pen)

            FixtureType.POTION_RACK -> fxPotionRack(f, u, pen)
            FixtureType.CAULDRON -> fxCauldron(f, u, pen, contents)
            FixtureType.TELESCOPE -> fxTelescope(f, u, pen)
            FixtureType.CRYSTAL_BALL -> fxCrystalBall(f, u, pen)
            FixtureType.SPELLBOOK -> fxSpellbook(f, u, pen)

            FixtureType.PIER -> fxPier(f, u, pen)
            FixtureType.FISHING_SPOT -> fxFishingSpot(f, u, pen)
            FixtureType.BOAT -> fxBoat(f, u, pen)
            FixtureType.UMBRELLA -> fxUmbrella(f, u, pen)
            FixtureType.LOUNGER -> fxLounger(f, u, pen)
            FixtureType.SANDCASTLE -> fxSandcastle(f, u, pen)

            FixtureType.TENT -> fxTent(f, u, pen)
            FixtureType.CAMPFIRE -> fxCampfire(f, u, pen)
            FixtureType.LOG -> fxLog(f, u, pen)
            FixtureType.STUMP -> fxStump(f, u, pen)
            FixtureType.OWL_TREE -> fxOwlTree(f, u, pen)

            FixtureType.PINE_TREE -> fxPineTree(f, u, pen)
            FixtureType.SAUNA -> fxSauna(f, u, pen)
            FixtureType.SLED_HILL -> fxSledHill(f, u, pen)
            FixtureType.SNOWMAN -> fxSnowman(f, u, pen)
            FixtureType.SKI_JUMP -> fxSkiJump(f, u, pen)
            FixtureType.ICE_POND -> fxIcePond(f, u, pen)
            FixtureType.COCOA_STAND -> fxKiosk(f, u, pen)
            FixtureType.BENCH -> fxBench(f, u, pen)
            FixtureType.LAMP_POST -> fxLampPost(f, u, pen)

            FixtureType.TRACTOR -> fxTractor(f, u, pen)
            FixtureType.HAY_BALE -> fxHayBale(f, u, pen)
            FixtureType.CHICKEN_COOP -> fxCoop(f, u, pen)
            FixtureType.VEGETABLE_PATCH -> fxVegPatch(f, u, pen)
            FixtureType.WATER_TROUGH -> fxTrough(f, u, pen)
            FixtureType.WORKBENCH -> fxWorkbench(f, u, pen, contents)
            FixtureType.TOOL_WALL -> fxToolWall(f, u, pen)
            FixtureType.WOOD_PILE -> fxWoodPile(f, u, pen)
            FixtureType.TIRE_STACK -> fxTireStack(f, u, pen)

            FixtureType.ROCKET_SHIP -> fxRocket(f, u, pen)
            FixtureType.CONTROL_PANEL -> fxControlPanel(f, u, pen)
            FixtureType.PORTHOLE -> fxPorthole(f, u, pen)
            FixtureType.GRAVITY_LEVER -> fxGravityLever(f, u, pen)
            FixtureType.ORRERY -> fxOrrery(f, u, pen)
            FixtureType.SPACE_BED -> fxSpaceBed(f, u, pen)
            FixtureType.FOOD_DISPENSER -> fxFoodDispenser(f, u, pen)
            else -> fxPending(f, u, pen)
        }
    }
}

/** A plain painted box for furniture whose own drawing is still on its way. */
private fun DrawScope.fxPending(f: Fixture, u: Float, pen: Pen) {
    val w = f.spec.w * u
    val h = f.spec.h * u
    val hue = FX_PENDING[f.type.ordinal % FX_PENDING.size]
    box3d(Rect(-w / 2, -h, w / 2, 0f), 0.08f * u, hue, pen, radius = 0.012f * u)
}

private val FX_PENDING = listOf(Color(0xFFFFC83D), Color(0xFF7CCBFF), Color(0xFFFF8FB1), Color(0xFF8BE3B5), Color(0xFFC9A4FF))

/** Draws the part of a fixture that sits in front of whoever uses it (a duvet, a bath side, glass). */
fun DrawScope.drawFixtureFront(f: Fixture, u: Float, pen: Pen) {
    if (!f.spec.front && !f.spec.glass) return
    if (drawRoomsFront(f, u, pen) || drawRidesFront(f, u, pen) || drawDecorFront(f, u, pen)) return
    fxBobbed(f, u) {
        when (f.type) {
            FixtureType.BED -> fxBedFront(f, u, pen)
            FixtureType.BATH -> fxBathFront(f, u, pen)
            FixtureType.BOAT -> fxBoatFront(f, u, pen)
            FixtureType.DRYER_HOOD -> fxDryerFront(f, u, pen)
            FixtureType.DISPLAY_CASE -> fxDisplayGlass(f, u, pen)
            FixtureType.TRACTOR -> fxTractorFront(f, u, pen)
            FixtureType.ROCKET_SHIP -> fxRocketFront(f, u, pen)
            else -> Unit
        }
    }
}

/** Fixtures that stand in water, lie flat or fly, and so cast no soft shadow under them. */
private val FX_NO_SHADOW = setOf(FixtureType.BOAT, FixtureType.PIER, FixtureType.ICE_POND, FixtureType.ROCKET_SHIP, FixtureType.VEGETABLE_PATCH)

/** The boat floats on the waves; the seats move with it. */
private inline fun DrawScope.fxBobbed(f: Fixture, u: Float, block: DrawScope.() -> Unit) {
    if (f.type == FixtureType.BOAT && f.bob != 0f) translate(0f, f.bob * u) { block() } else block()
}

/** How deep each fixture's footprint is, for its shadow on the floor. */
private fun fxDepthOf(type: FixtureType): Float = when (type) {
    FixtureType.BED, FixtureType.SOFA, FixtureType.TABLE, FixtureType.BATH, FixtureType.SAUNA, FixtureType.TENT -> 0.17f
    FixtureType.ROUND_TABLE, FixtureType.UMBRELLA, FixtureType.OWL_TREE, FixtureType.PINE_TREE, FixtureType.SLED_HILL,
    FixtureType.SKI_JUMP, FixtureType.TRACTOR, FixtureType.ORRERY, FixtureType.CHICKEN_COOP -> 0.16f
    FixtureType.CHAIR, FixtureType.STOOL, FixtureType.LAMP, FixtureType.PLANT_BIG, FixtureType.LAMP_POST, FixtureType.RADIO,
    FixtureType.CASH_REGISTER, FixtureType.BLENDER, FixtureType.CRYSTAL_BALL, FixtureType.FISHING_SPOT -> 0.08f
    else -> 0.13f
}

private fun DrawScope.fxFootShadow(u: Float, w: Float, d: Float) {
    val dx = FX_DX * d * u
    val dy = FX_DY * d * u
    val width = w * u + dx
    val height = -dy + 0.03f * u
    drawOval(Ink.shadow, Offset(-w * u / 2f + 0.01f * u, dy - 0.012f * u), Size(width, height))
}

// ---------------------------------------------------------------------------------------------- kit

internal const val FX_PI = 3.1415927f
internal const val FX_DX = Oblique.DX
internal const val FX_DY = Oblique.DY
private const val FX_KAPPA = 0.5523f

/** Colours shared by the furniture: everyday, light Scandinavian (docs/ART_GUIDE.md). */
internal object FxC {
    val oak = Color(0xFFE2BE8A)
    val oakDark = Color(0xFFC49A62)
    val wood = Color(0xFFC98A55)
    val woodDark = Color(0xFFA0663B)
    val walnut = Color(0xFF6E4630)
    val teak = Color(0xFFA9683A)
    val paint = Color(0xFFF7F5F0)
    val sage = Color(0xFF9CB8A0)
    val dusty = Color(0xFF7FA3C4)
    val mustard = Color(0xFFE8B04A)
    val blush = Color(0xFFF2B8B0)
    val charcoal = Color(0xFF3A3A44)
    val terracotta = Color(0xFFD9774F)
    val red = Color(0xFFD2443A)
    val falun = Color(0xFFB8342B)
    val spruce = Color(0xFF1F7048)
    val spruceLight = Color(0xFF2E8B57)
    val leaf = Color(0xFF6FAE5A)
    val fjord = Color(0xFF2F6FB8)
    val sky = Color(0xFF5AA9E6)
    val birch = Color(0xFFF2EEE6)
    val snow = Color(0xFFF4F8FF)
    val snowShade = Color(0xFFC9D6EE)
    val auroraGreen = Color(0xFF7CFFB2)
    val auroraPink = Color(0xFFD77BFF)
    val yellow = Color(0xFFFFC83D)
    val green = Color(0xFF3BC46B)
    val iron = Color(0xFF3A3844)
    val steel = Color(0xFFBAC4D4)
    val brass = Color(0xFFE0B04A)
    val cream = Color(0xFFF6EEDC)
    val porcelain = Color(0xFFF7F8FA)
    val mint = Color(0xFF8FD9C0)
    val pink = Color(0xFFF08CB8)
    val lilac = Color(0xFFB9A2F0)
    val stone = Color(0xFF9EA3B0)
    val flame1 = Color(0xFFFF5A2E)
    val flame2 = Color(0xFFFFA23A)
    val flame3 = Color(0xFFFFE680)
    val warm = Color(0xFFFFD37A)
    val water = Color(0xFF6CC3EE)
    val soil = Color(0xFF6B4A33)
    val straw = Color(0xFFE8C85A)
    val rubber = Color(0xFF2E2B36)
    val sand = Color(0xFFF0CF8A)
    val night = Color(0xFF14203A)
}

internal fun fxFrac(x: Float): Float = x - floor(x)

/** The point a fraction [f] of the way from [a] to [b]. */
internal fun fxMix(a: Offset, b: Offset, f: Float): Offset = Offset(a.x + (b.x - a.x) * f, a.y + (b.y - a.y) * f)

/** A closed triangle or quadrilateral through pixel points. */
internal fun fxPath(a: Offset, b: Offset, c: Offset, d: Offset? = null): Path = Path().apply {
    moveTo(a.x, a.y)
    lineTo(b.x, b.y)
    lineTo(c.x, c.y)
    if (d != null) lineTo(d.x, d.y)
    close()
}

/** Screen point in pixels of a 3D point: [x] across, [y] height (up is negative), [z] depth, all in units. */
internal fun fxQ(u: Float, x: Float, y: Float, z: Float): Offset = Offset((x + FX_DX * z) * u, (y + FX_DY * z) * u)

/**
 * A quadrilateral through four pixel points, its corners rounded by [rad] pixels. The faces of every
 * 3D shape are built from these.
 */
internal fun fxQuad(x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, rad: Float = 0f): Path {
    val path = Path()
    if (rad <= 0f) {
        path.moveTo(x0, y0)
        path.lineTo(x1, y1)
        path.lineTo(x2, y2)
        path.lineTo(x3, y3)
        path.close()
        return path
    }
    val xs = floatArrayOf(x0, x1, x2, x3)
    val ys = floatArrayOf(y0, y1, y2, y3)
    for (i in 0..3) {
        val px = xs[(i + 3) % 4]
        val py = ys[(i + 3) % 4]
        val cx = xs[i]
        val cy = ys[i]
        val nx = xs[(i + 1) % 4]
        val ny = ys[(i + 1) % 4]
        val kp = min(rad / hypot(px - cx, py - cy).coerceAtLeast(0.001f), 0.5f)
        val kn = min(rad / hypot(nx - cx, ny - cy).coerceAtLeast(0.001f), 0.5f)
        val ax = cx + (px - cx) * kp
        val ay = cy + (py - cy) * kp
        if (i == 0) path.moveTo(ax, ay) else path.lineTo(ax, ay)
        path.quadraticTo(cx, cy, cx + (nx - cx) * kn, cy + (ny - cy) * kn)
    }
    path.close()
    return path
}

/** Fills a face and rims it in ink. */
internal fun DrawScope.fxFace(path: Path, color: Color, pen: Pen) {
    drawPath(path, color)
    drawPath(path, Ink.line, style = pen.stroke)
}

/**
 * A box in oblique 3D, all in scene units: the front face (l, t)–(r, b) sits [z] units back from the
 * fixture's front plane and the box is [d] units deep. The top is lit, the right side shaded, and the
 * front gets the crisp cel shade. [rad] rounds the corners of every face.
 */
internal fun DrawScope.fxBox(
    u: Float, l: Float, t: Float, r: Float, b: Float, d: Float, color: Color, pen: Pen,
    rad: Float = 0f, z: Float = 0f, top: Color = color.lighten(0.18f), side: Color = color.darken(0.22f), front: Boolean = true,
) {
    val ox = FX_DX * z * u
    val oy = FX_DY * z * u
    val dx = FX_DX * d * u
    val dy = FX_DY * d * u
    val lx = l * u + ox
    val rx = r * u + ox
    val ty = t * u + oy
    val by = b * u + oy
    val rr = rad * u
    fxFace(fxQuad(rx, ty, rx + dx, ty + dy, rx + dx, by + dy, rx, by, rr * 0.7f), side, pen)
    fxFace(fxQuad(lx, ty, lx + dx, ty + dy, rx + dx, ty + dy, rx, ty, rr * 0.7f), top, pen)
    if (front) inkedRound(Rect(lx, ty, rx, by), rr, color, pen)
}

/** The front face rectangle of a box [z] units back, in pixels. */
internal fun fxFront(u: Float, l: Float, t: Float, r: Float, b: Float, z: Float = 0f): Rect =
    Rect((l + FX_DX * z) * u, (t + FX_DY * z) * u, (r + FX_DX * z) * u, (b + FX_DY * z) * u)

/** A flat horizontal board face from [l] to [r] at height [y], from depth [z0] to [z1] (units). */
internal fun fxFlat(u: Float, l: Float, r: Float, y: Float, z0: Float, z1: Float, rad: Float = 0f): Path {
    val a = fxQ(u, l, y, z0)
    val b = fxQ(u, l, y, z1)
    val c = fxQ(u, r, y, z1)
    val d = fxQ(u, r, y, z0)
    return fxQuad(a.x, a.y, b.x, b.y, c.x, c.y, d.x, d.y, rad * u)
}

/** A vertical face running back in depth at [x], from height [t] to [b], depth [z0] to [z1] (units). */
internal fun fxDeep(u: Float, x: Float, t: Float, b: Float, z0: Float, z1: Float, rad: Float = 0f): Path {
    val a = fxQ(u, x, t, z0)
    val c = fxQ(u, x, t, z1)
    val e = fxQ(u, x, b, z1)
    val g = fxQ(u, x, b, z0)
    return fxQuad(a.x, a.y, c.x, c.y, e.x, e.y, g.x, g.y, rad * u)
}

/** A horizontal circle of radius [r] pixels centred on the pixel point (cx, cy), seen in oblique 3D. */
internal fun fxDisc(cx: Float, cy: Float, r: Float): Path {
    val k = FX_KAPPA * r
    fun mx(x: Float, z: Float) = cx + x + FX_DX * z
    fun my(z: Float) = cy + FX_DY * z
    return Path().apply {
        moveTo(mx(r, 0f), my(0f))
        cubicTo(mx(r, k), my(k), mx(k, r), my(r), mx(0f, r), my(r))
        cubicTo(mx(-k, r), my(r), mx(-r, k), my(k), mx(-r, 0f), my(0f))
        cubicTo(mx(-r, -k), my(-k), mx(-k, -r), my(-r), mx(0f, -r), my(-r))
        cubicTo(mx(k, -r), my(-r), mx(r, -k), my(-k), mx(r, 0f), my(0f))
        close()
    }
}

/** A flat horizontal ellipse, [rx] across and [rz] deep (pixels), centred on the pixel point (cx, cy). */
internal fun fxDisc2(cx: Float, cy: Float, rx: Float, rz: Float): Path {
    val kx = FX_KAPPA * rx
    val kz = FX_KAPPA * rz
    fun mx(x: Float, z: Float) = cx + x + FX_DX * z
    fun my(z: Float) = cy + FX_DY * z
    return Path().apply {
        moveTo(mx(rx, 0f), my(0f))
        cubicTo(mx(rx, kz), my(kz), mx(kx, rz), my(rz), mx(0f, rz), my(rz))
        cubicTo(mx(-kx, rz), my(rz), mx(-rx, kz), my(kz), mx(-rx, 0f), my(0f))
        cubicTo(mx(-rx, -kz), my(-kz), mx(-kx, -rz), my(-rz), mx(0f, -rz), my(-rz))
        cubicTo(mx(kx, -rz), my(-rz), mx(rx, -kz), my(-kz), mx(rx, 0f), my(0f))
        close()
    }
}

/** A point on the rim of a flat horizontal circle of radius [r] pixels around (cx, cy), at [angle] radians (pi/2 is at the back). */
internal fun fxRim(cx: Float, cy: Float, r: Float, angle: Float): Offset {
    val x = r * kotlin.math.cos(angle)
    val z = r * sin(angle)
    return Offset(cx + x + FX_DX * z, cy + FX_DY * z)
}

/**
 * An upright cylinder or cone in pixels: a bottom disc of radius [rb] centred on (cx, yb) and a top disc
 * of radius [rt] centred on (cx, yt). The side is shaded from light (left) to dark (right).
 */
internal fun DrawScope.fxCyl(
    cx: Float, yb: Float, yt: Float, rb: Float, rt: Float, color: Color, pen: Pen,
    top: Color = color.lighten(0.2f), cap: Boolean = true, bottom: Boolean = true,
) {
    if (bottom) fxFace(fxDisc(cx, yb, rb), color.darken(0.12f), pen)
    val body = Path().apply {
        moveTo(cx - rt * 1.118f, yt + rt * 0.161f)
        lineTo(cx + rt * 1.118f, yt - rt * 0.161f)
        lineTo(cx + rb * 1.118f, yb - rb * 0.161f)
        lineTo(cx - rb * 1.118f, yb + rb * 0.161f)
        close()
    }
    val span = maxOf(rt, rb) * 1.118f
    drawPath(body, Brush.horizontalGradient(0f to color.lighten(0.16f), 0.5f to color, 1f to color.darken(0.26f), startX = cx - span, endX = cx + span))
    drawLine(Ink.line, Offset(cx - rt * 1.118f, yt + rt * 0.161f), Offset(cx - rb * 1.118f, yb + rb * 0.161f), pen.lw)
    drawLine(Ink.line, Offset(cx + rt * 1.118f, yt - rt * 0.161f), Offset(cx + rb * 1.118f, yb - rb * 0.161f), pen.lw)
    if (cap) fxFace(fxDisc(cx, yt, rt), top, pen)
}

/** A round post from height [yb] to [yt] (units) whose centre is at ([x], [z]); radius [r] units. */
internal fun DrawScope.fxPost(u: Float, x: Float, z: Float, yb: Float, yt: Float, r: Float, color: Color, pen: Pen) {
    val b = fxQ(u, x, yb, z)
    val t = fxQ(u, x, yt, z)
    fxCyl(b.x, b.y, t.y, r * u, r * u, color, pen)
}

/**
 * The inside of an open cupboard: a recessed box behind the opening (l, t)–(r, b), [d] units deep.
 * Only the back wall, the left wall and the floor can be seen, as through a real opening.
 */
internal fun DrawScope.fxHollow(u: Float, l: Float, t: Float, r: Float, b: Float, d: Float, wall: Color, pen: Pen, back: Color = wall.darken(0.28f)) {
    val lx = l * u
    val rx = r * u
    val ty = t * u
    val by = b * u
    val dx = FX_DX * d * u
    val dy = FX_DY * d * u
    clipRect(lx, ty, rx, by) {
        drawRect(back, Offset(lx, ty), Size(rx - lx, by - ty))
        drawPath(fxQuad(lx, ty, lx + dx, ty + dy, lx + dx, by + dy, lx, by), wall.darken(0.1f))
        drawPath(fxQuad(lx, by, lx + dx, by + dy, rx + dx, by + dy, rx, by), wall.lighten(0.06f))
        drawLine(Ink.line.copy(alpha = 0.55f), Offset(lx + dx, ty), Offset(lx + dx, by + dy), pen.lw * 0.6f)
        drawLine(Ink.line.copy(alpha = 0.55f), Offset(lx + dx, by + dy), Offset(rx, by + dy), pen.lw * 0.6f)
    }
    drawRect(Ink.line, Offset(lx, ty), Size(rx - lx, by - ty), style = pen.stroke)
}

/** A shelf board inside a hollow: its top face with the front edge at height [y], and its front edge strip. */
internal fun DrawScope.fxInShelf(u: Float, l: Float, r: Float, y: Float, d: Float, color: Color, pen: Pen, thick: Float = 0.008f) {
    clipRect(l * u, (y + FX_DY * d) * u - pen.lw, r * u, (y + thick) * u) {
        fxFace(fxFlat(u, l, r, y, 0f, d), color.lighten(0.12f), pen)
    }
    inkedRound(Rect(l * u, y * u, r * u, (y + thick) * u), 0f, color, pen, shade = false)
}

/**
 * A door swung open on a vertical hinge at [hx] (units), from height [t] to [b], [w] wide. [side] is -1
 * for a door hinged on the left (it swings out to the left) and +1 for one hinged on the right.
 * [angle] in degrees; the inside of the door faces us.
 */
internal fun DrawScope.fxOpenDoor(u: Float, hx: Float, t: Float, b: Float, w: Float, side: Float, color: Color, inside: Color, pen: Pen, angle: Float = 125f): Path {
    val v = fxDoorVec(u, w, side, angle)
    val ox = v.x
    val oy = v.y
    val door = fxQuad(hx * u, t * u, hx * u + ox, t * u + oy, hx * u + ox, b * u + oy, hx * u, b * u, 0.004f * u)
    drawPath(door, inside)
    drawPath(door, Ink.line, style = pen.stroke)
    // The door's edge, so it has thickness.
    val e = 0.008f * u * side
    val edge = fxQuad(hx * u + ox, t * u + oy, hx * u + ox + e, t * u + oy - 0.003f * u, hx * u + ox + e, b * u + oy - 0.003f * u, hx * u + ox, b * u + oy)
    fxFace(edge, color.darken(0.15f), pen)
    return door
}

/** Where the free edge of an open door ends up relative to its hinge, in pixels (see [fxOpenDoor]). */
internal fun fxDoorVec(u: Float, w: Float, side: Float, angle: Float): Offset {
    val a = angle * FX_PI / 180f
    val vx = -side * w * kotlin.math.cos(a)
    val vz = -w * kotlin.math.sin(a)
    return Offset((vx + FX_DX * vz) * u, FX_DY * vz * u)
}

/** A closed polygon through scene-unit points (x, y pairs), scaled to pixels by [u]. */
internal fun fxPoly(u: Float, vararg xy: Float): Path {
    val path = Path()
    path.moveTo(xy[0] * u, xy[1] * u)
    var i = 2
    while (i < xy.size) {
        path.lineTo(xy[i] * u, xy[i + 1] * u)
        i += 2
    }
    path.close()
    return path
}

/** [blobPath] through scene-unit points. */
internal fun fxBlob(u: Float, vararg xy: Float): Path {
    val px = FloatArray(xy.size) { xy[it] * u }
    return blobPath(*px)
}

/** A pointed leaf from (ax, ay) to (bx, by) in pixels, [wide] times its length across. */
internal fun fxLeaf(ax: Float, ay: Float, bx: Float, by: Float, wide: Float): Path {
    val nx = -(by - ay) * wide
    val ny = (bx - ax) * wide
    val mx = (ax + bx) / 2
    val my = (ay + by) / 2
    return Path().apply {
        moveTo(ax, ay)
        quadraticTo(mx + nx, my + ny, bx, by)
        quadraticTo(mx - nx, my - ny, ax, ay)
        close()
    }
}

/** A heart centred on (cx, cy) in pixels, [s] about half its width. */
internal fun fxHeart(cx: Float, cy: Float, s: Float): Path = Path().apply {
    moveTo(cx, cy + s * 0.75f)
    cubicTo(cx - s * 1.3f, cy - s * 0.05f, cx - s * 0.7f, cy - s * 1.05f, cx, cy - s * 0.45f)
    cubicTo(cx + s * 0.7f, cy - s * 1.05f, cx + s * 1.3f, cy - s * 0.05f, cx, cy + s * 0.75f)
    close()
}

/** One tongue of flame standing on (cx, by), in pixels. */
internal fun fxFlamePath(cx: Float, by: Float, w: Float, h: Float, sway: Float): Path = Path().apply {
    moveTo(cx - w / 2, by)
    cubicTo(cx - w * 0.58f, by - h * 0.45f, cx - w * 0.15f + sway * 0.4f, by - h * 0.7f, cx + sway, by - h)
    cubicTo(cx + w * 0.2f + sway * 0.4f, by - h * 0.7f, cx + w * 0.58f, by - h * 0.45f, cx + w / 2, by)
    quadraticTo(cx, by + w * 0.3f, cx - w / 2, by)
    close()
}

/** A flickering three-colour flame on (cx, by), in pixels. [seed] keeps neighbouring flames out of step. */
internal fun DrawScope.fxFire(cx: Float, by: Float, w: Float, h: Float, t: Float, seed: Float, pen: Pen, outline: Boolean = true) {
    val flick = 1f + 0.14f * sin(t * 9.1f + seed) + 0.06f * sin(t * 17.3f + seed * 2f)
    val sway = w * 0.22f * sin(t * 5.3f + seed)
    val outer = fxFlamePath(cx, by, w, h * flick, sway)
    drawPath(outer, FxC.flame1)
    if (outline) drawPath(outer, Ink.line, style = pen.thin)
    drawPath(fxFlamePath(cx, by, w * 0.66f, h * 0.72f * flick, sway * 0.8f), FxC.flame2)
    drawPath(fxFlamePath(cx, by, w * 0.34f, h * 0.44f * flick, sway * 0.5f), FxC.flame3)
}

/** Soft rising puffs of smoke or steam from (x, y), in pixels. */
internal fun DrawScope.fxPuffs(
    x: Float, y: Float, t: Float, size: Float, rise: Float, color: Color, alpha: Float,
    count: Int = 4, speed: Float = 0.45f, drift: Float = size,
) {
    for (i in 0 until count) {
        val ph = fxFrac(t * speed + i / count.toFloat())
        val a = alpha * (1f - ph) * (ph * 5f).coerceAtMost(1f)
        if (a <= 0.01f) continue
        val cx = x + sin(ph * 5f + i * 1.7f) * size * 0.4f + drift * ph
        drawCircle(color.copy(alpha = a), size * (0.45f + ph * 0.9f), Offset(cx, y - ph * rise))
    }
}

/** A soft round glow of light. */
internal fun DrawScope.fxGlow(c: Offset, r: Float, color: Color, alpha: Float) {
    val a = alpha.coerceIn(0f, 1f)
    if (a <= 0.01f || r <= 0f) return
    drawCircle(Brush.radialGradient(0f to color.copy(alpha = a), 1f to color.copy(alpha = 0f), center = c, radius = r), r, c)
}

/**
 * Overlapping circles drawn as one inked shape (foliage, foam, snow): (x, y, r) triples in pixels.
 * With [shade] each circle gets the crisp cel-shade crescent.
 */
internal fun DrawScope.fxCloud(color: Color, pen: Pen, shade: Boolean, vararg c: Float) {
    var i = 0
    while (i < c.size) {
        drawCircle(Ink.line, c[i + 2] + pen.lw, Offset(c[i], c[i + 1]))
        i += 3
    }
    i = 0
    val base = if (shade) color.shadow() else color
    while (i < c.size) {
        drawCircle(base, c[i + 2], Offset(c[i], c[i + 1]))
        i += 3
    }
    if (!shade) return
    i = 0
    while (i < c.size) {
        val r = c[i + 2]
        drawCircle(color, r * 0.84f, Offset(c[i] - r * 0.1f, c[i + 1] - r * 0.13f))
        i += 3
    }
}

/** A few thin wavy grain lines across a board, in a darker tone of [color]. */
internal fun DrawScope.fxGrain(rect: Rect, color: Color, pen: Pen, lines: Int = 2, vertical: Boolean = false) {
    val c = color.darken(0.3f).copy(alpha = 0.55f)
    val path = Path()
    for (i in 1..lines) {
        val f = i / (lines + 1f)
        val wob = (if (i % 2 == 0) 1f else -1f)
        if (!vertical) {
            val y = rect.top + rect.height * f
            val d = rect.height * 0.12f * wob
            path.moveTo(rect.left + rect.width * 0.05f, y)
            path.quadraticTo(rect.left + rect.width * 0.3f, y + d, rect.left + rect.width * 0.5f, y)
            path.quadraticTo(rect.left + rect.width * 0.72f, y - d, rect.right - rect.width * 0.05f, y + d * 0.4f)
        } else {
            val x = rect.left + rect.width * f
            val d = rect.width * 0.12f * wob
            path.moveTo(x, rect.top + rect.height * 0.05f)
            path.quadraticTo(x + d, rect.top + rect.height * 0.3f, x, rect.top + rect.height * 0.5f)
            path.quadraticTo(x - d, rect.top + rect.height * 0.72f, x + d * 0.4f, rect.bottom - rect.height * 0.05f)
        }
    }
    drawPath(path, c, style = pen.thin)
}

/** A thin detail line with round ends. */
internal fun DrawScope.fxLine(a: Offset, b: Offset, color: Color, width: Float) {
    drawLine(color, a, b, strokeWidth = width, cap = StrokeCap.Round)
}

/** A nail head or rivet. */
internal fun DrawScope.fxNail(c: Offset, r: Float) {
    drawCircle(Ink.line.copy(alpha = 0.75f), r, c)
    drawCircle(Color.White.copy(alpha = 0.45f), r * 0.4f, Offset(c.x - r * 0.3f, c.y - r * 0.3f))
}

/** A round bolt or screw head with an ink rim. */
internal fun DrawScope.fxBolt(c: Offset, r: Float, pen: Pen, color: Color = FxC.steel) {
    drawCircle(color, r, c)
    drawCircle(Ink.line, r, c, style = pen.thin)
    drawCircle(Color.White.copy(alpha = 0.6f), r * 0.35f, Offset(c.x - r * 0.3f, c.y - r * 0.3f))
}

/** An eight-pointed knitting star. */
internal fun DrawScope.fxStar8(c: Offset, r: Float, color: Color, width: Float) {
    val d = r * 0.7071f
    drawLine(color, Offset(c.x - r, c.y), Offset(c.x + r, c.y), width, StrokeCap.Round)
    drawLine(color, Offset(c.x, c.y - r), Offset(c.x, c.y + r), width, StrokeCap.Round)
    drawLine(color, Offset(c.x - d, c.y - d), Offset(c.x + d, c.y + d), width, StrokeCap.Round)
    drawLine(color, Offset(c.x - d, c.y + d), Offset(c.x + d, c.y - d), width, StrokeCap.Round)
}

/** Rows of little knit "v" stitches across [rect]. */
internal fun DrawScope.fxKnit(rect: Rect, color: Color, cols: Int, rows: Int, width: Float) {
    val cw = rect.width / cols
    val ch = rect.height / rows
    for (j in 0 until rows) {
        for (i in 0 until cols) {
            val x = rect.left + (i + 0.5f) * cw
            val y = rect.top + (j + 0.5f) * ch
            drawLine(color, Offset(x - cw * 0.32f, y - ch * 0.28f), Offset(x, y + ch * 0.28f), width, StrokeCap.Round)
            drawLine(color, Offset(x + cw * 0.32f, y - ch * 0.28f), Offset(x, y + ch * 0.28f), width, StrokeCap.Round)
        }
    }
}

/** Gingham: crossing see-through stripes of [color] over whatever is below. */
internal fun DrawScope.fxGingham(rect: Rect, color: Color, cells: Int) {
    val s = rect.width / (cells * 2f)
    val c = color.copy(alpha = 0.42f)
    for (i in 0 until cells) drawRect(c, Offset(rect.left + (i * 2 + 0.5f) * s, rect.top), Size(s, rect.height))
    val rows = (rect.height / (s * 2f)).toInt().coerceAtLeast(1)
    for (j in 0 until rows) drawRect(c, Offset(rect.left, rect.top + (j * 2 + 0.5f) * s), Size(rect.width, s))
}

/** A musical note, for the radio and the piano. */
internal fun DrawScope.fxNote(c: Offset, s: Float, color: Color, alpha: Float) {
    if (alpha <= 0.01f) return
    val col = color.copy(alpha = alpha)
    drawOval(col, Offset(c.x - s * 0.55f, c.y - s * 0.34f), Size(s * 1.1f, s * 0.68f))
    drawLine(col, Offset(c.x + s * 0.48f, c.y), Offset(c.x + s * 0.48f, c.y - s * 1.8f), s * 0.2f, StrokeCap.Round)
    drawLine(col, Offset(c.x + s * 0.48f, c.y - s * 1.8f), Offset(c.x + s * 1.1f, c.y - s * 1.3f), s * 0.22f, StrokeCap.Round)
}

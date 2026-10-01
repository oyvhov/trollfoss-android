package app.trollfoss.ui.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import app.trollfoss.domain.PlaceId
import kotlin.math.floor
import kotlin.math.sin

/*
 * The farm («Garden»): an ordinary farmyard in oblique 3D. A big red barn on the left with its doors slid
 * open, a workshop shed on the right, a fence, an apple tree and dirt tracks on the grass, and fields,
 * a white farmhouse, hills, mountains and a glimpse of the fjord far behind.
 */

private const val FARM_HZ = 0.62f
private const val YARD_BACK = 0.705f

private class FarmStatic(
    val ground: Path, val groundEdge: Path, val stripes: Path, val tracks: Path, val ruts: Path, val tufts: Path,
    val flowers: List<Offset>, val rails: Path, val posts: Path, val barnFront: FloatArray, val planks: List<Offset>,
    val shedPlanks: List<Offset>, val apples: List<Offset>, val fallen: List<Offset>,
)

private fun yardBackY(x: Float) = YARD_BACK + 0.003f * sin(x * 4.3f)

private val farmStatic = Memo { u ->
    val back = PlaceId.FARM.back
    val ground = Path()
    val groundEdge = Path()
    for (i in 0..52) {
        val x = -0.4f + i * 0.1f
        if (i == 0) {
            ground.moveTo(x * u, yardBackY(x) * u)
            groundEdge.moveTo(x * u, yardBackY(x) * u)
        } else {
            ground.lineTo(x * u, yardBackY(x) * u)
            groundEdge.lineTo(x * u, yardBackY(x) * u)
        }
    }
    ground.lineTo(4.8f * u, FRONT_Y * u)
    ground.lineTo(-0.4f * u, FRONT_Y * u)
    ground.close()
    val stripes = Path()
    var sx = -0.6f
    while (sx < 4.8f) {
        stripes.floorQuad(u, 0f, sx, sx + 0.14f, FRONT_Y, back)
        sx += 0.28f
    }
    val tracks = Path().apply {
        floorQuad(u, 0f, 0.04f, 0.64f, FRONT_Y, back)
        floorQuad(u, 0f, 0.3f, 4.8f, 0.93f, 0.875f)
    }
    val ruts = Path()
    for (off in floatArrayOf(0.13f, 0.47f)) {
        ruts.moveTo((0.04f + off) * u, FRONT_Y * u)
        ruts.lineTo((0.04f + off + recede(back)) * u, back * u)
    }
    for (y in floatArrayOf(0.89f, 0.915f)) {
        ruts.moveTo((0.6f + recede(y)) * u, y * u)
        ruts.lineTo((4.8f + recede(y)) * u, y * u)
    }
    val tufts = Path()
    for (i in 0 until 26) {
        val x = -0.2f + hash01(i, 701) * 4.8f
        val y = mix(YARD_BACK + 0.01f, FRONT_Y - 0.01f, hash01(i, 702))
        if (y > 0.87f && y < 0.935f && x > 0.3f) continue
        tufts.addPath(tuftPath(x * u, y * u, 0.026f * u * (0.7f + 0.5f * hash01(i, 703)), 0f))
    }
    val flowers = ArrayList<Offset>(30)
    for (i in 0 until 30) flowers.add(Offset((-0.2f + hash01(i, 711) * 4.8f) * u, mix(YARD_BACK + 0.01f, FRONT_Y - 0.01f, hash01(i, 712)) * u))
    val rails = Path()
    val posts = Path()
    for (y in floatArrayOf(0.68f, 0.7f)) rails.addRect(Rect(-0.4f * u, y * u, 4.8f * u, (y + 0.01f) * u))
    var px = -0.3f
    while (px < 4.8f) {
        posts.addRoundRect(RoundRect(Rect((px - 0.011f) * u, 0.668f * u, (px + 0.011f) * u, 0.722f * u), CornerRadius(0.004f * u)))
        px += 0.25f
    }
    val barnFront = floatArrayOf(0f, 0.38f, 0.14f, 0.2f, 0.6f, 0.08f, 1.06f, 0.2f, 1.2f, 0.38f, 1.2f, back, 0f, back)
    val planks = ArrayList<Offset>(40)
    var bx = 0.06f
    while (bx < 1.2f) {
        planks.add(Offset(bx * u, 0.06f * u))
        planks.add(Offset(bx * u, back * u))
        bx += 0.06f
    }
    val shedPlanks = ArrayList<Offset>(20)
    var shx = 3.47f
    while (shx < 4.0f) {
        shedPlanks.add(Offset(shx * u, 0.2f * u))
        shedPlanks.add(Offset(shx * u, back * u))
        shx += 0.055f
    }
    val apples = ArrayList<Offset>(14)
    for (i in 0 until 14) {
        val a = hash01(i, 721) * 6.283f
        val r = 0.5f + 0.45f * hash01(i, 722)
        apples.add(Offset((2.62f + kotlin.math.cos(a) * 0.17f * r) * u, (0.43f + sin(a) * 0.12f * r) * u))
    }
    val fallen = listOf(Offset(2.52f * u, 0.75f * u), Offset(2.71f * u, 0.758f * u), Offset(2.78f * u, 0.748f * u))
    FarmStatic(ground, groundEdge, stripes, tracks, ruts, tufts, flowers, rails, posts, barnFront, planks, shedPlanks, apples, fallen)
}

internal fun DrawScope.farmBack(st: Stage, pen: Pen) {
    val u = st.u
    val t = pen.t
    val n = pen.night
    drawSky(st, pen, Mood.SUMMER, FARM_HZ)
    drawStars(st, pen, 0.48f)
    drawMoon(Offset(st.fx(0.7f, 0.04f), 0.12f * u), 0.038f * u, pen, ramp((n - 0.3f) / 0.5f))
    drawAurora(st, pen, 0.75f, 0.02f, 0.28f)
    drawSun(Offset(st.fx(0.82f, 0.04f), 0.14f * u), 0.05f * u, pen, (1f - n) * (1f - overcast(pen)))
    drawRainbow(Offset(st.fx(0.45f, 0.08f), FARM_HZ * u), 0.46f * u, 0.016f * u, pen.rainbow)
    drawClouds(st, pen, 0.05f, 0.25f, 4, 0.06f, salt = 3)
    drawGulls(st, pen, 2, 0.12f, 0.3f, salt = 5)
    drawPeaks(
        st, 0.08f, 0.5f, 0.55f, 0.1f, 0.2f,
        Color(0xFFAFC4E4).atNight(n, 0.75f), Color(0xFF97AED6).atNight(n, 0.75f),
        Color(0xFFF1F6FF).atNight(n, 0.7f), Color(0xFFD2DEF2).atNight(n, 0.7f), 0.3f, 41,
    )
    // A glimpse of the fjord between the hills.
    val fp = 0.12f
    val span = st.span(fp)
    val fjord = Path().apply {
        moveTo(st.px(0.25f * span, fp), 0.505f * u)
        quadraticTo(st.px(0.5f * span, fp), 0.48f * u, st.px(0.8f * span, fp), 0.505f * u)
        quadraticTo(st.px(0.5f * span, fp), 0.52f * u, st.px(0.25f * span, fp), 0.505f * u)
        close()
    }
    drawPath(fjord, Color(0xFF6FB4E6).atNight(n, 0.7f))
    drawLine(Color.White, Offset(st.px(0.45f * span, fp), 0.502f * u), Offset(st.px(0.6f * span, fp), 0.502f * u), strokeWidth = pen.lw * 0.8f, alpha = 0.6f * (1f - n))
    drawForestRow(st, 0.2f, 0.545f, 0.03f, 43, 0.05f, 0.03f, 0.06f, pen.farTrees(Color(0xFF5E8F74)).atNight(n, 0.72f), null, skip = 0.45f, ground = pen.farGround(Color(0xFF8DBF8B)).atNight(n, 0.72f))
    farmFields(st, pen)
    val mp = 0.5f
    drawPath(ridgePath(st, mp, 0.712f, 0.008f, 47), pen.farGround(Color(0xFF93C96F)).atNight(n, 0.6f))
    val hx = 0.55f * st.span(mp)
    val hbx = st.px(hx, mp)
    if (hbx > -0.4f * u && hbx < st.w + 0.4f * u) {
        val hy = (ridgeY(hx, 0.712f, 0.008f, 47) + 0.006f) * u
        drawHouse3d(hbx + 0.2f * u, hy, 0.09f * u, 0.06f * u, 0.045f * u, 0.1f * u, Color(0xFFC0463A), Color(0xFF4A4A57), pen, n, outline = false, sideWindows = 0)
        drawHouse3d(hbx, hy, 0.17f * u, 0.12f * u, 0.07f * u, 0.2f * u, Color(0xFFF4F1EA), Color(0xFF4A4A57), pen, n, door = Color(0xFF3E6FA8), sideWindows = 3, chimney = true, t = t)
    }

    val fs = farmStatic.of(u)
    inScene(st) {
        drawPath(fs.ground, Brush.verticalGradient(0f to pen.ground(Color(0xFF8CC46A)).atNight(n, 0.5f), 1f to pen.ground(Color(0xFF6DAF55)).atNight(n, 0.45f), startY = YARD_BACK * u, endY = FRONT_Y * u))
        drawPath(fs.groundEdge, Ink.line, alpha = 0.6f, style = pen.thin)
        drawPath(fs.stripes, Color.White, alpha = 0.06f)
        val wood = Color(0xFFC9A27A).atNight(n, 0.45f)
        drawPath(fs.rails, wood)
        drawPath(fs.rails, Ink.line, style = pen.thin)
        drawPath(fs.posts, wood.darken(0.08f))
        drawPath(fs.posts, Ink.line, style = pen.thin)
    }
    if (st.sees(2.35f, 2.9f)) appleTree(st, pen, fs)
    if (st.sees(-0.1f, 1.8f)) barn(st, pen, fs)
    if (st.sees(3.25f, 4.1f)) shed(st, pen, fs)
    inScene(st) {
        val dirt = pen.sandy(Color(0xFFCFAA76)).atNight(n, 0.45f)
        drawPath(fs.tracks, dirt)
        drawPath(fs.ruts, dirt.darken(0.2f), style = Stroke(pen.lw * 1.4f, cap = StrokeCap.Round))
        drawPath(fs.tracks, dirt.darken(0.3f), alpha = 0.6f, style = pen.thin)
        drawPath(fs.tufts, pen.blade(Color(0xFF55A044)).atNight(n, 0.45f))
        if (!pen.winter) drawPoints(fs.flowers, PointMode.Points, Color(0xFFFFF6D8).atNight(n, 0.4f), strokeWidth = 0.008f * u, cap = StrokeCap.Round)
    }
    drawBase(st, pen, pen.ground(Color(0xFF6FAE5A)).atNight(n, 0.45f), Color(0xFF7A5438).atNight(n, 0.45f))
    drawBaseStones(st, pen, Color(0xFFA9A3A0).atNight(n, 0.45f), 49)
}

/** Patchwork fields on the far slope, each one a flat piece of ground seen in oblique. */
private fun DrawScope.farmFields(st: Stage, pen: Pen) {
    val u = st.u
    val n = pen.night
    val p = 0.34f
    val ridge = ridgePath(st, p, 0.64f, 0.06f, 45)
    drawPath(ridge, pen.farGround(Color(0xFFA5D06F)).atNight(n, 0.65f))
    val colors = listOf(Color(0xFFEBCB5E), Color(0xFFB5DC76), Color(0xFFB98B5A), Color(0xFF86C267))
    val patches = List(colors.size) { Path() }
    val rows = ArrayList<Offset>(80)
    val hedges = Path()
    val spacing = 0.42f
    val l0 = st.cam * p - 0.6f
    val l1 = st.cam * p + st.vw + 0.3f
    val yf = 0.712f
    val yb = 0.5f
    val s = (yf - yb) * RECEDE
    for (i in floor(l0 / spacing).toInt()..floor(l1 / spacing).toInt()) {
        val x0 = i * spacing
        val x1 = x0 + spacing * 0.96f
        val k = (hash01(i, 731) * colors.size).toInt().coerceIn(0, colors.size - 1)
        val quad = Path().apply {
            moveTo(st.px(x0 + s, p), yb * u)
            lineTo(st.px(x1 + s, p), yb * u)
            lineTo(st.px(x1, p), yf * u)
            lineTo(st.px(x0, p), yf * u)
            close()
        }
        patches[k].addPath(quad)
        hedges.addPath(quad)
        if (k != 3) {
            var rx = x0 + 0.04f
            while (rx < x1) {
                rows.add(Offset(st.px(rx, p), yf * u))
                rows.add(Offset(st.px(rx + s, p), yb * u))
                rx += 0.05f
            }
        }
    }
    clipPath(ridge) {
        for ((k, path) in patches.withIndex()) drawPath(path, pen.field(k, colors[k]).atNight(n, 0.65f))
        drawPoints(rows, PointMode.Lines, Color(0xFF6E5A36).atNight(n, 0.65f), strokeWidth = pen.lw * 0.5f, alpha = 0.35f)
        drawPath(hedges, pen.plant(Color(0xFF4F8A45)).atNight(n, 0.65f), style = Stroke(pen.lw * 1.6f, join = StrokeJoin.Round))
    }
}

private fun DrawScope.appleTree(st: Stage, pen: Pen, fs: FarmStatic) {
    val u = st.u
    val n = pen.night
    val trunk = Path().apply {
        poly(st.x(2.6f), 0.5f * u, st.x(2.645f), 0.5f * u, st.x(2.66f), 0.748f * u, st.x(2.585f), 0.748f * u)
    }
    drawPath(trunk, Color(0xFF8A5A3A).atNight(n, 0.45f))
    drawPath(trunk, Ink.line, style = pen.stroke)
    capsule(st.o(2.625f, 0.55f), st.o(2.54f, 0.47f), 0.012f * u, Color(0xFF8A5A3A).atNight(n, 0.45f), pen)
    if (pen.winter) {
        bareCrown(st.x(2.62f), 0.46f * u, 0.22f * u, 0.2f * u, pen, 733, n)
        return
    }
    val crown = crownPath(st.x(2.62f), 0.42f * u, 0.2f * u, 0.15f * u, 733)
    inked(crown, pen.foliage(Color(0xFF5DAE4E), 733).atNight(n, 0.45f), pen)
    if (pen.season == app.trollfoss.domain.Season.SPRING) {
        blossomDots(st.x(2.62f), 0.42f * u, 0.2f * u, 0.15f * u, 733, n, count = 30, size = 0.02f * u)
        return
    }
    val red = Color(0xFFE0463A).atNight(n, 0.35f)
    inScene(st) {
        drawPoints(fs.apples, PointMode.Points, red, strokeWidth = 0.022f * u, cap = StrokeCap.Round)
        drawPoints(fs.fallen, PointMode.Points, red, strokeWidth = 0.02f * u, cap = StrokeCap.Round)
        drawPoints(fs.apples, PointMode.Points, Color.White, strokeWidth = 0.006f * u, cap = StrokeCap.Round, alpha = 0.7f)
    }
}

/** The big red barn: a gambrel-roofed box in oblique 3D with its doors slid open on hay. */
private fun DrawScope.barn(st: Stage, pen: Pen, fs: FarmStatic) {
    val u = st.u
    val n = pen.night
    val red = Color(0xFFB8342B).atNight(n, 0.45f)
    val white = Color(0xFFF7F3EC).atNight(n, 0.45f)
    val roof = pen.snowy(Color(0xFF55505C)).atNight(n, 0.45f)
    val pts = FloatArray(fs.barnFront.size)
    for (i in fs.barnFront.indices step 2) {
        pts[i] = st.x(fs.barnFront[i])
        pts[i + 1] = fs.barnFront[i + 1] * u
    }
    val depth = 0.28f * u
    prism(pts, depth, red, pen) { i, _ -> if (i in 0..3) roof.lighten(if (i == 1) 0.12f else 0f) else red.darken(0.22f) }
    val front = Path().apply { poly(*pts) }
    clipPath(front) { inScene(st) { drawPoints(fs.planks, PointMode.Lines, red.darken(0.25f), strokeWidth = pen.lw * 0.6f) } }
    // A window on the long side.
    val vx = Oblique.DX * depth
    val vy = Oblique.DY * depth
    val sw = Path().apply {
        val x = st.x(1.2f)
        moveTo(x + vx * 0.35f, 0.5f * u + vy * 0.35f)
        lineTo(x + vx * 0.65f, 0.5f * u + vy * 0.65f)
        lineTo(x + vx * 0.65f, 0.6f * u + vy * 0.65f)
        lineTo(x + vx * 0.35f, 0.6f * u + vy * 0.35f)
        close()
    }
    drawPath(sw, Color(0xFF3B3040).atNight(n, 0.3f))
    drawPath(sw, white, style = Stroke(pen.lw * 1.6f))
    // Trim along the roof edge and the corners.
    val trim = Path().apply {
        moveTo(pts[0], pts[1])
        for (i in 1..4) lineTo(pts[i * 2], pts[i * 2 + 1])
    }
    drawPath(trim, white, style = Stroke(0.014f * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(trim, Ink.line, style = pen.thin)
    for (x in floatArrayOf(0f, 1.17f)) {
        drawRect(white, st.o(x, 0.38f), Size(0.03f * u, (PlaceId.FARM.back - 0.38f) * u))
        drawRect(Ink.line, st.o(x, 0.38f), Size(0.03f * u, (PlaceId.FARM.back - 0.38f) * u), style = pen.thin)
    }
    // The open doorway: dark inside with hay.
    val door = st.rect(0.3f, 0.45f, 0.9f, PlaceId.FARM.back)
    drawRect(Brush.verticalGradient(listOf(Color(0xFF231812), Color(0xFF3E2C20)), startY = door.top, endY = door.bottom), door.topLeft, door.size)
    val hay = Color(0xFFE2BE5C).atNight(n, 0.4f)
    inkedRound(st.rect(0.33f, 0.62f, 0.5f, 0.7f), 0.01f * u, hay.darken(0.12f), pen, shade = false)
    inkedRound(st.rect(0.33f, 0.7f, 0.5f, 0.78f), 0.01f * u, hay.darken(0.05f), pen, shade = false)
    val pile = Path().apply {
        moveTo(st.x(0.5f), PlaceId.FARM.back * u)
        quadraticTo(st.x(0.62f), 0.63f * u, st.x(0.76f), 0.68f * u)
        quadraticTo(st.x(0.86f), 0.7f * u, st.x(0.9f), PlaceId.FARM.back * u)
        close()
    }
    drawPath(pile, hay)
    drawPath(pile, Ink.line, style = pen.thin)
    val straws = ArrayList<Offset>(12)
    for (k in 0 until 6) {
        val sx = 0.56f + k * 0.055f
        straws.add(st.o(sx, 0.7f + (k % 2) * 0.02f))
        straws.add(st.o(sx + 0.02f, 0.69f + (k % 3) * 0.015f))
    }
    drawPoints(straws, PointMode.Lines, hay.darken(0.3f), strokeWidth = pen.lw * 0.7f, cap = StrokeCap.Round)
    drawRect(white, door.topLeft, door.size, style = Stroke(0.012f * u))
    drawRect(Ink.line, door.topLeft, door.size, style = pen.thin)
    // The sliding doors, pushed aside on their rail.
    drawLine(Color(0xFF3B3040), st.o(0.02f, 0.432f), st.o(1.18f, 0.432f), strokeWidth = 0.008f * u, cap = StrokeCap.Round)
    for ((l, r) in listOf(0.04f to 0.3f, 0.9f to 1.16f)) {
        val d = st.rect(l, 0.44f, r, PlaceId.FARM.back - 0.004f)
        drawRect(red.lighten(0.05f), d.topLeft, d.size)
        val x = Path().apply {
            moveTo(d.left, d.top)
            lineTo(d.right, d.bottom)
            moveTo(d.right, d.top)
            lineTo(d.left, d.bottom)
        }
        clipPath(Path().apply { addRect(d) }) { drawPath(x, white, style = Stroke(0.012f * u)) }
        drawRect(white, d.topLeft, d.size, style = Stroke(0.012f * u))
        drawRect(Ink.line, d.topLeft, d.size, style = pen.stroke)
    }
    // The hayloft.
    val loft = st.rect(0.5f, 0.2f, 0.7f, 0.33f)
    drawRect(Color(0xFF231812), loft.topLeft, loft.size)
    val tuft = Path().apply {
        moveTo(loft.left, loft.bottom)
        quadraticTo(loft.left + loft.width * 0.3f, loft.bottom - loft.height * 0.35f, loft.center.x, loft.bottom - loft.height * 0.2f)
        quadraticTo(loft.right - loft.width * 0.2f, loft.bottom - loft.height * 0.4f, loft.right, loft.bottom)
        close()
    }
    drawPath(tuft, hay)
    drawRect(white, loft.topLeft, loft.size, style = Stroke(0.012f * u))
    drawRect(Ink.line, loft.topLeft, loft.size, style = pen.thin)
    box3d(st.rect(0.575f, 0.13f, 0.625f, 0.16f), 0.05f * u, Color(0xFF6E4A33).atNight(n, 0.4f), pen)
}

/** The workshop: an open lean-to shed whose back wall carries the tool wall. */
private fun DrawScope.shed(st: Stage, pen: Pen, fs: FarmStatic) {
    val u = st.u
    val n = pen.night
    val back = PlaceId.FARM.back
    val wood = Color(0xFFB58B61).atNight(n, 0.45f)
    val wall = st.rect(3.42f, 0.2f, 4.0f, back)
    drawRect(Brush.verticalGradient(listOf(wood.darken(0.35f), wood, wood.darken(0.08f)), startY = wall.top, endY = wall.bottom), wall.topLeft, wall.size)
    inScene(st) { drawPoints(fs.shedPlanks, PointMode.Lines, wood.darken(0.3f), strokeWidth = pen.lw * 0.6f) }
    drawRect(Ink.line, wall.topLeft, wall.size, style = pen.stroke)
    val d = 0.16f
    val sx = -Oblique.DX * d
    val sy = -Oblique.DY * d
    val left = Path().apply {
        poly(st.x(3.42f + sx), (0.2f + sy) * u, st.x(3.42f), 0.2f * u, st.x(3.42f), back * u, st.x(3.42f + sx), (back + sy) * u)
    }
    drawPath(left, wood.darken(0.25f))
    drawPath(left, Ink.line, style = pen.stroke)
    val right = Path().apply {
        poly(st.x(4.0f + sx), (0.2f + sy) * u, st.x(4.0f), 0.2f * u, st.x(4.0f), back * u, st.x(4.0f + sx), (back + sy) * u)
    }
    drawPath(right, wood.darken(0.15f))
    drawPath(right, Ink.line, style = pen.stroke)
    val roofTop = Path().apply {
        poly(st.x(3.38f), 0.185f * u, st.x(4.06f), 0.185f * u, st.x(4.06f + sx), (0.185f + sy) * u, st.x(3.38f + sx), (0.185f + sy) * u)
    }
    drawPath(roofTop, pen.snowy(Color(0xFF6B6672)).atNight(n, 0.45f))
    drawPath(roofTop, Ink.line, style = pen.stroke)
    inkedRound(Rect(st.x(3.38f + sx), (0.185f + sy) * u, st.x(4.06f + sx), (0.205f + sy) * u), 0.003f * u, Color(0xFF55505C).atNight(n, 0.45f), pen, shade = false)
    for (x in floatArrayOf(3.42f + sx, 4.0f + sx)) {
        inkedRound(Rect(st.x(x) - 0.012f * u, (0.205f + sy) * u, st.x(x) + 0.012f * u, (back + sy) * u), 0.004f * u, Color(0xFF8A6443).atNight(n, 0.45f), pen, shade = false)
    }
}

internal fun DrawScope.farmFront(st: Stage, pen: Pen) {
    val u = st.u
    for ((i, x) in floatArrayOf(0.9f, 1.45f, 2.2f, 3.15f, 4.25f).withIndex()) {
        if (st.sees(x - 0.05f, x + 0.05f)) {
            drawTuft(st.x(x), (FRONT_Y + 0.004f) * u, 0.034f * u, Color(0xFF5FA34C).atNight(pen.night, 0.45f), pen, sin(pen.t * 1.2f + i * 2f) * 0.004f * u)
        }
    }
}

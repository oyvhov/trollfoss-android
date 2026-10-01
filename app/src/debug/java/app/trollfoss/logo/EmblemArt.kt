package app.trollfoss.logo

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import app.trollfoss.ui.art.Ink
import app.trollfoss.ui.art.darken
import app.trollfoss.ui.art.hash01
import app.trollfoss.ui.art.inked
import app.trollfoss.ui.art.inkedCircle
import app.trollfoss.ui.art.lighten
import app.trollfoss.ui.art.twinkle

/**
 * The emblem: Rumle in front of the great waterfall, a rainbow in the mist, snow-capped mountains and pines,
 * in a round badge with a thick ink ring. Drawn in a 1024 x 1024 box and scaled to the canvas.
 */
internal fun DrawScope.drawEmblem() {
    val k = size.minDimension / 1024f
    val ox = (size.width - 1024f * k) / 2f
    val oy = (size.height - 1024f * k) / 2f
    withTransform({
        translate(ox, oy)
        scale(k, k, pivot = Offset.Zero)
    }) { emblem() }
}

/** The emblem as a square of side [d] with its top left at ([left], [top]). */
internal fun DrawScope.drawEmblemIn(left: Float, top: Float, d: Float) {
    val k = d / 1024f
    withTransform({
        translate(left, top)
        scale(k, k, pivot = Offset.Zero)
    }) { emblem() }
}

private fun DrawScope.emblem() {
    val c = Offset(512f, 512f)
    val clip = Path().apply { addOval(Rect(c, 466f)) }
    clipPath(clip) { emblemScene() }
    // A warm gold band, then the thick ink ring.
    drawCircle(L.sun, 464f, c, style = Stroke(12f))
    drawArc(L.sunDeep, -5f, 110f, false, Offset(c.x - 464f, c.y - 464f), Size(928f, 928f), style = Stroke(12f))
    drawArc(L.sunTop, 190f, 80f, false, Offset(c.x - 464f, c.y - 464f), Size(928f, 928f), style = Stroke(12f))
    drawCircle(Ink.line, 488f, c, style = Stroke(36f))
}

private val rockBody = Color(0xFF627A98)
private val rockDark = Color(0xFF50667F)
private val mossGreen = Color(0xFF6FAE5A)

private fun DrawScope.emblemScene() {
    val lw = 10f
    val pen = logoPen(lw)

    // ---- sky, sun, clouds
    drawRect(
        Brush.verticalGradient(
            0f to Color(0xFF2E8DE5), 0.3f to Color(0xFF58B4F3), 0.55f to Color(0xFFA6DCFB), 0.8f to Color(0xFFFFEEC6),
            startY = 40f, endY = 700f,
        ),
        Offset(0f, 0f), Size(1024f, 1024f),
    )
    val sunC = Offset(262f, 228f)
    lgGlow(sunC, 250f, Color(0xFFFFE08A), 0.6f)
    for (i in 0 until 12) {
        val a = i * 30f + 8f
        drawLine(L.sunTop, polar(sunC, 74f, a), polar(sunC, if (i % 2 == 0) 100f else 90f, a), strokeWidth = 11f, cap = StrokeCap.Round)
    }
    inkedCircle(sunC, 54f, L.sun, pen)
    drawCircle(Color.White.copy(alpha = 0.55f), 13f, Offset(sunC.x - 18f, sunC.y - 20f))
    lgCloud(812f, 214f, 40f, lw)
    lgCloud(396f, 128f, 24f, lw)

    // ---- the mountains
    val blue = Color(0xFF93AAD8)
    val blueShade = Color(0xFF7890C4)
    val line = Color(0xFF3B4A8A)
    lgMountain(226f, 372f, 250f, 230f, 720f, blue, blueShade, L.snow, L.snowShade, 0.3f, lw * 0.8f, 3, outline = 0.55f, outlineColor = line)
    lgMountain(824f, 340f, 240f, 260f, 720f, blue, blueShade, L.snow, L.snowShade, 0.32f, lw * 0.8f, 5, outline = 0.55f, outlineColor = line)
    lgMountain(514f, 84f, 340f, 350f, 720f, Color(0xFF8AA2D2), Color(0xFF6E88BE), L.snow, L.snowShade, 0.4f, lw, 8, outline = 0.8f, outlineColor = line)

    // ---- two rock shoulders with snow on top; the falls pour between them
    val wallBody = Color(0xFF5F76A6)
    val prof = floatArrayOf(32f, 270f, 64f, 252f, 104f, 226f, 148f, 240f, 178f, 292f, 200f, 420f, 216f, 600f, 210f, 740f, 12f, 740f)
    for (i in 0 until 2) {
        fun xd(dx: Float) = if (i == 0) 512f - dx else 512f + dx
        val wall = Path().apply {
            moveTo(xd(prof[0]), prof[1])
            var k = 2
            while (k < prof.size) {
                lineTo(xd(prof[k]), prof[k + 1])
                k += 2
            }
            close()
        }
        inked(wall, wallBody, pen)
        clipPath(wall) {
            val snow = polyPath(
                xd(270f), 190f, xd(0f), 190f, xd(0f), 290f, xd(30f), 292f, xd(58f), 268f, xd(84f), 292f, xd(104f), 262f,
                xd(128f), 304f, xd(150f), 284f, xd(182f), 322f, xd(270f), 330f,
            )
            drawPath(snow, L.snow)
            if (i == 0) {
                drawPath(polyPath(xd(70f), 190f, xd(0f), 190f, xd(0f), 290f, xd(30f), 292f, xd(60f), 268f, xd(70f), 270f), L.snowShade)
                drawPath(polyPath(xd(14f), 380f, xd(60f), 400f, xd(60f), 760f, xd(14f), 760f), wallBody.darken(0.16f))
            } else {
                drawPath(polyPath(xd(104f), 190f, xd(270f), 190f, xd(270f), 330f, xd(182f), 322f, xd(150f), 284f, xd(128f), 304f, xd(104f), 262f), L.snowShade)
            }
            drawLine(wallBody.lighten(0.25f), Offset(xd(196f), 470f), Offset(xd(160f), 460f), strokeWidth = 8f, cap = StrokeCap.Round)
            drawLine(wallBody.lighten(0.25f), Offset(xd(206f), 560f), Offset(xd(166f), 552f), strokeWidth = 8f, cap = StrokeCap.Round)
        }
        drawPath(wall, Ink.line, style = pen.stroke)
    }

    // ---- the rainbow, big and soft behind the falls
    lgRainbow(Offset(512f, 706f), 350f, 16f, 0.93f)

    // ---- forest hills on both sides of the falls
    val hillL = Path().apply {
        moveTo(-30f, 540f)
        quadraticTo(110f, 440f, 250f, 478f)
        quadraticTo(350f, 504f, 420f, 580f)
        lineTo(440f, 760f)
        lineTo(-30f, 760f)
        close()
    }
    inked(hillL, Color(0xFF3F9A5C), pen)
    val hillR = Path().apply {
        moveTo(1054f, 540f)
        quadraticTo(914f, 440f, 774f, 478f)
        quadraticTo(674f, 504f, 604f, 580f)
        lineTo(584f, 760f)
        lineTo(1054f, 760f)
        close()
    }
    inked(hillR, Color(0xFF3F9A5C), pen)
    lgPine(90f, 560f, 60f, 116f, L.gran, lw)
    lgPine(160f, 520f, 54f, 104f, L.granLight, lw)
    lgPine(236f, 540f, 62f, 120f, L.gran, lw)
    lgPine(322f, 570f, 46f, 86f, L.granLight, lw)
    lgPine(940f, 560f, 60f, 116f, L.gran, lw)
    lgPine(868f, 520f, 54f, 104f, L.granLight, lw)
    lgPine(790f, 540f, 62f, 120f, L.gran, lw)
    lgPine(704f, 572f, 46f, 86f, L.granLight, lw)

    // Mist where the rainbow comes down behind the hills.
    lgPuffs(listOf(Triple(330f, 486f, 30f), Triple(296f, 506f, 24f), Triple(362f, 506f, 26f), Triple(694f, 486f, 30f), Triple(728f, 506f, 24f), Triple(662f, 506f, 26f)), Color.White, 0.9f)

    // ---- the waterfall: a wide bright plume pouring out of the gorge
    val fall = Path().apply {
        moveTo(462f, 262f)
        quadraticTo(512f, 274f, 562f, 262f)
        quadraticTo(556f, 500f, 664f, 730f)
        lineTo(364f, 730f)
        quadraticTo(468f, 500f, 462f, 262f)
        close()
    }
    drawPath(fall, Brush.verticalGradient(0f to Color(0xFFD6F0FF), 0.3f to Color(0xFFFFFFFF), 1f to Color(0xFFD2EEFF), startY = 262f, endY = 730f))
    clipPath(fall) {
        drawPath(polyPath(522f, 240f, 700f, 240f, 700f, 740f, 566f, 740f), Color(0xFFB9E0F7))
        for ((i, x) in listOf(486f, 504f, 526f, 546f).withIndex()) {
            drawLine(Color(0xFF8CCBF3), Offset(x, 300f + i * 26f), Offset(x + (x - 512f) * 1.5f, 620f), strokeWidth = 9f, cap = StrokeCap.Round)
        }
        drawLine(Color.White, Offset(480f, 296f), Offset(468f, 430f), strokeWidth = 9f, cap = StrokeCap.Round)
        drawLine(Color.White, Offset(500f, 330f), Offset(494f, 440f), strokeWidth = 6f, cap = StrokeCap.Round)
        // A ledge where the water tumbles.
        drawArc(Color.White, 190f, 160f, false, Offset(402f, 436f), Size(220f, 40f), style = Stroke(11f, cap = StrokeCap.Round))
        drawArc(Ink.line, 190f, 160f, false, Offset(402f, 446f), Size(220f, 40f), alpha = 0.3f, style = Stroke(4f, cap = StrokeCap.Round))
    }
    drawPath(
        Path().apply {
            moveTo(462f, 262f)
            quadraticTo(468f, 500f, 364f, 730f)
            moveTo(562f, 262f)
            quadraticTo(556f, 500f, 664f, 730f)
        },
        Ink.line, style = Stroke(lw, cap = StrokeCap.Round),
    )
    // The brink, where the water tips over.
    drawPath(Path().apply { moveTo(462f, 262f); quadraticTo(512f, 274f, 562f, 262f) }, Ink.line, style = Stroke(lw, cap = StrokeCap.Round))

    // ---- the pool, mist and foam
    val pool = Path().apply {
        moveTo(-30f, 726f)
        quadraticTo(512f, 676f, 1054f, 726f)
        lineTo(1054f, 880f)
        lineTo(-30f, 880f)
        close()
    }
    drawPath(pool, Brush.verticalGradient(0f to Color(0xFF63D3DC), 1f to Color(0xFF2394B4), startY = 690f, endY = 880f))
    drawPath(Path().apply { moveTo(-30f, 726f); quadraticTo(512f, 676f, 1054f, 726f) }, Ink.line, style = Stroke(lw, cap = StrokeCap.Round))
    for ((x, y, w) in listOf(Triple(90f, 770f, 70f), Triple(200f, 800f, 54f), Triple(790f, 780f, 76f), Triple(900f, 812f, 50f), Triple(40f, 826f, 44f), Triple(840f, 836f, 60f))) {
        drawLine(Color.White.copy(alpha = 0.7f), Offset(x, y), Offset(x + w, y), strokeWidth = 7f, cap = StrokeCap.Round)
    }
    for ((i, x) in listOf(396f, 446f, 496f, 546f, 596f, 646f).withIndex()) {
        val r = 30f - abs2(i - 2) * 1.5f
        val y = 718f - (if (i % 2 == 0) 0f else 12f)
        drawCircle(Color(0xFFF2FAFF), r, Offset(x, y))
        drawCircle(Ink.line, r, Offset(x, y), style = Stroke(lw * 0.7f))
    }
    emblemHouse(Offset(196f, 806f), 60f, pen)

    // ---- the lawn
    val lawnTop = Path().apply {
        moveTo(-30f, 884f)
        quadraticTo(120f, 826f, 290f, 858f)
        lineTo(734f, 858f)
        quadraticTo(904f, 826f, 1054f, 884f)
    }
    val lawn = Path().apply {
        addPath(lawnTop)
        lineTo(1054f, 1044f)
        lineTo(-30f, 1044f)
        close()
    }
    drawPath(lawn, Brush.verticalGradient(0f to Color(0xFF7FC05F), 1f to Color(0xFF4E9A48), startY = 820f, endY = 1000f))
    drawPath(lawnTop, Ink.line, style = Stroke(lw, cap = StrokeCap.Round, join = StrokeJoin.Round))
    val petal = listOf(Color.White, L.sun, L.berry, Color.White, L.sunTop, L.berryTop)
    val spots = listOf(
        Offset(118f, 906f), Offset(214f, 950f), Offset(284f, 892f),
        Offset(918f, 908f), Offset(822f, 950f), Offset(750f, 892f),
    )
    for ((i, sp) in spots.withIndex()) {
        val col = petal[i % petal.size]
        drawCircle(Ink.line, 17f, sp)
        for (a in 0 until 5) drawCircle(col, 9f, polar(sp, 10f, a * 72f + 18f))
        drawCircle(if (col == L.sun) L.berry else L.sun, 6.5f, sp)
    }

    // ---- Rumle
    drawRumleBust(Offset(498f, 742f), 174f, 13f)

    // ---- glints
    twinkle(Offset(884f, 400f), 22f, Color.White)
    twinkle(Offset(130f, 450f), 16f, Color.White)
    twinkle(Offset(660f, 190f), 14f, Color.White, 0.9f)
}

private fun abs2(v: Int) = if (v < 0) -v else v

private fun DrawScope.emblemHouse(base: Offset, w: Float, pen: app.trollfoss.ui.art.Pen) {
    val h = w / 2f
    val wallH = w * 0.46f
    val roofH = w * 0.4f
    val wall = L.falun
    val front = Path().apply { addRect(Rect(base.x - h, base.y - wallH, base.x + h, base.y)) }
    val side = polyPath(base.x + h, base.y - wallH, base.x + h + w * 0.5f, base.y - wallH - w * 0.18f, base.x + h + w * 0.5f, base.y - w * 0.18f, base.x + h, base.y)
    drawPath(side, wall.darken(0.2f))
    drawPath(side, Ink.line, style = pen.stroke)
    inked(front, wall, pen)
    val roof = polyPath(base.x - h * 1.12f, base.y - wallH + 3f, base.x, base.y - wallH - roofH, base.x + h * 1.12f, base.y - wallH + 3f)
    val roofSide = polyPath(base.x, base.y - wallH - roofH, base.x + h * 1.12f, base.y - wallH + 3f, base.x + h * 1.12f + w * 0.5f, base.y - wallH - w * 0.18f + 3f, base.x + w * 0.5f, base.y - wallH - roofH - w * 0.18f)
    drawPath(roofSide, Color(0xFF3F3B4A).lighten(0.1f))
    drawPath(roofSide, Ink.line, style = pen.stroke)
    inked(roof, Color(0xFF4B4756), pen)
    drawRect(Color(0xFFFFE9A8), Offset(base.x - w * 0.28f, base.y - wallH * 0.78f), Size(w * 0.24f, w * 0.24f))
    drawRect(Ink.line, Offset(base.x - w * 0.28f, base.y - wallH * 0.78f), Size(w * 0.24f, w * 0.24f), style = Stroke(pen.lw * 0.7f))
    drawRect(L.woodDark, Offset(base.x + w * 0.08f, base.y - wallH * 0.6f), Size(w * 0.22f, wallH * 0.6f))
    drawRect(Ink.line, Offset(base.x + w * 0.08f, base.y - wallH * 0.6f), Size(w * 0.22f, wallH * 0.6f), style = Stroke(pen.lw * 0.7f))
}

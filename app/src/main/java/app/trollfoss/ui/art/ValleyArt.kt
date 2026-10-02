package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.FixtureType
import app.trollfoss.domain.PlaceId
import app.trollfoss.domain.Vagstaddalen
import kotlin.math.sin

/** The photo's grey cliff and birch meadow, with the winding river below the horizon. */
private val valleyRiver = Memo { u ->
    Path().apply {
        moveTo(3.43f * u, 0.70f * u)
        cubicTo(3.56f * u, 0.73f * u, 2.82f * u, 0.74f * u, 3.06f * u, 0.77f * u)
        cubicTo(3.25f * u, 0.79f * u, 2.8f * u, 0.80f * u, Vagstaddalen.RIVER_LEFT * u, 0.83f * u)
        lineTo(Vagstaddalen.RIVER_LEFT * u, 0.97f * u)
        lineTo(Vagstaddalen.RIVER_RIGHT * u, 0.97f * u)
        lineTo(Vagstaddalen.RIVER_RIGHT * u, 0.83f * u)
        cubicTo(3.7f * u, 0.80f * u, 3.46f * u, 0.79f * u, 3.34f * u, 0.76f * u)
        cubicTo(3.03f * u, 0.74f * u, 3.69f * u, 0.73f * u, 3.47f * u, 0.70f * u)
        close()
    }
}
private val valleyCoverLeft = GroundCover(0.06f, 2.7f, 0.8f, 0.96f, 2051, 0.65f)
private val valleyCoverRight = GroundCover(3.97f, 5.35f, 0.8f, 0.96f, 2052, 0.65f)

internal fun DrawScope.valleyBack(st: Stage, pen: Pen) {
    val u = st.u
    val n = pen.night
    drawSky(st, pen, Mood.SUMMER, 0.7f)
    drawStars(st, pen, 0.5f)
    drawSun(Offset(st.fx(0.22f, 0.04f), 0.13f * u), 0.05f * u, pen, (1f - n) * (1f - overcast(pen)))
    drawMoon(Offset(st.fx(0.22f, 0.04f), 0.13f * u), 0.04f * u, pen, n)
    drawClouds(st, pen, 0.04f, 0.22f, 3, 0.06f, salt = 51)
    inScene(st) {
        val cliff = Path().apply {
            moveTo(-0.2f * u, 0.7f * u); lineTo(0.1f * u, 0.51f * u)
            lineTo(0.65f * u, 0.31f * u); lineTo(1.4f * u, 0.26f * u)
            lineTo(2.1f * u, 0.29f * u); lineTo(2.8f * u, 0.25f * u)
            lineTo(3.9f * u, 0.30f * u); lineTo(4.7f * u, 0.4f * u)
            lineTo(5.6f * u, 0.7f * u); close()
        }
        inked(cliff, Color(0xFFADB0AA).atNight(n, 0.6f), pen, shade = false)
        clipPath(cliff) {
            for (k in 0..25) {
                val x = -0.2f + k * 0.23f + hash01(k, 2062) * 0.12f
                val y = 0.29f + hash01(k, 2060) * 0.18f
                val lean = -0.035f + hash01(k, 2063) * 0.09f
                val length = 0.08f + hash01(k, 2064) * 0.13f
                val crack = Path().apply {
                    moveTo(x * u, y * u)
                    lineTo((x + lean) * u, (y + length * 0.4f) * u)
                    lineTo((x + lean - 0.02f) * u, (y + length * 0.7f) * u)
                    lineTo((x + lean * 1.5f) * u, (y + length) * u)
                }
                drawPath(crack, Color(0xFF747B79).atNight(n, 0.6f), style = Stroke(0.004f * u))
                if (k % 2 == 0) drawLine(Color(0xFFC5C8BE).atNight(n, 0.6f), Offset((x + 0.016f) * u, y * u), Offset((x + lean + 0.016f) * u, (y + length) * u), 0.008f * u)
                val ledgeY = 0.43f + hash01(k, 2061) * 0.2f
                drawLine(Color(0xFF8D9490).atNight(n, 0.6f), Offset((x - 0.09f) * u, ledgeY * u), Offset((x + 0.07f) * u, (ledgeY - 0.009f) * u), 0.005f * u)
                if (k % 3 == 0) inkedOval(Rect((x - 0.13f) * u, 0.60f * u, (x + 0.13f) * u, 0.69f * u), pen.ground(Pal.grass).atNight(n, 0.5f), pen, shade = false)
            }
        }
    }
    inScene(st) {
        for (k in 0..14) {
            val x = k * 0.39f + hash01(k, 2065) * 0.15f
            if (st.sees(x - 0.12f, x + 0.12f)) drawBirch(x * u, 0.71f * u,
                (0.12f + hash01(k, 2066) * 0.07f) * u, pen, Color(0xFF83AE72), 2090 + k, n)
        }
    }
    val grass = pen.ground(Color(0xFF83B86A)).atNight(n, 0.5f)
    drawRect(Brush.verticalGradient(listOf(grass.lighten(0.12f), grass, grass.darken(0.12f)), startY = 0.7f * u, endY = u), Offset(0f, 0.7f * u), Size(st.w, 0.3f * u))
    drawBase(st, pen, Pal.woodDark, Color(0xFF5E4636))
    inScene(st) {
        val river = valleyRiver.of(u)
        drawPath(river, pen.ground(Color(0xFF4B8C54)).atNight(n, 0.6f), style = Stroke(0.026f * u, cap = StrokeCap.Round))
        drawPath(river, Brush.verticalGradient(listOf(Color(0xFFB8E6EA).atNight(n, 0.4f), Color(0xFF75C8D8).atNight(n, 0.4f), Color(0xFF398FAE).atNight(n, 0.4f)), startY = 0.70f * u, endY = u))
        drawPath(river, Ink.line, alpha = 0.5f, style = pen.thin)
        clipPath(river) {
            for (k in 0 until 18) {
                val y = 0.71f + k * 0.015f
                val x = 3.3f + sin(y * 24f) * 0.2f
                val drift = sin(pen.t * 1.7f + k) * 0.04f
                drawLine(Color.White, Offset((x + drift - 0.11f) * u, y * u), Offset((x + drift + 0.11f) * u, y * u), pen.lw, cap = StrokeCap.Round, alpha = 0.5f)
            }
        }
        // Pebbles beside the water; their fixed positions leave the playable banks open.
        for (k in 0 until 8) {
            val x = if (k % 2 == 0) 2.74f else 3.91f
            val y = 0.82f + k / 2 * 0.04f
            inkedOval(Rect((x - 0.035f) * u, y * u, (x + 0.035f) * u, (y + 0.025f) * u), Color(0xFFAAB8B6).atNight(n, 0.5f), pen)
        }
    }
    drawWaterPlane(st, pen, Vagstaddalen.RIVER_LEFT, Vagstaddalen.RIVER_RIGHT, 0.78f, Vagstaddalen.WATERLINE, Color.White, 4, 1f, 1f, 0.45f)
    valleyCoverLeft.draw(this, st, pen)
    valleyCoverRight.draw(this, st, pen)
    // Birch trees frame the valley without covering the cabin or the fishing bank.
    for (x in floatArrayOf(0.12f, 2.35f, 4.65f, 5.3f)) if (st.sees(x - 0.2f, x + 0.2f)) {
        drawBirch(st.x(x), 0.76f * u, 0.38f * u, pen, Color(0xFF8DB85E), 2100 + (x * 10).toInt(), n)
    }
}

internal fun DrawScope.valleyFront(st: Stage, pen: Pen) {
    drawWaterFront(st, pen, Vagstaddalen.RIVER_LEFT, Vagstaddalen.RIVER_RIGHT, Vagstaddalen.WATERLINE, Vagstaddalen.BED, Color(0xFF8BD8E5), Color(0xFF3A91B5))
}

internal fun DrawScope.drawMapValley(cx: Float, base: Float, s: Float, pen: Pen) {
    val river = Path().apply {
        moveTo(cx + s * 0.3f, base - s * 1.2f)
        cubicTo(cx + s * 1.2f, base - s * 0.8f, cx + s * 0.1f, base - s * 0.4f, cx + s * 0.7f, base + s * 0.13f)
    }
    drawPath(river, Pal.fjord.atNight(pen.night, 0.4f), style = Stroke(s * 0.18f, cap = StrokeCap.Round))
    drawPath(river, Color(0xFF91DBE9).atNight(pen.night, 0.3f), style = Stroke(s * 0.09f, cap = StrokeCap.Round))
    drawOval(pen.ground(Pal.grass).atNight(pen.night, 0.5f), Offset(cx - s * 0.95f, base - s * 0.13f), Size(s * 1.5f, s * 0.28f))
    translate(cx - s * 0.3f, base) {
        drawFixtureBack(Fixture(0, PlaceId.VAGSTADDALEN, FixtureType.MOUNTAIN_HUT, 0f, 0f), s, Pen(pen.lw, 0f, pen.night, pen.weather))
    }
}

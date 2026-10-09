package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import app.trollfoss.domain.Look

/** Three reusable cuts. The roots follow the head; only the crown volume and free ends stretch. */
internal fun DrawScope.portraitHair(look: Look, back: Boolean, color: Color, c: Offset, r: Float, pen: Pen) {
    val fit = HairFit(look)
    val full = 1f + (look.hairSize - 1f) * .4f
    val end = .75f + (look.hairLength - .65f) * 1.65f
    val wavy = look.hair == 20
    val short = look.hair == 21
    fun p(x: Float, y: Float) = Offset(c.x + x * r * fit.headWidth, c.y + y * r)
    fun Path.move(x: Float, y: Float) { val q = p(x, y); moveTo(q.x, q.y) }
    fun Path.line(x: Float, y: Float) { val q = p(x, y); lineTo(q.x, q.y) }
    fun Path.curve(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
        val a = p(x1, y1); val b = p(x2, y2); val d = p(x3, y3)
        cubicTo(a.x, a.y, b.x, b.y, d.x, d.y)
    }
    fun strand(path: Path, light: Boolean = false) = drawPath(path,
        if (light) color.lighten(.32f) else color.darken(.22f),
        style = Stroke(pen.lw * .72f, cap = StrokeCap.Round))

    if (short) {
        if (back) {
            val sideEnd = -.02f + (look.hairLength - .65f) * .22f
            for (s in floatArrayOf(-1f, 1f)) capsule(p(s * .93f, -.43f), p(s * .99f, sideEnd), r * .16f, color.darken(.1f), pen)
            return
        }
        val crown = 1.02f + (look.hairSize - .8f) * .4f + (look.hairLength - .65f) * .16f
        val crop = Path().apply {
            move(-1.01f, -.16f)
            curve(-1.13f, -.65f, -.98f, -crown, -.63f, -crown)
            curve(-.62f, -crown - .10f, -.41f, -crown - .14f, -.28f, -crown - .04f)
            curve(-.14f, -crown - .17f, .03f, -crown - .11f, .12f, -crown - .03f)
            curve(.27f, -crown - .12f, .41f, -crown - .1f, .5f, -crown + .01f)
            curve(.94f, -crown + .02f, 1.12f, -.73f, 1.01f, -.16f)
            line(.85f, -.25f)
            curve(.78f, -.45f, .89f, -.59f, .72f, -.68f)
            curve(.45f, -.58f, .15f, -.74f, -.06f, -.64f)
            curve(-.28f, -.74f, -.6f, -.56f, -.79f, -.65f)
            curve(-.92f, -.51f, -.79f, -.31f, -1.01f, -.16f); close()
        }
        inked(crop, color, pen)
        for (i in -3..3) {
            val x = i * .23f
            strand(Path().apply { move(x - .06f, -crown + .18f); curve(x + .03f, -crown + .08f, x + .17f, -crown + .16f, x + .12f, -.79f) }, light = i % 2 == 0)
        }
        // Grey at the temples stays subtle and works with every workshop hair colour.
        for (s in floatArrayOf(-1f, 1f)) strand(Path().apply {
            move(s * .93f, -.6f); curve(s * .99f, -.5f, s * .91f, -.39f, s * .94f, -.3f)
        }, light = true)
        return
    }

    if (back) {
        val shell = Path().apply {
            move(-.88f, -.83f)
            if (wavy) {
                curve(-1.35f * full, -.63f, -1.12f * full, -.18f, -1.25f * full, .1f)
                curve(-1.45f * full, .3f, -1.14f * full, .47f, -1.24f * full, .65f)
                curve(-1.50f * full, end - .34f, -1.11f * full, end - .19f, -1.31f * full, end - .08f)
                curve(-1.14f, end + .2f, -.87f, end + .14f, -.77f, end - .08f)
            } else {
                curve(-1.23f * full, -.45f, -1.12f * full, .15f, -1.17f * full, .52f)
                curve(-1.19f * full, end - .55f, -1.38f * full, end - .12f, -1.13f * full, end)
                curve(-.98f, end + .10f, -.76f, end - .03f, -.73f, end - .15f)
            }
            curve(-.18f, end + .05f, .3f, end + .05f, .78f, end - .1f)
            if (wavy) {
                curve(1.02f, end + .13f, 1.34f * full, end + .03f, 1.26f * full, end - .18f)
                curve(1.54f * full, end - .37f, 1.13f * full, end - .51f, 1.28f * full, .57f)
                curve(1.48f * full, .3f, 1.18f * full, .18f, 1.28f * full, -.1f)
                curve(1.42f * full, -.38f, 1.15f * full, -.7f, .83f, -.89f)
            } else {
                curve(.95f, end + .12f, 1.25f * full, end + .01f, 1.2f * full, end - .18f)
                curve(1.1f * full, end - .46f, 1.17f * full, .89f, 1.16f * full, .52f)
                curve(1.22f * full, -.10f, 1.21f * full, -.57f, .83f, -.89f)
            }
            curve(.38f, -1.26f, -.48f, -1.31f, -.88f, -.83f); close()
        }
        inked(shell, color.darken(.08f), pen)
        for (s in floatArrayOf(-1f, 1f)) for (k in 0..1) {
            val x = s * (1.04f + k * .1f) * full
            strand(Path().apply {
                move(x, -.34f)
                if (wavy) {
                    curve(x - s * .19f, .05f, x + s * .22f, .27f, x, .57f)
                    curve(x - s * .19f, end - .43f, x + s * .19f, end - .30f, x - s * .07f, end - .12f)
                } else curve(x - s * .10f, .28f, x - s * .08f, end - .56f, x - s * .07f, end - .14f)
            }, light = k == 0)
        }
        return
    }

    val crown = 1.08f + (look.hairSize - .8f) * .25f
    val fringe = Path().apply {
        move(-1.04f, .05f)
        if (wavy) {
            curve(-1.26f * full, -.19f, -1.05f * full, -.48f, -1.15f * full, -.67f)
            curve(-1.17f * full, -.93f, -.93f, -1.01f, -.72f, -1.04f)
            curve(-.66f, -crown - .07f, -.37f, -crown - .08f, -.2f, -crown)
            curve(.10f, -crown - .06f, .22f, -1.16f, .44f, -1.07f)
            curve(.73f, -1.19f, 1.03f * full, -.91f, 1.01f * full, -.67f)
            curve(1.25f * full, -.59f, 1.24f * full, -.22f, 1.09f * full, .14f)
        } else {
            curve(-1.14f * full, -.28f, -1.15f * full, -.79f, -.72f, -1.04f)
            curve(-.3f, -crown - .05f, .20f, -crown - .07f, .6f, -1.01f)
            curve(1.04f * full, -.83f, 1.17f * full, -.36f, 1.09f * full, .14f)
        }
        curve(.88f, .05f, .91f, -.27f, .82f, -.45f)
        // Side part: a broad swept lock, leaving both eyebrows clear.
        curve(.52f, -.35f, .04f, -.68f, -.26f, -.91f)
        curve(-.54f, -.79f, -.56f, -.50f, -.72f, -.31f)
        curve(-.86f, -.15f, -.85f, .02f, -1.04f, .05f); close()
    }
    inked(fringe, color, pen)
    strand(Path().apply { move(-.26f, -crown + .05f); curve(-.31f, -1.01f, -.25f, -.91f, -.2f, -.84f) })
    for (k in 0..2) strand(Path().apply {
        move(-.13f + k * .13f, -1.02f)
        curve(.17f + k * .13f, -.89f, .47f + k * .14f, -.80f, .79f + k * .05f, -.56f + k * .05f)
    }, light = k == 1)
    strand(Path().apply { move(-.46f, -.98f); curve(-.85f, -.74f, -.72f, -.43f, -.97f, -.24f) }, light = true)
}

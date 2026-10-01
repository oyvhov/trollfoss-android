package app.trollfoss.logo

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import app.trollfoss.domain.Look
import app.trollfoss.domain.PersonAnim
import app.trollfoss.domain.Pose
import app.trollfoss.domain.Species
import app.trollfoss.ui.art.Ink
import app.trollfoss.ui.art.Pen
import app.trollfoss.ui.art.blobPath
import app.trollfoss.ui.art.darken
import app.trollfoss.ui.art.drawPerson
import app.trollfoss.ui.art.groundShadow
import app.trollfoss.ui.art.inked
import app.trollfoss.ui.art.inkedCircle
import app.trollfoss.ui.art.inkedOval
import app.trollfoss.ui.art.inkedRound
import app.trollfoss.ui.art.lighten
import app.trollfoss.ui.art.rect

/** Scene units to pixels for the cast: a grown-up troll (0.34 units) is about 310 px tall. */
internal const val CAST_UNIT = 900f

private val rumleLook = Look(skin = 13, height = 1.14f, hair = 3, hairColor = 1, ears = 4, top = 4, topColor = 12, bottom = 0, bottomColor = 12, shoes = 12, extra = 0)
private val heddaLook = Look(skin = 1, height = 0.78f, hair = 3, hairColor = 5, top = 1, topColor = 5, bottom = 1, bottomColor = 11, shoes = 0, extra = 1)
private val alvaLook = Look(skin = 3, height = 0.78f, hair = 5, hairColor = 3, eyes = 1, top = 3, topColor = 8, bottom = 2, bottomColor = 6, shoes = 5)
private val oyvindLook = Look(skin = 2, height = 1.14f, hair = 1, hairColor = 1, top = 1, topColor = 6, bottom = 0, bottomColor = 11, shoes = 12, extra = 2)

internal enum class Cast { RUMLE, OYVIND, HEDDA, ALVA, CAT, DOG, PUFFIN, ELK, GOAT }

/** The height of a cast member in pixels at [unit] pixels per scene unit. */
internal fun castHeight(who: Cast, unit: Float): Float = when (who) {
    Cast.RUMLE -> Species.FOLK.height * rumleLook.height * unit * 1.08f
    Cast.OYVIND -> Species.FOLK.height * oyvindLook.height * unit
    Cast.HEDDA -> Species.FOLK.height * heddaLook.height * unit
    Cast.ALVA -> Species.FOLK.height * alvaLook.height * unit
    Cast.CAT -> Species.CAT.height * unit
    Cast.DOG -> Species.DOG.height * unit
    Cast.PUFFIN -> Species.PUFFIN.height * unit
    Cast.ELK -> Species.ELK.height * unit
    Cast.GOAT -> 0.16f * unit
}

/** Draws [who] with the feet at ([x], [feetY]), using the game's own figure drawing. */
internal fun DrawScope.drawCastMember(who: Cast, x: Float, feetY: Float, unit: Float, lw: Float, seed: Float = 0f) {
    val h = castHeight(who, unit)
    val pen = logoPen(lw)
    val anim = PersonAnim()
    groundShadow(x, feetY + h * 0.012f, h * (if (who == Cast.GOAT) 0.8f else 0.7f), 0.9f)
    translate(x, feetY) {
        when (who) {
            Cast.RUMLE -> {
                anim.wave = 1f
                drawPerson(Species.FOLK, rumleLook, Pose.STAND, anim, h, pen, holding = true, seed = seed)
                drawRumleWave(Offset.Zero, h, lw, mirror = true)
                drawRumleAccents(Offset.Zero, h, lw, grin = false)
            }
            Cast.OYVIND -> drawPerson(Species.FOLK, oyvindLook, Pose.STAND, anim, h, pen, seed = seed)
            Cast.HEDDA -> drawPerson(Species.FOLK, heddaLook, Pose.STAND, anim, h, pen, seed = seed)
            Cast.ALVA -> drawPerson(Species.FOLK, alvaLook, Pose.STAND, anim, h, pen, seed = seed)
            Cast.CAT -> drawPerson(Species.CAT, Look(skin = 0), Pose.STAND, anim, h, pen, seed = seed)
            Cast.DOG -> drawPerson(Species.DOG, Look(skin = 4), Pose.STAND, anim, h, pen, seed = seed)
            Cast.PUFFIN -> drawPerson(Species.PUFFIN, Look(skin = 0), Pose.STAND, anim, h, pen, seed = seed)
            Cast.ELK -> drawPerson(Species.ELK, Look(skin = 0), Pose.STAND, anim, h, pen, seed = seed)
            Cast.GOAT -> drawGoat(h, pen)
        }
    }
}

/** A boulder with a mossy top, for the goat. [w] wide, bottom centre at ([cx], [by]). */
internal fun DrawScope.boulder(cx: Float, by: Float, w: Float, h: Float, lw: Float) {
    val pen = logoPen(lw)
    val rock = Color(0xFF8E93AB)
    val body = blobPath(
        cx - w * 0.5f, by - h * 0.05f, cx - w * 0.52f, by - h * 0.5f, cx - w * 0.3f, by - h * 0.95f, cx + w * 0.05f, by - h * 1.02f,
        cx + w * 0.38f, by - h * 0.8f, cx + w * 0.52f, by - h * 0.4f, cx + w * 0.46f, by - h * 0.04f, cx, by + h * 0.02f,
    )
    inked(body, rock, pen)
    clipPath(body) {
        drawPath(
            Path().apply {
                moveTo(cx - w * 0.6f, by - h * 0.62f)
                quadraticTo(cx - w * 0.1f, by - h * 0.78f, cx + w * 0.6f, by - h * 0.7f)
                lineTo(cx + w * 0.6f, by - h * 1.2f)
                lineTo(cx - w * 0.6f, by - h * 1.2f)
                close()
            },
            Color(0xFF6FAE5A),
        )
        drawLine(rock.darken(0.25f), Offset(cx - w * 0.2f, by - h * 0.45f), Offset(cx - w * 0.06f, by - h * 0.2f), strokeWidth = lw * 0.7f, cap = StrokeCap.Round)
        drawLine(rock.darken(0.25f), Offset(cx + w * 0.22f, by - h * 0.5f), Offset(cx + w * 0.3f, by - h * 0.3f), strokeWidth = lw * 0.7f, cap = StrokeCap.Round)
    }
    drawPath(body, Ink.line, style = pen.stroke)
}

/** A mountain goat sitting up like the game's animals: cream coat, curved horns, a little beard. Origin at the feet. */
internal fun DrawScope.drawGoat(h: Float, pen: Pen) {
    fun o(x: Float, y: Float) = Offset(x * h, y * h)
    val coat = Color(0xFFF4EFE4)
    val horn = Color(0xFFE6D3A3)
    // Tail
    val tail = Path().apply {
        moveTo(0.22f * h, -0.12f * h)
        quadraticTo(0.34f * h, -0.2f * h, 0.3f * h, -0.34f * h)
    }
    drawPath(tail, Ink.line, style = Stroke(0.07f * h + pen.lw * 2, cap = StrokeCap.Round))
    drawPath(tail, coat, style = Stroke(0.07f * h, cap = StrokeCap.Round))
    // Body
    val body = blobPath(
        -0.27f * h, -0.02f * h, -0.32f * h, -0.25f * h, -0.19f * h, -0.48f * h, 0.19f * h, -0.48f * h,
        0.32f * h, -0.25f * h, 0.27f * h, -0.02f * h,
    )
    inked(body, coat, pen)
    drawOval(coat.darken(0.07f), o(-0.13f, -0.34f), Size(0.26f * h, 0.28f * h))
    for (side in listOf(-1f, 1f)) {
        inkedRound(Rect((side * 0.1f - 0.06f) * h, -0.09f * h, (side * 0.1f + 0.06f) * h, 0f), 0.03f * h, coat.darken(0.1f), pen)
        drawRect(Color(0xFF3A3340), o(side * 0.1f - 0.06f, -0.025f), Size(0.12f * h, 0.025f * h))
    }
    val c = o(0f, -0.64f)
    val r = 0.3f * h
    // Horns sweep up and back; ears stick out sideways.
    for (side in listOf(-1f, 1f)) {
        val hornPath = Path().apply {
            moveTo(c.x + side * r * 0.22f, c.y - r * 0.88f)
            quadraticTo(c.x + side * r * 0.85f, c.y - r * 1.05f, c.x + side * r * 1.05f, c.y - r * 1.5f)
            quadraticTo(c.x + side * r * 0.62f, c.y - r * 1.38f, c.x + side * r * 0.5f, c.y - r * 1.25f)
            quadraticTo(c.x + side * r * 0.4f, c.y - r * 1.1f, c.x + side * r * 0.62f, c.y - r * 0.98f)
            quadraticTo(c.x + side * r * 0.4f, c.y - r * 0.96f, c.x - side * r * 0.0f, c.y - r * 0.97f)
            close()
        }
        inked(hornPath, horn, pen)
        drawLine(horn.darken(0.35f), Offset(c.x + side * r * 0.52f, c.y - r * 1.0f), Offset(c.x + side * r * 0.6f, c.y - r * 1.16f), strokeWidth = pen.lw * 0.6f, cap = StrokeCap.Round)
        drawLine(horn.darken(0.35f), Offset(c.x + side * r * 0.74f, c.y - r * 1.12f), Offset(c.x + side * r * 0.84f, c.y - r * 1.28f), strokeWidth = pen.lw * 0.6f, cap = StrokeCap.Round)
        rotate(side * 34f, pivot = Offset(c.x + side * r * 0.8f, c.y - r * 0.3f)) {
            inkedOval(Rect(c.x + side * r * 0.8f - r * 0.32f, c.y - r * 0.46f, c.x + side * r * 0.8f + r * 0.32f, c.y - r * 0.2f), coat, pen)
            drawOval(Color(0xFFFFA3BE).copy(alpha = 0.6f), Offset(c.x + side * r * 0.8f - r * 0.18f, c.y - r * 0.4f), Size(r * 0.36f, r * 0.12f))
        }
    }
    // Beard
    val beard = Path().apply {
        moveTo(c.x - r * 0.2f, c.y + r * 0.85f)
        lineTo(c.x + r * 0.2f, c.y + r * 0.85f)
        quadraticTo(c.x + r * 0.12f, c.y + r * 1.25f, c.x, c.y + r * 1.4f)
        quadraticTo(c.x - r * 0.12f, c.y + r * 1.25f, c.x - r * 0.2f, c.y + r * 0.85f)
        close()
    }
    inked(beard, coat.darken(0.1f), pen)
    inkedCircle(c, r, coat, pen)
    inkedOval(Rect(c.x - r * 0.55f, c.y + r * 0.12f, c.x + r * 0.55f, c.y + r * 0.9f), coat.lighten(0.2f), pen)
    drawOval(Ink.line.copy(alpha = 0.8f), Offset(c.x - r * 0.28f, c.y + r * 0.4f), Size(r * 0.12f, r * 0.1f))
    drawOval(Ink.line.copy(alpha = 0.8f), Offset(c.x + r * 0.16f, c.y + r * 0.4f), Size(r * 0.12f, r * 0.1f))
    for (side in listOf(-1f, 1f)) {
        val e = Offset(c.x + side * r * 0.42f, c.y - r * 0.16f)
        val white = rect(e.x, e.y, r * 0.36f, r * 0.34f)
        drawOval(Color.White, white.topLeft, white.size)
        drawOval(Ink.line, white.topLeft, white.size, style = pen.thin)
        drawCircle(Color(0xFFF2C94C), r * 0.12f, e)
        drawRect(Ink.line, Offset(e.x - r * 0.12f, e.y - r * 0.03f), Size(r * 0.24f, r * 0.06f))
    }
    drawArc(Ink.line, 20f, 140f, false, Offset(c.x - r * 0.18f, c.y + r * 0.55f), Size(r * 0.36f, r * 0.18f), style = Stroke(pen.lw, cap = StrokeCap.Round))
    drawOval(Ink.blush, Offset(c.x - r * 0.9f, c.y + r * 0.08f), Size(r * 0.3f, r * 0.16f))
    drawOval(Ink.blush, Offset(c.x + r * 0.6f, c.y + r * 0.08f), Size(r * 0.3f, r * 0.16f))
}

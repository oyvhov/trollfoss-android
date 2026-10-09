package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import app.trollfoss.domain.Look
import app.trollfoss.domain.Palette
import kotlin.math.cos
import kotlin.math.sin

/** Small, readable motifs rather than the tiny text/logos in the photo references. */
internal fun DrawScope.portraitPattern(look: Look, h: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * h, y * h)
    val accent = argb(Palette.cloth[look.accent])
    fun sun(x: Float, y: Float, radius: Float, color: Color) {
        for (k in 0..9) {
            val a = k * Math.PI.toFloat() / 5f
            drawLine(color, p(x + cos(a) * radius * 1.25f, y + sin(a) * radius * 1.25f),
                p(x + cos(a) * radius * 1.65f, y + sin(a) * radius * 1.65f),
                strokeWidth = pen.lw * .85f, cap = StrokeCap.Round)
        }
        drawCircle(color, radius * h, p(x, y))
    }
    when (look.pattern) {
        7 -> {
            drawRect(accent.copy(alpha = .8f), p(-.2f, -.50f), Size(.09f * h, .28f * h))
            drawRect(accent.copy(alpha = .7f), p(.08f, -.31f), Size(.12f * h, .10f * h))
            sun(.045f, -.399f, .039f, argb(Palette.cloth[2]))
            for ((x, y) in listOf(-.07f to -.33f, .085f to -.26f)) {
                for (k in 0..4) {
                    val a = k * Math.PI.toFloat() * .4f
                    drawCircle(Color(0xFFFFE5EC), .018f * h, p(x + cos(a) * .021f, y + sin(a) * .021f))
                }
                drawCircle(accent, .011f * h, p(x, y))
            }
        }
        8 -> for (k in -6..6) {
            drawLine(accent, p(k * .034f, -.5f), p(k * .034f, -.2f), strokeWidth = h * .008f)
            drawLine(accent.copy(alpha = .6f), p(k * .034f + .013f, -.5f), p(k * .034f + .013f, -.2f), strokeWidth = h * .003f)
        }
        9 -> sun(0f, -.356f, .032f, accent.lighten(.72f))
        10 -> if (look.top != 12) meadowPattern(h, pen)
    }
}

/** Button shirts and an open jacket; patterns sit underneath the seams and collar. */
internal fun DrawScope.portraitShirt(look: Look, color: Color, h: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * h, y * h)
    val jacket = look.top == 12
    val seam = color.darken(.3f)
    if (jacket) {
        for (s in floatArrayOf(-1f, 1f)) {
            val panel = Path().apply {
                moveTo(s * .08f * h, -.49f * h)
                lineTo(s * .19f * h, -.49f * h)
                lineTo(s * .2f * h, -.22f * h)
                lineTo(s * .066f * h, -.22f * h)
                quadraticTo(s * .083f * h, -.36f * h, s * .08f * h, -.49f * h)
                close()
            }
            inked(panel, color, pen)
            if (look.pattern == 10) clipPath(panel) { meadowPattern(h, pen) }
            drawLine(color.lighten(.4f), p(s * .093f, -.42f), p(s * .084f, -.246f), strokeWidth = pen.lw * .6f)
            inkedRound(Rect(p(s * .126f - .027f, -.395f), p(s * .126f + .027f, -.34f)), h * .005f, color.darken(.05f), pen, shade = false)
            drawLine(seam, p(s * .151f, -.38f), p(s * .101f, -.38f), strokeWidth = pen.lw * .6f)
            for (k in 0..2) drawCircle(Color(0xFFF0DDB7), .005f * h, p(s * .078f, -.33f + k * .037f))
        }
    } else {
        // A glimpse of the tee through the open neck, then a short button placket.
        val neck = Path().apply {
            moveTo(-.06f * h, -.5f * h); lineTo(.06f * h, -.5f * h)
            lineTo(0f, -.398f * h); close()
        }
        inked(neck, Color(0xFFFFFCF4), pen, shade = false)
        drawLine(seam, p(0f, -.398f), p(0f, -.232f), strokeWidth = pen.lw * .65f)
        for (k in 0..3) inkedCircle(p(.013f, -.381f + k * .043f), h * .006f, color.lighten(.55f), pen, shade = false)
    }
    for (s in floatArrayOf(-1f, 1f)) {
        val collar = Path().apply {
            moveTo(s * .065f * h, -.491f * h)
            lineTo(s * .123f * h, -.463f * h)
            lineTo(s * .077f * h, -.41f * h)
            lineTo(s * .021f * h, -.461f * h); close()
        }
        inked(collar, color.lighten(.1f), pen, shade = false)
    }
    drawLine(seam, p(-.13f, -.238f), p(.13f, -.238f), strokeWidth = pen.lw * .5f)
}

internal fun DrawScope.meadowFlower(c: Offset, r: Float, pen: Pen) {
    val gold = Color(0xFFD9B96F)
    for (k in 0..4) {
        val a = k * Math.PI.toFloat() * .4f
        drawOval(Color(0xFFFFF4DA), Offset(c.x + cos(a)*r*.55f-r*.4f, c.y + sin(a)*r*.55f-r*.4f), Size(r*.8f,r*.8f))
    }
    drawCircle(gold, r*.36f, c)
    drawLine(gold, Offset(c.x+r*.8f,c.y+r*.4f), Offset(c.x+r*1.12f,c.y+r*.78f), pen.lw*.55f, StrokeCap.Round)
}

private fun DrawScope.meadowPattern(h: Float, pen: Pen) {
    for (s in floatArrayOf(-1f,1f)) for (k in 0..2) {
        meadowFlower(Offset(s*(.111f+(k%2)*.035f)*h,(-.423f+k*.079f)*h), .032f*h, pen)
    }
}

/** A stand collar and readable zip; the half zip exposes a little contrasting tee. */
internal fun DrawScope.zipTop(look: Look, color: Color, h: Float, pen: Pen) {
    fun p(x: Float,y: Float) = Offset(x*h,y*h)
    val full = look.top == 15
    val edge = color.lighten(.36f)
    val end = if(full) -.233f else -.352f
    val opening=Path().apply {
        moveTo(-.056f*h,-.5f*h);lineTo(.056f*h,-.5f*h)
        lineTo(.025f*h,-.41f*h);lineTo(-.025f*h,-.41f*h);close()
    }
    inked(opening,argb(Palette.cloth[look.accent]),pen,shade=false)
    for(s in floatArrayOf(-1f,1f)) {
        val collar=Path().apply {
            moveTo(s*.032f*h,-.486f*h);lineTo(s*.073f*h,-.50f*h)
            lineTo(s*.12f*h,-.46f*h);lineTo(s*.043f*h,-.395f*h);close()
        }
        inked(collar,color.lighten(.06f),pen,shade=false)
    }
    drawLine(color.darken(.4f),p(0f,-.418f),p(0f,end),pen.lw*1.5f)
    drawLine(edge,p(0f,-.415f),p(0f,end),pen.lw*.5f)
    inkedRound(Rect(p(-.011f,-.412f),p(.011f,-.384f)),.004f*h,Color(0xFFCBD0CD),pen,shade=false)
    drawLine(color.darken(.25f),p(-.12f,-.244f),p(.12f,-.244f),pen.lw*.5f)
    if(full) for(s in floatArrayOf(-1f,1f)) drawLine(edge,p(s*.097f,-.327f),p(s*.12f,-.276f),pen.lw*.55f,StrokeCap.Round)
}

/** Broad quilted bands, an open neck and a dark inner layer; readable at ordinary play size. */
internal fun DrawScope.quiltedJacket(look: Look, color: Color, h: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x*h, y*h)
    val seam = color.darken(.3f)
    for (k in 0..2) {
        val y = -.426f + k*.066f
        val band = Path().apply {
            moveTo(-.2f*h, y*h)
            quadraticTo(0f, (y+.024f)*h, .2f*h, y*h)
        }
        drawPath(band, seam, style = androidx.compose.ui.graphics.drawscope.Stroke(pen.lw*.65f))
        drawLine(color.lighten(.18f), p(-.12f,y+.018f), p(-.045f,y+.025f), pen.lw*.5f, StrokeCap.Round)
    }
    val opening = Path().apply {
        moveTo(-.075f*h,-.51f*h);lineTo(.075f*h,-.51f*h)
        lineTo(.044f*h,-.404f*h);lineTo(0f,-.341f*h)
        lineTo(-.044f*h,-.404f*h);close()
    }
    inked(opening, argb(Palette.cloth[look.accent]), pen, shade = false)
    drawLine(argb(Palette.cloth[look.accent]).lighten(.25f),p(0f,-.482f),p(0f,-.41f),pen.lw*.55f)
    for (s in floatArrayOf(-1f,1f)) {
        val collar = Path().apply {
            moveTo(s*.075f*h,-.50f*h);lineTo(s*.142f*h,-.466f*h)
            quadraticTo(s*.093f*h,-.451f*h,s*.068f*h,-.391f*h)
            lineTo(s*.028f*h,-.427f*h);close()
        }
        inked(collar,color.lighten(.1f),pen,shade=false)
        drawLine(seam,p(s*.11f,-.315f),p(s*.084f,-.266f),pen.lw*.8f,StrokeCap.Round)
    }
    drawLine(seam,p(0f,-.341f),p(0f,-.233f),pen.lw*1.5f)
    drawLine(color.lighten(.48f),p(0f,-.34f),p(0f,-.233f),pen.lw*.5f)
    inkedRound(Rect(p(-.009f,-.345f),p(.009f,-.32f)),h*.003f,Color(0xFFCBD0CD),pen,shade=false)
}

/** Seams follow the same curved arm as the sleeve, including a raised waving hand. */
internal fun DrawScope.quiltedSleeve(start: Offset, elbow: Offset, end: Offset, color: Color, h: Float, pen: Pen) {
    for (t in floatArrayOf(.3f,.57f,.8f)) {
        val inv = 1f-t
        val center = start*(inv*inv) + elbow*(2f*inv*t) + end*(t*t)
        val direction = (elbow-start)*inv + (end-elbow)*t
        val normal = Offset(-direction.y,direction.x) / direction.getDistance().coerceAtLeast(1f) * (.03f*h)
        drawLine(color.darken(.26f),center-normal,center+normal,pen.lw*.6f,StrokeCap.Round)
    }
}

/** A soft jaw shadow and a few short hairs keep the mouth and its reactions unobstructed. */
internal fun DrawScope.lightStubble(look: Look, hair: Color, h: Float, pen: Pen) {
    fun p(x: Float,y: Float) = Offset(x*h,y*h)
    val jaw = Path().apply {
        moveTo(-.235f*h,-.65f*h)
        quadraticTo(-.17f*h,-.603f*h,-.12f*h,-.596f*h)
        quadraticTo(0f,-.53f*h,.12f*h,-.596f*h)
        quadraticTo(.17f*h,-.603f*h,.235f*h,-.65f*h)
        lineTo(.26f*h,-.46f*h);lineTo(-.26f*h,-.46f*h);close()
    }
    clipPath(folkFace(look.face,h)) {
        drawPath(jaw,hair.copy(alpha=.16f))
        for (s in floatArrayOf(-1f,1f)) for(k in 0..3) {
            val x=s*(.118f+k*.025f)
            val y=-.526f-k*.017f
            drawLine(hair.copy(alpha=.48f),p(x,y),p(x-s*.004f,y+.012f),pen.lw*.55f,StrokeCap.Round)
        }
        for(k in -2..2) drawCircle(hair.copy(alpha=.4f),h*.003f,p(k*.025f,-.499f))
    }
    for(s in floatArrayOf(-1f,1f)) drawLine(hair.copy(alpha=.3f),p(s*.018f,-.619f),p(s*.068f,-.611f),pen.lw,StrokeCap.Round)
}

/** A distinct full beard, without hiding the shared animated mouth. */
internal fun DrawScope.fullBeard(look: Look, hair: Color, h: Float, pen: Pen) {
    fun p(x: Float,y: Float)=Offset(x*h,y*h)
    val beard=Path().apply {
        moveTo(-.232f*h,-.674f*h)
        cubicTo(-.21f*h,-.65f*h,-.17f*h,-.604f*h,-.092f*h,-.601f*h)
        quadraticTo(0f,-.633f*h,.092f*h,-.601f*h)
        cubicTo(.17f*h,-.604f*h,.21f*h,-.65f*h,.232f*h,-.674f*h)
        cubicTo(.26f*h,-.568f*h,.22f*h,-.414f*h,.127f*h,-.39f*h)
        quadraticTo(.081f*h,-.337f*h,.029f*h,-.363f*h)
        quadraticTo(-.02f*h,-.329f*h,-.067f*h,-.365f*h)
        cubicTo(-.17f*h,-.357f*h,-.268f*h,-.486f*h,-.232f*h,-.674f*h);close()
    }
    inked(beard,hair,pen)
    for(s in floatArrayOf(-1f,1f)) for(k in 0..3) {
        val strand=Path().apply {
            moveTo(s*(.087f+k*.036f)*h,(-.497f-k*.014f)*h)
            quadraticTo(s*(.105f+k*.027f)*h,-.435f*h,s*(.034f+k*.028f)*h,(-.38f-k*.005f)*h)
        }
        drawPath(strand,hair.lighten(.28f),style=androidx.compose.ui.graphics.drawscope.Stroke(pen.lw*.6f,cap=StrokeCap.Round))
    }
    drawOval(argb(Palette.skins[look.skin]),p(-.097f,-.625f),Size(.194f*h,.13f*h))
    for(s in floatArrayOf(-1f,1f)) {
        val moustache=Path().apply {
            moveTo(0f,-.63f*h)
            cubicTo(s*.043f*h,-.65f*h,s*.098f*h,-.613f*h,s*.12f*h,-.603f*h)
            quadraticTo(s*.069f*h,-.59f*h,0f,-.617f*h);close()
        }
        drawPath(moustache,hair.darken(.12f))
    }
}

internal fun DrawScope.trimmedBeard(look: Look, hair: Color, h: Float, pen: Pen) {
    fun p(x: Float, y: Float) = Offset(x * h, y * h)
    val beard = Path().apply {
        moveTo(-.235f * h, -.666f * h)
        cubicTo(-.18f * h, -.622f * h, -.167f * h, -.6f * h, -.123f * h, -.582f * h)
        cubicTo(-.092f * h, -.532f * h, .092f * h, -.532f * h, .123f * h, -.582f * h)
        cubicTo(.167f * h, -.6f * h, .18f * h, -.622f * h, .235f * h, -.666f * h)
        lineTo(.25f * h, -.45f * h); lineTo(-.25f * h, -.45f * h); close()
    }
    clipPath(folkFace(look.face, h)) {
        inked(beard, hair.darken(.2f), pen)
        for (s in floatArrayOf(-1f, 1f)) for (k in 0..3) {
            drawLine(hair.lighten(.42f), p(s * (.132f + k * .021f), -.512f - k * .02f),
                p(s * (.121f + k * .02f), -.494f - k * .022f), strokeWidth = pen.lw * .65f, cap = StrokeCap.Round)
        }
    }
    for (s in floatArrayOf(-1f, 1f)) {
        val moustache = Path().apply {
            moveTo(0f, -.626f * h)
            quadraticTo(s * .05f * h, -.642f * h, s * .085f * h, -.609f * h)
            quadraticTo(s * .047f * h, -.605f * h, 0f, -.617f * h); close()
        }
        drawPath(moustache, hair.darken(.28f))
    }
}

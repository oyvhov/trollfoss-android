package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import app.trollfoss.domain.Look

/** Four reusable portrait cuts, with fixed temples and adjustable free ends/crown volume. */
internal fun DrawScope.familyHair(look: Look, back: Boolean, color: Color, c: Offset, r: Float, pen: Pen) {
    val fit = HairFit(look)
    val full = 1f + (look.hairSize - 1f) * .35f
    val end = .72f + (look.hairLength - .65f) * 1.5f
    val crown = 1.07f + (look.hairSize - .8f) * .25f
    fun p(x: Float, y: Float) = Offset(c.x + x * r * fit.headWidth, c.y + y * r)
    fun Path.m(x: Float, y: Float) { val a=p(x,y); moveTo(a.x,a.y) }
    fun Path.l(x: Float, y: Float) { val a=p(x,y); lineTo(a.x,a.y) }
    fun Path.c(x1: Float,y1: Float,x2: Float,y2: Float,x3: Float,y3: Float) {
        val a=p(x1,y1);val b=p(x2,y2);val d=p(x3,y3);cubicTo(a.x,a.y,b.x,b.y,d.x,d.y)
    }
    fun stroke(path: Path, light: Boolean = false) = drawPath(path,
        if(light) color.lighten(.33f) else color.darken(.23f),
        style=Stroke(pen.lw*.68f,cap=StrokeCap.Round))

    if (back) {
        when (look.hair) {
            22,25 -> {
                val bottom = -.02f + (look.hairLength - .65f) * .2f
                for(s in floatArrayOf(-1f,1f)) capsule(p(s*.96f,-.48f),p(s*.98f,bottom),r*.15f,color.darken(.15f),pen)
            }
            23 -> {
                val shell=Path().apply {
                    m(-.84f,-.9f)
                    c(-1.27f*full,-.61f,-1.16f*full,.38f,-1.25f*full,end-.03f)
                    c(-1.2f*full,end+.18f,-.91f,end+.13f,-.71f,end-.1f)
                    l(.72f,end-.1f)
                    c(.97f,end+.13f,1.25f*full,end+.17f,1.27f*full,end-.02f)
                    c(1.18f*full,.29f,1.24f*full,-.66f,.83f,-.92f)
                    c(.34f,-1.22f,-.4f,-1.24f,-.84f,-.9f);close()
                }
                inked(shell,color,pen)
                clipPath(shell) {
                    for(s in floatArrayOf(-1f,1f)) for(k in 0..2) {
                        val x=s*(.9f+k*.105f)*full
                        val strand=Path().apply {m(x,-.14f);c(x-s*.07f,.39f,x+s*.10f,end-.48f,x-s*.035f,end-.05f)}
                        drawPath(strand,color.lighten(.24f+k*.08f),style=Stroke(r*.07f,cap=StrokeCap.Round))
                    }
                }
            }
            24 -> {
                // Low tied hair stays behind the ear and shoulder, rather than a high floating ponytail.
                val tail=Path().apply {
                    m(.77f,-.18f)
                    c(1.39f*full,-.06f,1.39f*full,.44f,1.18f*full,.73f)
                    c(1.06f*full,1.05f,1.20f*full,end-.08f,1.42f*full,end)
                    c(1.17f*full,end+.21f,.72f,end+.08f,.68f,end-.26f)
                    c(.66f,.66f,1.03f,.32f,.77f,-.18f);close()
                }
                inked(tail,color,pen)
                stroke(Path().apply {m(.99f,.16f);c(1.28f,.66f,.84f,end-.34f,1.22f,end-.08f)},true)
                inkedCircle(p(.94f,.03f),r*.105f,Color(0xFF494653),pen,shade=false)
            }
        }
        return
    }

    when (look.hair) {
        22 -> {
            val lift=(look.hairLength-.65f)*.25f
            val shape=Path().apply {
                m(-1f,-.04f)
                c(-1.14f,-.57f,-.9f,-crown,-.55f,-crown-.04f)
                c(-.53f,-crown-.22f-lift,-.31f,-crown-.25f-lift,-.22f,-crown-.13f)
                c(.1f,-crown-.29f-lift,.66f,-crown-.24f-lift,.89f,-crown+.02f)
                c(1.11f,-.84f,1.12f,-.41f,1f,-.03f)
                l(.86f,-.18f)
                c(.85f,-.51f,.81f,-.7f,.65f,-.74f)
                c(.24f,-.93f,-.08f,-.86f,-.34f,-.78f)
                c(-.74f,-.81f,-.86f,-.49f,-.86f,-.19f)
                l(-1f,-.04f);close()
            }
            inked(shape,color,pen)
            for(k in 0..3) stroke(Path().apply {
                m(-.72f+k*.22f,-.79f-k*.015f)
                c(-.66f+k*.22f,-1.03f-lift,-.34f+k*.24f,-1.17f-lift,.25f+k*.16f,-1.02f)
            },k==1)
        }
        23,24 -> {
            val tied=look.hair==24
            val part=if(tied) -.16f else .03f
            val shape=Path().apply {
                m(-1.02f,.03f)
                c(-1.22f*full,-.49f,-.94f,-crown,-.32f,-crown)
                c(.21f,-crown-.13f,.94f,-crown+.03f,1.04f,-.58f)
                c(1.13f,-.32f,1.09f,-.1f,1.02f,.03f)
                l(.88f,-.13f)
                c(.85f,-.43f,.47f,-.77f,part,-.93f)
                c(-.48f,-.77f,-.88f,-.53f,-.9f,-.13f)
                l(-1.02f,.03f);close()
            }
            inked(shape,if(tied) color else color.darken(.13f),pen)
            stroke(Path().apply {m(part,-crown+.02f);c(part-.02f,-1.08f,part-.02f,-1f,part,-.93f)})
            for(s in floatArrayOf(-1f,1f)) for(k in 0..2) stroke(Path().apply {
                m(part+s*(.07f+k*.09f),-crown+.08f+k*.006f)
                c(s*(.4f+k*.08f),-1.01f,s*(.82f+k*.035f),-.68f,s*(.9f+k*.03f),-.27f+k*.025f)
            },k==1)
        }
        25 -> {
            val drop=(look.hairLength-.65f)*.10f
            val fringe=Path().apply {
                m(-1.01f,-.14f)
                c(-1.11f,-.68f,-.81f,-crown,-.3f,-crown)
                c(.02f,-crown-.08f,.58f,-crown+.01f,.82f,-.93f)
                c(1.08f,-.68f,1.11f,-.38f,1.02f,-.14f)
                l(.86f,-.37f)
                c(.75f,-.29f+drop,.48f,-.29f+drop,.37f,-.41f+drop)
                c(.24f,-.27f+drop,.08f,-.28f+drop,-.04f,-.43f+drop)
                c(-.23f,-.28f+drop,-.52f,-.29f+drop,-.66f,-.41f)
                c(-.78f,-.26f,-.85f,-.2f,-1.01f,-.14f);close()
            }
            inked(fringe,color,pen)
            for(k in -3..3) stroke(Path().apply {
                m(k*.17f,-crown+.1f)
                c(k*.22f,-.85f,k*.2f+.04f,-.55f,k*.23f,-.36f+drop)
            },k%2==0)
        }
    }
}

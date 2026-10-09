package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import app.trollfoss.domain.Look

/** Loose swept waves with fixed roots; the workshop changes the fullness and free ends. */
internal fun DrawScope.tousledHair(look: Look, back: Boolean, color: Color, c: Offset, r: Float, pen: Pen) {
    val full = 1f + (look.hairSize - 1f) * .45f
    val end = .72f + (look.hairLength - .65f) * 1.15f
    fun p(x: Float, y: Float) = Offset(c.x + x * r * full, c.y + y * r)
    fun Path.move(x: Float, y: Float) { val q = p(x,y); moveTo(q.x,q.y) }
    fun Path.curve(x1: Float,y1: Float,x2: Float,y2: Float,x3: Float,y3: Float) {
        val a=p(x1,y1); val b=p(x2,y2); val d=p(x3,y3); cubicTo(a.x,a.y,b.x,b.y,d.x,d.y)
    }
    if (back) {
        val shell=Path().apply {
            move(-.9f,-.75f)
            curve(-1.22f,-.65f,-1.30f,-.2f,-1.14f,.04f)
            curve(-1.4f,.1f,-1.13f,.45f,-1.22f,.56f)
            curve(-1.0f,.54f,-1.17f,end,-.91f,end*.88f)
            curve(-.92f,end*1.16f,-.54f,end*1.18f,-.59f,end*.9f)
            curve(-.24f,end*1.1f,.3f,end*1.12f,.6f,end*.92f)
            curve(.65f,end*1.2f,1.02f,end*1.07f,.96f,end*.8f)
            curve(1.24f,end*.86f,1.31f,.44f,1.11f,.32f)
            curve(1.36f,.26f,1.25f,-.08f,1.16f,-.2f)
            curve(1.3f,-.5f,1.05f,-.85f,.87f,-.87f)
            curve(.3f,-1.3f,-.5f,-1.3f,-.9f,-.75f); close()
        }
        inked(shell,color.darken(.09f),pen)
        for(side in floatArrayOf(-1f,1f)) {
            val curl=Path().apply { move(side*1.02f,.05f); curve(side*.75f,.28f,side*1.13f,.57f,side*.82f,end*.86f) }
            drawPath(curl,color.darken(.25f),style=Stroke(pen.lw*.8f,cap=StrokeCap.Round))
        }
        return
    }
    val fringe=Path().apply {
        move(-1.03f,-.10f)
        curve(-1.24f,-.22f,-1.26f,-.56f,-1.03f,-.62f)
        curve(-1.28f,-.8f,-.93f,-1.1f,-.7f,-1.08f)
        curve(-.68f,-1.29f,-.27f,-1.3f,-.12f,-1.14f)
        curve(.06f,-1.4f,.44f,-1.3f,.55f,-1.16f)
        curve(.77f,-1.22f,.93f,-1.02f,1.17f,-1.15f)
        curve(1.21f,-.94f,1.08f,-.83f,.99f,-.84f)
        curve(1.25f,-.64f,1.18f,-.42f,1.30f,-.34f)
        curve(1.05f,-.3f,.93f,-.42f,.87f,-.55f)
        curve(.96f,-.25f,.71f,-.15f,.67f,-.26f)
        curve(.83f,-.50f,.52f,-.69f,.45f,-.91f)
        curve(.67f,-.46f,.25f,-.12f,.07f,-.25f)
        curve(.30f,-.4f,.2f,-.65f,.16f,-.8f)
        curve(.14f,-.45f,-.16f,-.12f,-.38f,-.30f)
        curve(-.13f,-.36f,-.22f,-.62f,-.31f,-.67f)
        curve(-.42f,-.41f,-.55f,-.32f,-.7f,-.4f)
        curve(-.72f,-.16f,-.94f,-.17f,-1.03f,-.10f); close()
    }
    inked(fringe,color,pen)
    for(i in -2..2) {
        val x=i*.35f
        val strand=Path().apply { move(x,-1.04f); curve(x-.13f,-.95f,x-.11f,-.69f,x-.25f,-.55f) }
        drawPath(strand,if(i%2==0) color.lighten(.25f) else color.darken(.20f),style=Stroke(pen.lw*.85f,cap=StrokeCap.Round))
    }
}

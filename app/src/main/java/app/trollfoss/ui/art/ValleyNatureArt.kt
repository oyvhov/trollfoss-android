package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import app.trollfoss.domain.Fixture
import kotlin.math.sin

private val beachShape = Memo { u -> Path().apply {
    moveTo(2.8f * u, .805f * u)
    cubicTo(2.57f * u, .78f * u, 2.40f * u, .79f * u, 2.26f * u, .825f * u)
    cubicTo(2.05f * u, .87f * u, 2.13f * u, .93f * u, 2.19f * u, .975f * u)
    lineTo(2.8f * u, .975f * u); close()
} }

internal fun DrawScope.valleyBeach(st: Stage, pen: Pen) {
    if (!st.sees(2.08f, 2.83f)) return
    val u = st.u
    inScene(st) {
        val sand = pen.sandy(Color(0xFFE6CF9A)).atNight(pen.night, .5f)
        val beach = beachShape.of(u)
        inked(beach, sand, pen, shade = false)
        clipPath(beach) {
            drawOval(sand.lighten(.16f), Offset(2.17f * u, .82f * u), Size(.56f * u, .19f * u))
            drawRect(sand.darken(.15f), Offset(2.74f * u, .79f * u), Size(.09f * u, .20f * u))
            val grains = Path()
            for (i in 0 until 24) {
                val x = 2.15f + hash01(i, 5101) * .64f
                val y = .81f + hash01(i, 5102) * .16f
                grains.moveTo(x * u, y * u); grains.lineTo((x + .008f) * u, (y + .002f) * u)
            }
            drawPath(grains, sand.darken(.25f), style = pen.thin)
        }
        // A little woven mat is a clear dry changing place for the two draggable outfits.
        val mat = Path().apply { poly(2.17f*u,.941f*u,2.49f*u,.941f*u,2.52f*u,.981f*u,2.20f*u,.981f*u) }
        inked(mat, Color(0xFFC4D9CB).atNight(pen.night,.5f),pen,shade=false)
        for(i in 0..4) drawLine(Color(0xFF4E918A).atNight(pen.night,.5f),Offset((2.21f+i*.052f)*u,.947f*u),Offset((2.23f+i*.052f)*u,.976f*u),pen.lw*.75f)
    }
}

private val fallsRock = Memo { u -> Path().apply {
    poly(3.18f*u,.718f*u,3.30f*u,.568f*u,3.45f*u,.54f*u,3.47f*u,.376f*u,
        3.60f*u,.286f*u,3.84f*u,.30f*u,3.95f*u,.418f*u,3.92f*u,.57f*u,
        4.03f*u,.694f*u,3.80f*u,.748f*u)
} }
private val fallingWater = Memo { u -> Path().apply {
    moveTo(3.62f*u,.31f*u); lineTo(3.79f*u,.321f*u)
    cubicTo(3.76f*u,.389f*u,3.87f*u,.44f*u,3.85f*u,.475f*u)
    quadraticTo(3.81f*u,.511f*u,3.71f*u,.52f*u)
    cubicTo(3.69f*u,.57f*u,3.80f*u,.65f*u,3.83f*u,.705f*u)
    quadraticTo(3.67f*u,.75f*u,3.42f*u,.71f*u)
    cubicTo(3.52f*u,.65f*u,3.56f*u,.586f*u,3.54f*u,.531f*u)
    quadraticTo(3.51f*u,.501f*u,3.63f*u,.48f*u)
    cubicTo(3.69f*u,.436f*u,3.59f*u,.37f*u,3.62f*u,.31f*u)
    close()
} }

/** Two rocky drops empty into the original playable river; the rock nook to the right stays usable. */
internal fun DrawScope.valleyFalls(st: Stage, pen: Pen) {
    if (!st.sees(3.18f, 4.04f)) return
    val u = st.u
    val n = pen.night
    inScene(st) {
        val rock = fallsRock.of(u)
        inked(rock, Color(0xFF7F9293).atNight(n,.55f),pen)
        val seams = Path().apply {
            moveTo(3.34f*u,.59f*u); lineTo(3.51f*u,.604f*u); lineTo(3.47f*u,.65f*u)
            moveTo(3.52f*u,.385f*u); lineTo(3.65f*u,.405f*u)
            moveTo(3.81f*u,.355f*u); lineTo(3.90f*u,.41f*u); lineTo(3.84f*u,.435f*u)
            moveTo(3.82f*u,.58f*u); lineTo(3.92f*u,.606f*u)
        }
        drawPath(seams,Color(0xFF506A72).atNight(n,.5f),style=pen.stroke)
        for (i in 0..3) {
            val x = if(i<2) 3.40f+i*.09f else 3.86f+(i-2)*.025f
            val y = if(i<2) .60f-i*.08f else .47f+(i-2)*.18f
            inkedOval(Rect((x-.07f)*u,(y-.012f)*u,(x+.065f)*u,(y+.012f)*u),pen.ground(Color(0xFF6D9866)).atNight(n,.5f),pen,shade=false)
        }
        val water = fallingWater.of(u)
        inked(water,Color(0xFF80D3DB).atNight(n,.35f),pen,shade=false)
        clipPath(water) {
            drawPath(water,Color(0xFFE7FAEB).atNight(n,.3f),style=Stroke(.025f*u))
            val streams=Path()
            for(i in 0..5) {
                val x=3.625f+i*.028f
                streams.moveTo(x*u,.323f*u)
                streams.cubicTo((x-.02f)*u,.4f*u,(x+.10f)*u,.457f*u,(x+.015f)*u,.487f*u)
                val lower=3.555f+i*.035f
                streams.moveTo(lower*u,.535f*u)
                streams.cubicTo((lower+.015f)*u,.59f*u,(lower+.01f)*u,.64f*u,(lower-.045f+i*.019f)*u,.711f*u)
            }
            drawPath(streams,Color(0xFFF2FFF6).atNight(n,.25f),style=Stroke(.010f*u))
            val glints=Path()
            for(i in 0..8) {
                val phase=fxFrac(pen.t*.48f+i*.113f)
                val y=.32f+phase*.39f
                val x=3.56f+(i%4)*.063f
                glints.moveTo(x*u,y*u);glints.lineTo((x+.006f)*u,(y+.027f)*u)
            }
            drawPath(glints,Color.White.copy(alpha=.8f),style=pen.stroke)
        }
        for(i in 0..6) {
            val x=3.45f+i*.055f
            val lift=sin(pen.t*2.3f+i)*.003f
            drawOval(Color(0xFFE6F7ED).atNight(n,.3f),Offset(x*u,(.693f+lift+(i%2)*.008f)*u),Size(.075f*u,.023f*u))
        }
        drawOval(Color(0xFFE9FAEE).atNight(n,.3f),Offset(3.58f*u,.492f*u),Size(.26f*u,.027f*u))
        // Thin spray arcs, never a blur, and all motion uses the engine's reduced-motion clock.
        val spray=Path()
        for(i in 0..3) {
            val x=3.45f+i*.104f
            spray.moveTo(x*u,.701f*u)
            spray.quadraticTo((x-.018f)*u,(.652f+sin(pen.t*2f+i)*.005f)*u,(x-.042f)*u,.685f*u)
        }
        drawPath(spray,Color(0xFFD5F9EE).atNight(n,.25f),style=pen.thin)
    }
}

internal fun DrawScope.valleyFish(st: Stage, pen: Pen) {
    if (!st.sees(2.85f,3.84f)) return
    val u=st.u
    for(i in 0..3) {
        val phase=pen.t*.55f+i*1.7f
        val x=3.33f+sin(phase)*.34f
        val y=.86f+i*.022f+sin(phase*1.4f)*.006f
        translate(st.x(x),y*u) {
            scale(if(kotlin.math.cos(phase)>=0f) 1f else -1f,1f,pivot=Offset.Zero) {
                val fish=Color(if(i%2==0) 0xFFBC9F5D else 0xFF668D96).atNight(pen.night,.4f)
                inked(Path().apply { poly(-.029f*u,0f,-.050f*u,-.018f*u,-.050f*u,.018f*u) },fish,pen,shade=false)
                inkedOval(Rect(-.034f*u,-.015f*u,.036f*u,.015f*u),fish,pen)
                drawLine(Color(0xFFE5E0B4).atNight(pen.night,.4f),Offset(-.019f*u,.004f*u),Offset(.022f*u,.004f*u),pen.lw*.7f)
                drawCircle(Ink.line,.003f*u,Offset(.022f*u,-.004f*u))
            }
        }
    }
}

internal fun DrawScope.valleyBirch(x:Float,y:Float,h:Float,pen:Pen,salt:Int) {
    drawBirch(x,y,h,pen,Color(0xFF8DB85E),salt,pen.night)
    if(pen.winter) return
    val leaves=Path()
    val veins=Path()
    for(i in 0 until 12) {
        val px=x+(hash01(i,salt)-.5f)*h*.36f
        val py=y-h*(.67f+hash01(i,salt+1)*.24f)
        leaves.moveTo(px-h*.031f,py)
        leaves.quadraticTo(px,py-h*.035f,px+h*.030f,py)
        leaves.quadraticTo(px,py+h*.02f,px-h*.031f,py)
        veins.moveTo(px-h*.019f,py);veins.lineTo(px+h*.018f,py-h*.004f)
    }
    drawPath(leaves,pen.foliage(Color(0xFFBCD880),salt).atNight(pen.night,.5f))
    drawPath(veins,pen.foliage(Color(0xFF507D47),salt).atNight(pen.night,.5f),style=pen.thin)
    drawOval(Pal.birch.darken(.5f).atNight(pen.night,.5f),Offset(x-h*.018f,y-h*.30f),Size(h*.035f,h*.055f),style=pen.thin)
}

internal fun DrawScope.valleyPine(f:Fixture,u:Float,pen:Pen) {
    rotate(f.angle*57.3f,Offset(0f,-.04f*u)) {
        capsule(Offset(0f,-.015f*u),Offset(0f,-.66f*u),.028f*u,Color(0xFF966E47),pen)
        for(i in 0..4) {
            val y=-.07f-i*.118f
            val w=.18f-i*.027f
            val crown=Path().apply {
                poly(0f,(y-.196f)*u,w*.45f*u,(y-.11f)*u,w*.25f*u,(y-.12f)*u,w*u,y*u,
                    w*.30f*u,(y-.008f)*u,0f,(y+.015f)*u,-w*u,y*u,-w*.27f*u,(y-.12f)*u,-w*.46f*u,(y-.10f)*u)
            }
            val green=pen.conifer(if(i%2==0) Color(0xFF377859) else Color(0xFF498E61)).atNight(pen.night,.5f)
            inked(crown,green,pen)
            val needles=Path()
            for(s in floatArrayOf(-1f,1f)) for(k in 1..3) {
                needles.moveTo(s*w*.10f*u,(y-.035f*k)*u)
                needles.lineTo(s*w*(.88f-k*.14f)*u,(y-.019f*k)*u)
            }
            drawPath(needles,green.lighten(.2f),style=pen.thin)
            if(pen.winter) drawLine(Pal.snow,Offset(-w*.36f*u,(y-.10f)*u),Offset(0f,(y-.19f)*u),.016f*u)
        }
    }
}

internal fun DrawScope.valleyBankDetails(st:Stage,pen:Pen) {
    val u=st.u
    // Small clusters frame clear play areas. Offscreen clusters do no drawing work.
    for(x in floatArrayOf(.26f,1.29f,2.73f,3.92f,4.70f,5.24f)) {
        if(!st.sees(x-.12f,x+.12f)) continue
        val y=if(x in 2.7f..4.0f) .78f else .91f
        val px=st.x(x)
        val grass=pen.plant(Color(0xFF538654)).atNight(pen.night,.5f)
        val fern=Path()
        for(i in 0..3) {
            val xx=px+(i-1.5f)*.015f*u
            fern.moveTo(xx,y*u);fern.quadraticTo(xx-.008f*u,(y-.035f)*u,xx+(i-1.5f)*.013f*u,(y-.057f)*u)
            fern.moveTo(xx-.006f*u,(y-.026f)*u);fern.lineTo(xx-.018f*u,(y-.042f)*u)
            fern.moveTo(xx-.004f*u,(y-.037f)*u);fern.lineTo(xx+.011f*u,(y-.048f)*u)
        }
        drawPath(fern,grass,style=pen.stroke)
        inkedOval(Rect(px-.053f*u,(y+.005f)*u,px+.014f*u,(y+.028f)*u),Color(0xFFA7B2A3).atNight(pen.night,.5f),pen)
        drawLine(pen.ground(Color(0xFF88A464)).atNight(pen.night,.5f),Offset(px-.04f*u,(y+.010f)*u),Offset(px-.014f*u,(y+.008f)*u),pen.lw*1.8f)
    }
}

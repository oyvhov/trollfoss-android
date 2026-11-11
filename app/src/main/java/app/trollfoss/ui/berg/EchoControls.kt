package app.trollfoss.ui.berg

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.trollfoss.domain.EchoNote
import app.trollfoss.ui.*
import app.trollfoss.ui.components.*
import app.trollfoss.ui.theme.T
import kotlin.math.*

internal fun DrawScope.echoSign(key:Int) {
    val s=size.minDimension;val c=center
    when(key) {
        0 -> {
            repeat(8) {i->
                val a=i*PI.toFloat()/4f;val d=Offset(cos(a),sin(a))
                drawLine(T.Ink,c+d*(s*.27f),c+d*(s*.40f),s*.055f,StrokeCap.Round)
            }
            drawCircle(T.Ink,s*.22f,c);drawCircle(Color(0xFFF7C873),s*.17f,c)
        }
        1 -> {
            val leaf=Path().apply {
                moveTo(s*.22f,s*.77f);quadraticTo(s*.02f,s*.27f,s*.77f,s*.15f)
                quadraticTo(s*.91f,s*.79f,s*.22f,s*.77f);close()
            }
            drawPath(leaf,Color(0xFF89BA88));drawPath(leaf,T.Ink,style=Stroke(s*.055f))
            drawLine(T.Ink,Offset(s*.20f,s*.85f),Offset(s*.64f,s*.31f),s*.05f,StrokeCap.Round)
        }
        else -> {
            val moon=Path().apply {
                moveTo(s*.62f,s*.16f);cubicTo(s*.09f,s*.13f,s*.08f,s*.84f,s*.57f,s*.85f)
                quadraticTo(s*.82f,s*.86f,s*.89f,s*.62f);cubicTo(s*.45f,s*.77f,s*.36f,s*.33f,s*.62f,s*.16f);close()
            }
            drawPath(moon,Color(0xFFCDB8E2));drawPath(moon,T.Ink,style=Stroke(s*.055f))
        }
    }
}

@Composable
internal fun EchoControls(notes:List<EchoNote>,button:Dp,modifier:Modifier,
                          play:(Int)->Unit,clear:()->Unit,close:()->Unit) {
    Column(modifier,horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(4.dp)) {
        if(notes.isNotEmpty()) {
            val toneNames=notes.map { SBerg.echoTones[it.tone].str() }.joinToString(", ")
            val description=SBerg.yourSong.str()+". "+toneNames
            Row(Modifier.background(T.Cream,RoundedCornerShape(16.dp)).padding(horizontal=7.dp,vertical=2.dp)
                .semantics {contentDescription=description}) {
                notes.forEach {note->Canvas(Modifier.size(19.dp)){echoSign(note.tone)}}
            }
        }
        Row(horizontalArrangement=Arrangement.spacedBy(4.dp)) {
            for(key in 0..2) RoundButton(SBerg.echoTones[key].str(),{play(key)},size=button,tone=Tones.Cream){echoSign(key)}
            if(notes.isNotEmpty()) RoundButton(SBerg.clearSong.str(),clear,size=button,tone=Tones.Cream,icon=Icons.Refresh)
            CloseButton(close,size=button)
        }
    }
}

package app.trollfoss.ui.berg

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.trollfoss.ui.components.PressSurface
import app.trollfoss.ui.components.Tones
import app.trollfoss.ui.theme.T

/** The whole label and illustration are one tap target, including the text beside the pictogram. */
@Composable
internal fun TrailButton(label:String,onClick:()->Unit,modifier:Modifier=Modifier,subtitle:String?=null,icon:DrawScope.()->Unit) {
    PressSurface(onClick=onClick,modifier=modifier.heightIn(min=52.dp),tone=Tones.Cream,
        contentPadding=PaddingValues(horizontal=12.dp,vertical=6.dp),description=subtitle ?: label) {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(9.dp)) {
            Canvas(Modifier.size(36.dp),onDraw=icon)
            Column {
                Text(label,color=T.Ink,fontSize=16.sp)
                if(subtitle!=null) Text(subtitle,color=T.Ink,fontSize=14.sp)
            }
        }
    }
}

internal object CaveIcons {
    fun mouse(scope:DrawScope)=with(scope) {
        val s=size.minDimension
        for(x in listOf(.24f,.76f)) {
            drawCircle(T.Ink,.204f*s,Offset(x*s,.28f*s))
            drawCircle(Color(0xFFC69585),.16f*s,Offset(x*s,.28f*s))
        }
        drawOval(T.Ink,Offset(.14f*s,.25f*s),Size(.72f*s,.61f*s))
        drawOval(Color(0xFFB69677),Offset(.18f*s,.28f*s),Size(.64f*s,.54f*s))
        for(x in listOf(.37f,.63f)) {
            drawCircle(T.Ink,.048f*s,Offset(x*s,.49f*s))
            val dir=if(x<.5f) -1 else 1
            drawLine(T.Ink,Offset(x*s,.66f*s),Offset((x+dir*.29f)*s,.71f*s),.035f*s,StrokeCap.Round)
        }
        drawCircle(Color(0xFFAB685D),.068f*s,Offset(.5f*s,.66f*s))
    }
    fun spring(scope:DrawScope)=with(scope) {
        val s=size.minDimension
        drawOval(Color(0xFF4C827E),Offset(.07f*s,.67f*s),Size(.86f*s,.22f*s),style=Stroke(.04f*s))
        val drop=Path().apply {
            moveTo(.5f*s,.08f*s)
            cubicTo(.45f*s,.26f*s,.25f*s,.44f*s,.28f*s,.56f*s)
            cubicTo(.32f*s,.79f*s,.68f*s,.79f*s,.72f*s,.56f*s)
            cubicTo(.75f*s,.43f*s,.56f*s,.24f*s,.5f*s,.08f*s);close()
        }
        drawPath(drop,Color(0xFF8FD5C6));drawPath(drop,T.Ink,style=Stroke(.045f*s))
    }
}

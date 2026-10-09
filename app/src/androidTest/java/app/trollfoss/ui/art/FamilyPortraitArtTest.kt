package app.trollfoss.ui.art

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.trollfoss.domain.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Proof sheets use the exact game renderer, fixed clocks and no private photo assets. */
@RunWith(AndroidJUnit4::class)
class FamilyPortraitArtTest {
    private val moreNames=listOf("Sondre","Elise","Sølve","Hedda","Berit","Olvar")
    private val world=WorldFactory.create()
    private val out get() = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), "figure-style").apply { mkdirs() }
    private val paper = Color(0xFFFFF6E8)
    private fun labelPaint(size: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color=0xff2b2140.toInt(); textSize=size; textAlign=Paint.Align.CENTER; typeface=Typeface.DEFAULT_BOLD
    }
    private fun save(bitmap: Bitmap, name: String) = File(out, name).outputStream().use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
    }
    private fun portrait(p: Person, face: Face, time: Float): Bitmap = Bitmap.createBitmap(400,520,Bitmap.Config.ARGB_8888).also { b ->
        CanvasDrawScope().draw(Density(1f),LayoutDirection.Ltr,Canvas(b.asImageBitmap()),Size(400f,520f)) {
            drawRect(paper)
            val anim=PersonAnim().apply { motion=false; figureTime=time; this.face=face; lookX=.2f }
            translate(200f,470f) { drawPortrait(p,anim,310f,Pen(3f,time)) }
        }
    }

    private fun DrawScope.drawPortrait(p: Person, a: PersonAnim, h: Float, pen: Pen) {
        drawPerson(p.species,p.look,Pose.STAND,a,h,pen)
        world.worn(p,Slot.FACE)?.let { glasses ->
            val scale=Anatomy.headRadius(p.species)*2f*h/glasses.type.fitsHead
            val gh=glasses.type.h*scale
            val point=Anatomy.fraction(p.species,Pose.STAND,Part.GLASSES)
            figureHead(p.species,a,h,Pose.STAND) {
                translate(point[0]*h,point[1]*h+gh*.5f) {
                    drawThing(glasses.type,glasses.variant,0,glasses.type.w*scale,gh,pen)
                }
            }
        }
    }

    @Test fun familyPortraitsKeepTheirExpressionsWhenMotionIsOff() {
        val people=world.people()
        for (name in listOf("Eira","Olve","Tuva","Øyvind","Eilev")+moreNames) {
            val p=people.single { it.name==name }
            val still=portrait(p,Face.HAPPY,0f)
            val later=portrait(p,Face.HAPPY,8f)
            val laugh=portrait(p,Face.LAUGH,8f)
            try {
                assertTrue("$name stays still",still.sameAs(later))
                assertFalse("$name keeps a visible reaction",still.sameAs(laugh))
                save(still,"portrait-$name.png")
            } finally { still.recycle();later.recycle();laugh.recycle() }
        }
    }

    @Test fun captureMoreFamilyPortraits() {
        val family=moreNames.map { name -> world.people().single { it.name==name } }
        val sheet=Bitmap.createBitmap(2160,1100,Bitmap.Config.ARGB_8888)
        try {
            CanvasDrawScope().draw(Density(1f),LayoutDirection.Ltr,Canvas(sheet.asImageBitmap()),Size(2160f,1100f)) {
                drawRect(paper)
                for((col,p) in family.withIndex()) for(row in 0..1) {
                    val a=PersonAnim().apply {
                        figureTime=1.2f+col*.37f;lookX=if(row==0) .2f else -.2f
                        face=if(row==0) Face.HAPPY else Face.LAUGH;wave=if(row==0) 0f else .8f
                    }
                    translate(180f+col*360f,515f+row*480f) {
                        drawPortrait(p,a,300f*p.look.height/1.03f,Pen(3.1f,1.2f))
                    }
                }
            }
            android.graphics.Canvas(sheet).apply {
                drawText("Trollfoss · fleire i familien",1080f,60f,labelPaint(38f))
                family.forEachIndexed { i,p -> drawText(p.name,180f+i*360f,126f,labelPaint(34f)) }
            }
            save(sheet,"more-family-portraits.png")
        } finally { sheet.recycle() }
    }

    @Test fun captureFamilyAndWholeCastInTheSameStyle() {
        val people=world.people()
        val family=listOf("Eira","Olve","Tuva","Øyvind","Eilev").map { name -> people.single { it.name==name } }
        val sheet=Bitmap.createBitmap(1800,1100,Bitmap.Config.ARGB_8888)
        try {
            CanvasDrawScope().draw(Density(1f),LayoutDirection.Ltr,Canvas(sheet.asImageBitmap()),Size(1800f,1100f)) {
                drawRect(paper)
                for ((col,p) in family.withIndex()) for (row in 0..1) {
                    val a=PersonAnim().apply {
                        figureTime=1.2f+col*.37f; lookX=if(row==0) .2f else -.2f
                        face=if(row==0) Face.HAPPY else Face.LAUGH; wave=if(row==0) 0f else .8f
                    }
                    translate(180f+col*360f,515f+row*480f) {
                        drawPerson(p.species,p.look,Pose.STAND,a,300f*p.look.height/1.03f,Pen(3.1f,1.2f))
                    }
                }
            }
            android.graphics.Canvas(sheet).apply {
                drawText("Trollfoss · familien",900f,60f,labelPaint(38f))
                family.forEachIndexed { i,p -> drawText(p.name,180f+i*360f,126f,labelPaint(34f)) }
            }
            save(sheet,"family-portraits.png")
        } finally { sheet.recycle() }

        // All named residents, followed by one of each animal that has no named resident.
        val named=people.filter { it.name.isNotBlank() }
        val cast=named+people.filter { p -> named.none { it.species==p.species } }.distinctBy { it.species }
        val columns=6; val cellW=270; val cellH=335
        val all=Bitmap.createBitmap(columns*cellW,100+((cast.size+columns-1)/columns)*cellH,Bitmap.Config.ARGB_8888)
        try {
            CanvasDrawScope().draw(Density(1f),LayoutDirection.Ltr,Canvas(all.asImageBitmap()),Size(all.width.toFloat(),all.height.toFloat())) {
                drawRect(paper)
                for ((i,p) in cast.withIndex()) {
                    val a=PersonAnim().apply { motion=false;lookX=if(i%2==0) .3f else -.3f }
                    val h=if(p.species==Species.FOLK) 205f*p.look.height else 190f
                    translate((i%columns)*cellW+cellW/2f,100f+(i/columns)*cellH+275f) {
                        drawPortrait(p,a,h,Pen(2.4f))
                    }
                }
            }
            val labels=mapOf(Species.CAT to "Katt",Species.DOG to "Hund",Species.BUNNY to "Kanin",Species.DRAGON to "Drake",
                Species.ELK to "Elg",Species.PUFFIN to "Lundefugl",Species.COW to "Ku",Species.SHEEP to "Sau",
                Species.CHICKEN to "Høne",Species.HORSE to "Hest",Species.GOAT to "Geit")
            android.graphics.Canvas(all).apply {
                drawText("Trollfoss · heile figurflokken",all.width/2f,58f,labelPaint(36f))
                cast.forEachIndexed { i,p -> drawText(p.name.ifBlank { labels[p.species] ?: p.species.name },
                    (i%columns)*cellW+cellW/2f,100f+(i/columns)*cellH+318f,labelPaint(25f)) }
            }
            save(all,"whole-cast.png")
        } finally { all.recycle() }
    }
}

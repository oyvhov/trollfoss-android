package app.trollfoss.ui.art

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
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

/** Fixed clocks and real drawing code: these are render evidence, not mock gameplay screenshots. */
@RunWith(AndroidJUnit4::class)
class FigureStyleArtTest {
    private val out get() = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), "figure-style").apply { mkdirs() }
    private fun examples(): List<Person> {
        val people = WorldFactory.create().people()
        return listOf(people.first { it.name == "Hedda" }, people.first { it.name == "Eilev" },
            people.first { it.species == Species.CAT }, people.first { it.species == Species.ROBOT },
            people.first { it.species == Species.GHOST })
    }

    private fun render(species: Species, look: Look, a: PersonAnim, time: Float): Bitmap =
        Bitmap.createBitmap(400, 520, Bitmap.Config.ARGB_8888).also { bitmap ->
            CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(bitmap.asImageBitmap()), Size(400f,520f)) {
                drawRect(Color(0xFFFFF6E8))
                translate(200f,470f) { drawPerson(species,look,a.pose,a,260f,Pen(3f,time),holding=a.holding) }
            }
        }

    @Test fun reducedMotionFreezesEverySpeciesButStillShowsAReaction() {
        for (species in Species.entries) {
            val a=PersonAnim().apply { motion=false; lookX=.4f }
            val first=render(species,Look(),a,1f)
            a.figureTime=9f
            val later=render(species,Look(),a,9f)
            a.face=Face.WOW
            val reaction=render(species,Look(),a,9f)
            try {
                assertTrue("$species keeps still", first.sameAs(later))
                assertFalse("$species still reacts", first.sameAs(reaction))
            } finally { first.recycle(); later.recycle(); reaction.recycle() }
        }
    }

    @Test fun captureEverySpeciesAndEveryPose() {
        val cellW=260; val cellH=360
        val bitmap=Bitmap.createBitmap(cellW*Pose.entries.size,cellH*Species.entries.size,Bitmap.Config.ARGB_8888)
        try {
            CanvasDrawScope().draw(Density(1f),LayoutDirection.Ltr,Canvas(bitmap.asImageBitmap()),Size(bitmap.width.toFloat(),bitmap.height.toFloat())) {
                drawRect(Color(0xFFFFF6E8))
                for ((row,species) in Species.entries.withIndex()) for ((col,pose) in Pose.entries.withIndex()) {
                    val a=PersonAnim().apply { this.pose=pose;figureTime=1.3f;face=if(pose==Pose.LIE) Face.SLEEP else Face.GRIN;wave=.7f }
                    translate(col*cellW+cellW/2f,row*cellH+270f) { drawPerson(species,Residents.eilevLook(),pose,a,170f,Pen(2.4f,1.3f)) }
                }
            }
            val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=0xff2b2140.toInt();textSize=20f }
            android.graphics.Canvas(bitmap).apply {
                for((row,species) in Species.entries.withIndex()) for((col,pose) in Pose.entries.withIndex()) drawText("${species.name} / ${pose.name}",col*cellW+12f,row*cellH+30f,paint)
            }
            File(out,"all-species-poses.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
        } finally { bitmap.recycle() }
    }

    @Test fun captureHairAndClothesAtWorkshopExtremes() {
        val rows=(Styles.HAIRS*2+7)/8
        val bitmap=Bitmap.createBitmap(1800,rows*355+25,Bitmap.Config.ARGB_8888)
        try {
            CanvasDrawScope().draw(Density(1f),LayoutDirection.Ltr,Canvas(bitmap.asImageBitmap()),Size(bitmap.width.toFloat(),bitmap.height.toFloat())) {
                drawRect(Color(0xFFFFF6E8))
                for (i in 0 until Styles.HAIRS*2) {
                    val large=i%2==1
                    val look=Residents.eilevLook().copy(hair=i/2,hairSize=if(large) 1.7f else .65f,hairLength=if(large) 1.8f else .35f,
                        top=(i/2)%Styles.TOPS,bottom=(i/2)%Styles.BOTTOMS,face=(i/2)%Styles.FACES).safe()
                    val a=PersonAnim().apply {motion=false}
                    translate(110f+(i%8)*225f,300f+(i/8)*355f) { drawPerson(Species.FOLK,look,Pose.STAND,a,160f,Pen(2f,0f)) }
                }
            }
            File(out,"workshop-extremes.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
        } finally { bitmap.recycle() }
    }

    @Test fun captureEilevPortrait() {
        val a=PersonAnim().apply {figureTime=1.2f;lookX=.3f}
        val bitmap=render(Species.FOLK,Residents.eilevLook(),a,1.2f)
        try { File(out,"eilev.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) } }
        finally {bitmap.recycle()}
    }

    @Test fun captureFiveFiguresAndDrawingCost() {
        val people = examples()
        val bitmap = Bitmap.createBitmap(1800, 1100, Bitmap.Config.ARGB_8888)
        val scope = CanvasDrawScope()
        fun draw(time: Float) = scope.draw(Density(1f), LayoutDirection.Ltr, Canvas(bitmap.asImageBitmap()), Size(1800f, 1100f)) {
            drawRect(Color(0xFFFFF6E8))
            people.forEachIndexed { i, p ->
                for (row in 0..1) {
                    val a = PersonAnim().apply {
                        figureTime = time + i * .37f
                        face = if (row == 0) Face.HAPPY else Face.LAUGH
                        lookX = if (row == 0) .5f else -.2f; lookY = -.1f
                        wave = if (row == 0) 0f else .8f
                    }
                    translate(180f + i * 360f, 500f + row * 500f) {
                        drawPerson(p.species, p.look, Pose.STAND, a, 270f, Pen(3.1f, time), seed = i * .37f)
                    }
                }
            }
        }
        try {
            draw(1.2f)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xff2b2140.toInt(); textSize = 37f; textAlign = Paint.Align.CENTER; typeface = android.graphics.Typeface.DEFAULT_BOLD }
            android.graphics.Canvas(bitmap).apply {
                drawText("Trollfoss", 900f, 60f, paint)
                people.forEachIndexed { i, p -> drawText(if (p.species == Species.CAT) "Katt" else p.name, 180f + i * 360f, 120f, paint) }
            }
            File(out, "five-figures.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            repeat(12) { draw(it / 13f) }
            val samples = LongArray(60) {
                val start = System.nanoTime(); draw(it / 13f); System.nanoTime() - start
            }.sorted()
            File(out, "draw-cost.txt").writeText("Software canvas; 10 figures at h=270px; 12 warmup frames; 60 samples.\nmedian_ms=${samples[30] / 1e6}\np95_ms=${samples[57] / 1e6}\n")
            assertTrue(File(out, "five-figures.png").length() > 10_000)
        } finally { bitmap.recycle() }
    }
}

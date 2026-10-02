package app.trollfoss

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import app.trollfoss.domain.*
import app.trollfoss.ui.art.*

/** Debug contact sheets: every hair, named friend and pose, with extreme sliders and worn attachments. */
class WorkshopSheetActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).hide(WindowInsetsCompat.Type.systemBars())
        val page = intent.getStringExtra("page") ?: "hair"
        val people = WorldFactory.create().people().filter { it.species == Species.FOLK }
        setContent {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(Color(0xFFFFF1DC))
                val count = if (page == "folk") people.size else 18
                val cols = 6; val rows = (count + cols - 1) / cols
                val cw = size.width / cols; val ch = size.height / rows
                for (i in 0 until count) {
                    val x = (i % cols + 0.5f) * cw; val y = (i / cols + 0.87f) * ch
                    val pose = if (page == "poses") Pose.entries[i % Pose.entries.size] else Pose.STAND
                    val look = when (page) {
                        "folk" -> people[i].look
                        "poses" -> Look(hair = 9 + i % 9, hairSize = 1.5f, hairLength = 1.6f, eyeColor = i % Palette.eyes.size, eyeSize = 1.25f, face = i % Styles.FACES, top = i % Styles.TOPS, topColor = i % Palette.cloth.size, pattern = i % Styles.PATTERNS)
                        else -> Look(hair = i, hairSize = if (i % 3 == 0) 1.5f else 1f, hairLength = if (i % 2 == 0) 1.6f else 0.65f, eyeColor = i % Palette.eyes.size, face = i % Styles.FACES, top = i % Styles.TOPS, topColor = i % Palette.cloth.size, pattern = i % Styles.PATTERNS, extra = i % Styles.EXTRAS)
                    }
                    val h = ch * 0.63f
                    val p = Person(i + 1, Species.FOLK, look, 1f).apply { this.x = 0f; this.y = 0f; anim.pose = pose }
                    val pen = Pen(ch * 0.007f, 1f)
                    translate(x, y) {
                        drawPerson(Species.FOLK, look, pose, p.anim, h, pen, holding = page == "poses")
                        if (page == "poses") {
                            val at = Anatomy.at(p, Part.HAT)
                            translate(at[0] / p.h * h, at[1] / p.h * h) { drawThing(ThingType.CROWN, 0, 0, h * 0.5f, h * 0.18f, pen) }
                            val hand = Anatomy.at(p, Part.HAND)
                            translate(hand[0] / p.h * h, hand[1] / p.h * h) { drawThing(ThingType.APPLE, 0, 0, h * 0.14f, h * 0.14f, pen) }
                        }
                    }
                    drawLine(Ink.line.copy(alpha = 0.15f), Offset(i % cols * cw, y + ch * 0.04f), Offset((i % cols + 1) * cw, y + ch * 0.04f), strokeWidth = 2f)
                }
            }
        }
    }
}

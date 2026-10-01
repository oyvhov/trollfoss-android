package app.trollfoss

import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import app.trollfoss.domain.Anatomy
import app.trollfoss.domain.Face
import app.trollfoss.domain.Look
import app.trollfoss.domain.Part
import app.trollfoss.domain.Person
import app.trollfoss.domain.PersonAnim
import app.trollfoss.domain.Pose
import app.trollfoss.domain.Slot
import app.trollfoss.domain.Species
import app.trollfoss.domain.ThingType
import app.trollfoss.ui.art.Pen
import app.trollfoss.ui.art.drawHouseXray
import app.trollfoss.ui.art.drawPerson
import app.trollfoss.ui.art.drawThing
import app.trollfoss.ui.art.groundShadow
import app.trollfoss.ui.art.headWidth
import app.trollfoss.ui.screens.drawSpeciesThumb

/**
 * Debug only: a contact sheet of Rolf and Sture for checking the art, since the two of them live on the
 * floors of a big house. `--es page faces|poses|worn|small|big|peek` picks the sheet; `--ef t 1.0` the
 * clock (blinks, wobble, peeks); `--es dark 1` the attic backdrop. Hats, glasses and held things are drawn
 * with the same anchors and sizes as the engine uses.
 */
class FigurarSheetActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val page = intent.getStringExtra("page") ?: "faces"
        val t = intent.getFloatExtra("t", 1.0f)
        val dark = intent.getStringExtra("dark") == "1"
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setBackgroundDrawable(ColorDrawable(AndroidColor.BLACK))
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
        setContent {
            Canvas(Modifier.fillMaxSize()) {
                sheet(page, t, dark)
                Log.i("FigurarSheet", "drawn $page ${size.width.toInt()}x${size.height.toInt()}")
            }
        }
    }
}

private class Cell(
    val species: Species,
    val pose: Pose = Pose.STAND,
    val face: Face = Face.HAPPY,
    val look: Look = Look(skin = 0),
    val hat: ThingType? = null,
    val glasses: ThingType? = null,
    val hand: ThingType? = null,
    val time: Float? = null,
    val xray: Boolean = false,
    val thumb: Boolean = false,
    val tweak: PersonAnim.() -> Unit = {},
)

private fun DrawScope.sheet(page: String, t: Float, dark: Boolean) {
    val wall = if (dark || page == "xray") Color(0xFF1B3A38) else Color(0xFFF1E4CB)
    val floor = if (dark) Color(0xFF45352F) else Color(0xFFC98A55)
    drawRect(wall)
    val rows: List<List<Cell>> = when (page) {
        "faces" -> listOf(Species.ROBOT, Species.GHOST).map { s -> Face.entries.map { Cell(s, face = it) } }
        "poses" -> listOf(Species.ROBOT, Species.GHOST).map { s ->
            listOf(
                Cell(s),
                Cell(s, tweak = { walkTo = 1f; walkPhase = 0.5f }),
                Cell(s, tweak = { walkTo = 1f; walkPhase = 1.5f }),
                Cell(s, Pose.SIT),
                Cell(s, Pose.LIE, Face.SLEEP),
                Cell(s, Pose.HELD, Face.OOH),
                Cell(s, Pose.SWIM),
                Cell(s, Pose.FLOAT, Face.WOW),
                Cell(s, tweak = { dance = 1.2f }),
                Cell(s, tweak = { wave = 0.85f }),
                Cell(s, tweak = { talk = 1f }),
                Cell(s, tweak = { sneeze = 0.5f }),
            )
        }
        "worn" -> listOf(Species.ROBOT, Species.GHOST).map { s ->
            listOf(
                Cell(s, hat = ThingType.CAP, glasses = ThingType.SUNGLASSES),
                Cell(s, hat = ThingType.CROWN, face = Face.GRIN),
                Cell(s, hat = ThingType.WIZARD_HAT, glasses = ThingType.ROUND_GLASSES),
                Cell(s, hand = ThingType.APPLE, hat = ThingType.PARTY_HAT),
                Cell(s, hand = ThingType.BALLOON),
                Cell(s, hand = ThingType.CAKE, face = Face.WOW),
                Cell(s, hat = ThingType.CHEF_HAT, tweak = { wave = 0.8f }),
                Cell(s, hat = ThingType.SUN_HAT, glasses = ThingType.STAR_GLASSES, pose = Pose.SIT),
            )
        }
        "peek" -> listOf(Species.ROBOT, Species.GHOST).map { s ->
            listOf(7.4f, 8.0f, 8.6f, 9.0f, 9.4f, 10.0f).map { Cell(s, time = it) }
        }
        "big" -> listOf(listOf(Cell(Species.ROBOT, hand = ThingType.APPLE), Cell(Species.GHOST, hand = ThingType.APPLE)))
        "xray" -> listOf(listOf(Cell(Species.ROBOT, xray = true, time = 1.0f), Cell(Species.ROBOT, xray = true, time = 1.4f), Cell(Species.GHOST, xray = true, time = 1.0f), Cell(Species.GHOST, xray = true, time = 1.3f)))
        "small" -> listOf(listOf(Cell(Species.ROBOT), Cell(Species.GHOST)))
        "thumb" -> listOf(listOf(Cell(Species.ROBOT, thumb = true), Cell(Species.GHOST, thumb = true), Cell(Species.CAT, thumb = true), Cell(Species.GOAT, thumb = true)))
        else -> listOf(listOf(Cell(Species.ROBOT)))
    }
    val rowH = size.height / rows.size
    for ((r, row) in rows.withIndex()) {
        val top = r * rowH
        drawRect(floor, Offset(0f, top + rowH * 0.8f), Size(size.width, rowH * 0.2f))
        if (page == "small") {
            smallRow(row, t)
            continue
        }
        val cellW = size.width / row.size
        for ((i, cell) in row.withIndex()) {
            val hp = minOf(cellW * 0.95f, rowH * 0.74f * (if (page == "big") 1.05f else 1f))
            val base = top + rowH * 0.9f
            figure(cell, cellW * (i + 0.5f), base, hp, cell.time ?: t, dark)
        }
    }
}

/** The "small" page: the same figures at the real sizes of the game, 40 to 90 dp, on light and dark. */
private fun DrawScope.smallRow(row: List<Cell>, t: Float) {
    val sizes = listOf(40f, 50f, 60f, 90f)
    val backs = listOf(Color(0xFFF1E4CB), Color(0xFF2B2744), Color(0xFFC98A55), Color(0xFF7FB4E8))
    val bandH = size.height / backs.size
    var y = 0f
    for (back in backs) {
        drawRect(back, Offset(0f, y), Size(size.width, bandH))
        var x = 30f * density
        for (cell in row) {
            for (dp in sizes) {
                val hp = dp * density
                figure(cell, x + hp * 0.5f, y + bandH * 0.9f, hp, t, back == backs[1])
                x += hp * 1.1f
            }
            x += 40f * density
        }
        y += bandH
    }
}

private fun DrawScope.figure(cell: Cell, cx: Float, base: Float, hp: Float, t: Float, dark: Boolean) {
    if (cell.thumb) {
        // As the task cards and stickers show it: a square box, drawn by drawSpeciesThumb.
        for ((k, side) in listOf(0.55f, 0.3f, 0.16f).withIndex()) {
            val box = hp * side * 1.9f
            val x0 = cx - hp * 0.5f + k * hp * 0.9f * (if (k == 0) 0f else 1f) + (if (k == 2) hp * 0.5f else 0f) - (if (k == 0) 0f else hp * 0.4f)
            drawRect(Color.White.copy(alpha = 0.55f), Offset(x0, base - box), Size(box, box))
            drawSpeciesThumb(cell.species, androidx.compose.ui.geometry.Rect(x0, base - box, x0 + box, base))
        }
        return
    }
    val person = Person(1, cell.species, cell.look, 1f)
    val a = person.anim
    a.face = cell.face
    a.pose = cell.pose
    a.apply(cell.tweak)
    val u = hp / person.h
    val pen = Pen(hp * 0.0125f, t, if (dark) 0.8f else 0f)
    // Seats and beds are drawn under the figure so sitting and lying read.
    var oy = base
    when (cell.pose) {
        Pose.SIT -> {
            oy = base - hp * 0.2f
            drawRect(Color(0xFF8A5A3B), Offset(cx - hp * 0.4f, oy), Size(hp * 0.8f, hp * 0.05f))
            drawRect(Color(0xFF8A5A3B), Offset(cx - hp * 0.36f, oy), Size(hp * 0.05f, base - oy))
            drawRect(Color(0xFF8A5A3B), Offset(cx + hp * 0.31f, oy), Size(hp * 0.05f, base - oy))
        }
        Pose.LIE -> {
            oy = base - hp * 0.24f
            drawRect(Color(0xFFD9E3F5), Offset(cx - hp * 0.7f, oy + hp * 0.12f), Size(hp * 1.4f, hp * 0.1f))
        }
        else -> groundShadow(cx, base, hp * 0.6f)
    }
    val holding = cell.hand != null
    translate(cx, oy) {
        if (cell.xray) drawHouseXray(cell.species, hp, pen, t) else drawPerson(cell.species, cell.look, cell.pose, a, hp, pen, holding, seed = 0.3f)
        person.x = 0f
        person.y = 0f
        for (type in listOfNotNull(cell.hat, cell.glasses, cell.hand)) drawWorn(person, type, u, pen)
    }
}

/** The same sizes and anchors as the engine's drawCarried. */
private fun DrawScope.drawWorn(p: Person, type: ThingType, u: Float, pen: Pen) {
    val slot = type.slot
    val s = when (slot) {
        Slot.HEAD, Slot.FACE -> headWidth(p.species, p.h) / type.fitsHead
        Slot.HAND -> if (p.species.pet) 0.8f else 1f
    }
    val c = when (slot) {
        Slot.HEAD -> Anatomy.at(p, Part.HAT).let { Offset(it[0], it[1] - type.h * s * 0.45f) }
        Slot.FACE -> Anatomy.at(p, Part.GLASSES).let { Offset(it[0], it[1]) }
        Slot.HAND -> Anatomy.at(p, Part.HAND).let { Offset(it[0], it[1] - (if (type == ThingType.BALLOON) type.h * 0.5f else 0f)) }
    }
    translate(c.x * u, c.y * u + type.h * s * u * 0.5f) {
        scale(s, s, pivot = Offset.Zero) {
            drawThing(type, 0, 0, type.w * u, type.h * u, Pen(pen.lw / s, pen.t))
        }
    }
}

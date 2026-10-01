package app.trollfoss.ui.play

import androidx.compose.ui.graphics.Color
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.AtticCode
import app.trollfoss.domain.Face
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.Species
import app.trollfoss.domain.Thing
import kotlin.math.pow

/**
 * Storhuset, attic floor: effects. [code] is in [app.trollfoss.domain.HouseFx].ATTIC up to the next block (100 codes),
 * [arg] is the small number the rule sent along (see [AtticCode] for what each code means). Everything here is
 * kind: Sture giggles, dust flies, nobody is frightened.
 */
object AtticFx {
    private val dust = Color(0xFFE9E1D0)
    private val gold = Color(0xFFFFD447)
    private val paper = Color(0xFFF7F0DC)

    fun play(s: FxStage, code: Int, arg: Int, x: Float, y: Float, fixture: Fixture?, thing: Thing?) {
        val sture = s.world.people().firstOrNull { it.species == Species.GHOST && it.name == "Sture" }
        when (code) {
            AtticCode.HIDE -> {
                // He whooshes away to hide and giggles behind his hand.
                s.sfx(Sfx.SWISH, 0.6f, 1.6f)
                s.after(0.12f) { s.sfx(Sfx.GIGGLE, 0.45f, 1.5f) }
                s.burst(PKind.DUST, x, y, 7, 0.25f, 0.014f, Color(0xFFDDE8FF), up = 0.05f)
                s.burst(PKind.STAR, x, y, 4, 0.2f, 0.01f, Color(0xFFBFD6FF))
            }
            AtticCode.TELL -> {
                // A muffled giggle from under the sheet, and a few sparkles of dust: «here I am».
                s.sfx(Sfx.GIGGLE, 0.42f, 1.7f)
                s.after(0.25f) { s.sfx(Sfx.GIGGLE, 0.3f, 1.9f) }
                s.burst(PKind.DUST, x, y - 0.04f, 4, 0.12f, 0.01f, dust, up = 0.1f)
                s.burst(PKind.SPARK, x, y, 3, 0.15f, 0.008f, Color(0xFFBFD6FF))
            }
            AtticCode.CAUGHT -> {
                s.sfx(Sfx.SPARKLE, 0.8f)
                s.after(0.1f) { s.sfx(Sfx.CHIME, 0.7f, 1.2f) }
                sture?.let { p -> s.voice(p, Sfx.GIGGLE, 0.8f); s.after(0.35f) { s.voice(p, Sfx.AT_OOO, 0.5f, own = true) } }
                s.burst(PKind.CONFETTI, x, y, 16, 0.7f, 0.011f, up = 0.6f, life = 1.6f)
                s.burst(PKind.HEART, x, y - 0.05f, 4, 0.2f, 0.014f, up = 0.3f)
                s.laughAround(x, sture)
                s.haptic()
                s.changed()
            }
            AtticCode.STICKER -> {
                // The third time: a fanfare, and a sticker tumbles out of his sheet.
                s.sfx(Sfx.FANFARE, 0.9f)
                s.burst(PKind.STAR, x, y, 22, 0.8f, 0.014f, gold, up = 0.6f, life = 1.6f)
                s.burst(PKind.CONFETTI, x, y, 30, 0.9f, 0.012f, up = 0.8f, life = 2f)
                s.haptic()
                s.changed()
            }
            AtticCode.SNEEZE -> {
                // «Ah … ah … ATSJO!» and a big cloud of dust.
                sture?.let { p -> s.voice(p, Sfx.SNEEZE, 0.8f, own = true) }
                s.after(0.75f) {
                    s.burst(PKind.DUST, x, y, 16, 0.5f, 0.02f, dust, up = 0.1f, life = 1.2f)
                    s.burst(PKind.SPARK, x, y, 6, 0.5f, 0.01f, Color.White)
                    s.sfx(Sfx.POOF, 0.5f, 1.3f)
                }
            }
            AtticCode.TICKLE -> {
                sture?.let { p -> s.voice(p, Sfx.GIGGLE, 0.7f) }
                s.person(arg)?.let { p ->
                    s.voice(p, Sfx.TICKLE, 0.8f)
                    s.faces(p, Face.LAUGH, 1.8f, Face.GRIN, 1f)
                }
                s.burst(PKind.SPARK, x, y, 8, 0.3f, 0.01f, Color(0xFFBFD6FF))
                s.burst(PKind.HEART, x, y - 0.06f, 2, 0.15f, 0.012f, up = 0.3f)
            }
            AtticCode.BOO -> {
                sture?.let { p -> s.voice(p, Sfx.AT_OOO, 0.7f, own = true) }
                s.person(arg)?.let { p ->
                    s.faces(p, Face.OOH, 0.55f, Face.LAUGH, 1.3f)
                    s.after(0.55f) { s.voice(p, Sfx.GIGGLE, 0.7f) }
                }
                s.burst(PKind.STAR, x, y, 5, 0.25f, 0.01f, Color(0xFFBFD6FF))
            }
            AtticCode.SWAP -> {
                s.sfx(Sfx.POP, 0.45f, 1.1f + s.random.nextFloat() * 0.3f)
                s.burst(PKind.SPARK, x, y, 6, 0.3f, 0.01f, Color(0xFFBFD6FF))
                s.burst(PKind.DUST, x, y, 3, 0.15f, 0.012f, Color.White, up = 0.05f)
            }
            AtticCode.SCARED -> {
                sture?.let { p -> s.voice(p, Sfx.OOH, 0.8f) }
                s.sfx(Sfx.SQUEAK, 0.45f, 1.9f)
                s.burst(PKind.DROP, x, y - 0.03f, 4, 0.3f, 0.009f, Color(0xFF9ADAFF), up = 0.3f)
                s.burst(PKind.STAR, x, y, 5, 0.25f, 0.01f, Color(0xFFBFD6FF))
            }
            AtticCode.KEY -> {
                // The key flies out of his sheet with a spring and a giggle.
                s.sfx(Sfx.AT_SPRING, 0.6f)
                sture?.let { p -> s.voice(p, Sfx.GIGGLE, 0.8f) }
                s.burst(PKind.STAR, x, y, 14, 0.5f, 0.013f, gold, up = 0.4f)
                s.laughAround(x, sture)
            }
            AtticCode.KEY_HINT -> {
                // Something golden glints under the sheet.
                s.sfx(Sfx.CHIME, 0.22f, 1.8f)
                s.burst(PKind.SPARK, x, y, 3, 0.1f, 0.011f, gold, up = 0.05f, life = 0.7f)
            }
            AtticCode.RETURN -> {
                s.sfx(Sfx.WHOOSH, 0.5f, 1.3f)
                s.after(0.25f) { sture?.let { p -> s.voice(p, Sfx.AT_OOO, 0.6f, own = true) } }
                s.burst(PKind.STAR, x, y, 8, 0.4f, 0.011f, Color(0xFFBFD6FF), up = 0.5f)
            }
            AtticCode.BLOW -> {
                s.sfx(Sfx.SWISH, 0.5f, 1.9f)
                s.after(0.2f) { sture?.let { p -> s.voice(p, Sfx.GIGGLE, 0.5f) } }
                s.burst(PKind.SMOKE, x, y, 4, 0.1f, 0.016f, Color(0xFFD8D4DC), up = 0.2f, life = 1.2f)
            }

            // ---- the sheets, the trunk, the horse, the spiders, the cartons and the window
            AtticCode.SHEET -> when (arg) {
                2 -> {
                    s.sfx(Sfx.SWISH, 0.4f, 0.8f)
                    s.burst(PKind.DUST, x, y, 5, 0.2f, 0.016f, dust, up = 0.05f)
                }
                else -> {
                    s.sfx(Sfx.SWISH, 0.5f, 0.9f)
                    s.sfx(Sfx.POOF, 0.35f, 1.2f)
                    s.burst(PKind.DUST, x, y, 12, 0.4f, 0.02f, dust, up = 0.1f, life = 1.2f)
                    if (arg == 1) {
                        s.after(0.15f) { s.sfx(Sfx.POP, 0.7f, 1.1f) }
                        s.burst(PKind.SPARK, x, y, 6, 0.4f, 0.011f, Color.White)
                    }
                }
            }
            AtticCode.TRUNK -> {
                s.sfx(Sfx.AT_CREAK, 0.6f, 0.9f)
                val tint = listOf(Color(0xFFFFC83D), Color(0xFFDDE8FF), Color(0xFFFF9EC7), Color(0xFFFFB02E), Color.White, Color(0xFFB9A2F0))[arg % 6]
                for (i in 0 until 3) s.after(0.15f + i * 0.12f) { s.sfx(Sfx.POP, 0.7f, 0.85f + 0.18f * i) }
                s.after(0.2f) { s.sfx(Sfx.SPARKLE, 0.6f) }
                s.burst(PKind.STAR, x, y, 10, 0.55f, 0.012f, tint, up = 0.6f)
                s.burst(PKind.DUST, x, y, 8, 0.35f, 0.016f, dust, up = 0.1f)
                s.laughAround(x, null)
            }
            AtticCode.TRUNK_SHUT -> {
                s.sfx(Sfx.SHUT, 0.6f, 0.8f)
                s.sfx(Sfx.AT_CREAK, 0.35f, 1.4f)
                s.burst(PKind.DUST, x, y, 5, 0.2f, 0.014f, dust, up = 0.05f)
            }
            AtticCode.HORSE -> when (arg) {
                2 -> {
                    s.sfx(Sfx.CHOMP, 0.6f, 0.9f)
                    s.after(0.3f) { s.sfx(Sfx.NEIGH, 0.7f, 1.1f) }
                    s.burst(PKind.HEART, x, y - 0.08f, 5, 0.25f, 0.015f, up = 0.3f)
                    s.laughAround(x, null)
                }
                1 -> {
                    s.sfx(Sfx.NEIGH, 0.6f, 1.2f)
                    for (i in 0 until 4) s.after(i * 0.28f) { s.sfx(Sfx.AT_CREAK, 0.4f, 1.1f + 0.1f * (i % 2)) }
                    s.burst(PKind.DUST, x, y + 0.05f, 10, 0.4f, 0.02f, dust, up = 0.05f)
                }
                else -> {
                    s.sfx(Sfx.AT_CREAK, 0.5f, 1f)
                    s.after(0.5f) { s.sfx(Sfx.AT_CREAK, 0.4f, 1.15f) }
                    s.burst(PKind.DUST, x, y + 0.05f, 4, 0.2f, 0.014f, dust, up = 0.05f)
                }
            }
            AtticCode.SPIDER -> {
                s.sfx(Sfx.AT_KNIT, 0.6f, 1f + arg * 0.07f)
                s.after(0.5f) { s.sfx(Sfx.GIGGLE, 0.3f, 2f) }
                val yarn = listOf(Color(0xFFE8A6B0), Color(0xFFE8B04A), Color(0xFF6AB7C2))
                if (arg >= 4) {
                    // A new sweater is ready: tossed down with a flourish.
                    s.sfx(Sfx.MAGIC, 0.5f, 1.2f)
                    s.after(0.3f) { s.sfx(Sfx.POP, 0.7f, 1f) }
                    s.burst(PKind.CONFETTI, x, y, 14, 0.5f, 0.011f, yarn[s.random.nextInt(3)], up = 0.5f, life = 1.5f)
                    s.burst(PKind.HEART, x, y, 3, 0.2f, 0.013f, up = 0.3f)
                } else {
                    s.burst(PKind.SPARK, x, y, 5, 0.25f, 0.01f, yarn[arg % 3])
                }
            }
            AtticCode.CARTON -> {
                s.sfx(Sfx.AT_SPRING, 0.8f, 0.95f + s.random.nextFloat() * 0.2f)
                s.sfx(Sfx.OPEN, 0.5f, 1.2f)
                s.burst(PKind.CRUMB, x, y, 8, 0.4f, 0.012f, Color(0xFFC9A06A), up = 0.4f)
                s.burst(PKind.STAR, x, y - 0.03f, 4, 0.3f, 0.011f, gold)
            }
            AtticCode.WINDOW -> if (arg == 1) {
                s.sfx(Sfx.OPEN, 0.6f, 1.1f)
                s.sfx(Sfx.AT_WIND, 0.5f)
                s.burst(PKind.DUST, x, y, 10, 0.5f, 0.016f, dust, up = 0.0f, life = 1.4f)
                if (s.world.night) {
                    s.after(0.7f) { s.sfx(Sfx.OWL, 0.5f, 1.1f) }
                } else {
                    s.after(0.4f) { s.sfx(Sfx.CHIRP, 0.5f, 1f) }
                    repeat(2) { s.particle(Particle(PKind.BIRD, x - 0.1f, y - 0.05f - it * 0.04f, 0.3f + it * 0.1f, -0.05f, 2.2f, 0.012f, Color(0xFF2B2140))) }
                }
            } else {
                s.sfx(Sfx.SHUT, 0.5f, 1.1f)
            }
            AtticCode.LIGHT -> if (arg == 1) {
                s.sfx(Sfx.CLICK, 0.5f, 1.3f)
                s.sfx(Sfx.POOF, 0.3f, 1.7f)
                s.burst(PKind.SPARK, x, y, 6, 0.3f, 0.011f, Color(0xFFFFC96B), up = 0.2f)
            } else {
                s.sfx(Sfx.POOF, 0.25f, 0.9f)
                s.burst(PKind.SMOKE, x, y, 3, 0.1f, 0.014f, Color(0xFFD8D4DC), up = 0.2f, life = 1.1f)
            }

            // ---- the nook
            AtticCode.CHAIR -> {
                s.sfx(Sfx.POOF, 0.4f, 1.1f)
                s.sfx(Sfx.BOING, 0.3f, 1.5f)
                s.burst(PKind.DUST, x, y, 8, 0.35f, 0.017f, dust, up = 0.1f)
            }
            AtticCode.RECORD -> {
                s.sfx(Sfx.AT_SCRATCH, 0.35f, 1f)
                s.after(0.35f) { tune(s, arg, x, y) }
            }
            AtticCode.RECORD_STOP -> s.sfx(Sfx.AT_SCRATCH, 0.35f, 0.75f)
            AtticCode.SKIP -> {
                s.sfx(Sfx.AT_SCRATCH, 0.5f, 1.1f)
                s.after(0.15f) { motif(s, 0.0f, x, y) }
            }
            AtticCode.BOOKS -> {
                s.sfx(Sfx.THUD, 0.5f, 1.2f)
                for (i in 0 until 3) s.after(i * 0.09f) { s.sfx(Sfx.PAGE, 0.6f, 1f + i * 0.15f) }
                s.burst(PKind.CONFETTI, x, y, 10, 0.5f, 0.012f, paper, up = 0.5f, life = 1.4f)
                s.burst(PKind.DUST, x, y + 0.1f, 6, 0.25f, 0.015f, dust, up = 0.05f)
            }
            AtticCode.BOOKS_BACK -> {
                s.sfx(Sfx.POP, 0.5f, 0.9f)
                s.sfx(Sfx.PAGE, 0.5f, 1.3f)
                s.burst(PKind.SPARK, x, y, 5, 0.3f, 0.01f, Color.White)
            }
            AtticCode.FORT -> if (arg == 1) {
                s.sfx(Sfx.SWISH, 0.5f, 1.2f)
                s.after(0.2f) { s.sfx(Sfx.GIGGLE, 0.35f, 1.3f) }
                s.burst(PKind.DUST, x, y, 6, 0.3f, 0.015f, dust, up = 0.05f)
            } else {
                s.sfx(Sfx.SWISH, 0.4f, 0.8f)
            }
            AtticCode.SHADOWS -> {
                when (arg) {
                    0 -> s.sfx(Sfx.CHIRP, 0.5f, 0.8f)
                    1 -> { s.sfx(Sfx.CHIRP, 0.6f, 1.2f); s.after(0.15f) { s.sfx(Sfx.CHIRP, 0.5f, 1.4f) } }
                    2 -> { s.sfx(Sfx.WOOF, 0.5f, 1.3f); s.after(0.25f) { s.sfx(Sfx.WOOF, 0.45f, 1.3f) } }
                    else -> s.sfx(Sfx.SPARKLE, 0.5f)
                }
                s.burst(PKind.SPARK, x, y, 5, 0.25f, 0.01f, Color(0xFFFFE9A8))
            }
            AtticCode.CLOCK -> {
                s.sfx(Sfx.CLICK, 0.5f, 0.7f)
                for (i in 0 until arg.coerceIn(1, 12)) s.after(0.3f + i * 0.85f) {
                    s.sfx(Sfx.AT_DONG, 0.8f, 1f)
                    s.particle(Particle(PKind.NOTE, x + (i % 2) * 0.04f, y - 0.05f, 0.05f, -0.1f, 1.6f, 0.012f, Color(0xFFB98A2A)))
                }
            }

            // ---- the tower
            AtticCode.STARS -> {
                s.sfx(Sfx.CHIME, 0.7f, listOf(0.85f, 0.95f, 1.07f, 1.2f, 1.42f)[arg.coerceIn(0, 4)])
                s.burst(PKind.STAR, x, y, 8, 0.4f, 0.012f, Color(0xFFFFE18A), up = 0.2f)
            }
            AtticCode.STARS_DONE -> {
                for ((i, r) in listOf(0.85f, 1.07f, 1.2f, 1.42f, 1.7f).withIndex()) s.after(i * 0.1f) { s.sfx(Sfx.CHIME, 0.7f, r) }
                s.after(0.6f) { s.sfx(Sfx.MAGIC, 0.8f) }
                s.burst(PKind.STAR, x, y, 20, 0.7f, 0.014f, gold, up = 0.4f, life = 1.5f)
                // a shooting star across the room
                s.particle(Particle(PKind.STAR, x - 0.3f, y - 0.15f, 1.2f, 0.35f, 1.4f, 0.016f, Color(0xFFFFF3C4), 0f, 360f))
                for (k in 1..5) s.particle(Particle(PKind.SPARK, x - 0.3f - k * 0.04f, y - 0.15f - k * 0.012f, 1.2f, 0.35f, 1.0f, 0.01f, Color(0xFFFFE18A)))
                s.haptic()
            }
            AtticCode.VANE -> {
                s.sfx(Sfx.AT_WIND, 0.45f)
                s.sfx(Sfx.AT_CREAK, 0.35f, 1.6f)
                s.burst(PKind.DUST, x, y, 4, 0.4f, 0.012f, Color.White, up = 0f)
            }
            AtticCode.ARMILLARY -> {
                s.sfx(Sfx.CHIME, 0.6f, if (arg == 1) 1.4f else 0.9f)
                s.sfx(Sfx.WHIRR, 0.25f, 1.6f)
                s.burst(PKind.STAR, x, y, 6, 0.35f, 0.011f, Color(0xFFFFE18A))
            }
            AtticCode.BAROMETER -> when (arg) {
                0 -> {
                    for (i in 0 until 3) s.after(i * 0.15f) { s.sfx(Sfx.DROP, 0.4f, 1.4f + 0.2f * i) }
                    s.burst(PKind.DROP, x, y - 0.1f, 6, 0.1f, 0.01f, Color(0xFF9ADAFF), up = -0.1f)
                }
                1 -> {
                    s.sfx(Sfx.CHIME, 0.6f, 1.5f)
                    s.burst(PKind.STAR, x, y, 6, 0.3f, 0.012f, gold)
                }
                else -> {
                    s.sfx(Sfx.SPARKLE, 0.6f)
                    s.burst(PKind.CONFETTI, x, y, 10, 0.4f, 0.01f, up = 0.4f)
                }
            }
            AtticCode.OWL -> {
                s.sfx(Sfx.OWL, 0.8f, 1f)
                s.after(0.7f) { s.sfx(Sfx.OWL, 0.6f, 0.9f) }
                repeat(3) { s.particle(Particle(PKind.LEAF, x + (it - 1) * 0.04f, y, (it - 1) * 0.08f, 0.05f, 2f, 0.012f, Color(0xFFB89868))) }
            }

            // ---- the secret room
            AtticCode.MAP -> when (arg) {
                0 -> { s.sfx(Sfx.PAGE, 0.6f, 0.8f); s.sfx(Sfx.SWISH, 0.4f, 0.9f) }
                1 -> { s.sfx(Sfx.PAGE, 0.7f, 1f); s.after(0.15f) { s.sfx(Sfx.MAGIC, 0.4f) } }
                2, 3 -> {
                    for (i in 0 until 3) s.after(i * 0.12f) { s.sfx(Sfx.NOTE, 0.6f, 2f.pow((arg * 3 + i * 2 - 8) / 12f)) }
                    s.burst(PKind.SPARK, x, y, 5, 0.25f, 0.01f, Color(0xFF8FE3B5))
                }
                else -> {
                    s.sfx(Sfx.FANFARE, 0.7f)
                    s.after(0.3f) { s.sfx(Sfx.AT_COINS, 0.7f) }
                    s.burst(PKind.HEART, x, y, 6, 0.3f, 0.015f, Color(0xFF3BC46B), up = 0.4f)
                    s.burst(PKind.STAR, x, y, 10, 0.5f, 0.012f, gold, up = 0.4f)
                    s.haptic()
                }
            }
            AtticCode.CHEST -> if (arg == 0) {
                s.sfx(Sfx.AT_CREAK, 0.6f, 0.8f)
                s.after(0.25f) { s.sfx(Sfx.AT_COINS, 0.9f) }
                s.after(0.45f) { s.sfx(Sfx.SPARKLE, 0.8f) }
                s.after(0.6f) { s.sfx(Sfx.COIN, 0.7f, 1.2f) }
                s.burst(PKind.STAR, x, y, 22, 0.8f, 0.014f, gold, up = 0.9f, life = 1.5f)
                s.burst(PKind.SPARK, x, y, 10, 0.6f, 0.012f, Color.White, up = 0.8f)
                s.laughAround(x, null)
                s.haptic()
            } else {
                s.sfx(Sfx.SHUT, 0.6f, 0.8f)
                s.sfx(Sfx.AT_CREAK, 0.3f, 1.3f)
            }
            AtticCode.GLOBE -> if (arg == 0) {
                s.sfx(Sfx.WHIRR, 0.4f, 0.9f)
                s.sfx(Sfx.AT_CREAK, 0.3f, 1.5f)
            } else {
                // It stops: a click and a note for the place it points at.
                s.sfx(Sfx.CLICK, 0.5f, 0.9f)
                s.sfx(Sfx.NOTE, 0.6f, 2f.pow(listOf(-5, -3, 0, 2, 4, 7)[(arg - 1).coerceIn(0, 5)] / 12f))
                s.burst(PKind.STAR, x, y, 4, 0.2f, 0.01f, Color(0xFF8FE3B5))
            }
            AtticCode.TREE -> {
                if (arg == 1) sture?.let { p -> s.voice(p, Sfx.AT_OOO, 0.5f, own = true) } else s.sfx(Sfx.GIGGLE, 0.45f, listOf(0.9f, 1.6f, 0.75f, 1.5f, 1.2f, 1.9f)[arg.coerceIn(0, 5)])
                s.sfx(Sfx.POP, 0.45f, 1.2f + arg * 0.08f)
                s.burst(PKind.SPARK, x, y, 6, 0.35f, 0.011f, gold)
            }
            AtticCode.TREE_DONE -> {
                s.sfx(Sfx.FANFARE, 0.8f)
                s.burst(PKind.CONFETTI, x, y, 24, 0.8f, 0.012f, up = 0.7f, life = 1.8f)
                s.burst(PKind.HEART, x, y, 6, 0.3f, 0.015f, up = 0.4f)
                s.haptic()
            }
            AtticCode.CHANDELIER -> {
                if (arg == 1) s.sfx(Sfx.CLICK, 0.5f, 1.3f)
                for (i in 0 until 4) s.after(i * 0.07f) { s.sfx(Sfx.CHIME, 0.35f, 1.6f + 0.15f * i) }
                s.burst(PKind.SPARK, x, y, 6, 0.3f, 0.01f, Color.White)
            }
            else -> Unit
        }
    }

    // ------------------------------------------------------------------ the silly records

    /** One note: [semi] semitones from the base note (the gramophone's "tin" voice is the same note sound as the piano's). */
    private fun note(s: FxStage, at: Float, semi: Int, vol: Float = 0.55f) {
        s.after(at) { s.sfx(Sfx.NOTE, vol, 2f.pow(semi.coerceIn(-12, 12) / 12f)) }
    }

    /** A little tune that the stuck record keeps repeating. */
    private fun motif(s: FxStage, at: Float, x: Float, y: Float) {
        note(s, at, 0, 0.6f)
        note(s, at + 0.28f, 4, 0.6f)
        note(s, at + 0.56f, 2, 0.6f)
        s.after(at + 0.3f) { s.particle(Particle(PKind.NOTE, x - 0.07f, y - 0.05f, -0.05f, -0.12f, 1.4f, 0.012f, Color(0xFF8B5CF6))) }
    }

    /** The six records; their lengths are in `AtticTunes` (domain). */
    private fun tune(s: FxStage, record: Int, x: Float, y: Float) {
        when (record) {
            0 -> {
                // The spider waltz: oom-pa-pa, four bars, and a hiccup at the end of the record.
                val melody = listOf(0, 2, 4, 7, 4, 2, 4, 2, 0, -3, 0, 0)
                for (bar in 0 until 4) {
                    val t0 = bar * 1.65f
                    note(s, t0, -12, 0.6f)
                    note(s, t0 + 0.55f, melody[bar * 3] - 0, 0.5f)
                    note(s, t0 + 1.1f, melody[bar * 3 + 1] + 0, 0.5f)
                }
                s.after(6.9f) { s.sfx(Sfx.HICCUP, 0.7f, 1f) }
            }
            1 -> {
                // The chicken polka: bright notes with a cluck on the off-beat.
                val m = listOf(0, 4, 7, 4, 0, 4, 7, 12, 9, 7, 4, 7, 4, 2, 0, -5)
                for (i in m.indices) {
                    note(s, i * 0.35f, m[i], 0.5f)
                    if (i % 4 == 3) s.after(i * 0.35f + 0.17f) { s.sfx(Sfx.CLUCK, 0.55f, 1f + (i % 8) * 0.04f) }
                }
                s.after(5.9f) { s.sfx(Sfx.BOING, 0.5f, 1.3f) }
            }
            2 -> {
                // The tuba march: slow low notes, and a big burp now and then, played on the horn.
                for (i in 0 until 8) {
                    note(s, i * 0.9f, if (i % 2 == 0) -12 else -5, 0.65f)
                    note(s, i * 0.9f + 0.45f, if (i % 2 == 0) 0 else -3, 0.45f)
                }
                s.after(2.7f) { s.sfx(Sfx.BURP, 0.7f, 1.3f) }
                s.after(5.4f) { s.sfx(Sfx.BURP, 0.7f, 0.9f) }
            }
            3 -> {
                // The ghost's lullaby: slow, wobbly and sleepy, with an «ooo» at each end.
                s.sfx(Sfx.AT_OOO, 0.4f, 0.8f)
                val m = listOf(7, 4, 2, 0, -3, 0)
                for (i in m.indices) {
                    note(s, 0.4f + i * 1.2f, m[i], 0.5f)
                    s.after(0.4f + i * 1.2f + 0.05f) { s.sfx(Sfx.NOTE, 0.3f, 2f.pow(m[i].coerceIn(-12, 12) / 12f) * 1.03f) }
                }
                s.after(7.3f) { s.sfx(Sfx.AT_OOO, 0.4f, 0.7f) }
            }
            4 -> {
                // The stuck record: a few notes, then the same three over and over (the rules add the skips).
                note(s, 0f, 0, 0.55f); note(s, 0.4f, 2, 0.55f); note(s, 0.8f, 4, 0.55f); note(s, 1.2f, 2, 0.55f)
            }
            else -> {
                // Disco dust: a fast arpeggio up and down with a pop on every beat, and glitter at the end.
                val m = listOf(0, 4, 7, 12, 7, 4, 0, 4, 7, 12, 9, 7, 4, 2, 0, -5, 0, 4, 7, 12)
                for (i in m.indices) {
                    note(s, i * 0.28f, m[i], 0.5f)
                    if (i % 2 == 0) s.after(i * 0.28f) { s.sfx(Sfx.POP, 0.25f, 1.4f) }
                }
                s.after(5.6f) {
                    s.sfx(Sfx.SPARKLE, 0.7f)
                    s.burst(PKind.STAR, x, y - 0.1f, 10, 0.5f, 0.012f, Color(0xFFFF9EC7), up = 0.5f)
                }
            }
        }
        // Notes float out of the horn while it plays.
        for (k in 0 until 4) s.after(0.5f + k * 1.1f) {
            s.particle(Particle(PKind.NOTE, x - 0.07f, y - 0.07f, -0.06f + 0.03f * k, -0.14f, 1.8f, 0.013f, listOf(Color(0xFF8B5CF6), Color(0xFFFF9EC7), Color(0xFF2F9BFF), Color(0xFFFFC83D))[k]))
        }
    }
}

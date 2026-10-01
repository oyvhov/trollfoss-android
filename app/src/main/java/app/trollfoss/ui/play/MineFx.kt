package app.trollfoss.ui.play

import androidx.compose.ui.graphics.Color
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.Face
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.MineEvent
import app.trollfoss.domain.Person
import app.trollfoss.domain.Thing
import kotlin.math.abs

/**
 * Mitt hus: effects of building (hammering, sawing, dust, sparkle) and of the furniture of the rooms and the yard.
 * [code] is in [app.trollfoss.domain.HouseFx].MINE up to the next hundred (see [MineEvent]).
 */
object MineFx {
    private val sawdust = Color(0xFFE3B27A)
    private val soot = Color(0xFF4A4452)
    private val sand = Color(0xFFF0CF8A)
    private val paper = Color(0xFFFFF7EA)
    private val sockColors = listOf(Color(0xFFD2443A), Color(0xFF5AA9E6), Color(0xFFE8B04A), Color(0xFF3BC46B), Color(0xFFF08CB8))
    private val blocks = listOf(Color(0xFFD2443A), Color(0xFFFFC83D), Color(0xFF5AA9E6), Color(0xFF3BC46B), Color(0xFFF08CB8))

    fun play(s: FxStage, code: Int, arg: Int, x: Float, y: Float, fixture: Fixture?, thing: Thing?) {
        val r = s.random
        fun pitch(base: Float = 1f, spread: Float = 0.2f) = base + (r.nextFloat() - 0.5f) * spread
        when (code) {
            MineEvent.CHANGED -> s.changed()
            MineEvent.HAMMER -> {
                s.sfx(Sfx.MI_HAMMER, 0.7f, pitch(1f, 0.3f))
                s.burst(PKind.DUST, x, y, 4, 0.2f, 0.012f, up = 0.1f, life = 0.7f)
                s.burst(PKind.SPARK, x, y - 0.02f, 3, 0.3f, 0.01f, Color(0xFFFFD447))
            }
            MineEvent.SAW -> {
                s.sfx(Sfx.MI_SAW, 0.5f, pitch())
                s.burst(PKind.DUST, x, y, 7, 0.25f, 0.008f, sawdust, up = 0.1f, life = 0.8f)
            }
            MineEvent.DRILL -> {
                s.sfx(Sfx.MI_DRILL, 0.45f, pitch())
                s.burst(PKind.SPARK, x, y, 4, 0.3f, 0.01f, Color(0xFFBFE3FA))
            }
            MineEvent.PLANK -> {
                s.sfx(Sfx.MI_PLANK, 0.7f, pitch())
                s.burst(PKind.DUST, x, y, 6, 0.3f, 0.014f, up = 0.05f, life = 0.7f)
            }
            MineEvent.DUST -> s.burst(PKind.DUST, x, y, 8, 0.3f, 0.014f, up = 0.1f, life = 0.8f)
            MineEvent.WALLS_UP -> {
                s.sfx(Sfx.BOING, 0.7f, 0.9f)
                s.after(0.05f) { s.sfx(Sfx.MI_PLANK, 0.8f, 0.8f) }
                s.burst(PKind.DUST, x, y, 16, 0.55f, 0.022f, up = 0.2f, life = 1f)
                s.burst(PKind.STAR, x, y - 0.3f, 8, 0.5f, 0.012f, Color(0xFFFFD447), up = 0.4f)
                s.haptic()
            }
            MineEvent.PIECE -> {
                s.sfx(Sfx.POP, 0.5f, pitch(1.1f, 0.6f))
                s.burst(PKind.SPARK, x, y, 4, 0.25f, 0.011f)
            }
            MineEvent.ROOM_DONE -> {
                s.sfx(Sfx.MI_PLING, 0.9f)
                s.after(0.25f) { s.sfx(Sfx.SPARKLE, 0.7f) }
                s.burst(PKind.CONFETTI, x, 0.35f, 32, 0.9f, 0.012f, up = 0.9f, life = 1.8f)
                s.burst(PKind.HEART, x, 0.5f, 4, 0.3f, 0.016f, up = 0.4f, life = 1.3f)
                s.haptic()
                s.changed()
            }
            MineEvent.FOUNDATION_START -> {
                s.sfx(Sfx.WHOOSH, 0.7f, 1.1f)
                s.after(0.3f) { s.sfx(Sfx.MI_PLANK, 0.8f, 0.9f) }
                s.burst(PKind.SPARK, x, 0.5f, 10, 0.5f, 0.012f, Color(0xFFFFD447), up = 0.3f)
            }
            MineEvent.FOUNDATION_DONE, MineEvent.UPPER_DONE -> {
                s.sfx(Sfx.FANFARE, 0.9f)
                s.after(0.3f) { s.sfx(Sfx.MI_PLING, 0.8f) }
                s.burst(PKind.CONFETTI, x, 0.3f, 56, 1.1f, 0.013f, up = 1f, life = 2.2f)
                s.burst(PKind.STAR, x, 0.4f, 14, 0.7f, 0.014f, Color(0xFFFFD447), up = 0.5f)
                s.burst(PKind.HEART, x, 0.5f, 5, 0.3f, 0.018f, up = 0.4f, life = 1.5f)
                s.haptic()
                s.changed()
            }
            MineEvent.CRANE -> {
                s.sfx(Sfx.MI_CRANE, 0.55f, pitch())
                s.burst(PKind.DUST, x, y + 0.3f, 3, 0.15f, 0.01f, up = 0f, life = 0.6f)
            }
            MineEvent.DEMOLISH -> {
                s.sfx(Sfx.MI_CRASH, 0.85f)
                s.after(0.2f) { s.sfx(Sfx.THUD, 0.6f, 0.8f) }
                s.burst(PKind.DUST, x, y, 22, 0.7f, 0.026f, up = 0.2f, life = 1.2f)
                // The furniture goes to the store in a sparkling cloud.
                s.burst(PKind.STAR, x, 0.65f, 12, 0.5f, 0.013f, Color(0xFFFFD447), up = 0.9f, life = 1.2f)
                s.after(0.3f) { s.sfx(Sfx.ZIP, 0.5f) }
                s.haptic()
            }
            MineEvent.SELECT -> {
                s.sfx(Sfx.POP, 0.5f, 1.4f)
                s.burst(PKind.SPARK, x, y, 5, 0.3f, 0.012f, Color(0xFFFFD447))
            }
            MineEvent.DENIED -> {
                s.sfx(Sfx.HMM, 0.5f, 0.8f)
                s.after(0.1f) { s.sfx(Sfx.BONK, 0.25f, 1.4f) }
            }
            MineEvent.LOOK -> {
                s.sfx(Sfx.SWISH, 0.5f, 1.2f)
                s.after(0.1f) { s.sfx(Sfx.SPARKLE, 0.5f) }
                s.burst(PKind.SPARK, x, 0.45f, 10, 0.5f, 0.013f, up = 0.3f)
            }
            MineEvent.PARTY_START -> {
                s.sfx(Sfx.MI_DINGDONG, 0.9f)
                s.after(0.9f) { s.sfx(Sfx.FANFARE, 0.8f) }
                s.burst(PKind.CONFETTI, x, 0.25f, 60, 1.2f, 0.013f, up = 1f, life = 2.4f)
                s.haptic()
            }
            MineEvent.GUEST -> {
                if (arg >= 0) {
                    s.after(arg * 0.12f) {
                        s.sfx(Sfx.POP, 0.6f, 0.9f + arg * 0.1f)
                        s.burst(PKind.STAR, x, y - 0.12f, 6, 0.4f, 0.012f, Color(0xFFFFD447), up = 0.4f)
                    }
                } else {
                    s.sfx(Sfx.POOF, 0.5f, 1.2f)
                    s.burst(PKind.DUST, x, y - 0.1f, 8, 0.3f, 0.016f, up = 0.1f)
                }
            }
            MineEvent.PARTY_END -> {
                s.sfx(Sfx.CHIME, 0.8f)
                s.burst(PKind.CONFETTI, x, 0.3f, 40, 1f, 0.013f, up = 0.9f, life = 2f)
                s.burst(PKind.HEART, x, 0.5f, 6, 0.3f, 0.018f, up = 0.4f, life = 1.5f)
                s.changed()
            }
            MineEvent.CONFETTI -> {
                s.burst(PKind.CONFETTI, x, y, 26, 0.9f, 0.012f, up = 0.8f, life = 1.8f)
                s.sfx(Sfx.POP, 0.4f, pitch(1.3f, 0.4f))
            }
            MineEvent.DOOR_PASS -> {
                s.sfx(Sfx.OPEN, 0.5f, 0.9f)
                s.after(0.25f) { s.sfx(Sfx.SHUT, 0.45f, 0.9f) }
                s.burst(PKind.SPARK, x, y, 4, 0.2f, 0.01f)
            }
            MineEvent.STARTED_TAP -> {
                s.sfx(Sfx.MI_HAMMER, 0.6f, 1.1f)
                s.after(0.12f) { s.sfx(Sfx.BOING, 0.5f, 1.3f) }
                s.burst(PKind.DUST, x, y, 8, 0.4f, 0.02f, up = 0.1f)
                s.burst(PKind.STAR, x, y - 0.3f, 5, 0.3f, 0.012f, Color(0xFFFFD447), up = 0.4f)
            }
            else -> if (code >= MineEvent.FLARE) furniture(s, code, arg, x, y, fixture)
        }
    }

    private fun furniture(s: FxStage, code: Int, arg: Int, x: Float, y: Float, f: Fixture?) {
        val r = s.random
        fun near(reach: Float = 0.5f): List<Person> = s.world.bodiesIn(s.place).filterIsInstance<Person>().filter { !it.held && abs(it.x - x) < reach }
        when (code) {
            MineEvent.FLARE -> {
                s.sfx(Sfx.POOF, 0.5f, 1.2f)
                s.sfx(Sfx.SIZZLE, 0.4f, 1.1f)
                s.burst(PKind.SPARK, x, y - 0.05f, 9, 0.5f, 0.012f, Color(0xFFFFA23A), up = 0.6f)
                if (arg == 1) {
                    // The fire sneezes: «atsjo!» and a puff of soot.
                    s.after(0.25f) {
                        s.sfx(Sfx.SNEEZE, 0.6f, 1.5f)
                        s.burst(PKind.DUST, x, y - 0.1f, 16, 0.7f, 0.016f, soot, up = 0.3f, life = 1f)
                        s.laughAround(x, null, 0.5f, 0.8f)
                    }
                }
            }
            MineEvent.CANDLE -> {
                if (arg == 1) {
                    s.sfx(Sfx.CHIME, 0.5f, 1.3f)
                    s.burst(PKind.HEART, x, y - 0.05f, 3, 0.2f, 0.014f, up = 0.3f, life = 1.2f)
                } else {
                    s.sfx(Sfx.POP, 0.3f, 0.8f)
                    s.burst(PKind.STEAM, x, y - 0.06f, 3, 0.1f, 0.012f, Color.White, up = 0.1f, life = 1.2f)
                }
            }
            MineEvent.CHANDELIER -> {
                for (k in 0 until 3) s.after(k * 0.11f) { s.sfx(Sfx.CHIME, 0.35f, 1.4f + k * 0.25f) }
                s.burst(PKind.SPARK, x, y, 8, 0.2f, 0.012f, Color(0xFFBFE9F2), up = -0.15f)
            }
            MineEvent.TOAST -> {
                s.sfx(Sfx.POP, 0.7f, 1.1f)
                s.after(0.1f) { s.sfx(Sfx.BOING, 0.4f, 1.6f) }
                s.burst(PKind.CRUMB, x, y, 6, 0.3f, 0.009f, Color(0xFFD9A058), up = 0.8f, life = 0.9f)
                for (p in near(0.45f)) s.faces(p, Face.OOH, 0.3f, Face.YUM, 1f)
            }
            MineEvent.NIGHT_LIGHT -> {
                s.sfx(Sfx.CLICK, 0.5f, 1f)
                if (arg == 1) {
                    s.after(0.1f) { s.sfx(Sfx.MAGIC, 0.3f, 1.4f) }
                    s.burst(PKind.STAR, x, y - 0.1f, 6, 0.3f, 0.012f, Color(0xFFFFE680), up = 0.2f, life = 1.4f)
                }
            }
            MineEvent.BLOCKS_FALL -> {
                s.sfx(Sfx.MI_CRASH, 0.8f)
                s.burst(PKind.CONFETTI, x, y - 0.1f, 14, 0.7f, 0.011f, up = 0.6f, life = 1.2f)
                s.laughAround(x, null, 0.45f, 0.8f)
            }
            MineEvent.BLOCKS_BUILD -> {
                s.sfx(Sfx.BOING, 0.6f, 1.2f)
                s.after(0.12f) { s.sfx(Sfx.POP, 0.5f, 1.4f) }
                s.burst(PKind.SPARK, x, y - 0.05f, 6, 0.3f, 0.012f)
            }
            MineEvent.HORSE -> {
                s.sfx(Sfx.NEIGH, 0.5f, 1.3f)
                s.burst(PKind.SPARK, x, y, 4, 0.3f, 0.011f)
                for (p in near(0.3f)) s.faces(p, Face.LAUGH, 1.5f, Face.GRIN, 1f)
            }
            MineEvent.LADDER -> {
                s.sfx(Sfx.SWISH, 0.5f, 1f)
                s.after(0.15f) { s.sfx(Sfx.PAGE, 0.5f, 1f) }
                s.burst(PKind.CONFETTI, x, y - 0.15f, 5, 0.4f, 0.012f, paper, up = 0.6f, life = 1.4f)
            }
            MineEvent.GLOBE -> {
                s.sfx(Sfx.WHIRR, 0.4f, 1.4f)
                s.burst(PKind.SPARK, x, y, 4, 0.3f, 0.011f)
            }
            MineEvent.SAW_BENCH -> {
                s.sfx(Sfx.MI_SAW, 0.7f, 1f)
                s.burst(PKind.DUST, x, y, 12, 0.3f, 0.009f, sawdust, up = 0.2f, life = 1f)
            }
            MineEvent.SOCKS -> {
                s.sfx(Sfx.POP, 0.7f, 1.1f)
                for (k in 0 until 6) s.burst(PKind.CONFETTI, x, y - 0.1f, 1, 0.5f, 0.015f, sockColors[(k + arg) % sockColors.size], up = 0.7f, life = 1.4f)
                s.laughAround(x, null, 0.4f, 0.7f)
            }
            MineEvent.STRUM -> {
                for (k in 0 until 3) s.after(k * 0.13f) { s.sfx(Sfx.NOTE, 0.4f, 0.9f + k * 0.26f) }
                s.burst(PKind.NOTE, x, y - 0.1f, 4, 0.3f, 0.016f, Color(0xFF8B5CF6), up = 0.4f, life = 1.6f)
            }
            MineEvent.WATER_BED -> {
                s.sfx(Sfx.SPLASH, 0.3f, 1.6f)
                s.burst(PKind.DROP, x, y - 0.12f, 8, 0.3f, 0.008f, Color(0xFF9ADAFF), up = 0.2f, life = 0.9f)
                if (arg == 3) {
                    s.after(0.3f) { s.sfx(Sfx.CHIME, 0.5f, 1.2f) }
                    s.burst(PKind.HEART, x, y - 0.1f, 4, 0.3f, 0.016f, up = 0.4f, life = 1.4f)
                }
            }
            MineEvent.HANGING_POT -> {
                s.sfx(Sfx.SWISH, 0.3f, 1.3f)
                s.burst(PKind.LEAF, x, y, 3, 0.15f, 0.012f, Color(0xFF3BC46B), up = 0f, life = 1.6f)
            }
            MineEvent.COAT_RACK -> {
                s.sfx(Sfx.BONK, 0.45f, 1.2f)
                s.after(0.1f) { s.sfx(Sfx.SWISH, 0.4f, 1f) }
                for (p in near(0.4f)) s.faces(p, Face.OOH, 0.4f, Face.GRIN, 0.9f)
            }
            MineEvent.MAILBOX -> {
                s.sfx(Sfx.CLICK, 0.6f, if (arg == 1) 1.2f else 0.8f)
                if (arg == 1) {
                    s.after(0.1f) { s.sfx(Sfx.DING, 0.3f, 1.4f) }
                    s.burst(PKind.HEART, x, y - 0.1f, 2, 0.2f, 0.015f, up = 0.3f, life = 1.2f)
                }
            }
            MineEvent.FENCE -> {
                s.sfx(Sfx.CHIRP, 0.5f, 1f + r.nextFloat() * 0.3f)
                s.burst(PKind.BIRD, x, y - 0.12f, 1, 0.1f, 0.016f, up = 0.1f, life = 2f)
            }
            MineEvent.FLOWER_BED -> {
                s.sfx(Sfx.SPARKLE, 0.35f, 1.3f)
                s.burst(PKind.BUTTERFLY, x, y - 0.06f, 2, 0.15f, 0.014f, up = 0.1f, life = 3f)
                s.burst(PKind.LEAF, x, y - 0.05f, 6, 0.3f, 0.011f, Color(0xFFF4B6C8), up = 0.3f, life = 1.5f)
            }
            MineEvent.SWING -> {
                s.sfx(Sfx.SWISH, 0.5f, 1.3f)
                s.after(0.6f) { s.sfx(Sfx.SWISH, 0.4f, 1f) }
                s.after(1.2f) { s.sfx(Sfx.SWISH, 0.3f, 1.2f) }
                for (p in near(0.3f)) s.faces(p, Face.LAUGH, 2f, Face.GRIN, 1f)
            }
            MineEvent.BIRD_BATH -> {
                s.sfx(Sfx.SPLASH, 0.5f, 1.3f)
                s.after(0.1f) { s.sfx(Sfx.CHIRP, 0.5f, 1.2f) }
                s.burst(PKind.DROP, x, y - 0.08f, 10, 0.4f, 0.008f, Color(0xFF9ADAFF), up = 0.5f, life = 0.9f)
            }
            MineEvent.GNOME -> {
                s.sfx(Sfx.GIGGLE, 0.4f, 1.9f)
                s.burst(PKind.SPARK, x, y - 0.05f, 4, 0.2f, 0.01f)
                if (arg % 5 == 0) {
                    s.after(0.2f) { s.sfx(Sfx.TICKLE, 0.35f, 1.8f) }
                    s.burst(PKind.STAR, x, y, 8, 0.4f, 0.012f, Color(0xFFFFD447), up = 0.4f)
                }
            }
            MineEvent.SANDBOX -> {
                s.sfx(Sfx.POOF, 0.4f, 1.4f)
                s.burst(PKind.DUST, x, y - 0.03f, 10, 0.3f, 0.01f, sand, up = 0.2f, life = 0.9f)
            }
            MineEvent.APPLE_TREE -> {
                s.sfx(Sfx.SWISH, 0.6f, 0.9f)
                s.burst(PKind.LEAF, x, y - 0.15f, 12, 0.35f, 0.012f, Color(0xFF6FAE5A), up = -0.1f, life = 1.8f)
            }
            MineEvent.APPLE_BONK -> {
                s.person(arg)?.let { p ->
                    s.after(0.35f) {
                        s.sfx(Sfx.BONK, 0.7f, 1f)
                        s.faces(p, Face.DIZZY, 0.6f, Face.LAUGH, 1.4f)
                        s.voice(p, Sfx.OOF, 0.7f)
                        s.burst(PKind.STAR, p.x, p.y - p.h, 5, 0.3f, 0.012f, Color(0xFFFFD447), up = 0.3f)
                        s.laughAround(p.x, p, 0.5f, 0.8f)
                    }
                }
            }
        }
    }
}

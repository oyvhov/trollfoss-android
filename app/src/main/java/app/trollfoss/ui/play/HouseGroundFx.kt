package app.trollfoss.ui.play

import androidx.compose.ui.graphics.Color
import app.trollfoss.audio.Sfx
import app.trollfoss.domain.Face
import app.trollfoss.domain.Fixture
import app.trollfoss.domain.GroundCode
import app.trollfoss.domain.Person
import app.trollfoss.domain.Thing
import kotlin.math.abs
import kotlin.math.pow

/**
 * Storhuset, ground floor: effects. [code] is in [app.trollfoss.domain.HouseFx].GROUND up to the next block
 * (see [GroundCode]), [arg] is the small number the rule sent along. Sound and sparkle only; what happens
 * in the world is decided by `domain/HouseGroundRules.kt`.
 */
object GroundFx {
    private val gold = Color(0xFFFFD447)
    private val warm = Color(0xFFFFB02E)
    private val white = Color(0xFFFFFFFF)
    private val green = Color(0xFF7BE05A)
    private val water = Color(0xFF9ADAFF)
    private val pink = Color(0xFFFF8FB1)
    private val party = listOf(Color(0xFFFF4D6D), Color(0xFFFFC83D), Color(0xFF2F9BFF), Color(0xFF2FD18B), Color(0xFFB57BFF))

    /** Birthday song: semitones above the key note, and how many beats each note lasts. */
    private val SONG = intArrayOf(
        0, 0, 2, 0, 5, 4, 0, 0, 2, 0, 7, 5, 0, 0, 12, 9, 5, 4, 2, 10, 10, 9, 5, 7, 5,
    )
    private val SONG_BEATS = floatArrayOf(
        0.75f, 0.25f, 1f, 1f, 1f, 2f, 0.75f, 0.25f, 1f, 1f, 1f, 2f, 0.75f, 0.25f, 1f, 1f, 1f, 1f, 2f, 0.75f, 0.25f, 1f, 1f, 1f, 2f,
    )

    fun play(s: FxStage, code: Int, arg: Int, x: Float, y: Float, fixture: Fixture?, thing: Thing?) {
        when (code) {
            GroundCode.CLOCK -> clock(s, arg, x, y)
            GroundCode.CREAK -> {
                s.sfx(Sfx.GR_CREAK, 0.8f, 0.9f)
                s.burst(PKind.DUST, x, y + 0.2f, 4, 0.15f, 0.012f, up = 0.05f)
            }
            GroundCode.STAIRS -> s.sfx(Sfx.GR_CREAK, 0.3f, 1.5f)
            GroundCode.LIFT -> {
                s.sfx(Sfx.TICK, 0.6f, 1.5f)
                s.after(0.15f) { s.sfx(Sfx.TICK, 0.5f, 1.3f) }
                s.burst(PKind.SPARK, x, y - 0.1f, 4, 0.2f, 0.009f, gold)
            }
            GroundCode.HAT -> {
                s.sfx(Sfx.POP, 0.6f, 0.9f + s.random.nextFloat() * 0.3f)
                s.burst(PKind.DUST, x, y, 4, 0.2f, 0.01f, up = 0.1f)
            }
            GroundCode.UMBRELLA -> if (arg == 1) {
                s.sfx(Sfx.SWISH, 0.6f, 1.4f)
                s.after(0.1f) { s.sfx(Sfx.POP, 0.7f, 0.8f) }
                s.burst(PKind.SPARK, x, y, 5, 0.3f, 0.01f)
            } else {
                s.sfx(Sfx.SHUT, 0.5f, 1.2f)
            }
            GroundCode.ARMOUR -> armour(s, arg, x, y, fixture)
            GroundCode.POST -> when (arg) {
                1 -> {
                    s.sfx(Sfx.PAGE, 0.8f, 1.2f)
                    s.after(0.12f) { s.sfx(Sfx.CHIME, 0.5f, 1.3f) }
                    s.burst(PKind.SPARK, x, y, 6, 0.3f, 0.01f, gold)
                }
                2 -> {
                    s.sfx(Sfx.CLICK, 0.6f, 0.9f)
                    s.after(0.1f) { s.sfx(Sfx.POP, 0.6f, 1.2f) }
                }
                else -> {
                    s.sfx(Sfx.CLICK, 0.6f, 0.7f)
                    s.sfx(Sfx.TICK, 0.5f, 0.8f)
                }
            }
            GroundCode.CHANDELIER -> {
                s.sfx(Sfx.GR_TINKLE, 0.8f)
                s.sfx(Sfx.CLICK, 0.5f, if (arg == 1) 1.3f else 0.8f)
                s.burst(PKind.SPARK, x, y + 0.05f, 12, 0.35f, 0.011f, white)
                s.burst(PKind.STAR, x, y + 0.08f, 4, 0.25f, 0.009f, gold, up = 0.05f)
            }
            GroundCode.PORTRAIT -> portrait(s, arg, x, y)
            GroundCode.WINDOW -> if (arg == 2) {
                s.sfx(Sfx.SPARKLE, 0.7f, 1.1f)
                for (c in party) s.burst(PKind.SPARK, x + (s.random.nextFloat() - 0.5f) * 0.1f, y, 3, 0.3f, 0.011f, c, up = 0.05f)
            } else {
                s.sfx(Sfx.SWISH, 0.5f, 0.9f)
            }
            GroundCode.FIRE -> if (arg == 1) {
                s.sfx(Sfx.GR_WHOOMP, 0.8f, 1.1f)
                s.after(0.3f) { s.sfx(Sfx.GR_CRACKLE, 0.6f) }
                s.burst(PKind.SPARK, x, y + 0.1f, 10, 0.4f, 0.011f, warm, up = 0.4f)
            } else {
                s.sfx(Sfx.POOF, 0.5f, 1.4f)
                s.burst(PKind.SMOKE, x, y + 0.1f, 5, 0.1f, 0.03f, Color(0xFFB8B4C8), up = 0.2f, life = 1.4f)
            }
            GroundCode.STOCKING -> {
                s.sfx(Sfx.POP, 0.7f, 1.2f)
                s.after(0.1f) { s.sfx(Sfx.SPARKLE, 0.5f, 1.2f) }
                s.burst(PKind.STAR, x - 0.1f, y, 6, 0.3f, 0.011f, pink)
            }
            GroundCode.SOFA -> if (arg == 1) {
                s.sfx(Sfx.COIN, 0.8f, 1f)
                s.after(0.15f) { s.sfx(Sfx.COIN, 0.7f, 1.2f) }
                s.after(0.3f) { s.sfx(Sfx.COIN, 0.7f, 1.4f) }
                s.burst(PKind.STAR, x, y - 0.1f, 10, 0.5f, 0.012f, gold)
            } else {
                s.sfx(Sfx.THUD, 0.35f, 1.5f)
                s.burst(PKind.DUST, x, y - 0.1f, 8, 0.25f, 0.014f, up = 0.2f)
            }
            GroundCode.TV -> tv(s, arg, x, y)
            GroundCode.FILM -> film(s, arg, x, y)
            GroundCode.POPCORN -> {
                s.sfx(Sfx.POP, 0.6f, 1.3f)
                s.burst(PKind.CRUMB, x, y, 5, 0.3f, 0.009f, Color(0xFFFFF1B0), up = 0.3f)
            }
            GroundCode.GLOBE -> {
                s.sfx(Sfx.SWISH, 0.6f, 1.2f)
                if (arg == 1) {
                    s.after(0.12f) { s.sfx(Sfx.POP, 0.8f, 1.1f) }
                    s.sfx(Sfx.SPARKLE, 0.5f, 1.3f)
                    s.burst(PKind.STAR, x, y - 0.1f, 6, 0.3f, 0.01f, gold)
                }
            }
            GroundCode.FISH -> {
                s.sfx(Sfx.BUBBLE, 0.5f, 1.1f)
                s.after(0.12f) { s.sfx(Sfx.BUBBLE, 0.4f, 1.4f) }
                s.burst(PKind.BUBBLE, x, y, 4, 0.1f, 0.008f, white, up = 0.2f, life = 1.3f)
            }
            GroundCode.FISH_FEED -> {
                for (i in 0 until 3) s.after(i * 0.16f) { s.sfx(Sfx.BUBBLE, 0.5f, 1f + i * 0.2f) }
                s.after(0.5f) { s.sfx(Sfx.YUM, 0.4f, 1.6f) }
                s.burst(PKind.BUBBLE, x, y - 0.05f, 7, 0.15f, 0.01f, white, up = 0.3f, life = 1.5f)
                s.burst(PKind.HEART, x, y - 0.1f, 2, 0.1f, 0.013f, up = 0.3f, life = 1.3f)
            }
            GroundCode.LAMP -> s.sfx(Sfx.CLICK, 0.6f, if (arg == 1) 1.3f else 0.8f)
            GroundCode.BOOKS -> {
                s.sfx(Sfx.GR_FLAP, 0.8f, 1f + s.random.nextFloat() * 0.2f)
                s.sfx(Sfx.PAGE, 0.5f, 1.2f)
                repeat(4 + arg * 2) {
                    s.particle(Particle(PKind.LEAF, x + (s.random.nextFloat() - 0.5f) * 0.14f, y, (s.random.nextFloat() - 0.5f) * 0.5f, -0.25f - s.random.nextFloat() * 0.2f, 1.6f, 0.014f, Color(0xFFF7EEDC), s.random.nextFloat() * 360f, 300f))
                }
            }
            GroundCode.LEVER -> lever(s, x, y)
            GroundCode.LADDER -> s.sfx(Sfx.GR_RATTLE, 0.7f, 0.95f + arg * 0.05f)
            GroundCode.DESK -> {
                s.sfx(Sfx.SWISH, 0.5f, 2f)
                s.after(0.25f) { s.sfx(Sfx.SWISH, 0.5f, 1.8f) }
                s.burst(PKind.NOTE, x + 0.05f, y - 0.1f, 1, 0.1f, 0.01f, Color(0xFF3B2A55), up = 0.2f)
            }
            GroundCode.LECTERN -> if (arg == 9) {
                s.sfx(Sfx.GR_SHUSH, 0.9f)
                s.after(0.1f) { s.sfx(Sfx.OOH, 0.4f, 1.4f) }
                for (p in s.world.bodiesIn(s.place)) if (p is Person && !p.held && abs(p.x - x) < 0.9f) s.faces(p, Face.OOH, 0.5f, Face.GRIN, 1f)
            } else {
                s.sfx(Sfx.GR_TALK, 0.8f, 0.9f + arg * 0.1f)
                s.burst(PKind.NOTE, x, y - 0.05f, 2, 0.12f, 0.012f, Color(0xFF8B5CF6), up = 0.15f, life = 1.4f)
            }
            GroundCode.BUST -> {
                s.sfx(Sfx.HMM, 0.7f, 0.75f)
                s.after(0.35f) { s.sfx(Sfx.POOF, 0.35f, 1.6f) }
                s.burst(PKind.DUST, x, y - 0.1f, 6, 0.2f, 0.013f, white, up = 0.2f)
            }
            GroundCode.TABLE -> table(s, arg, x, y)
            GroundCode.CAKE -> cake(s, arg, x, y)
            GroundCode.RANGE -> when (arg) {
                0 -> {
                    s.sfx(Sfx.CLICK, 0.6f, 1.2f)
                    s.after(0.1f) { s.sfx(Sfx.GR_WHOOMP, 0.4f, 1.6f) }
                    s.burst(PKind.SPARK, x, y - 0.1f, 4, 0.2f, 0.009f, warm)
                }
                1 -> s.sfx(Sfx.CLICK, 0.5f, 0.8f)
                2 -> {
                    s.sfx(Sfx.DING, 0.8f)
                    s.burst(PKind.STEAM, x, y, 6, 0.1f, 0.02f, white, up = 0.1f, life = 1.4f)
                }
                else -> {
                    s.sfx(Sfx.SHUT, 0.6f)
                    s.after(0.2f) { s.sfx(Sfx.TICK, 0.5f, 1f) }
                }
            }
            GroundCode.PIZZA -> when (arg) {
                0 -> {
                    s.sfx(Sfx.GR_WHOOMP, 0.8f)
                    s.burst(PKind.SPARK, x, y - 0.05f, 8, 0.4f, 0.011f, warm, up = 0.3f)
                }
                1 -> {
                    s.sfx(Sfx.DING, 0.8f, 1.2f)
                    s.after(0.2f) { s.sfx(Sfx.POP, 0.8f) }
                    s.burst(PKind.STEAM, x, y - 0.05f, 6, 0.1f, 0.02f, white, up = 0.1f, life = 1.4f)
                    s.burst(PKind.STAR, x, y - 0.1f, 8, 0.4f, 0.011f, gold)
                    for (p in s.world.bodiesIn(s.place)) if (p is Person && !p.held && abs(p.x - x) < 0.9f) s.faces(p, Face.YUM, 0.5f, Face.GRIN, 1f)
                }
                else -> {
                    s.sfx(Sfx.POOF, 0.5f, 1.3f)
                    s.burst(PKind.SMOKE, x, y - 0.05f, 4, 0.1f, 0.025f, Color(0xFFB8B4C8), up = 0.2f, life = 1.2f)
                }
            }
            GroundCode.MIXER -> mixer(s, arg, x, y)
            GroundCode.SINK -> if (arg == 1) {
                s.sfx(Sfx.SPLASH, 0.35f, 1.5f)
                s.burst(PKind.DROP, x, y, 6, 0.2f, 0.008f, water, up = 0.1f)
            } else {
                s.sfx(Sfx.CLICK, 0.5f, 0.8f)
            }
            GroundCode.FOUNTAIN -> if (arg == 1) {
                s.sfx(Sfx.COIN, 0.8f)
                s.after(0.2f) { s.sfx(Sfx.MAGIC, 0.7f) }
                s.after(0.8f) { s.sfx(Sfx.CHIME, 0.6f, 1.2f) }
                s.burst(PKind.STAR, x, y - 0.15f, 14, 0.6f, 0.013f, gold)
                s.burst(PKind.HEART, x, y - 0.2f, 4, 0.3f, 0.014f, up = 0.4f, life = 1.5f)
            } else {
                s.sfx(Sfx.SPLASH, 0.5f, 1.1f)
                s.sfx(Sfx.BUBBLE, 0.4f, 1.2f)
                repeat(10) { s.particle(Particle(PKind.DROP, x + (s.random.nextFloat() - 0.5f) * 0.2f, y - 0.15f, (s.random.nextFloat() - 0.5f) * 0.4f, -0.5f - s.random.nextFloat() * 0.4f, 0.9f, 0.01f, water)) }
            }
            GroundCode.GROW -> grow(s, arg, x, y)
            GroundCode.BLOOM -> {
                s.sfx(Sfx.POP, 0.8f, 1.1f)
                s.sfx(Sfx.SPARKLE, 0.5f, 1.2f)
                s.burst(PKind.HEART, x, y, 4, 0.25f, 0.015f, up = 0.4f, life = 1.4f)
                s.burst(PKind.LEAF, x, y, 6, 0.3f, 0.012f, green)
            }
            GroundCode.SOFIE -> sofie(s, arg, x, y)
            GroundCode.HAMMOCK -> {
                s.sfx(Sfx.SWISH, 0.4f, 0.8f)
                s.sfx(Sfx.BOING, 0.25f, 0.8f)
            }
            GroundCode.ROLF -> rolf(s, arg, x, y)
            GroundCode.GARDEN_DOOR -> {
                s.sfx(Sfx.CHIME, 0.35f, 1.5f)
                s.burst(PKind.SPARK, x, y, 4, 0.2f, 0.009f, white)
            }
            GroundCode.PUFF -> {
                s.sfx(Sfx.THUD, 0.3f, 1.6f)
                s.burst(PKind.DUST, x, y - 0.08f, 6, 0.2f, 0.012f, up = 0.15f)
            }
            else -> Unit
        }
    }

    private fun clock(s: FxStage, n: Int, x: Float, y: Float) {
        for (i in 0 until n) s.after(i * 0.8f) {
            s.sfx(Sfx.GR_BONG, 0.85f, 1f)
            s.burst(PKind.NOTE, x, y + 0.05f, 1, 0.1f, 0.012f, Color(0xFF8B5CF6), up = 0.15f, life = 1.3f)
        }
        if (n == 12) {
            s.after(n * 0.8f) {
                s.sfx(Sfx.CUCKOO, 0.8f)
                s.sfx(Sfx.COIN, 0.8f, 1.2f)
                s.burst(PKind.CONFETTI, x, y + 0.1f, 22, 0.8f, 0.012f, up = 0.7f, life = 1.8f)
                s.burst(PKind.STAR, x, y, 8, 0.5f, 0.012f, gold)
            }
        }
        for (p in s.world.bodiesIn(s.place)) if (p is Person && !p.held && abs(p.x - x) < 0.6f && p.anim.face != Face.SLEEP) p.anim.face = Face.OOH.also { p.anim.faceTime = 0.5f }
    }

    private fun armour(s: FxStage, arg: Int, x: Float, y: Float, f: Fixture?) {
        when (arg) {
            0 -> {
                s.sfx(Sfx.GR_CLANK, 0.8f, 1f)
                s.burst(PKind.SPARK, x, y, 5, 0.3f, 0.009f, white)
            }
            1 -> {
                s.sfx(Sfx.GR_CLANK, 0.6f, 1.3f)
                s.after(0.25f) { s.sfx(Sfx.TICK, 0.7f, 0.7f) }
                s.after(0.9f) { s.sfx(Sfx.GR_CLANK, 0.5f, 0.9f) }
            }
            2 -> {
                s.sfx(Sfx.HICCUP, 0.9f, 0.7f)
                s.sfx(Sfx.GR_CLANK, 0.4f, 1.6f)
                s.burst(PKind.STAR, x, y - 0.15f, 3, 0.2f, 0.009f, white, life = 0.5f)
            }
            3 -> {
                s.sfx(Sfx.HICCUP, 0.9f, 0.6f)
                s.after(0.1f) { s.sfx(Sfx.POP, 0.9f, 0.7f) }
                s.after(0.3f) { s.sfx(Sfx.BOING, 0.7f, 1.4f) }
                s.after(0.7f) { s.sfx(Sfx.GR_CLANK, 0.9f, 0.7f) }
                s.after(1.0f) { s.sfx(Sfx.SQUEAK, 0.8f, 1.6f) }
                s.burst(PKind.STAR, x, y - 0.3f, 10, 0.5f, 0.012f, gold, up = 0.4f)
                s.burst(PKind.CONFETTI, x, y - 0.3f, 12, 0.5f, 0.011f, up = 0.5f, life = 1.4f)
                s.laughAround(x, null, 0.5f, 0.9f)
            }
            else -> {
                s.sfx(Sfx.BOING, 0.6f, 1.2f)
                s.after(0.4f) { s.sfx(Sfx.GR_CLANK, 0.8f, 1.1f) }
            }
        }
        if (f == null) return
    }

    private fun portrait(s: FxStage, variant: Int, x: Float, y: Float) {
        when (variant) {
            0 -> {
                s.sfx(Sfx.PRRT, 0.5f, 2f)
                s.after(0.3f) { s.sfx(Sfx.GIGGLE, 0.4f, 1.3f) }
            }
            1 -> {
                s.sfx(Sfx.OOH, 0.5f, 0.8f)
                s.after(0.4f) { s.sfx(Sfx.HMM, 0.5f, 0.8f) }
            }
            else -> {
                s.sfx(Sfx.GIGGLE, 0.5f, 0.8f)
                s.sfx(Sfx.TICK, 0.4f, 1.2f)
            }
        }
        s.burst(PKind.SPARK, x, y, 4, 0.2f, 0.009f, white)
        s.laughAround(x, null, 0.6f, 0.8f)
    }

    private fun tv(s: FxStage, channel: Int, x: Float, y: Float) {
        s.sfx(Sfx.CLICK, 0.6f, 1.2f)
        when (channel) {
            1 -> for ((i, n) in listOf(0, 4, 7, 4).withIndex()) s.after(0.12f + i * 0.13f) { s.sfx(Sfx.NOTE, 0.4f, 2f.pow(n / 12f)) }
            2 -> s.after(0.1f) { s.sfx(Sfx.WHOOSH, 0.35f, 1.4f) }
            3 -> {
                s.after(0.1f) { s.sfx(Sfx.BUBBLE, 0.4f, 1.2f) }
                s.after(0.3f) { s.sfx(Sfx.BUBBLE, 0.4f, 1.5f) }
            }
            4 -> s.after(0.1f) { s.sfx(Sfx.SIZZLE, 0.35f) }
            5 -> s.after(0.1f) { s.sfx(Sfx.SWISH, 0.3f, 1.8f) }
            else -> s.sfx(Sfx.CLICK, 0.5f, 0.7f)
        }
        s.burst(PKind.SPARK, x, y, 3, 0.15f, 0.008f, Color(0xFF7CCBFF))
    }

    private fun film(s: FxStage, arg: Int, x: Float, y: Float) {
        if (arg == 1) {
            s.sfx(Sfx.GR_POPCORN, 0.8f)
            s.after(0.5f) { s.sfx(Sfx.GR_FILM, 0.8f) }
            s.burst(PKind.CRUMB, x, y, 14, 0.4f, 0.01f, Color(0xFFFFF1B0), up = 0.5f, life = 1.2f)
            s.burst(PKind.STAR, x, y - 0.1f, 8, 0.4f, 0.012f, gold)
            for (p in s.world.bodiesIn(s.place)) if (p is Person && !p.held && abs(p.x - x) < 1f && p.anim.face != Face.SLEEP) s.faces(p, Face.WOW, 0.8f, Face.GRIN, 2f)
            s.haptic()
        } else {
            s.sfx(Sfx.FANFARE, 0.5f, 1.2f)
            s.after(0.3f) { s.sfx(Sfx.CLICK, 0.6f, 1.2f) }
            s.burst(PKind.CONFETTI, x, y, 16, 0.6f, 0.012f, up = 0.6f, life = 1.6f)
        }
    }

    private fun lever(s: FxStage, x: Float, y: Float) {
        s.sfx(Sfx.CLICK, 0.8f, 0.7f)
        s.after(0.2f) { s.sfx(Sfx.GR_CREAK, 0.8f, 0.7f) }
        s.after(0.3f) { s.sfx(Sfx.RUMBLE, 0.8f, 0.9f) }
        s.after(1.1f) { s.sfx(Sfx.MAGIC, 0.7f, 0.9f) }
        s.burst(PKind.DUST, x, y + 0.2f, 14, 0.3f, 0.02f, up = 0.1f, life = 1.4f)
        s.burst(PKind.STAR, x, y, 10, 0.5f, 0.012f, gold)
        for (p in s.world.bodiesIn(s.place)) if (p is Person && !p.held && abs(p.x - x) < 1.2f && p.anim.face != Face.SLEEP) s.faces(p, Face.WOW, 1f, Face.GRIN, 1.2f)
        s.haptic()
    }

    private fun table(s: FxStage, arg: Int, x: Float, y: Float) {
        when (arg) {
            1 -> {
                s.sfx(Sfx.GR_BELL, 0.9f)
                s.after(0.35f) { s.sfx(Sfx.WHOOSH, 0.35f, 1.5f) }
                for (i in 0 until 6) s.after(0.5f + i * 0.28f) {
                    s.sfx(Sfx.GR_CLINK, 0.55f, 0.9f + i * 0.06f)
                    s.burst(PKind.SPARK, x - 0.4f + i * 0.16f, y - 0.02f, 2, 0.15f, 0.008f, white)
                }
                s.after(2.4f) { s.sfx(Sfx.SPARKLE, 0.5f, 1.2f) }
                for (p in s.world.bodiesIn(s.place)) if (p is Person && !p.held && abs(p.x - x) < 1.2f && p.anim.face != Face.SLEEP) s.faces(p, Face.WOW, 0.9f, Face.GRIN, 1.2f)
            }
            0 -> {
                s.sfx(Sfx.GR_BELL, 0.8f, 0.85f)
                s.after(0.3f) { s.sfx(Sfx.WHOOSH, 0.4f, 1.3f) }
                for (i in 0 until 3) s.after(0.4f + i * 0.2f) { s.sfx(Sfx.GR_CLINK, 0.4f, 1.1f - i * 0.1f) }
            }
            2 -> s.sfx(Sfx.GR_CLINK, 0.6f, 1f)
            else -> s.sfx(Sfx.TICK, 0.5f, 0.9f)
        }
    }

    private fun cake(s: FxStage, arg: Int, x: Float, y: Float) {
        when (arg) {
            1 -> {
                // «Happy birthday to you …»: one note after the other, a music-box tune.
                var at = 0.1f
                for (i in SONG.indices) {
                    val semi = SONG[i]
                    val beats = SONG_BEATS[i]
                    s.after(at) {
                        s.sfx(Sfx.NOTE, 0.55f, 2f.pow(semi / 12f))
                        if (i % 6 == 0) s.burst(PKind.NOTE, x + (s.random.nextFloat() - 0.5f) * 0.2f, y - 0.1f, 1, 0.1f, 0.013f, party[(i / 6) % party.size], up = 0.2f, life = 1.5f)
                    }
                    at += beats * 0.28f
                }
                for (p in s.world.bodiesIn(s.place)) if (p is Person && !p.held && abs(p.x - x) < 1.3f && p.anim.face != Face.SLEEP) {
                    s.faces(p, Face.GRIN, 1f, Face.LAUGH, 5f)
                    s.voice(p, Sfx.HMM, 0.4f)
                }
            }
            2 -> {
                s.sfx(Sfx.WHOOSH, 0.7f, 1.6f)
                s.after(0.25f) { s.sfx(Sfx.POOF, 0.6f, 1.4f) }
                s.after(0.6f) { s.sfx(Sfx.FANFARE, 0.7f) }
                s.burst(PKind.SMOKE, x, y - 0.1f, 6, 0.1f, 0.02f, Color(0xFFB8B4C8), up = 0.3f, life = 1.4f)
                s.burst(PKind.CONFETTI, x, y - 0.1f, 28, 0.8f, 0.012f, up = 0.8f, life = 2f)
                s.laughAround(x, null, 0.7f, 1.3f)
                s.haptic()
            }
            else -> {
                s.sfx(Sfx.GR_WHOOMP, 0.4f, 1.7f)
                s.after(0.2f) { s.sfx(Sfx.SPARKLE, 0.5f, 1.2f) }
                s.burst(PKind.SPARK, x, y - 0.1f, 6, 0.2f, 0.009f, warm, up = 0.3f)
            }
        }
    }

    private fun mixer(s: FxStage, arg: Int, x: Float, y: Float) {
        if (arg == 0) {
            s.sfx(Sfx.GR_BLEND, 0.8f)
            s.burst(PKind.STEAM, x, y - 0.12f, 3, 0.05f, 0.012f, white, up = 0.1f, life = 1f)
        } else {
            s.sfx(Sfx.SPLAT, 0.9f, 0.9f)
            s.after(0.1f) { s.sfx(Sfx.POP, 0.8f, 0.7f) }
            for (i in 0 until 22) {
                val c = party[i % party.size]
                s.particle(Particle(PKind.DROP, x, y - 0.1f, (s.random.nextFloat() - 0.5f) * 1.6f, -0.9f - s.random.nextFloat() * 0.9f, 1.3f, 0.012f, c))
            }
            s.laughAround(x, null, 0.5f, 1f)
        }
    }

    private fun grow(s: FxStage, stage: Int, x: Float, y: Float) {
        if (stage < 0) {
            s.sfx(Sfx.SWISH, 0.5f, 1.2f)
            repeat(6) { s.particle(Particle(PKind.LEAF, x + (s.random.nextFloat() - 0.5f) * 0.18f, y - 0.1f, (s.random.nextFloat() - 0.5f) * 0.2f, 0.05f, 2.2f, 0.012f, green, s.random.nextFloat() * 360f, 200f)) }
            return
        }
        s.sfx(Sfx.SPLASH, 0.4f, 1.3f)
        s.after(0.2f) { s.sfx(Sfx.POP, 0.7f, 0.8f + stage * 0.15f) }
        repeat(8) { s.particle(Particle(PKind.DROP, x + (s.random.nextFloat() - 0.5f) * 0.16f, y - 0.35f, (s.random.nextFloat() - 0.5f) * 0.3f, -0.1f, 0.9f, 0.009f, water)) }
        s.burst(PKind.STAR, x, y - 0.2f, 4 + stage * 3, 0.3f, 0.01f, green)
        if (stage >= 3) {
            s.after(0.4f) { s.sfx(Sfx.CHIME, 0.7f, 1.2f) }
            s.burst(PKind.HEART, x, y - 0.3f, 4, 0.25f, 0.015f, up = 0.4f, life = 1.4f)
            s.burst(PKind.BUTTERFLY, x, y - 0.3f, 2, 0.15f, 0.013f, pink, up = 0.05f, life = 5f)
        }
    }

    private fun sofie(s: FxStage, arg: Int, x: Float, y: Float) {
        when (arg) {
            0 -> {
                s.sfx(Sfx.CHOMP, 0.8f, 1.1f)
                s.after(0.15f) { s.sfx(Sfx.GIGGLE, 0.4f, 1.5f) }
            }
            1 -> {
                s.sfx(Sfx.BURP, 0.9f, 0.6f)
                s.burst(PKind.BUBBLE, x, y - 0.2f, 5, 0.12f, 0.014f, green, up = 0.3f, life = 1.5f)
                s.laughAround(x, null, 0.5f, 1f)
            }
            2 -> {
                s.sfx(Sfx.BURP, 1f, 0.5f)
                s.after(0.45f) { s.sfx(Sfx.POP, 0.9f, 0.8f) }
                s.after(0.7f) { s.sfx(Sfx.SPLAT, 0.5f, 1.4f) }
                s.burst(PKind.BUBBLE, x, y - 0.2f, 8, 0.15f, 0.015f, green, up = 0.35f, life = 1.6f)
                repeat(10) { s.particle(Particle(PKind.DROP, x + 0.03f, y - 0.25f, 0.3f + s.random.nextFloat() * 0.5f, -0.6f - s.random.nextFloat() * 0.5f, 1.1f, 0.011f, green)) }
                s.laughAround(x, null, 0.9f, 1.3f)
                s.haptic()
            }
            3 -> {
                s.sfx(Sfx.GR_SLURP, 0.8f)
                s.after(0.3f) { s.sfx(Sfx.CHOMP, 0.8f, 0.9f) }
                s.after(0.55f) { s.sfx(Sfx.GULP, 0.7f, 0.7f) }
                s.burst(PKind.HEART, x, y - 0.2f, 2, 0.15f, 0.013f, up = 0.3f, life = 1.2f)
            }
            else -> {
                s.sfx(Sfx.SPLAT, 0.6f, 1.5f)
                s.sfx(Sfx.SPRAY, 0.4f, 1.2f)
                s.burst(PKind.DROP, x, y - 0.2f, 6, 0.3f, 0.008f, green, up = 0.3f)
            }
        }
    }

    private fun rolf(s: FxStage, arg: Int, x: Float, y: Float) {
        when (arg) {
            0 -> {
                s.sfx(Sfx.BEEP, 0.6f, 1.1f)
                s.after(0.15f) { s.sfx(Sfx.BEEP, 0.6f, 0.9f) }
            }
            1 -> {
                s.sfx(Sfx.GR_TADA, 0.7f)
                s.burst(PKind.SPARK, x, y, 6, 0.3f, 0.01f, gold)
            }
            2 -> {
                s.sfx(Sfx.BONK, 0.8f, 0.8f)
                s.after(0.25f) { s.sfx(Sfx.BEEP, 0.5f, 0.8f) }
                s.after(0.45f) { s.sfx(Sfx.BEEP, 0.5f, 0.65f) }
                s.burst(PKind.STAR, x, y, 6, 0.4f, 0.011f, gold, up = 0.3f, life = 0.7f)
            }
            3 -> {
                s.sfx(Sfx.POP, 0.5f, 1.4f)
                s.sfx(Sfx.BEEP, 0.4f, 1.5f)
            }
            4 -> {
                s.sfx(Sfx.DROP, 0.4f, 1.2f)
                s.after(0.15f) { s.sfx(Sfx.GR_TADA, 0.4f, 1.2f) }
                s.burst(PKind.SPARK, x, y, 4, 0.25f, 0.009f, white)
            }
            5 -> {
                s.sfx(Sfx.GR_TADA, 0.6f, 0.9f)
                s.after(0.5f) { s.sfx(Sfx.GR_CLINK, 0.6f, 0.8f) }
            }
            else -> {
                s.sfx(Sfx.CLICK, 0.5f, 1.2f)
                s.after(0.15f) { s.sfx(Sfx.BEEP, 0.4f, 1.3f) }
            }
        }
    }
}

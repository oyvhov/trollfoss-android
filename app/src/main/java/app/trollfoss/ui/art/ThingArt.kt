package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import app.trollfoss.domain.Cat
import app.trollfoss.domain.ThingType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Draws a thing with its origin at the bottom centre, inside the box (-w/2, -h) to (w/2, 0) in pixels.
 * [variant] is the colour or flavour, [used] how many bites have been taken (drawn as bite marks, or a
 * lower level for drinks), and [cook] a hint of browning for food on the stove (below zero: a rocket in
 * flight). Hats are drawn as they sit on a head; glasses are centred in their box.
 */
fun DrawScope.drawThing(type: ThingType, variant: Int, used: Int, w: Float, h: Float, pen: Pen, cook: Float = 0f) {
    val bites = if (type.cat == Cat.FOOD && type.bites > 0 && type != ThingType.SPACE_FOOD) used.coerceIn(0, 3) else 0
    val warm = if ((type.cat == Cat.FOOD || type.cat == Cat.DRINK) && cook > 0f) (cook / 2f).coerceIn(0f, 1f) else 0f
    if (bites == 0 && warm == 0f) {
        thing(type, variant, used, w, h, pen, cook)
        return
    }
    // Bites and browning are painted into a layer of their own, so they only ever touch the thing.
    val pad = pen.lw * 4f + maxOf(w, h) * 0.15f
    val bounds = Rect(-w / 2 - pad, -h - pad, w / 2 + pad, pad)
    val canvas = drawContext.canvas
    canvas.saveLayer(bounds, LayerPaint)
    if (bites > 0) {
        val spots = biteSpots(type, bites, w, h)
        canvas.save()
        var i = 0
        while (i < spots.size) {
            canvas.clipPath(biteHole(bounds, spots[i], spots[i + 1], spots[i + 2]))
            i += 3
        }
        thing(type, variant, used, w, h, pen, cook)
        canvas.restore()
        // The bitten edge: a band of the pale inside, then ink. SrcAtop keeps both on what is left.
        val flesh = fleshOf(type)
        if (flesh != null) {
            val band = maxOf(pen.lw * 1.5f, minOf(w, h) * 0.06f)
            i = 0
            while (i < spots.size) {
                drawCircle(flesh, spots[i + 2] + band / 2, Offset(spots[i], spots[i + 1]), style = Stroke(band), blendMode = BlendMode.SrcAtop)
                i += 3
            }
        }
        i = 0
        while (i < spots.size) {
            drawCircle(Ink.line, spots[i + 2], Offset(spots[i], spots[i + 1]), style = Stroke(pen.lw * 1.8f), blendMode = BlendMode.SrcAtop)
            i += 3
        }
    } else {
        thing(type, variant, used, w, h, pen, cook)
    }
    if (warm > 0f) drawRect(ThingInk.toast.copy(alpha = 0.34f * warm), bounds.topLeft, bounds.size, blendMode = BlendMode.SrcAtop)
    canvas.restore()
    if (warm > 0.1f && type.cat == Cat.FOOD) sizzleSteam(w, h, pen, warm)
}

/** One exhaustive `when`: the compiler makes sure every thing has its drawing. */
private fun DrawScope.thing(type: ThingType, v: Int, used: Int, w: Float, h: Float, pen: Pen, cook: Float): Unit = when (type) {
    // Food
    ThingType.APPLE -> thApple(w, h, pen)
    ThingType.BANANA -> thBanana(w, h, pen)
    ThingType.STRAWBERRY -> thStrawberry(w, h, pen)
    ThingType.CARROT -> thCarrot(w, h, pen)
    ThingType.WATERMELON -> thWatermelon(w, h, pen)
    ThingType.CLOUDBERRY -> thCloudberry(w, h, pen)
    ThingType.BREAD -> thBread(w, h, pen)
    ThingType.BUN -> thBun(w, h, pen)
    ThingType.WAFFLE -> thWaffle(w, h, pen)
    ThingType.CAKE -> thCake(w, h, pen)
    ThingType.CUPCAKE -> thCupcake(v, w, h, pen)
    ThingType.PIZZA -> thPizza(w, h, pen)
    ThingType.PANCAKE -> thPancake(w, h, pen)
    ThingType.COOKIE -> thCookie(w, h, pen)
    ThingType.ICE_CREAM -> thIceCream(v, w, h, pen)
    ThingType.LOLLIPOP -> thLollipop(v, w, h, pen)
    ThingType.BROWN_CHEESE -> thBrownCheese(w, h, pen)
    ThingType.EGG -> thEgg(v, w, h, pen)
    ThingType.FRIED_EGG -> thFriedEgg(w, h, pen)
    ThingType.FISH -> thFish(false, w, h, pen)
    ThingType.GRILLED_FISH -> thFish(true, w, h, pen)
    ThingType.SAUSAGE -> thSausage(false, w, h, pen)
    ThingType.GRILLED_SAUSAGE -> thSausage(true, w, h, pen)
    ThingType.MARSHMALLOW -> thMarshmallow(false, w, h, pen)
    ThingType.TOASTED_MARSHMALLOW -> thMarshmallow(true, w, h, pen)
    ThingType.DOUGH -> thDough(w, h, pen)
    ThingType.POTATO -> thPotato(w, h, pen)
    ThingType.SPACE_FOOD -> thSpaceFood(v, used, w, h, pen)

    // Drinks and potions
    ThingType.MILK -> thMilk(used, w, h, pen)
    ThingType.JUICE -> thJuice(used, w, h, pen)
    ThingType.SMOOTHIE -> thSmoothie(v, used, w, h, pen)
    ThingType.COCOA -> thCocoa(used, w, h, pen)
    ThingType.POTION_GROW -> thPotionGrow(w, h, pen)
    ThingType.POTION_SHRINK -> thPotionShrink(w, h, pen)
    ThingType.POTION_RAINBOW -> thPotionRainbow(w, h, pen)
    ThingType.POTION_FLOAT -> thPotionFloat(w, h, pen)
    ThingType.POTION_NORMAL -> thPotionNormal(w, h, pen)

    // Hats and glasses
    ThingType.CAP -> thCap(v, w, h, pen)
    ThingType.BEANIE -> thBeanie(v, w, h, pen)
    ThingType.CROWN -> thCrown(v, w, h, pen)
    ThingType.PARTY_HAT -> thPartyHat(v, w, h, pen)
    ThingType.VIKING_HELMET -> thVikingHelmet(w, h, pen)
    ThingType.FLOWER_CROWN -> thFlowerCrown(w, h, pen)
    ThingType.CHEF_HAT -> thChefHat(w, h, pen)
    ThingType.SUN_HAT -> thSunHat(w, h, pen)
    ThingType.WIZARD_HAT -> thWizardHat(w, h, pen)
    ThingType.BOW -> thBow(v, w, h, pen)
    ThingType.NISSE_HAT -> thNisseHat(w, h, pen)
    ThingType.SUNGLASSES -> thSunglasses(w, h, pen)
    ThingType.ROUND_GLASSES -> thRoundGlasses(w, h, pen)
    ThingType.SPACE_HELMET -> thSpaceHelmet(w, h, pen)
    ThingType.STAR_GLASSES -> thStarGlasses(w, h, pen)
    ThingType.GARMENT -> thGarment(v, w, h, pen)

    // Toys
    ThingType.BALL -> thBall(w, h, pen)
    ThingType.BEACH_BALL -> thBeachBall(w, h, pen)
    ThingType.TEDDY -> thTeddy(w, h, pen)
    ThingType.BALLOON -> thBalloon(v, w, h, pen)
    ThingType.DUCK -> thDuck(w, h, pen)
    ThingType.GUITAR -> thGuitar(w, h, pen)
    ThingType.DRUM -> thDrum(w, h, pen)
    ThingType.BOOK -> thBook(v, w, h, pen)
    ThingType.PHONE -> thPhone(w, h, pen)
    ThingType.TOY_CAR -> thToyCar(v, w, h, pen)
    ThingType.SWIM_RING -> thSwimRing(w, h, pen)
    ThingType.ROCKET -> thRocket(cook, w, h, pen)
    ThingType.SNOWBALL -> thSnowball(w, h, pen)
    ThingType.SLED -> thSled(w, h, pen)

    // Tools
    ThingType.SCISSORS -> thScissors(w, h, pen)
    ThingType.HAIR_DRYER -> thHairDryer(w, h, pen)
    ThingType.COMB -> thComb(w, h, pen)
    ThingType.SPRAY -> thSpray(v, w, h, pen)
    ThingType.WAND -> thWand(w, h, pen)
    ThingType.BUCKET -> thBucket(v, w, h, pen)
    ThingType.SPADE -> thSpade(w, h, pen)
    ThingType.TOOTHBRUSH -> thToothbrush(w, h, pen)
    ThingType.HAMMER -> thHammer(w, h, pen)
    ThingType.SAW -> thSaw(w, h, pen)
    ThingType.WRENCH -> thWrench(w, h, pen)
    ThingType.SCREWDRIVER -> thScrewdriver(w, h, pen)
    ThingType.WATERING_CAN -> thWateringCan(w, h, pen)
    ThingType.SEEDS -> thSeeds(w, h, pen)

    // Nature
    ThingType.SHELL -> thShell(w, h, pen)
    ThingType.STARFISH -> thStarfish(w, h, pen)
    ThingType.FLOWER -> thFlower(v, w, h, pen)
    ThingType.MUSHROOM -> thMushroom(w, h, pen)
    ThingType.PINECONE -> thPinecone(w, h, pen)
    ThingType.STICK -> thStick(w, h, pen)
    ThingType.ROCK -> thRock(w, h, pen)
    ThingType.LEAF -> thLeaf(w, h, pen)
    ThingType.FEATHER -> thFeather(w, h, pen)

    // Home
    ThingType.CUP -> thCup(v, w, h, pen)
    ThingType.PILLOW -> thPillow(v, w, h, pen)
    ThingType.PLANT_POT -> thPlantPot(w, h, pen)
    ThingType.CANDLE -> thCandle(w, h, pen)
    ThingType.PLANK -> thPlank(w, h, pen)
    ThingType.TIRE -> thTire(w, h, pen)
    ThingType.BIRDHOUSE -> thBirdhouse(w, h, pen)

    // Magic and treasure
    ThingType.GEM -> thGem(v, w, h, pen)
    ThingType.GIFT -> thGift(v, w, h, pen)
    ThingType.COIN -> thCoin(w, h, pen)
    ThingType.BOOT -> thBoot(w, h, pen)
    ThingType.SLIME -> thSlime(v, w, h, pen)
    ThingType.STAR_JAR -> thStarJar(w, h, pen)
    ThingType.DRAGON_EGG -> thDragonEgg(cook, w, h, pen)
    ThingType.PLANET -> thPlanet(v, w, h, pen)

    // Jokes
    ThingType.WHOOPEE -> thWhoopee(v, w, h, pen)
    ThingType.BANANA_PEEL -> thBananaPeel(w, h, pen)
    ThingType.PEPPER -> thPepper(w, h, pen)

    // Tivoli, shop, doctor, stage and sea
    ThingType.CANDY_FLOSS -> thCandyFloss(v, w, h, pen)
    ThingType.POPCORN -> thPopcorn(w, h, pen)
    ThingType.SODA -> thSoda(v, used, w, h, pen)
    ThingType.SYRUP -> thSyrup(w, h, pen)
    ThingType.BANDAGE -> thBandage(w, h, pen)
    ThingType.THERMOMETER -> thThermometer(w, h, pen)
    ThingType.STETHOSCOPE -> thStethoscope(w, h, pen)
    ThingType.MICROPHONE -> thMicrophone(w, h, pen)
    ThingType.PEARL -> thPearl(w, h, pen)
    ThingType.DIVING_MASK -> thDivingMask(w, h, pen)
}

// ------------------------------------------------------------------ bites and cooking

private val LayerPaint = Paint()

/** A hole for one bite: the whole layer plus one circle, even-odd, so the circle is cut away. */
private fun biteHole(bounds: Rect, x: Float, y: Float, r: Float): Path = Path().apply {
    fillType = PathFillType.EvenOdd
    addRect(bounds)
    addOval(Rect(x - r, y - r, x + r, y + r))
}

// Where the bites land in the eaten part, as fractions of it: first the upper right corner, then down
// the right side, then along the top.
private val BITE_AT = floatArrayOf(0.94f, 0.1f, 1.02f, 0.48f, 0.6f, -0.02f)
private val FISH_BITES = floatArrayOf(0.14f, -0.92f, -0.05f, -1.02f, -0.24f, -1.0f)
private const val TOOTH_COS = 0.766f
private const val TOOTH_SIN = 0.643f

/** Circles (x, y, r) for [n] bites: each bite is a big round mouthful with two small tooth scallops. */
private fun biteSpots(type: ThingType, n: Int, w: Float, h: Float): FloatArray {
    val e = eatBox(type)
    val l = e[0] * w
    val t = e[1] * h
    val r = e[2] * w
    val b = e[3] * h
    val ew = r - l
    val eh = b - t
    val fish = type == ThingType.FISH || type == ThingType.GRILLED_FISH
    val rad = if (fish) h * 0.26f else minOf(ew, eh) * 0.3f
    val cx = (l + r) / 2
    val cy = (t + b) / 2
    val out = FloatArray(n * 9)
    for (k in 0 until n) {
        // A fish is bitten along its back, so the tail stays on.
        val px = if (fish) FISH_BITES[k * 2] * w else l + ew * BITE_AT[k * 2]
        val py = if (fish) FISH_BITES[k * 2 + 1] * h else t + eh * BITE_AT[k * 2 + 1]
        var dx = cx - px
        var dy = cy - py
        val len = sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)
        dx /= len
        dy /= len
        val o = k * 9
        out[o] = px
        out[o + 1] = py
        out[o + 2] = rad
        out[o + 3] = px + (dx * TOOTH_COS - dy * TOOTH_SIN) * rad * 0.95f
        out[o + 4] = py + (dx * TOOTH_SIN + dy * TOOTH_COS) * rad * 0.95f
        out[o + 5] = rad * 0.42f
        out[o + 6] = px + (dx * TOOTH_COS + dy * TOOTH_SIN) * rad * 0.95f
        out[o + 7] = py + (-dx * TOOTH_SIN + dy * TOOTH_COS) * rad * 0.95f
        out[o + 8] = rad * 0.42f
    }
    return out
}

private val EAT_ALL = floatArrayOf(-0.5f, -1f, 0.5f, 0f)
private val EAT_SCOOP = floatArrayOf(-0.5f, -1f, 0.5f, -0.55f)
private val EAT_LOLLY = floatArrayOf(-0.5f, -0.97f, 0.5f, -0.62f)
private val EAT_MALLOW = floatArrayOf(-0.45f, -0.98f, 0.45f, -0.64f)
private val EAT_CARROT = floatArrayOf(-0.46f, -0.76f, 0.46f, -0.05f)
private val EAT_CUPCAKE = floatArrayOf(-0.5f, -0.96f, 0.5f, -0.38f)
private val EAT_CAKE = floatArrayOf(-0.46f, -0.86f, 0.46f, -0.1f)
private val EAT_APPLE = floatArrayOf(-0.49f, -0.84f, 0.49f, -0.03f)
private val EAT_FISH = floatArrayOf(-0.5f, -0.95f, 0.3f, -0.05f)
private val EAT_CHEESE = floatArrayOf(-0.3f, -0.82f, 0.46f, -0.02f)

private fun eatBox(type: ThingType): FloatArray = when (type) {
    ThingType.ICE_CREAM -> EAT_SCOOP
    ThingType.LOLLIPOP -> EAT_LOLLY
    ThingType.TOASTED_MARSHMALLOW, ThingType.MARSHMALLOW -> EAT_MALLOW
    ThingType.CARROT -> EAT_CARROT
    ThingType.CUPCAKE -> EAT_CUPCAKE
    ThingType.CAKE -> EAT_CAKE
    ThingType.APPLE -> EAT_APPLE
    ThingType.GRILLED_FISH, ThingType.FISH -> EAT_FISH
    ThingType.BROWN_CHEESE -> EAT_CHEESE
    else -> EAT_ALL
}

/** The pale inside a bite shows, or null where the inside looks like the outside. */
private fun fleshOf(type: ThingType): Color? = when (type) {
    ThingType.APPLE -> Color(0xFFFFF1C4)
    ThingType.BANANA -> Color(0xFFFFF6D2)
    ThingType.STRAWBERRY -> Color(0xFFFFB8C0)
    ThingType.CARROT -> Color(0xFFFFB870)
    ThingType.BREAD, ThingType.BUN -> Color(0xFFFBE2AE)
    ThingType.WAFFLE, ThingType.PANCAKE -> Color(0xFFFBDC98)
    ThingType.CAKE, ThingType.CUPCAKE -> Color(0xFFFFE08A)
    ThingType.PIZZA -> Color(0xFFFFE9A8)
    ThingType.COOKIE -> Color(0xFFEAC085)
    ThingType.GRILLED_FISH, ThingType.FISH -> Color(0xFFFFF7EE)
    ThingType.GRILLED_SAUSAGE, ThingType.SAUSAGE -> Color(0xFFF5A894)
    ThingType.TOASTED_MARSHMALLOW, ThingType.MARSHMALLOW -> Color(0xFFFFFFFF)
    ThingType.POTATO -> Color(0xFFFFF0B8)
    ThingType.FRIED_EGG -> Color(0xFFFFFFFF)
    else -> null
}

/** Two little curls of steam rising off food that warms on the stove. */
private fun DrawScope.sizzleSteam(w: Float, h: Float, pen: Pen, warm: Float) {
    val s = maxOf(h, w * 0.5f)
    for (k in 0 until 2) {
        val ph = (pen.t * 0.6f + k * 0.5f) % 1f
        val x = (k - 0.5f) * w * 0.3f
        val y0 = -h * 0.75f - ph * s * 0.45f
        val sway = sin(pen.t * 3f + k * 2f) * s * 0.05f
        val p = Path().apply {
            moveTo(x, y0)
            quadraticTo(x + s * 0.1f + sway, y0 - s * 0.14f, x, y0 - s * 0.28f)
            quadraticTo(x - s * 0.1f - sway, y0 - s * 0.42f, x + sway, y0 - s * 0.56f)
        }
        drawPath(p, Color.White.copy(alpha = 0.75f * warm * (1f - ph)), style = Stroke(pen.lw * 1.1f, cap = StrokeCap.Round))
    }
}

// ------------------------------------------------------------------ shared kit for things

/** Colours the thing drawings share (docs/ART_GUIDE.md palette). */
internal object ThingInk {
    val cream = Color(0xFFFFF4DC)
    val paper = Color(0xFFF7F4EE)
    val wood = Color(0xFFC98A55)
    val woodDark = Color(0xFFA0663B)
    val woodLight = Color(0xFFE3B27A)
    val bark = Color(0xFF7A4A2A)
    val falun = Color(0xFFB8342B)
    val red = Color(0xFFD2443A)
    val tomato = Color(0xFFFF5A4E)
    val berry = Color(0xFFFF4D6D)
    val yellow = Color(0xFFFFC83D)
    val sun = Color(0xFFFFD23F)
    val green = Color(0xFF3BC46B)
    val leaf = Color(0xFF5DBB4A)
    val gran = Color(0xFF2E8B57)
    val blue = Color(0xFF2F6FB8)
    val sky = Color(0xFF4AB3FF)
    val pink = Color(0xFFFF6FA8)
    val purple = Color(0xFF8B5CF6)
    val orange = Color(0xFFFF9F43)
    val snow = Color(0xFFF4F8FF)
    val snowShade = Color(0xFFC9D6EE)
    val gold = Color(0xFFFFC83D)
    val goldDark = Color(0xFFD99A1E)
    val silver = Color(0xFFD9DDE8)
    val steel = Color(0xFFA9B1C2)
    val dark = Color(0xFF3A3340)
    val toast = Color(0xFF7A3E12)
    val glass = Color(0x40DDF3FF)
}

internal val SIDES = floatArrayOf(-1f, 1f)

/** Builds a path in fractions of the thing's box: x from -0.5 to 0.5, y from -1 (top) to 0 (bottom). */
internal class ThingSketch(private val w: Float, private val h: Float) {
    val path = Path()
    fun m(x: Float, y: Float) = path.moveTo(x * w, y * h)
    fun l(x: Float, y: Float) = path.lineTo(x * w, y * h)
    fun q(x1: Float, y1: Float, x: Float, y: Float) = path.quadraticTo(x1 * w, y1 * h, x * w, y * h)
    fun c(x1: Float, y1: Float, x2: Float, y2: Float, x: Float, y: Float) =
        path.cubicTo(x1 * w, y1 * h, x2 * w, y2 * h, x * w, y * h)
    fun z() = path.close()
}

internal inline fun thSketch(w: Float, h: Float, block: ThingSketch.() -> Unit): Path = ThingSketch(w, h).apply(block).path

/** A flat fill with a fine ink rim, for small details. */
internal fun DrawScope.thFill(path: Path, color: Color, pen: Pen) {
    drawPath(path, color)
    drawPath(path, Ink.line, style = pen.thin)
}

internal fun DrawScope.thDot(center: Offset, r: Float, color: Color, pen: Pen) {
    drawCircle(color, r, center)
    drawCircle(Ink.line, r, center, style = pen.thin)
}

/**
 * Several overlapping shapes in one [path] drawn as a single inked silhouette: the ink goes round the
 * outside only, like a cloud or a bunch of flowers.
 */
internal fun DrawScope.thUnion(path: Path, color: Color, pen: Pen, shade: Boolean = true) {
    drawPath(path, Ink.line, style = Stroke(pen.lw * 2f, join = StrokeJoin.Round))
    if (!shade) {
        drawPath(path, color)
        return
    }
    val b = path.getBounds()
    val s = minOf(b.width, b.height) * 0.12f
    drawPath(path, color.shadow())
    clipPath(path) { translate(-s * 0.5f, -s) { drawPath(path, color) } }
}

/** A leaf or lens shape from [a] to [b], [bulge] wide on each side of the middle. */
internal fun thLens(a: Offset, b: Offset, bulge: Float): Path {
    val mx = (a.x + b.x) / 2
    val my = (a.y + b.y) / 2
    val dx = b.x - a.x
    val dy = b.y - a.y
    val len = sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)
    val nx = -dy / len * bulge * 2f
    val ny = dx / len * bulge * 2f
    return Path().apply {
        moveTo(a.x, a.y)
        quadraticTo(mx + nx, my + ny, b.x, b.y)
        quadraticTo(mx - nx, my - ny, a.x, a.y)
        close()
    }
}

/** A heart of width [s] centred on ([cx], [cy]), point down. */
internal fun thHeart(cx: Float, cy: Float, s: Float): Path = Path().apply {
    moveTo(cx, cy + 0.42f * s)
    cubicTo(cx - 0.1f * s, cy + 0.3f * s, cx - 0.5f * s, cy + 0.1f * s, cx - 0.5f * s, cy - 0.15f * s)
    cubicTo(cx - 0.5f * s, cy - 0.42f * s, cx - 0.12f * s, cy - 0.5f * s, cx, cy - 0.25f * s)
    cubicTo(cx + 0.12f * s, cy - 0.5f * s, cx + 0.5f * s, cy - 0.42f * s, cx + 0.5f * s, cy - 0.15f * s)
    cubicTo(cx + 0.5f * s, cy + 0.1f * s, cx + 0.1f * s, cy + 0.3f * s, cx, cy + 0.42f * s)
    close()
}

private val STAR8_COS = FloatArray(16) { cos(Math.toRadians(it * 22.5 - 90.0)).toFloat() }
private val STAR8_SIN = FloatArray(16) { sin(Math.toRadians(it * 22.5 - 90.0)).toFloat() }

/** The eight-pointed Selbu rose of lusekofte knitting. */
internal fun thSelbu(c: Offset, r: Float): Path = Path().apply {
    for (i in 0 until 16) {
        val rr = if (i % 2 == 0) r else r * 0.46f
        val x = c.x + rr * STAR8_COS[i]
        val y = c.y + rr * STAR8_SIN[i]
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

/** A round fluffy pompom outline with [bumps] soft bumps. */
internal fun thFluff(c: Offset, r: Float, bumps: Int): Path = Path().apply {
    val step = (2.0 * Math.PI / bumps)
    for (i in 0..bumps) {
        val a = i * step
        val x = c.x + r * cos(a).toFloat()
        val y = c.y + r * sin(a).toFloat()
        if (i == 0) {
            moveTo(x, y)
        } else {
            val m = a - step / 2
            quadraticTo(c.x + r * 1.28f * cos(m).toFloat(), c.y + r * 1.28f * sin(m).toFloat(), x, y)
        }
    }
    close()
}

/** A soft round glow; a gradient, not a blur. */
internal fun DrawScope.thGlow(center: Offset, radius: Float, color: Color, alpha: Float) {
    if (alpha <= 0f || radius <= 0f) return
    drawCircle(
        Brush.radialGradient(listOf(color.copy(alpha = alpha), color.copy(alpha = alpha * 0.45f), color.copy(alpha = 0f)), center, radius),
        radius,
        center,
    )
}

/** A soft volumetric highlight for round things: light from the upper left, fading out. */
internal fun DrawScope.thSheen(c: Offset, r: Float, alpha: Float = 0.42f) {
    drawCircle(
        Brush.radialGradient(
            listOf(Color.White.copy(alpha = alpha), Color.White.copy(alpha = alpha * 0.3f), Color.White.copy(alpha = 0f)),
            Offset(c.x - r * 0.38f, c.y - r * 0.42f),
            r * 0.95f,
        ),
        r,
        c,
    )
}

/**
 * Oblique depth for a flat shape: copies of [front] step back by ([dx], [dy]) pixels (up and to the
 * right, as the world's «skrå-3D»), filled with [band] and inked round the outside only. Draw the
 * front face on top afterwards.
 */
internal fun DrawScope.thExtrude(front: Path, dx: Float, dy: Float, band: Color, pen: Pen) {
    val len = sqrt(dx * dx + dy * dy)
    val steps = (len / (pen.lw * 0.7f)).toInt().coerceIn(2, 10)
    val ink = Stroke(pen.lw * 2f, join = StrokeJoin.Round)
    for (i in steps downTo 0) translate(dx * i / steps, dy * i / steps) { drawPath(front, Ink.line, style = ink) }
    for (i in steps downTo 1) translate(dx * i / steps, dy * i / steps) { drawPath(front, band) }
}

/** Oblique offset, in pixels, for [depth] pixels of depth. */
internal fun obliqueX(depth: Float): Float = Oblique.DX * depth
internal fun obliqueY(depth: Float): Float = Oblique.DY * depth

/** A twinkle that swells and fades on its own beat. */
internal fun DrawScope.thGlint(center: Offset, radius: Float, t: Float, speed: Float, phase: Float, color: Color = Color.White) {
    val s = sin(t * speed + phase)
    if (s <= 0f) return
    twinkle(center, radius * (0.4f + 0.6f * s), color, s)
}

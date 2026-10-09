package app.trollfoss.domain

import kotlin.random.Random

/**
 * Who a figure is. Folk are the people of Trollfoss; the others are animals and creatures. [height] is
 * the standing height in scene units at look height 1.0.
 */
enum class Species(val height: Float, val widthRatio: Float) {
    FOLK(0.30f, 0.5f),
    CAT(0.12f, 0.95f),
    DOG(0.135f, 0.95f),
    BUNNY(0.125f, 0.8f),
    DRAGON(0.14f, 1.0f),
    ELK(0.17f, 0.8f),
    PUFFIN(0.11f, 0.8f),
    COW(0.2f, 1.0f),
    SHEEP(0.15f, 1.0f),
    CHICKEN(0.1f, 0.8f),
    HORSE(0.23f, 0.85f),
    GOAT(0.15f, 0.9f),

    // Storhuset's own: the friendly ghost, who floats, and the robot butler.
    GHOST(0.24f, 0.62f),
    ROBOT(0.27f, 0.6f),
    ;

    val pet: Boolean get() = this != FOLK
}

/** How many choices each part of a figure has. The art draws every index below these counts. */
object Styles {
    /** Original indices 0..8 stay stable; 9 ponytail, 10 pigtails, 11 afro, 12 space buns,
     * 13 quiff, 14 locks with beads, 15 waves, 16 mohawk, 17 curly pigtails. */
    // Appended portraits: 18 tousled waves, 19 long side part, 20 shoulder waves, 21 short crop.
    const val HAIRS = 26 // 22 swept crop, 23 long centre part, 24 tied back, 25 soft fringe.
    /** 0 round, 1 bright, 2 sleepy, 3 lashes, 4 dots, 5 narrow, 6 soft oval, 7 star pupils. */
    const val EYES = 8
    /** 0 plain, 1 cat, 2 bunny, 3 bear, 4 troll. */
    const val EARS = 5
    /** Original 0..6, then 7 jacket, 8 button shirt, 9 ribbed sweater, 10 sports shirt, 11 cape. */
    const val TOPS = 17 // 12 open jacket, 13 soft top, 14 quarter zip, 15 zipped fleece, 16 quilted jacket.
    /** 0 trousers, 1 shorts, 2 skirt, 3 cargo trousers, 4 striped socks. */
    const val BOTTOMS = 5
    /** Original 0..3, then 4 freckles/stubble, 5 cheek stars, 6 patch, 7 earrings, 8 dimples. */
    const val EXTRAS = 12 // 9 trimmed beard, 10 full beard and moustache, 11 light stubble.
    const val FACES = 4
    const val NOSES = 4
    const val MOUTHS = 6 // 5 broad smile with a little row of teeth.
    const val PATTERNS = 11 // 6 football, 7 flowers/sun, 8 pinstripes, 9 sun, 10 meadow flowers.

    /** Figure heights the workshop offers: child, youth, grown-up, tall. */
    val HEIGHTS = floatArrayOf(0.78f, 0.9f, 1.03f, 1.14f)
}

/** Colours shared by the domain and the art, as ARGB. */
object Palette {
    val skins = intArrayOf(
        0xFFFFE3CC.toInt(), 0xFFF9D0B0.toInt(), 0xFFEDB58D.toInt(), 0xFFD9986B.toInt(),
        0xFFB97A4E.toInt(), 0xFF94603C.toInt(), 0xFF6E4428.toInt(), 0xFF4A2E1C.toInt(),
        // Creature colours for trolls and other folk.
        0xFF8FD3FF.toInt(), 0xFF7EE0A1.toInt(), 0xFFC9A4FF.toInt(), 0xFFFF9EC7.toInt(),
        0xFFFFD35C.toInt(), 0xFF7FB5A6.toInt(),
    )
    const val HUMAN_SKINS = 8

    val hairs = intArrayOf(
        0xFF2B1D16.toInt(), 0xFF5A3824.toInt(), 0xFF9C5B2E.toInt(), 0xFFE8B04A.toInt(),
        0xFFF4E3B5.toInt(), 0xFFC74B2A.toInt(), 0xFFA9A9B8.toInt(), 0xFFFF6FA8.toInt(),
        0xFF4A8BFF.toInt(), 0xFF3DDC97.toInt(),
        0xFFAD6CE8.toInt(), 0xFFEEF4FF.toInt(), 0xFF20BFC2.toInt(), 0xFFEA925C.toInt(),
        0xFFA67B50.toInt(), // Light brown waves; appended so old colours keep their meaning.
        0xFFCBB58D.toInt(), 0xFF6C675F.toInt(), 0xFF775744.toInt(), // Natural blonde, salt/pepper, soft brown.
        0xFFC67A3C.toInt(), // Soft copper.
    )

    val eyes = intArrayOf(
        0xFF805535.toInt(), 0xFF3C86C5.toInt(), 0xFF3C9168.toInt(), 0xFF6C778E.toInt(),
        0xFFC28B37.toInt(), 0xFF392C4D.toInt(), 0xFFAE6ACF.toInt(), 0xFFEC759F.toInt(),
        0xFF24B5BC.toInt(), 0xFFDF7540.toInt(), 0xFFADD358.toInt(), 0xFFBECCE6.toInt(),
    )

    val cloth = intArrayOf(
        0xFFFF5A4E.toInt(), 0xFFFF9F43.toInt(), 0xFFFFD23F.toInt(), 0xFF3BC46B.toInt(),
        0xFF1FB5A8.toInt(), 0xFF4AB3FF.toInt(), 0xFF3D6BFF.toInt(), 0xFF8B5CF6.toInt(),
        0xFFFF6FA8.toInt(), 0xFFF7F4EE.toInt(), 0xFF8E93A6.toInt(), 0xFF2F3552.toInt(),
        0xFFA0663B.toInt(), 0xFFF3DDB5.toInt(),
        0xFFA4D43B.toInt(), 0xFF158783.toInt(), // Lime boots and deep teal sportswear.
        0xFFB699CE.toInt(), 0xFF597E9F.toInt(), 0xFFC49894.toInt(), // Lilac, denim, dusty rose.
        0xFFCAC5B8.toInt(), 0xFFBE6570.toInt(), 0xFFD9B96F.toInt(), // Stone, berry, warm ochre.
        0xFF373941.toInt(), 0xFF615046.toInt(), 0xFFAFB2B0.toInt(), // Charcoal, cocoa, ash grey.
        0xFF28699E.toInt(), // Outdoor jacket blue; keep every earlier colour index stable.
    )

    /** Fur for cats, dogs and bunnies. */
    val furs = intArrayOf(
        0xFFF5A04A.toInt(), 0xFF9DA3AE.toInt(), 0xFF3A3340.toInt(), 0xFFF5F1EA.toInt(),
        0xFF8C5A36.toInt(), 0xFFEBD2A8.toInt(),
    )

    /** Coats for the moose calf. */
    val elk = intArrayOf(0xFF8C5A36.toInt(), 0xFF6E4428.toInt(), 0xFFA0663B.toInt())

    /** Scales for the dragon. */
    val scales = intArrayOf(0xFF5BD68A.toInt(), 0xFFFF8A5B.toInt(), 0xFF7FB0FF.toInt(), 0xFFC97BFF.toInt())

    /** Shells for Rolf the robot butler: mint, butter, coral, sky and lilac (five, so look 10 is the mint one). */
    val robots = intArrayOf(0xFF5FCDBE.toInt(), 0xFFF3C75B.toInt(), 0xFFF08A86.toInt(), 0xFF6DB4F0.toInt(), 0xFFB79BF0.toInt())

    /** Sheets for Sture the ghost: blue-white, mint-white and lilac-white (three, so look 9 is the blue-white one). */
    val ghosts = intArrayOf(0xFFEAF5FF.toInt(), 0xFFE4F8EC.toInt(), 0xFFF0E8FF.toInt())

    /** Smoothie, slime and gem colours; the first six are the fruit colours in [ThingType.FRUITS] order. */
    val juice = intArrayOf(
        0xFF8BD450.toInt(), 0xFFFFE066.toInt(), 0xFFFF5C8A.toInt(), 0xFFFF9A3D.toInt(),
        0xFFFF6B6B.toInt(), 0xFFFFB347.toInt(), 0xFF9B6BFF.toInt(), 0xFF4FC3FF.toInt(),
    )

    fun furFor(species: Species, index: Int): Int = when (species) {
        Species.DRAGON -> scales[index.mod(scales.size)]
        Species.ELK -> elk[index.mod(elk.size)]
        Species.PUFFIN -> 0xFF2B2140.toInt()
        Species.COW -> intArrayOf(0xFFF5F1EA.toInt(), 0xFF8C5A36.toInt(), 0xFF3A3340.toInt())[index.mod(3)]
        Species.SHEEP -> intArrayOf(0xFFF5F1EA.toInt(), 0xFFE8DCC8.toInt(), 0xFF3A3340.toInt())[index.mod(3)]
        Species.GOAT -> intArrayOf(0xFFF5F1EA.toInt(), 0xFFB98B6A.toInt(), 0xFF5A5564.toInt())[index.mod(3)]
        Species.CHICKEN -> intArrayOf(0xFFF5F1EA.toInt(), 0xFFC96A2B.toInt(), 0xFF3A3340.toInt())[index.mod(3)]
        Species.HORSE -> intArrayOf(0xFFE3C08A.toInt(), 0xFFD6A96A.toInt())[index.mod(2)]
        Species.ROBOT -> robots[index.mod(robots.size)]
        Species.GHOST -> ghosts[index.mod(ghosts.size)]
        else -> furs[index.mod(furs.size)]
    }
}

/**
 * Everything the figure workshop can change. Out-of-range values from an old save or a newer version
 * are clamped by [safe], so drawing never fails.
 */
data class Look(
    val skin: Int = 2,
    val height: Float = 1.03f,
    val hair: Int = 1,
    val hairColor: Int = 1,
    val eyes: Int = 0,
    val ears: Int = 0,
    val top: Int = 0,
    val topColor: Int = 0,
    val bottom: Int = 0,
    val bottomColor: Int = 11,
    val shoes: Int = 9,
    val extra: Int = 0,
    val eyeColor: Int = 0,
    val hairSize: Float = 1f,
    val hairLength: Float = 1f,
    val eyeSize: Float = 1f,
    val eyeSpacing: Float = 1f,
    val face: Int = 0,
    val nose: Int = 0,
    val mouth: Int = 0,
    val pattern: Int = 0,
    val accent: Int = 9,
) {
    fun safe(): Look = copy(
        skin = skin.mod(Palette.skins.size),
        height = height.finite(1.03f, 0.7f, 1.2f),
        hair = hair.mod(Styles.HAIRS),
        hairColor = hairColor.mod(Palette.hairs.size),
        eyes = eyes.mod(Styles.EYES),
        ears = ears.mod(Styles.EARS),
        top = top.mod(Styles.TOPS),
        topColor = topColor.mod(Palette.cloth.size),
        bottom = bottom.mod(Styles.BOTTOMS),
        bottomColor = bottomColor.mod(Palette.cloth.size),
        shoes = shoes.mod(Palette.cloth.size),
        extra = extra.mod(Styles.EXTRAS),
        eyeColor = eyeColor.mod(Palette.eyes.size),
        hairSize = hairSize.finite(1f, 0.8f, 1.5f),
        hairLength = hairLength.finite(1f, 0.65f, 1.6f),
        eyeSize = eyeSize.finite(1f, 0.75f, 1.25f),
        eyeSpacing = eyeSpacing.finite(1f, 0.8f, 1.2f),
        face = face.mod(Styles.FACES),
        nose = nose.mod(Styles.NOSES),
        mouth = mouth.mod(Styles.MOUTHS),
        pattern = pattern.mod(Styles.PATTERNS),
        accent = accent.mod(Palette.cloth.size),
    )

    companion object {
        /** A believable random figure: mostly human skin, now and then a creature colour. */
        fun random(random: Random = Random.Default): Look {
            val creature = random.nextFloat() < 0.18f
            val grown = random.nextFloat() < 0.5f
            val height = if (grown) Styles.HEIGHTS[2 + random.nextInt(2)] else Styles.HEIGHTS[random.nextInt(2)]
            return Look(
                skin = if (creature) Palette.HUMAN_SKINS + random.nextInt(Palette.skins.size - Palette.HUMAN_SKINS) else random.nextInt(Palette.HUMAN_SKINS),
                height = height,
                hair = random.nextInt(Styles.HAIRS),
                hairColor = random.nextInt(Palette.hairs.size),
                eyes = random.nextInt(Styles.EYES),
                ears = if (creature) 1 + random.nextInt(Styles.EARS - 1) else 0,
                top = random.nextInt(Styles.TOPS),
                topColor = random.nextInt(Palette.cloth.size),
                bottom = random.nextInt(Styles.BOTTOMS),
                bottomColor = random.nextInt(Palette.cloth.size),
                shoes = random.nextInt(Palette.cloth.size),
                extra = if (grown && random.nextFloat() < 0.35f) 1 + random.nextInt(Styles.EXTRAS - 1) else if (random.nextFloat() < 0.2f) 1 else 0,
                eyeColor = random.nextInt(Palette.eyes.size),
                hairSize = 0.9f + random.nextFloat() * 0.45f,
                hairLength = 0.8f + random.nextFloat() * 0.6f,
                eyeSize = 0.85f + random.nextFloat() * 0.3f,
                eyeSpacing = 0.9f + random.nextFloat() * 0.2f,
                face = random.nextInt(Styles.FACES),
                nose = random.nextInt(Styles.NOSES),
                mouth = random.nextInt(Styles.MOUTHS),
                pattern = random.nextInt(Styles.PATTERNS),
                accent = random.nextInt(Palette.cloth.size),
            )
        }

        /** Children speak higher; each figure keeps its own pitch. */
        fun voiceFor(look: Look, random: Random = Random.Default): Float =
            (1.55f - look.height * 0.6f + (random.nextFloat() - 0.5f) * 0.25f).coerceIn(0.75f, 1.6f)
    }
}

private fun Float.finite(fallback: Float, min: Float, max: Float): Float =
    if (isFinite()) coerceIn(min, max) else fallback

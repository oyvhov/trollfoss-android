package app.trollvik.domain

import kotlin.random.Random

/**
 * Who a figure is. Folk are the people of Trollvik; the others are animals and creatures. [height] is
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
    ;

    val pet: Boolean get() = this != FOLK
}

/** How many choices each part of a figure has. The art draws every index below these counts. */
object Styles {
    /** 0 bald, 1 short, 2 fringe, 3 curls, 4 long, 5 braids, 6 bun, 7 spiky, 8 bob. */
    const val HAIRS = 9
    /** 0 round, 1 happy, 2 sleepy, 3 lashes, 4 dots. */
    const val EYES = 5
    /** 0 plain, 1 cat, 2 bunny, 3 bear, 4 troll. */
    const val EARS = 5
    /** 0 tee, 1 hoodie, 2 dress, 3 stripes, 4 overalls, 5 lusekofte, 6 bunad. */
    const val TOPS = 7
    /** 0 trousers, 1 shorts, 2 skirt. */
    const val BOTTOMS = 3
    /** 0 none, 1 freckles, 2 beard, 3 moustache. */
    const val EXTRAS = 4

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
    )

    val cloth = intArrayOf(
        0xFFFF5A4E.toInt(), 0xFFFF9F43.toInt(), 0xFFFFD23F.toInt(), 0xFF3BC46B.toInt(),
        0xFF1FB5A8.toInt(), 0xFF4AB3FF.toInt(), 0xFF3D6BFF.toInt(), 0xFF8B5CF6.toInt(),
        0xFFFF6FA8.toInt(), 0xFFF7F4EE.toInt(), 0xFF8E93A6.toInt(), 0xFF2F3552.toInt(),
        0xFFA0663B.toInt(), 0xFFF3DDB5.toInt(),
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

    /** Smoothie, slime and gem colours; the first six are the fruit colours in [ThingType.FRUITS] order. */
    val juice = intArrayOf(
        0xFF8BD450.toInt(), 0xFFFFE066.toInt(), 0xFFFF5C8A.toInt(), 0xFFFF9A3D.toInt(),
        0xFFFF6B6B.toInt(), 0xFFFFB347.toInt(), 0xFF9B6BFF.toInt(), 0xFF4FC3FF.toInt(),
    )

    fun furFor(species: Species, index: Int): Int = when (species) {
        Species.DRAGON -> scales[index.mod(scales.size)]
        Species.ELK -> elk[index.mod(elk.size)]
        Species.PUFFIN -> 0xFF2B2140.toInt()
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
) {
    fun safe(): Look = copy(
        skin = skin.mod(Palette.skins.size),
        height = height.coerceIn(0.7f, 1.2f),
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
            )
        }

        /** Children speak higher; each figure keeps its own pitch. */
        fun voiceFor(look: Look, random: Random = Random.Default): Float =
            (1.55f - look.height * 0.6f + (random.nextFloat() - 0.5f) * 0.25f).coerceIn(0.75f, 1.6f)
    }
}

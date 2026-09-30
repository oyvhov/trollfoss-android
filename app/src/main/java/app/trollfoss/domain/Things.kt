package app.trollfoss.domain

/** What kind of thing something is. Decides where it goes when it is dropped on a figure. */
enum class Cat { FOOD, DRINK, POTION, HAT, GLASSES, GARMENT, TOY, TOOL, NATURE, HOME, MAGIC }

/** Where a figure carries a thing. */
enum class Slot { HAND, HEAD, FACE }

/**
 * Every loose thing in Trollfoss. Sizes are in scene units: the scene is one unit tall, and a grown-up
 * figure is about 0.30. [bites] is how many bites or sips an edible thing lasts. [lift] multiplies
 * gravity: below zero the thing rises, like a balloon. [variants] is how many colours or flavours the
 * art knows; spawners pick one of them.
 */
enum class ThingType(
    val w: Float,
    val h: Float,
    val cat: Cat,
    val bites: Int = 0,
    val bounce: Float = 0.18f,
    val lift: Float = 1f,
    val buoyant: Boolean = false,
    val rolls: Boolean = false,
    val glows: Boolean = false,
    val variants: Int = 1,
) {
    // Food
    APPLE(0.055f, 0.06f, Cat.FOOD, bites = 3, bounce = 0.32f, buoyant = true, rolls = true),
    BANANA(0.085f, 0.045f, Cat.FOOD, bites = 3, buoyant = true),
    STRAWBERRY(0.038f, 0.045f, Cat.FOOD, bites = 2, buoyant = true),
    CARROT(0.03f, 0.085f, Cat.FOOD, bites = 3),
    WATERMELON(0.085f, 0.05f, Cat.FOOD, bites = 3, buoyant = true),
    CLOUDBERRY(0.035f, 0.035f, Cat.FOOD, bites = 1, buoyant = true, rolls = true),
    BREAD(0.11f, 0.06f, Cat.FOOD, bites = 3, buoyant = true),
    BUN(0.06f, 0.04f, Cat.FOOD, bites = 3, buoyant = true),
    WAFFLE(0.08f, 0.07f, Cat.FOOD, bites = 3, buoyant = true),
    CAKE(0.12f, 0.09f, Cat.FOOD, bites = 4),
    CUPCAKE(0.05f, 0.06f, Cat.FOOD, bites = 2, variants = 4),
    PIZZA(0.08f, 0.05f, Cat.FOOD, bites = 3),
    PANCAKE(0.08f, 0.025f, Cat.FOOD, bites = 3),
    COOKIE(0.045f, 0.045f, Cat.FOOD, bites = 2, rolls = true),
    ICE_CREAM(0.04f, 0.09f, Cat.FOOD, bites = 3, variants = 5),
    LOLLIPOP(0.035f, 0.09f, Cat.FOOD, bites = 3, variants = 4),
    BROWN_CHEESE(0.07f, 0.05f, Cat.FOOD, bites = 3),
    EGG(0.035f, 0.045f, Cat.FOOD, rolls = true, variants = 2),
    FRIED_EGG(0.07f, 0.02f, Cat.FOOD, bites = 2),
    FISH(0.1f, 0.045f, Cat.FOOD, buoyant = true),
    GRILLED_FISH(0.1f, 0.045f, Cat.FOOD, bites = 3),
    SAUSAGE(0.08f, 0.025f, Cat.FOOD),
    GRILLED_SAUSAGE(0.08f, 0.025f, Cat.FOOD, bites = 2),
    MARSHMALLOW(0.03f, 0.1f, Cat.FOOD),
    TOASTED_MARSHMALLOW(0.03f, 0.1f, Cat.FOOD, bites = 2),
    DOUGH(0.06f, 0.04f, Cat.FOOD),
    POTATO(0.045f, 0.035f, Cat.FOOD, bites = 2, rolls = true),
    SPACE_FOOD(0.035f, 0.07f, Cat.FOOD, bites = 3, variants = 3),

    // Drinks and potions
    MILK(0.045f, 0.08f, Cat.DRINK, bites = 3, buoyant = true),
    JUICE(0.04f, 0.065f, Cat.DRINK, bites = 3),
    SMOOTHIE(0.045f, 0.085f, Cat.DRINK, bites = 3, variants = 8),
    COCOA(0.05f, 0.05f, Cat.DRINK, bites = 3),
    POTION_GROW(0.04f, 0.07f, Cat.POTION, bites = 1, glows = true),
    POTION_SHRINK(0.04f, 0.07f, Cat.POTION, bites = 1, glows = true),
    POTION_RAINBOW(0.04f, 0.07f, Cat.POTION, bites = 1, glows = true),
    POTION_FLOAT(0.04f, 0.07f, Cat.POTION, bites = 1, glows = true),
    POTION_NORMAL(0.04f, 0.07f, Cat.POTION, bites = 1, glows = true),

    // Hats and glasses
    CAP(0.13f, 0.07f, Cat.HAT, variants = 4),
    BEANIE(0.13f, 0.09f, Cat.HAT, variants = 4),
    CROWN(0.11f, 0.08f, Cat.HAT, glows = true, variants = 2),
    PARTY_HAT(0.08f, 0.12f, Cat.HAT, variants = 4),
    VIKING_HELMET(0.16f, 0.1f, Cat.HAT),
    FLOWER_CROWN(0.14f, 0.05f, Cat.HAT),
    CHEF_HAT(0.12f, 0.14f, Cat.HAT),
    SUN_HAT(0.19f, 0.07f, Cat.HAT),
    WIZARD_HAT(0.12f, 0.17f, Cat.HAT),
    BOW(0.08f, 0.05f, Cat.HAT, variants = 4),
    NISSE_HAT(0.12f, 0.13f, Cat.HAT),
    SUNGLASSES(0.13f, 0.05f, Cat.GLASSES),
    ROUND_GLASSES(0.13f, 0.05f, Cat.GLASSES),
    SPACE_HELMET(0.17f, 0.15f, Cat.HAT),
    STAR_GLASSES(0.14f, 0.065f, Cat.GLASSES),

    /** A top on a hanger. The variant is `style * 16 + colour` (see [Garment]). */
    GARMENT(0.085f, 0.075f, Cat.GARMENT),

    // Toys
    BALL(0.05f, 0.05f, Cat.TOY, bounce = 0.74f, buoyant = true, rolls = true),
    BEACH_BALL(0.085f, 0.085f, Cat.TOY, bounce = 0.8f, lift = 0.7f, buoyant = true, rolls = true),
    TEDDY(0.07f, 0.085f, Cat.TOY, buoyant = true),
    BALLOON(0.06f, 0.17f, Cat.TOY, bounce = 0.4f, lift = -0.35f, variants = 6),
    DUCK(0.05f, 0.045f, Cat.TOY, buoyant = true),
    GUITAR(0.06f, 0.15f, Cat.TOY),
    DRUM(0.065f, 0.05f, Cat.TOY),
    BOOK(0.05f, 0.06f, Cat.TOY, variants = 4),
    PHONE(0.03f, 0.05f, Cat.TOY),
    TOY_CAR(0.075f, 0.04f, Cat.TOY, rolls = true, variants = 3),
    SWIM_RING(0.11f, 0.06f, Cat.TOY, buoyant = true, bounce = 0.5f),
    ROCKET(0.03f, 0.09f, Cat.TOY),
    SNOWBALL(0.04f, 0.04f, Cat.TOY, bounce = 0.3f, rolls = true),
    SLED(0.13f, 0.05f, Cat.TOY, rolls = true),

    // Tools
    SCISSORS(0.06f, 0.04f, Cat.TOOL),
    HAIR_DRYER(0.07f, 0.06f, Cat.TOOL),
    COMB(0.065f, 0.02f, Cat.TOOL),
    /** Hair colour spray; the variant is the hair colour it gives. */
    SPRAY(0.03f, 0.07f, Cat.TOOL, variants = 10),
    WAND(0.02f, 0.1f, Cat.TOOL, glows = true),
    BUCKET(0.06f, 0.06f, Cat.TOOL, variants = 3),
    SPADE(0.03f, 0.09f, Cat.TOOL),
    TOOTHBRUSH(0.015f, 0.07f, Cat.TOOL),
    HAMMER(0.05f, 0.09f, Cat.TOOL),
    SAW(0.11f, 0.05f, Cat.TOOL),
    WRENCH(0.03f, 0.09f, Cat.TOOL),
    SCREWDRIVER(0.02f, 0.08f, Cat.TOOL),
    WATERING_CAN(0.09f, 0.07f, Cat.TOOL),
    SEEDS(0.05f, 0.06f, Cat.NATURE),

    // Nature
    SHELL(0.04f, 0.03f, Cat.NATURE),
    STARFISH(0.05f, 0.05f, Cat.NATURE),
    FLOWER(0.03f, 0.08f, Cat.NATURE, variants = 5),
    MUSHROOM(0.04f, 0.04f, Cat.NATURE),
    PINECONE(0.03f, 0.04f, Cat.NATURE, rolls = true),
    STICK(0.1f, 0.015f, Cat.NATURE, buoyant = true),
    ROCK(0.05f, 0.035f, Cat.NATURE),
    LEAF(0.04f, 0.03f, Cat.NATURE, lift = 0.15f, buoyant = true),
    FEATHER(0.02f, 0.06f, Cat.NATURE, lift = 0.12f, buoyant = true),

    // Home
    CUP(0.04f, 0.04f, Cat.HOME, variants = 4),
    PILLOW(0.1f, 0.05f, Cat.HOME, variants = 4),
    PLANT_POT(0.06f, 0.1f, Cat.HOME),
    CANDLE(0.025f, 0.06f, Cat.HOME, glows = true),
    PLANK(0.14f, 0.025f, Cat.HOME, buoyant = true),
    TIRE(0.07f, 0.07f, Cat.TOY, bounce = 0.55f, rolls = true),
    BIRDHOUSE(0.07f, 0.09f, Cat.HOME),

    // Magic and treasure
    GEM(0.04f, 0.04f, Cat.MAGIC, glows = true, variants = 5),
    GIFT(0.07f, 0.07f, Cat.MAGIC, variants = 5),
    COIN(0.03f, 0.03f, Cat.MAGIC, rolls = true),
    BOOT(0.06f, 0.06f, Cat.NATURE),
    SLIME(0.06f, 0.035f, Cat.MAGIC, bounce = 0.55f, variants = 5),
    STAR_JAR(0.04f, 0.06f, Cat.MAGIC, glows = true),
    DRAGON_EGG(0.05f, 0.065f, Cat.MAGIC, glows = true),
    /** A real planet as a toy. Variant: 0 Mercury, 1 Venus, 2 Earth, 3 Moon, 4 Mars, 5 Jupiter, 6 Saturn, 7 Uranus, 8 Neptune, 9 Pluto. */
    PLANET(0.07f, 0.07f, Cat.MAGIC, bounce = 0.6f, rolls = true, variants = 10),

    // Jokes
    /** Sit on it and it says «prrrt». */
    WHOOPEE(0.085f, 0.03f, Cat.TOY, bounce = 0.4f, variants = 2),
    /** Left over after a banana. Whoever lands on it slips. */
    BANANA_PEEL(0.075f, 0.022f, Cat.NATURE),
    /** A pinch at the nose and … atsjo! */
    PEPPER(0.028f, 0.06f, Cat.HOME),

    // Tivoli, shop, doctor, stage, sea
    CANDY_FLOSS(0.06f, 0.11f, Cat.FOOD, bites = 3, variants = 2),
    POPCORN(0.05f, 0.07f, Cat.FOOD, bites = 3),
    /** Fizzy: three quick sips and the hiccups come. Variant: 0 cola, 1 orange, 2 lemon. */
    SODA(0.035f, 0.065f, Cat.DRINK, bites = 3, variants = 3),
    /** Medicine: a sour face, then a happy one. */
    SYRUP(0.035f, 0.075f, Cat.DRINK, bites = 1),
    /** A plaster, worn on the cheek. */
    BANDAGE(0.05f, 0.025f, Cat.GLASSES),
    THERMOMETER(0.018f, 0.075f, Cat.TOOL),
    /** Held close to a figure, you hear its heart. */
    STETHOSCOPE(0.07f, 0.07f, Cat.TOOL),
    MICROPHONE(0.025f, 0.08f, Cat.TOY),
    PEARL(0.025f, 0.025f, Cat.MAGIC, glows = true, rolls = true),

    // Heileberget
    /** Worn at the eyes, like glasses: everything is far away. */
    BINOCULARS(0.11f, 0.06f, Cat.GLASSES),
    /** Hot cocoa in a steel flask; three sips. */
    THERMOS(0.035f, 0.085f, Cat.DRINK, bites = 3),
    DIVING_MASK(0.14f, 0.07f, Cat.GLASSES),
    ;

    /** Head width of a grown-up figure; hats and glasses are drawn at this size and scaled to fit a head. */
    val fitsHead: Float get() = 0.155f

    val slot: Slot get() = when (cat) {
        Cat.HAT -> Slot.HEAD
        Cat.GLASSES -> Slot.FACE
        else -> Slot.HAND
    }

    val edible: Boolean get() = bites > 0

    val drink: Boolean get() = cat == Cat.DRINK || cat == Cat.POTION

    val potion: Boolean get() = cat == Cat.POTION

    /** Tools that change hair when dropped on a figure's head. */
    val hairTool: Boolean get() = this == SCISSORS || this == HAIR_DRYER || this == COMB || this == SPRAY

    /** Workshop tools: dropped on the workbench, they build what lies on it. */
    val buildTool: Boolean get() = this == HAMMER || this == SAW || this == WRENCH || this == SCREWDRIVER

    val fruit: Boolean get() = this in FRUITS

    companion object {
        val FRUITS = listOf(APPLE, BANANA, STRAWBERRY, CARROT, WATERMELON, CLOUDBERRY)
    }
}

/** Garments: the tops a figure can wear. The variant of a [ThingType.GARMENT] packs style and colour. */
object Garment {
    fun pack(style: Int, color: Int): Int = style.coerceIn(0, Styles.TOPS - 1) * 16 + color.coerceIn(0, 15)
    fun style(variant: Int): Int = (variant / 16).coerceIn(0, Styles.TOPS - 1)
    fun color(variant: Int): Int = (variant % 16).coerceIn(0, Palette.cloth.size - 1)
}

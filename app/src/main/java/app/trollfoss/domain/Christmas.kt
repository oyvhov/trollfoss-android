package app.trollfoss.domain

data class ChristmasOrigin(val place: PlaceId, val x: Float)

/** Permanent play belongs to the child, independently of the calendar decoration. */
class ChristmasState {
    val opened = linkedSetOf<Int>()
    val parcels = linkedMapOf<Int, Int>()
    val deeds = linkedSetOf<String>()
    val stocking = linkedMapOf<Int, Int>()
    var sleighStop = 0
    var rampStage = 0
    var origin: ChristmasOrigin? = null
        private set

    fun rememberOrigin(place: PlaceId, x: Float) {
        if (!place.christmas && x.isFinite()) {
            origin = ChristmasOrigin(place, x.coerceIn(0.2f, place.width - 0.2f))
        }
    }
}

object Christmas {
    val types = FixtureType.entries.filter { it.name.startsWith("XM_") }.toSet()
    val things = ThingType.entries.filter { it.name.startsWith("XM_") && it !in setOf(ThingType.XM_HAT, ThingType.XM_FACE, ThingType.XM_PARCEL, ThingType.XM_GINGER_HOUSE, ThingType.XM_STOCKING) }
    val catalogue by lazy { types.flatMap { type -> (0..2).map { CatalogueItem(type, it) } } }
    val ornaments = setOf(ThingType.XM_BAUBLE, ThingType.XM_HEART, ThingType.XM_BELL, ThingType.XM_MUSHROOM, ThingType.XM_CANDYCANE, ThingType.XM_STAR, ThingType.XM_PAPER_HEART, ThingType.XM_CHAIN, ThingType.XM_SNOWFLAKE, ThingType.XM_CONE, ThingType.XM_BOW, ThingType.XM_RIBBON)
    val trees = setOf(FixtureType.XM_TREE, FixtureType.XM_SMALL_TREE)
    val benches = setOf(FixtureType.XM_BAKE, FixtureType.XM_HOUSE_BENCH, FixtureType.XM_CRAFT, FixtureType.XM_WRAP, FixtureType.XM_TOY_BENCH)
    val stops = listOf(PlaceId.CHRISTMAS_SQUARE, PlaceId.MANOR_GROUND, PlaceId.FARM, PlaceId.HOME)
    const val FIRST_TOP = 19
    val clothes: List<Pair<ThingType, Int>> get() = (0..3).map { ThingType.XM_HAT to it } +
        (0..3).map { ThingType.XM_FACE to it } + (0..7).map { ThingType.GARMENT to Garment.pack(FIRST_TOP + it, if(it < 2) 0 else if(it < 4) 3 else if(it == 7) 5 else 9) }
    fun ornamentPoint(slot: Int): Pair<Float, Float> {
        if(slot==11) return 0f to -0.71f
        val row = slot / 4
        return ((slot % 4 - 1.5f) * (0.14f - row * 0.025f)) to (-0.2f - row * 0.2f)
    }
    fun specOf(type: FixtureType): FixtureSpec? {
        if (!type.name.startsWith("XM_")) return null
        fun surface(w: Float, h: Float) = FixtureSpec(w, h, surfaces=listOf(SurfaceSpec(-w/2+0.02f,w/2-0.02f,-h)))
        fun seat(w:Float, h:Float, count:Int=1) = FixtureSpec(w,h,spots=List(count) { SpotSpec((it-(count-1)/2f)*0.2f,-h*0.5f,Pose.SIT) })
        fun box(w:Float,h:Float,glass:Boolean=false) = FixtureSpec(w,h,glass=glass,container=RRect(-w/2,-h,w/2,0f),surfaces=listOf(SurfaceSpec(-w/2+0.025f,w/2-0.025f,-h*0.5f,interior=true)))
        return when(type) {
            FixtureType.XM_SOFA -> seat(0.58f,0.27f,2)
            FixtureType.XM_ARMCHAIR -> seat(0.32f,0.29f)
            FixtureType.XM_CHAIR -> seat(0.22f,0.26f)
            FixtureType.XM_BED -> FixtureSpec(0.52f,0.22f,spots=listOf(SpotSpec(0f,-0.13f,Pose.LIE)),surfaces=listOf(SurfaceSpec(-0.25f,0.25f,-0.13f)))
            FixtureType.XM_TABLE -> surface(0.65f,0.24f)
            FixtureType.XM_RUG -> FixtureSpec(0.65f,0.035f)
            FixtureType.XM_CUPBOARD -> box(0.36f,0.53f)
            FixtureType.XM_SHELF -> box(0.42f,0.35f,true)
            FixtureType.XM_TOYBOX -> box(0.38f,0.24f)
            FixtureType.XM_CHEST -> FixtureSpec(0.38f,0.24f,container=RRect(-0.19f,-0.24f,0.19f,0f),spots=listOf(SpotSpec(0f,-0.035f,Pose.SIT,hidden=true)),surfaces=listOf(SurfaceSpec(-0.16f,0.16f,-0.12f,interior=true)))
            FixtureType.XM_SACK -> box(0.26f,0.32f)
            FixtureType.XM_DISPLAY -> box(0.46f,0.32f,true)
            FixtureType.XM_TREE, FixtureType.XM_SMALL_TREE -> {
                val w = if(type == FixtureType.XM_TREE) 0.58f else 0.34f
                val h = if(type == FixtureType.XM_TREE) 0.74f else 0.43f
                FixtureSpec(w,h,dropZone=RRect(-w/2,-h,w/2,0f),light=RRect(-w,-h,w,0.1f))
            }
            FixtureType.XM_WREATH, FixtureType.XM_STAR, FixtureType.XM_PICTURE -> FixtureSpec(0.25f,0.25f,wall=true,light=if(type==FixtureType.XM_STAR) RRect(-0.3f,-0.4f,0.3f,0.2f) else null)
            FixtureType.XM_GARLAND, FixtureType.XM_LIGHTS -> FixtureSpec(0.65f,0.15f,wall=true,light=RRect(-0.4f,-0.3f,0.4f,0.15f))
            FixtureType.XM_CANDLES, FixtureType.XM_LANTERN -> FixtureSpec(0.2f,0.22f,light=RRect(-0.3f,-0.4f,0.3f,0.1f))
            FixtureType.XM_FIREPLACE -> FixtureSpec(0.48f,0.44f,light=RRect(-0.35f,-0.5f,0.35f,0.1f),surfaces=listOf(SurfaceSpec(-0.24f,0.24f,-0.44f)))
            FixtureType.XM_SNOWGLOBE -> FixtureSpec(0.22f,0.28f)
            FixtureType.XM_MUSICBOX -> FixtureSpec(0.22f,0.18f)
            in benches -> FixtureSpec(0.6f,0.3f,surfaces=listOf(SurfaceSpec(-0.28f,0.28f,-0.3f)),dropZone=RRect(-0.32f,-0.48f,0.32f,0f))
            FixtureType.XM_BELT -> FixtureSpec(0.7f,0.18f,dropZone=RRect(-0.36f,-0.35f,0.36f,0f))
            FixtureType.XM_MARKET, FixtureType.XM_COCOA -> FixtureSpec(0.66f,0.62f,surfaces=listOf(SurfaceSpec(-0.3f,0.3f,-0.24f)))
            FixtureType.XM_TROUGH -> FixtureSpec(0.42f,0.16f,dropZone=RRect(-0.23f,-0.3f,0.23f,0f))
            FixtureType.XM_SLEIGH -> FixtureSpec(0.68f,0.28f,spots=listOf(SpotSpec(-0.17f,-0.16f,Pose.SIT),SpotSpec(0.05f,-0.16f,Pose.SIT)),dropZone=RRect(0.13f,-0.35f,0.34f,0f))
            FixtureType.XM_CALENDAR -> FixtureSpec(0.58f,0.62f)
            FixtureType.XM_POST -> FixtureSpec(0.24f,0.36f,dropZone=RRect(-0.15f,-0.4f,0.15f,0f))
            else -> error("Missing Christmas fixture $type")
        }
    }
}

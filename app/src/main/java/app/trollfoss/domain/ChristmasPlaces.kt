package app.trollfoss.domain

object ChristmasPlaces {
    private val neighbourhood = listOf(PlaceId.CHRISTMAS_HOME, PlaceId.CHRISTMAS_WORKSHOP, PlaceId.CHRISTMAS_STABLE)
    private fun doorX(index: Int) = 0.35f + index * 0.5f

    fun visitTarget(world: World, place: PlaceId): VisitTarget? {
        val resident = when (place) {
            PlaceId.CHRISTMAS_HOME -> world.people().firstOrNull { it.place == place && it.name == "Julenissen" }
                ?: Players.activeTeam(world).firstOrNull { it.name == "Julenissen" }
            PlaceId.CHRISTMAS_STABLE -> world.people().firstOrNull { it.place == place && it.species == Species.REINDEER }
            else -> null
        }
        if (resident != null) {
            val x = if (resident.place == place) resident.x else 1.7f // Santa has travelled home with the team.
            return VisitTarget(x, VisitTarget.halfWidthOf(resident), resident)
        }
        val station = when (place) {
            PlaceId.CHRISTMAS_HOME -> FixtureType.XM_FIREPLACE
            PlaceId.CHRISTMAS_WORKSHOP -> FixtureType.XM_TOY_BENCH
            PlaceId.CHRISTMAS_STABLE -> FixtureType.XM_TROUGH
            else -> return null
        }
        return world.fixturesIn(place).firstOrNull { it.type == station }?.let { VisitTarget(it.x, it.spec.w / 2f) }
    }

    val floors: List<Floor> = listOf(PlaceId.CHRISTMAS_SQUARE,PlaceId.CHRISTMAS_HOME,PlaceId.CHRISTMAS_WORKSHOP,PlaceId.CHRISTMAS_STABLE).map { id -> object: Floor {
        override val place = id
        override fun blueprint() = ChristmasPlaces.blueprint(id)
        override val rooms = (0 until (id.width/2).toInt()).map { it*2f..(it+1)*2f }
        override val arrivals = listOf(Arrival("entry", 0.8f)) + if (id == PlaceId.CHRISTMAS_SQUARE)
            neighbourhood.mapIndexed { i, room -> Arrival(room.name, doorX(i)) } else emptyList()
        override val passages: List<Passage> = links(id).mapIndexed { index, to ->
            Passage("christmas-${id.name}-${to.name}", id, index, PassageKind.DOOR, to,
                if (to == PlaceId.CHRISTMAS_SQUARE) id.name else "entry")
        }
    } }
    private fun links(id:PlaceId): List<PlaceId> = if(id==PlaceId.CHRISTMAS_SQUARE) neighbourhood else listOf(PlaceId.CHRISTMAS_SQUARE)
    fun blueprint(id:PlaceId):PlaceSpec {
        val fs=mutableListOf<FixtureDef>()
        links(id).forEachIndexed { i,_ -> fs+=FixtureDef(FixtureType.DOOR,doorX(i),id.floor) }
        fun f(t:FixtureType,x:Float,y:Float=id.floor,v:Int=0) { fs+=FixtureDef(t,x,y,v) }
        when(id) {
            PlaceId.CHRISTMAS_SQUARE -> {
                f(FixtureType.XM_TREE,2.15f); f(FixtureType.XM_MARKET,3.3f); f(FixtureType.XM_COCOA,4.4f)
                f(FixtureType.XM_CALENDAR,5.5f);f(FixtureType.XM_POST,6.25f);f(FixtureType.XM_CHEST,6.9f);f(FixtureType.SNOWMAN,7.5f)
            }
            PlaceId.CHRISTMAS_HOME -> {
                f(FixtureType.XM_SOFA,1.5f);f(FixtureType.XM_FIREPLACE,2.25f);f(FixtureType.XM_TREE,3.1f)
                f(FixtureType.XM_TABLE,4.25f);f(FixtureType.XM_CHAIR,3.8f);f(FixtureType.XM_CHAIR,4.8f)
                f(FixtureType.XM_BAKE,5.65f);f(FixtureType.XM_CUPBOARD,6.5f);f(FixtureType.XM_BED,7.6f);f(FixtureType.XM_CHEST,8.5f);f(FixtureType.XM_MUSICBOX,9.2f)
                f(FixtureType.XM_STAR,1.5f,0.3f);f(FixtureType.XM_WREATH,4.3f,0.38f)
            }
            PlaceId.CHRISTMAS_WORKSHOP -> {
                f(FixtureType.XM_TOY_BENCH,1.5f);f(FixtureType.XM_CRAFT,2.7f);f(FixtureType.XM_WRAP,3.9f)
                f(FixtureType.XM_BELT,5.0f);f(FixtureType.XM_SACK,5.8f);f(FixtureType.XM_HOUSE_BENCH,6.65f)
                f(FixtureType.XM_SHELF,7.6f);f(FixtureType.XM_CHEST,8.5f);f(FixtureType.XM_LIGHTS,3.0f,0.3f)
            }
            PlaceId.CHRISTMAS_STABLE -> {
                f(FixtureType.XM_TROUGH,1.6f);f(FixtureType.WATER_TROUGH,2.5f);f(FixtureType.XM_SLEIGH,3.8f)
                f(FixtureType.XM_LANTERN,4.7f);f(FixtureType.SLED_HILL,5.65f);f(FixtureType.SNOWMAN,6.6f);f(FixtureType.XM_SMALL_TREE,7.3f)
            }
            else -> Unit
        }
        val things=when(id) {
            PlaceId.CHRISTMAS_WORKSHOP -> listOf(ThingDef(ThingType.XM_PAPER,2.6f,0.4f),ThingDef(ThingType.PLANK,1.5f,0.4f),ThingDef(ThingType.XM_GINGER_HEART,6.65f,0.4f))
            PlaceId.CHRISTMAS_STABLE -> listOf(ThingDef(ThingType.CARROT,1.6f,0.4f),ThingDef(ThingType.XM_BRUSH,2.0f,0.4f))
            else -> listOf(ThingDef(ThingType.XM_BAUBLE,2.05f,0.3f),ThingDef(ThingType.XM_STAR,2.25f,0.3f),ThingDef(ThingType.XM_PORRIDGE,4.1f,0.3f))
        }
        val people=when(id) {
            PlaceId.CHRISTMAS_HOME -> listOf(PersonDef(Species.FOLK,Look().copy(top=19,topColor=0,hairColor=11,extra=10),1.7f,name="Julenissen",hat=ThingType.XM_HAT))
            PlaceId.CHRISTMAS_WORKSHOP -> listOf(PersonDef(Species.FOLK,Look().copy(top=21,topColor=3,height=0.78f),2f,name="Tindra",hat=ThingType.XM_HAT,hatVariant=1),PersonDef(Species.FOLK,Look().copy(top=22,topColor=3,height=0.9f,skin=4),4f,name="Lumi",hat=ThingType.XM_HAT,hatVariant=1))
            PlaceId.CHRISTMAS_STABLE -> listOf(PersonDef(Species.REINDEER,Look(),1.8f,name="Stjerne"),PersonDef(Species.REINDEER,Look(),2.8f,name="Bjølle"))
            PlaceId.CHRISTMAS_SQUARE -> listOf(PersonDef(Species.FOLK,Look().copy(top=21,topColor=3,height=0.78f,skin=8),6.3f,name="Rampenissen",hat=ThingType.XM_HAT,hatVariant=1))
            else -> emptyList()
        }
        return PlaceSpec(id,listOf(Ground(0f,id.width,id.floor)),null,fs,things,people)
    }
}

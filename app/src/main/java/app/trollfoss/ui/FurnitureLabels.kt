package app.trollfoss.ui

import app.trollfoss.domain.*

/** Names for the pictures in the furniture catalogue and saved storage. */
object FurnitureLabels {
    private val names=mapOf(
        FixtureType.BED to Txt("Seng","Seng"),
        FixtureType.SOFA to Txt("Sofa","Sofa"),
        FixtureType.CHAIR to Txt("Stol","Stol"),
        FixtureType.STOOL to Txt("Krakk","Krakk"),
        FixtureType.TABLE to Txt("Bord","Bord"),
        FixtureType.ROUND_TABLE to Txt("Rundt bord","Rundt bord"),
        FixtureType.SHELF to Txt("Hylle","Hylle"),
        FixtureType.BOOKCASE to Txt("Bokhylle","Bokhylle"),
        FixtureType.WARDROBE to Txt("Klesskap","Klesskap"),
        FixtureType.CHEST to Txt("Kiste","Kiste"),
        FixtureType.MIRROR to Txt("Spegel","Speil"),
        FixtureType.LAMP to Txt("Lampe","Lampe"),
        FixtureType.TV to Txt("Fjernsyn","Fjernsyn"),
        FixtureType.RADIO to Txt("Radio","Radio"),
        FixtureType.PIANO to Txt("Piano","Piano"),
        FixtureType.CLOCK to Txt("Klokke","Klokke"),
        FixtureType.PLANT_BIG to Txt("Stor plante","Stor plante"),
        FixtureType.RUG to Txt("Teppe","Teppe"),
        FixtureType.PICTURE to Txt("Bilete","Bilde"),
        FixtureType.FLOWER_POT to Txt("Blomepotte","Blomsterpotte"),
        FixtureType.ARMCHAIR to Txt("Lenestol","Lenestol"),
        FixtureType.BEANBAG to Txt("Sekkestol","Sekkestol"),
        FixtureType.DESK to Txt("Skrivebord","Skrivebord"),
        FixtureType.BUNK_BED to Txt("Køyeseng","Køyeseng"),
        FixtureType.TOY_BOX to Txt("Leikekasse","Lekekasse"),
        FixtureType.TREASURE_BOX to Txt("Skattekiste","Skattekiste"),
        FixtureType.TRASH_BIN to Txt("Bossbøtte","Søppelbøtte"),
        FixtureType.AQUARIUM to Txt("Akvarium","Akvarium"),
        FixtureType.ROBOT_VACUUM to Txt("Robotstøvsugar","Robotstøvsuger"),
        FixtureType.DISCO_BALL to Txt("Diskokule","Diskokule"),
        FixtureType.TRAMPOLINE to Txt("Trampoline","Trampoline"),
        FixtureType.XYLOPHONE to Txt("Xylofon","Xylofon"),
        FixtureType.TRACTOR to Txt("Traktor","Traktor"),
        FixtureType.BUMPER_CAR to Txt("Radiobil","Radiobil"),
        FixtureType.BOAT to Txt("Båt","Båt"),
        FixtureType.BENCH to Txt("Benk","Benk"),
        FixtureType.LAMP_POST to Txt("Lyktestolpe","Lyktestolpe"),
        FixtureType.UMBRELLA to Txt("Parasoll","Parasoll"),
        FixtureType.LOUNGER to Txt("Solstol","Solstol"),
        FixtureType.STUMP to Txt("Stubbe","Stubbe"),
        FixtureType.LOG to Txt("Trestokk","Trestokk"),
        FixtureType.HAY_BALE to Txt("Høyball","Høyball"),
        FixtureType.SNOWMAN to Txt("Snømann","Snømann"),
        FixtureType.PINE_TREE to Txt("Furutre","Furutre"),
        FixtureType.CAMPFIRE to Txt("Bål","Bål"),
        FixtureType.TENT to Txt("Telt","Telt"),
        FixtureType.SANDCASTLE to Txt("Sandslott","Sandslott"),
        FixtureType.CORAL to Txt("Korall","Korall"),
        FixtureType.KELP to Txt("Tang","Tang"),
        FixtureType.SUBMARINE to Txt("Ubåt","Ubåt"),
        FixtureType.GIANT_CLAM to Txt("Stort skjel","Stort skjell"),
        FixtureType.MI_FENCE to Txt("Gjerde","Gjerde"),
        FixtureType.MI_FLOWER_BED to Txt("Blomebed","Blomsterbed"),
        FixtureType.MI_MAILBOX to Txt("Postkasse","Postkasse"),
        FixtureType.MI_SWING to Txt("Huske","Huske"),
        FixtureType.MI_BIRD_BATH to Txt("Fuglebad","Fuglebad"),
        FixtureType.MI_GNOME to Txt("Hagenisse","Hagenisse"),
        FixtureType.MI_SANDBOX to Txt("Sandkasse","Sandkasse"),
        FixtureType.MI_APPLE_TREE to Txt("Epletre","Epletre"),
        FixtureType.MI_FIREPLACE to Txt("Peis","Peis"),
        FixtureType.MI_DINING_TABLE to Txt("Spisebord","Spisebord"),
        FixtureType.MI_COUNTER to Txt("Kjøkenbenk","Kjøkkenbenk"),
        FixtureType.MI_BEDSIDE to Txt("Nattbord","Nattbord"),
        FixtureType.MI_BLOCKS to Txt("Byggjeklossar","Byggeklosser"),
        FixtureType.MI_ROCKING_HORSE to Txt("Gyngehest","Gyngehest"),
        FixtureType.MI_BIG_BOOKCASE to Txt("Stor bokhylle","Stor bokhylle"),
        FixtureType.MI_GLOBE to Txt("Globus","Globus"),
        FixtureType.MI_GUITAR to Txt("Gitar","Gitar"),
        FixtureType.MI_SAW_BENCH to Txt("Sagbenk","Sagbenk"),
        FixtureType.MI_LAUNDRY_BASKET to Txt("Skittentøykorg","Skittentøykurv"),
        FixtureType.MI_PLANT_BED to Txt("Plantekasse","Plantekasse"),
        FixtureType.MI_HANGING_POT to Txt("Hengjepotte","Hengepotte"),
        FixtureType.MI_CHANDELIER to Txt("Lysekrone","Lysekrone"),
        FixtureType.MI_COAT_RACK to Txt("Knaggrekke","Knaggrekke"),
        FixtureType.PLAY_FORT to Txt("Putehytte","Putehytte"),
        FixtureType.PLAY_CART to Txt("Trillevogn","Trillevogn"),
    )
    fun name(type:FixtureType):Txt = ToyReward.entries.firstOrNull { it.type==type }?.let(SP::name)
        ?: names[type]
        ?: names[FixtureType.entries.firstOrNull { it.name==type.name.substringAfter('_') }]
        ?: when {
            type.spec.pool!=null -> Txt("Vassleik","Vannlek")
            type.spec.container!=null -> Txt("Skap","Skap")
            type.spec.spots.isNotEmpty() -> Txt("Sitjeplass","Sitteplass")
            type.spec.light!=null -> Txt("Lys","Lys")
            else -> Txt("Møbel","Møbel")
        }
}

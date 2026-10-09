package com.example.data.models

/**
 * Abia State 17 Local Government Areas (LGAs) and key transit market corridors.
 */
data class LgaZone(
    val id: String,
    val name: String,
    val zoneCategory: String, // Commercial Hub, Administrative, Agricultural, Border Transit
    val keyMarketsAndJunctions: List<String>,
    val centerLat: Double,
    val centerLng: Double,
    val baseFareNaira: Int = 300,
    val activeKekeFleetCount: Int = 45
)

object AbiaCorridorData {
    val ALL_17_LGAS = listOf(
        LgaZone(
            id = "aba_north",
            name = "Aba North",
            zoneCategory = "Commercial Hub",
            keyMarketsAndJunctions = listOf("Ariaria International Market", "Brass Junction", "Eziama High School Junction", "Faulks Road"),
            centerLat = 5.1325,
            centerLng = 7.3512,
            baseFareNaira = 400,
            activeKekeFleetCount = 180
        ),
        LgaZone(
            id = "aba_south",
            name = "Aba South",
            zoneCategory = "Commercial Hub",
            keyMarketsAndJunctions = listOf("Ahia Ohuru (New Market)", "Bata Junction", "Cemetery Market", "Ngwa Road Market", "Main Park"),
            centerLat = 5.1118,
            centerLng = 7.3689,
            baseFareNaira = 400,
            activeKekeFleetCount = 220
        ),
        LgaZone(
            id = "umuahia_north",
            name = "Umuahia North",
            zoneCategory = "Administrative Capital",
            keyMarketsAndJunctions = listOf("Isi-Gate Central Terminal", "FMC Umuahia", "Government House Junction", "Bende Road Park"),
            centerLat = 5.5320,
            centerLng = 7.4860,
            baseFareNaira = 350,
            activeKekeFleetCount = 140
        ),
        LgaZone(
            id = "umuahia_south",
            name = "Umuahia South",
            zoneCategory = "Administrative & Trade",
            keyMarketsAndJunctions = listOf("Ubani International Market", "Ubakala Junction", "Old Umuahia Road", "Amakama Park"),
            centerLat = 5.4800,
            centerLng = 7.4600,
            baseFareNaira = 350,
            activeKekeFleetCount = 110
        ),
        LgaZone(
            id = "osisioma",
            name = "Osisioma Ngwa",
            zoneCategory = "Industrial Corridor",
            keyMarketsAndJunctions = listOf("Osisioma Flyover Junction", "NNPC Depot Gate", "Ariaria Tollgate Express", "Abayi Park"),
            centerLat = 5.1580,
            centerLng = 7.3240,
            baseFareNaira = 450,
            activeKekeFleetCount = 95
        ),
        LgaZone(
            id = "obingwa",
            name = "Obingwa",
            zoneCategory = "Agro-Logistics",
            keyMarketsAndJunctions = listOf("Opobo Junction Express", "Mgboko LGA HQ", "Ururuka Road Hub", "Abala Market"),
            centerLat = 5.1050,
            centerLng = 7.4320,
            baseFareNaira = 350,
            activeKekeFleetCount = 65
        ),
        LgaZone(
            id = "ugwunagbo",
            name = "Ugwunagbo",
            zoneCategory = "Transit Corridor",
            keyMarketsAndJunctions = listOf("Asa Nnentu Auto-Spareparts Market", "Ugwunagbo Junction", "Ihie High School Cross"),
            centerLat = 5.0450,
            centerLng = 7.3400,
            baseFareNaira = 400,
            activeKekeFleetCount = 50
        ),
        LgaZone(
            id = "ukwa_west",
            name = "Ukwa West",
            zoneCategory = "Energy & Commercial",
            keyMarketsAndJunctions = listOf("Obehie Central Market", "Owaza Oil Terminal Road", "Ogwe Junction"),
            centerLat = 4.9650,
            centerLng = 7.2780,
            baseFareNaira = 400,
            activeKekeFleetCount = 42
        ),
        LgaZone(
            id = "ukwa_east",
            name = "Ukwa East",
            zoneCategory = "Riverine Logistics",
            keyMarketsAndJunctions = listOf("Akwete Weaving Center", "Azumini Blue River Park", "Obohia Cross"),
            centerLat = 4.9820,
            centerLng = 7.4200,
            baseFareNaira = 350,
            activeKekeFleetCount = 35
        ),
        LgaZone(
            id = "isiala_ngwa_north",
            name = "Isiala Ngwa North",
            zoneCategory = "Agro-Logistics",
            keyMarketsAndJunctions = listOf("Okpuala Ngwa Central Park", "Ntigha Junction", "Mbawsi Railway Market"),
            centerLat = 5.3900,
            centerLng = 7.4100,
            baseFareNaira = 300,
            activeKekeFleetCount = 40
        ),
        LgaZone(
            id = "isiala_ngwa_south",
            name = "Isiala Ngwa South",
            zoneCategory = "Transit Corridor",
            keyMarketsAndJunctions = listOf("Omoba Railway Market", "Ovuokwu Central", "Umuikaa Junction"),
            centerLat = 5.2750,
            centerLng = 7.3750,
            baseFareNaira = 300,
            activeKekeFleetCount = 38
        ),
        LgaZone(
            id = "bende",
            name = "Bende",
            zoneCategory = "Agricultural Hub",
            keyMarketsAndJunctions = listOf("Bende Town Park", "Uzuakoli Leprosy Center Hub", "Item Central Market", "Alayi Junction"),
            centerLat = 5.5600,
            centerLng = 7.6400,
            baseFareNaira = 350,
            activeKekeFleetCount = 30
        ),
        LgaZone(
            id = "ohafia",
            name = "Ohafia",
            zoneCategory = "Commercial & Barracks",
            keyMarketsAndJunctions = listOf("Ebem Ohafia Park", "Asaga Junction", "14 Brigade Army Gate", "Elu Market"),
            centerLat = 5.6200,
            centerLng = 7.8200,
            baseFareNaira = 350,
            activeKekeFleetCount = 55
        ),
        LgaZone(
            id = "arochukwu",
            name = "Arochukwu",
            zoneCategory = "Border & Heritage",
            keyMarketsAndJunctions = listOf("Arochukwu Roundabout", "Ibom Central Market", "Ututu Junction"),
            centerLat = 5.3900,
            centerLng = 7.9150,
            baseFareNaira = 350,
            activeKekeFleetCount = 28
        ),
        LgaZone(
            id = "isuikwuato",
            name = "Isuikwuato",
            zoneCategory = "Transit & Military",
            keyMarketsAndJunctions = listOf("Otikpo Market", "Mbalano Junction", "Oviorji Park"),
            centerLat = 5.7150,
            centerLng = 7.4900,
            baseFareNaira = 350,
            activeKekeFleetCount = 32
        ),
        LgaZone(
            id = "umunneochi",
            name = "Umunneochi",
            zoneCategory = "Trade & University",
            keyMarketsAndJunctions = listOf("Nkwoagu Isuochi Market", "Lokpanta Cattle Market", "ABSU Uturu Gate 1 Park"),
            centerLat = 5.9200,
            centerLng = 7.4200,
            baseFareNaira = 400,
            activeKekeFleetCount = 60
        ),
        LgaZone(
            id = "ikwuano",
            name = "Ikwuano",
            zoneCategory = "Education & Produce",
            keyMarketsAndJunctions = listOf("MOUAU Umudike Main Gate Park", "Ahia Ndoro Market", "Ibeku/Ikwuano Border"),
            centerLat = 5.4700,
            centerLng = 7.5600,
            baseFareNaira = 350,
            activeKekeFleetCount = 75
        )
    )

    fun getLgaById(id: String): LgaZone? = ALL_17_LGAS.find { it.id == id }

    fun findNearestLga(lat: Double, lng: Double): LgaZone? {
        return ALL_17_LGAS.minByOrNull { lga ->
            val dLat = lga.centerLat - lat
            val dLng = lga.centerLng - lng
            dLat * dLat + dLng * dLng
        }
    }

    data class MarketCoord(
        val marketName: String,
        val lgaId: String,
        val lgaName: String,
        val lat: Double,
        val lng: Double,
        val description: String = "Tricycle Park & Terminal"
    )

    // Curated high-precision coordinates for primary transit landmarks & market corridors across Abia State
    val POPULAR_MARKET_COORDS = listOf(
        MarketCoord("Ariaria International Market", "aba_north", "Aba North", 5.1388, 7.3465, "West Africa trade hub, Faulks Road Keke stage"),
        MarketCoord("Brass Junction", "aba_north", "Aba North", 5.1275, 7.3620, "Faulks / Aba-Owerri road commercial intersection"),
        MarketCoord("Faulks Road", "aba_north", "Aba North", 5.1340, 7.3550, "Ariaria expressway feeder & leather goods sector"),
        MarketCoord("Eziama High School Junction", "aba_north", "Aba North", 5.1410, 7.3710, "Ogbor Hill northern link"),
        MarketCoord("Ahia Ohuru (New Market)", "aba_south", "Aba South", 5.1054, 7.3725, "Ngwa road textile & produce loading depot"),
        MarketCoord("Bata Junction", "aba_south", "Aba South", 5.1118, 7.3689, "Central city crossroads, factory road connecting line"),
        MarketCoord("Cemetery Market", "aba_south", "Aba South", 5.1180, 7.3790, "Foodstuffs and pharmaceutical wholesale park"),
        MarketCoord("Ngwa Road Market", "aba_south", "Aba South", 5.0990, 7.3750, "South Aba commuter hub & grain market"),
        MarketCoord("Main Park", "aba_south", "Aba South", 5.1160, 7.3650, "Intercity and state transit master terminal"),
        MarketCoord("Isi-Gate Central Terminal", "umuahia_north", "Umuahia North", 5.5265, 7.4912, "Umuahia capital nucleus, railway crossing stage"),
        MarketCoord("FMC Umuahia", "umuahia_north", "Umuahia North", 5.5340, 7.4850, "Federal Medical Centre gate tricycle rank"),
        MarketCoord("Government House Junction", "umuahia_north", "Umuahia North", 5.5410, 7.4960, "State administrative district terminal"),
        MarketCoord("Bende Road Park", "umuahia_north", "Umuahia North", 5.5290, 7.5020, "Gateway park towards Item, Bende, Ohafia"),
        MarketCoord("Ubani International Market", "umuahia_south", "Umuahia South", 5.4650, 7.4520, "Modern relocated capital foodstuff hypermarket"),
        MarketCoord("Ubakala Junction", "umuahia_south", "Umuahia South", 5.4520, 7.4720, "Enugu-Port Harcourt expressway feeder rank"),
        MarketCoord("Old Umuahia Road", "umuahia_south", "Umuahia South", 5.4850, 7.4680, "Heritage corridor transit park"),
        MarketCoord("Amakama Park", "umuahia_south", "Umuahia South", 5.4410, 7.4810, "Southern peri-urban tricycle interchange"),
        MarketCoord("Osisioma Flyover Junction", "osisioma", "Osisioma Ngwa", 5.1580, 7.3240, "Major Aba gateway junction & interchange flyover"),
        MarketCoord("NNPC Depot Gate", "osisioma", "Osisioma Ngwa", 5.1720, 7.3150, "Industrial depot loading zone"),
        MarketCoord("Ariaria Tollgate Express", "osisioma", "Osisioma Ngwa", 5.1480, 7.3320, "Expressway junction for interstate commuters"),
        MarketCoord("Abayi Park", "osisioma", "Osisioma Ngwa", 5.1410, 7.3390, "Abayi residential & commercial keke station"),
        MarketCoord("Opobo Junction Express", "obingwa", "Obingwa", 5.1050, 7.4320, "Ogbor Hill connecting node to Akwa Ibom"),
        MarketCoord("Asa Nnentu Auto-Spareparts Market", "ugwunagbo", "Ugwunagbo", 5.0450, 7.3400, "Automotive machinery & transit zone"),
        MarketCoord("Obehie Central Market", "ukwa_west", "Ukwa West", 4.9650, 7.2780, "Oil-bearing corridor commercial nexus"),
        MarketCoord("Akwete Weaving Center", "ukwa_east", "Ukwa East", 4.9820, 7.4200, "Traditional textile & riverine port park"),
        MarketCoord("Okpuala Ngwa Central Park", "isiala_ngwa_north", "Isiala Ngwa North", 5.3900, 7.4100, "Historical administrative junction"),
        MarketCoord("Omoba Railway Market", "isiala_ngwa_south", "Isiala Ngwa South", 5.2750, 7.3750, "Railway trade hub and agricultural transit"),
        MarketCoord("Bende Town Park", "bende", "Bende", 5.5600, 7.6400, "Hilly agricultural produce depot"),
        MarketCoord("Ebem Ohafia Park", "ohafia", "Ohafia", 5.6200, 7.8200, "Military barracks corridor & highland trade station"),
        MarketCoord("Arochukwu Roundabout", "arochukwu", "Arochukwu", 5.3900, 7.9150, "Cross River border transit loop"),
        MarketCoord("Otikpo Market", "isuikwuato", "Isuikwuato", 5.7150, 7.4900, "Railway transit & military school corridor"),
        MarketCoord("Lokpanta Cattle Market", "umunneochi", "Umunneochi", 5.9200, 7.4200, "Northern border interstate trade gateway"),
        MarketCoord("ABSU Uturu Gate 1 Park", "umunneochi", "Umunneochi", 5.8340, 7.4480, "Abia State University main campus keke rank"),
        MarketCoord("MOUAU Umudike Main Gate Park", "ikwuano", "Ikwuano", 5.4700, 7.5600, "Michael Okpara University agriculture transit park")
    )

    fun resolveCoordinates(marketName: String, fallbackLgaId: String): Pair<Double, Double> {
        val found = POPULAR_MARKET_COORDS.firstOrNull { it.marketName.equals(marketName, ignoreCase = true) }
        if (found != null) {
            return Pair(found.lat, found.lng)
        }
        val lga = getLgaById(fallbackLgaId)
        if (lga != null) {
            // Slight pseudo-offset based on string hash so same LGA points aren't stacked exactly on top of each other
            val hashOffset = (marketName.hashCode() % 100) * 0.0003
            return Pair(lga.centerLat + hashOffset, lga.centerLng + hashOffset)
        }
        return Pair(5.1118, 7.3689) // Default Aba South center
    }
}


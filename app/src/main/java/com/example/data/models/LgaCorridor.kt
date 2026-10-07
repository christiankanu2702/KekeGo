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
}

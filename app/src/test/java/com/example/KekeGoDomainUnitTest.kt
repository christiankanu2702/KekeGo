package com.example

import com.example.data.models.*
import org.junit.Assert.*
import org.junit.Test

class KekeGoDomainUnitTest {

    @Test
    fun testAbiaState17LgasIntegrity() {
        assertEquals("Abia State must have exactly 17 LGAs", 17, AbiaCorridorData.ALL_17_LGAS.size)

        val lgaNames = AbiaCorridorData.ALL_17_LGAS.map { it.name }
        assertTrue(lgaNames.contains("Aba North"))
        assertTrue(lgaNames.contains("Aba South"))
        assertTrue(lgaNames.contains("Umuahia North"))
        assertTrue(lgaNames.contains("Umuahia South"))
        assertTrue(lgaNames.contains("Osisioma Ngwa"))
        assertTrue(lgaNames.contains("Obingwa"))
        assertTrue(lgaNames.contains("Ugwunagbo"))
        assertTrue(lgaNames.contains("Ukwa West"))
        assertTrue(lgaNames.contains("Ukwa East"))
        assertTrue(lgaNames.contains("Isiala Ngwa North"))
        assertTrue(lgaNames.contains("Isiala Ngwa South"))
        assertTrue(lgaNames.contains("Bende"))
        assertTrue(lgaNames.contains("Ohafia"))
        assertTrue(lgaNames.contains("Arochukwu"))
        assertTrue(lgaNames.contains("Isuikwuato"))
        assertTrue(lgaNames.contains("Umunneochi"))
        assertTrue(lgaNames.contains("Ikwuano"))

        AbiaCorridorData.ALL_17_LGAS.forEach { zone ->
            assertTrue("${zone.name} must have key transit markets", zone.keyMarketsAndJunctions.isNotEmpty())
            assertTrue("${zone.name} base fare must be positive", zone.baseFareNaira >= 300)
        }
    }

    @Test
    fun testVolumetricCargoSeatCapacity() {
        // Zero cargo = full 3 seats
        assertEquals(3, CargoCalculator.calculateAvailableSeats(sacks = 0, basins = 0))

        // 1 sack = consumes 1 seat (2 left)
        assertEquals(2, CargoCalculator.calculateAvailableSeats(sacks = 1, basins = 0))

        // 2 sacks = consumes 2 seats (1 left)
        assertEquals(1, CargoCalculator.calculateAvailableSeats(sacks = 2, basins = 0))

        // 3 sacks = consumes all 3 seats (0 left)
        assertEquals(0, CargoCalculator.calculateAvailableSeats(sacks = 3, basins = 0))

        // 2 basins = consumes 1 seat (2 left)
        assertEquals(2, CargoCalculator.calculateAvailableSeats(sacks = 0, basins = 2))

        // 4 basins = consumes 2 seats (1 left)
        assertEquals(1, CargoCalculator.calculateAvailableSeats(sacks = 0, basins = 4))
    }

    @Test
    fun testRecommendedFareCalculation() {
        val baseFare = 400
        // No cargo, no wait = base fare
        assertEquals(400, CargoCalculator.calculateRecommendedFare(baseFare, sacks = 0, basins = 0, waitMins = 0))

        // 2 sacks (+300) + 15 mins wait (+450) = 400 + 300 + 450 = 1150
        assertEquals(1150, CargoCalculator.calculateRecommendedFare(baseFare, sacks = 2, basins = 0, waitMins = 15))
    }

    @Test
    fun testDriverComplianceModel() {
        val driver = UserAccount(
            uid = "driver_123",
            role = UserRole.DRIVER,
            stateRiderPermitId = "AB/KKE/2026/8941",
            unionBranchCode = "ATRWAN-ABA-ZONE-2",
            chassisNumber = "BAJAJ-RE-4S-782103",
            creditFloatBalanceNaira = 5000,
            maxCreditLimitNaira = 15000
        )
        assertTrue(driver.isComplianceVerified)
        assertEquals(10000, driver.maxCreditLimitNaira - driver.creditFloatBalanceNaira)
    }
}

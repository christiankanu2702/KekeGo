package com.example.data.models

enum class UserRole {
    PASSENGER,
    DRIVER,
    PARK_CAPTAIN,
    ADMIN
}

enum class DriverServiceFilter {
    PASSENGERS_ONLY,
    WAYBILL_ONLY,
    BOTH
}

data class UserAccount(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val phoneNumber: String = "",
    val role: UserRole = UserRole.PASSENGER,
    val homeLgaId: String = "aba_south",
    val walletBalanceNaira: Int = 3500,
    val vaultSavingsNaira: Int = 450, // Fractional change vault
    // Driver compliance fields
    val stateRiderPermitId: String = "AB/KKE/2026/8941",
    val unionBranchCode: String = "ATRWAN-ABA-ZONE-2",
    val chassisNumber: String = "BAJAJ-RE-4S-782103",
    val tricyclePlateNo: String = "ABA-882-KU",
    val isComplianceVerified: Boolean = true,
    val driverServiceFilter: DriverServiceFilter = DriverServiceFilter.BOTH,
    val creditFloatBalanceNaira: Int = 0, // Micro-advance float balance
    val maxCreditLimitNaira: Int = 15000,
    val dailyUnionLevyPaid: Boolean = true,
    val commissionAccruedNaira: Int = 300,
    // Park captain fields
    val managedParkName: String = "Bata Central Terminal Aba",
    val assignedLgaId: String = "aba_south"
)

enum class TransactionType {
    WALLET_TOPUP,
    TRIP_PAYMENT,
    WAYBILL_SETTLEMENT,
    VAULT_SWEEP,
    COMMISSION_DEDUCTION,
    UNION_LEVY,
    FLOAT_DISBURSEMENT,
    FLOAT_REPAYMENT
}

data class LedgerEntry(
    val id: String = "",
    val userId: String = "",
    val type: TransactionType = TransactionType.WALLET_TOPUP,
    val amountNaira: Int = 0,
    val referenceToken: String = "",
    val description: String = "",
    val timestampEpoch: Long = System.currentTimeMillis()
)

data class DisputeRecord(
    val id: String = "",
    val tripId: String = "",
    val reportedByUserId: String = "",
    val reporterName: String = "",
    val reporterRole: UserRole = UserRole.PASSENGER,
    val reasonCategory: String = "CARGO_DAMAGE", // FARE_DISPUTE, CARGO_DAMAGE, ROUTE_DEVIATION, CONDUCT
    val description: String = "",
    val locationLga: String = "Aba South",
    val timestampEpoch: Long = System.currentTimeMillis(),
    val status: String = "INVESTIGATING", // INVESTIGATING, RESOLVED, ESCALATED
    val photoEvidenceAttached: Boolean = true,
    val photoUriString: String? = null
)

data class SosAlert(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userRole: UserRole = UserRole.PASSENGER,
    val lgaId: String = "aba_south",
    val latitude: Double = 5.1118,
    val longitude: Double = 7.3689,
    val compressedOperationalLog: String = "",
    val timestampEpoch: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false
)

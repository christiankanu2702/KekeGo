package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.firebase.FirebaseManager
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.local.*
import com.example.data.models.*
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class KekeGoRepository(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val TAG = "KekeGoRepository"
    private val database by lazy { KekeGoDatabase.getInstance(context) }
    private val dao by lazy { database.kekeGoDao() }

    private val _currentUserAccount = MutableStateFlow<UserAccount?>(null)
    val currentUserAccount = _currentUserAccount.asStateFlow()

    private val _activeBids = MutableStateFlow<List<BidOffer>>(emptyList())
    val activeBids = _activeBids.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline = _isOnline.asStateFlow()

    private var tripsListener: ListenerRegistration? = null
    private var bidsListener: ListenerRegistration? = null

    init {
        // Pre-populate LGA zones locally into Room
        scope.launch(Dispatchers.IO) {
            val entities = AbiaCorridorData.ALL_17_LGAS.map { zone ->
                LgaZoneEntity(
                    id = zone.id,
                    name = zone.name,
                    zoneCategory = zone.zoneCategory,
                    marketsRaw = zone.keyMarketsAndJunctions.joinToString("|"),
                    centerLat = zone.centerLat,
                    centerLng = zone.centerLng,
                    baseFareNaira = zone.baseFareNaira,
                    activeKekeFleetCount = zone.activeKekeFleetCount
                )
            }
            dao.insertLgaZones(entities)
        }
    }

    private fun requireUserId(): String {
        return FirebaseManager.getCurrentUser()?.uid
            ?: _currentUserAccount.value?.uid
            ?: "USER_ABIA_OFFLINE"
    }

    fun initDemoSession(role: UserRole = UserRole.PASSENGER) {
        val uid = "DEMO_USER_ABIA"
        val demoAccount = UserAccount(
            uid = uid,
            email = "commuter.abia@kekego.ng",
            displayName = if (role == UserRole.DRIVER) "Kalu Driver (ABA-882-KU)" else "Chukwudi Commuter",
            role = role,
            walletBalanceNaira = if (role == UserRole.DRIVER) 6500 else 4200,
            vaultSavingsNaira = 450,
            stateRiderPermitId = "AB/KKE/2026/8941",
            unionBranchCode = "ATRWAN-ABA-ZONE-2",
            chassisNumber = "BAJAJ-RE-4S-782103",
            tricyclePlateNo = "ABA-882-KU"
        )
        _currentUserAccount.value = demoAccount
        scope.launch(Dispatchers.IO) {
            dao.insertOrUpdateTrip(
                Trip(
                    id = "TRIP_DEMO_01",
                    passengerId = "DEMO_PAX_1",
                    passengerName = "Ngozi Trader",
                    passengerPhone = "08039821033",
                    serviceMode = ServiceMode.WAYBILL,
                    status = TripStatus.REQUESTED,
                    originLgaId = "aba_south",
                    originMarket = "Ahia Ohuru (New Market)",
                    destinationLgaId = "aba_north",
                    destinationMarket = "Ariaria Market",
                    proposedFareNaira = 600,
                    cargoSacks = 2,
                    cargoBasins = 1,
                    waybillOtp = "482910"
                ).toEntity(isSynced = true)
            )
        }
    }

    fun initUserSession(role: UserRole = UserRole.PASSENGER) {
        val authUser = FirebaseManager.getCurrentUser() ?: return
        val uid = authUser.uid
        val email = authUser.email ?: "commuter@kekego.ng"
        val name = authUser.displayName ?: if (role == UserRole.DRIVER) "Kalu Driver" else "Christian Commuter"

        // Load or create User profile in Firestore
        scope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseManager.getFirestore(context)
                val docRef = db.collection("users").document(uid)
                val snapshot = docRef.get().await()

                val account = if (snapshot.exists()) {
                    UserAccount(
                        uid = uid,
                        email = snapshot.getString("email") ?: email,
                        displayName = snapshot.getString("displayName") ?: name,
                        phoneNumber = snapshot.getString("phoneNumber") ?: "08031234567",
                        role = snapshot.getString("role")?.let { UserRole.valueOf(it) } ?: role,
                        homeLgaId = snapshot.getString("homeLgaId") ?: "aba_south",
                        walletBalanceNaira = snapshot.getLong("walletBalanceNaira")?.toInt() ?: 3500,
                        vaultSavingsNaira = snapshot.getLong("vaultSavingsNaira")?.toInt() ?: 450,
                        stateRiderPermitId = snapshot.getString("stateRiderPermitId") ?: "AB/KKE/2026/8941",
                        unionBranchCode = snapshot.getString("unionBranchCode") ?: "ATRWAN-ABA-ZONE-2",
                        chassisNumber = snapshot.getString("chassisNumber") ?: "BAJAJ-RE-4S-782103",
                        tricyclePlateNo = snapshot.getString("tricyclePlateNo") ?: "ABA-882-KU",
                        isComplianceVerified = snapshot.getBoolean("isComplianceVerified") ?: true,
                        creditFloatBalanceNaira = snapshot.getLong("creditFloatBalanceNaira")?.toInt() ?: 0,
                        dailyUnionLevyPaid = snapshot.getBoolean("dailyUnionLevyPaid") ?: true,
                        commissionAccruedNaira = snapshot.getLong("commissionAccruedNaira")?.toInt() ?: 300,
                        managedParkName = snapshot.getString("managedParkName") ?: "Bata Central Terminal Aba",
                        assignedLgaId = snapshot.getString("assignedLgaId") ?: "aba_south"
                    )
                } else {
                    val defaultAccount = UserAccount(
                        uid = uid,
                        email = email,
                        displayName = name,
                        role = role,
                        walletBalanceNaira = if (role == UserRole.DRIVER) 5000 else 3500,
                        vaultSavingsNaira = 450
                    )
                    saveUserToFirestore(defaultAccount)
                    defaultAccount
                }
                _currentUserAccount.value = account
                startRealtimeListeners(account)
            } catch (e: Exception) {
                Log.w(TAG, "Offline or Firestore error loading user, falling back to local session", e)
                _isOnline.value = false
                val fallback = UserAccount(
                    uid = uid,
                    email = email,
                    displayName = name,
                    role = role
                )
                _currentUserAccount.value = fallback
            }
        }
    }

    private suspend fun saveUserToFirestore(account: UserAccount) {
        if (FirebaseManager.getCurrentUser() == null) return
        try {
            val db = FirebaseManager.getFirestore(context)
            val data = hashMapOf(
                "userId" to account.uid,
                "email" to account.email,
                "displayName" to account.displayName,
                "phoneNumber" to account.phoneNumber,
                "role" to account.role.name,
                "homeLgaId" to account.homeLgaId,
                "walletBalanceNaira" to account.walletBalanceNaira,
                "vaultSavingsNaira" to account.vaultSavingsNaira,
                "stateRiderPermitId" to account.stateRiderPermitId,
                "unionBranchCode" to account.unionBranchCode,
                "chassisNumber" to account.chassisNumber,
                "tricyclePlateNo" to account.tricyclePlateNo,
                "isComplianceVerified" to account.isComplianceVerified,
                "creditFloatBalanceNaira" to account.creditFloatBalanceNaira,
                "dailyUnionLevyPaid" to account.dailyUnionLevyPaid,
                "commissionAccruedNaira" to account.commissionAccruedNaira,
                "managedParkName" to account.managedParkName,
                "assignedLgaId" to account.assignedLgaId
            )
            db.collection("users").document(account.uid).set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/${account.uid}")
        }
    }

    fun switchUserRole(newRole: UserRole) {
        val current = _currentUserAccount.value ?: return
        val updated = current.copy(role = newRole)
        _currentUserAccount.value = updated
        scope.launch(Dispatchers.IO) {
            saveUserToFirestore(updated)
            startRealtimeListeners(updated)
        }
    }

    private fun startRealtimeListeners(account: UserAccount) {
        tripsListener?.remove()
        bidsListener?.remove()

        if (FirebaseManager.getCurrentUser() == null) {
            // Local-only / offline demo mode: do not attach Firestore listeners while unauthenticated
            return
        }

        try {
            val db = FirebaseManager.getFirestore(context)
            // Listen to trips
            tripsListener = db.collection("trips")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.LIST, "trips")
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        scope.launch(Dispatchers.IO) {
                            val tripEntities = snapshots.documents.mapNotNull { doc ->
                                try {
                                    TripEntity(
                                        id = doc.id,
                                        passengerId = doc.getString("passengerId") ?: "",
                                        passengerName = doc.getString("passengerName") ?: "Commuter",
                                        passengerPhone = doc.getString("passengerPhone") ?: "08012345678",
                                        driverId = doc.getString("driverId"),
                                        driverName = doc.getString("driverName"),
                                        driverPhone = doc.getString("driverPhone"),
                                        tricycleRegNo = doc.getString("tricycleRegNo"),
                                        serviceMode = doc.getString("serviceMode") ?: ServiceMode.PASSENGER.name,
                                        status = doc.getString("status") ?: TripStatus.REQUESTED.name,
                                        originLgaId = doc.getString("originLgaId") ?: "aba_south",
                                        originMarket = doc.getString("originMarket") ?: "Bata Junction",
                                        destinationLgaId = doc.getString("destinationLgaId") ?: "aba_north",
                                        destinationMarket = doc.getString("destinationMarket") ?: "Ariaria Market",
                                        intermediateStopsRaw = (doc.get("intermediateStops") as? List<*>)?.joinToString(",") ?: "",
                                        proposedFareNaira = doc.getLong("proposedFareNaira")?.toInt() ?: 500,
                                        finalAgreedFareNaira = doc.getLong("finalAgreedFareNaira")?.toInt() ?: 500,
                                        cargoSacks = doc.getLong("cargoSacks")?.toInt() ?: 0,
                                        cargoBasins = doc.getLong("cargoBasins")?.toInt() ?: 0,
                                        isWaitAndReturn = doc.getBoolean("isWaitAndReturn") ?: false,
                                        waitMinutes = doc.getLong("waitMinutes")?.toInt() ?: 0,
                                        waybillRecipientName = doc.getString("waybillRecipientName") ?: "",
                                        waybillRecipientPhone = doc.getString("waybillRecipientPhone") ?: "",
                                        waybillOtp = doc.getString("waybillOtp") ?: "",
                                        isOtpVerified = doc.getBoolean("isOtpVerified") ?: false,
                                        isPaidDigitally = doc.getBoolean("isPaidDigitally") ?: false,
                                        paymentRef = doc.getString("paymentRef"),
                                        createdAtEpoch = doc.getLong("createdAtEpoch") ?: System.currentTimeMillis(),
                                        isSyncedWithCloud = true
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            dao.insertTrips(tripEntities)
                        }
                    }
                }

            // Listen to active bids
            bidsListener = db.collection("bids")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.LIST, "bids")
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        val bids = snapshots.documents.mapNotNull { doc ->
                            try {
                                BidOffer(
                                    id = doc.id,
                                    tripId = doc.getString("tripId") ?: "",
                                    driverId = doc.getString("driverId") ?: "",
                                    driverName = doc.getString("driverName") ?: "",
                                    driverRating = doc.getDouble("driverRating") ?: 4.8,
                                    tricyclePlate = doc.getString("tricyclePlate") ?: "ABA-419-KU",
                                    counterFareNaira = doc.getLong("counterFareNaira")?.toInt() ?: 600,
                                    etaMinutes = doc.getLong("etaMinutes")?.toInt() ?: 4,
                                    createdAtEpoch = doc.getLong("createdAtEpoch") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        _activeBids.value = bids
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting Firestore listeners", e)
        }
    }

    // Room Flow for Trips
    fun getCachedTripsFlow(): Flow<List<Trip>> {
        return dao.getAllCachedTripsFlow().map { entities ->
            entities.map { it.toDomainTrip() }
        }
    }

    /**
     * Explicitly pull past trips from Cloud Firestore collection `trips`.
     * Persists results into local Room cache so the list view is instantly populated and works offline.
     */
    suspend fun syncPastTripsFromFirestore(): Int {
        if (FirebaseManager.getCurrentUser() == null) {
            // Seed sample past trips if offline/demo so user can immediately test the list view
            val demoPastTrips = listOf(
                Trip(
                    id = "TRIP_ABA_0981",
                    passengerId = requireUserId(),
                    passengerName = _currentUserAccount.value?.displayName ?: "Christian Commuter",
                    passengerPhone = "08031234567",
                    driverId = "DRV_KALU_88",
                    driverName = "Kalu Driver",
                    driverPhone = "08029910022",
                    tricycleRegNo = "ABA-882-KU",
                    serviceMode = ServiceMode.PASSENGER,
                    status = TripStatus.COMPLETED,
                    originLgaId = "aba_south",
                    originMarket = "Ahia Ohuru (New Market)",
                    destinationLgaId = "aba_north",
                    destinationMarket = "Ariaria International Market",
                    intermediateStops = listOf("Milverton Avenue Junction"),
                    proposedFareNaira = 650,
                    finalAgreedFareNaira = 650,
                    cargoSacks = 1,
                    cargoBasins = 1,
                    isPaidDigitally = true,
                    paymentRef = "PSTK_TRIP_88912",
                    createdAtEpoch = System.currentTimeMillis() - (3600000L * 5)
                ),
                Trip(
                    id = "TRIP_ABA_0442",
                    passengerId = requireUserId(),
                    passengerName = _currentUserAccount.value?.displayName ?: "Christian Commuter",
                    passengerPhone = "08031234567",
                    driverId = "DRV_EMEKA_14",
                    driverName = "Emeka Okoro",
                    driverPhone = "08051123344",
                    tricycleRegNo = "ABA-718-KU",
                    serviceMode = ServiceMode.WAYBILL,
                    status = TripStatus.COMPLETED,
                    originLgaId = "aba_south",
                    originMarket = "Bata Central Terminal",
                    destinationLgaId = "osisioma",
                    destinationMarket = "Osisioma Flyover Depot",
                    intermediateStops = emptyList(),
                    proposedFareNaira = 800,
                    finalAgreedFareNaira = 800,
                    cargoSacks = 3,
                    cargoBasins = 0,
                    waybillRecipientName = "Chinedu Spare Parts",
                    waybillRecipientPhone = "08035544332",
                    waybillOtp = "918234",
                    isOtpVerified = true,
                    isPaidDigitally = true,
                    createdAtEpoch = System.currentTimeMillis() - (86400000L * 1)
                ),
                Trip(
                    id = "TRIP_UMU_0119",
                    passengerId = requireUserId(),
                    passengerName = _currentUserAccount.value?.displayName ?: "Christian Commuter",
                    passengerPhone = "08031234567",
                    driverId = "DRV_SUNDAY_55",
                    driverName = "Sunday Kalu",
                    driverPhone = "08064433221",
                    tricycleRegNo = "ABA-410-KU",
                    serviceMode = ServiceMode.DAY_HIRE,
                    status = TripStatus.COMPLETED,
                    originLgaId = "umuahia_north",
                    originMarket = "Ubani Ibeku Modern Market",
                    destinationLgaId = "umuahia_south",
                    destinationMarket = "Old Timber Market",
                    intermediateStops = listOf("Isi Gate Commercial Hub"),
                    proposedFareNaira = 2500,
                    finalAgreedFareNaira = 2500,
                    waitMinutes = 45,
                    isWaitAndReturn = true,
                    isPaidDigitally = false,
                    createdAtEpoch = System.currentTimeMillis() - (86400000L * 3)
                )
            )
            dao.insertTrips(demoPastTrips.map { it.toEntity(isSynced = true) })
            return demoPastTrips.size
        }

        return try {
            val db = FirebaseManager.getFirestore(context)
            val snapshot = db.collection("trips").get().await()
            val entities = snapshot.documents.mapNotNull { doc ->
                try {
                    TripEntity(
                        id = doc.id,
                        passengerId = doc.getString("passengerId") ?: "",
                        passengerName = doc.getString("passengerName") ?: "Commuter",
                        passengerPhone = doc.getString("passengerPhone") ?: "08012345678",
                        driverId = doc.getString("driverId"),
                        driverName = doc.getString("driverName"),
                        driverPhone = doc.getString("driverPhone"),
                        tricycleRegNo = doc.getString("tricycleRegNo"),
                        serviceMode = doc.getString("serviceMode") ?: ServiceMode.PASSENGER.name,
                        status = doc.getString("status") ?: TripStatus.REQUESTED.name,
                        originLgaId = doc.getString("originLgaId") ?: "aba_south",
                        originMarket = doc.getString("originMarket") ?: "Bata Junction",
                        destinationLgaId = doc.getString("destinationLgaId") ?: "aba_north",
                        destinationMarket = doc.getString("destinationMarket") ?: "Ariaria Market",
                        intermediateStopsRaw = (doc.get("intermediateStops") as? List<*>)?.joinToString(",") ?: "",
                        proposedFareNaira = doc.getLong("proposedFareNaira")?.toInt() ?: 500,
                        finalAgreedFareNaira = doc.getLong("finalAgreedFareNaira")?.toInt() ?: 500,
                        cargoSacks = doc.getLong("cargoSacks")?.toInt() ?: 0,
                        cargoBasins = doc.getLong("cargoBasins")?.toInt() ?: 0,
                        isWaitAndReturn = doc.getBoolean("isWaitAndReturn") ?: false,
                        waitMinutes = doc.getLong("waitMinutes")?.toInt() ?: 0,
                        waybillRecipientName = doc.getString("waybillRecipientName") ?: "",
                        waybillRecipientPhone = doc.getString("waybillRecipientPhone") ?: "",
                        waybillOtp = doc.getString("waybillOtp") ?: "",
                        isOtpVerified = doc.getBoolean("isOtpVerified") ?: false,
                        isPaidDigitally = doc.getBoolean("isPaidDigitally") ?: false,
                        paymentRef = doc.getString("paymentRef"),
                        createdAtEpoch = doc.getLong("createdAtEpoch") ?: System.currentTimeMillis(),
                        isSyncedWithCloud = true
                    )
                } catch (e: Exception) {
                    null
                }
            }
            if (entities.isNotEmpty()) {
                dao.insertTrips(entities)
            }
            entities.size
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "trips")
            Log.w(TAG, "syncPastTripsFromFirestore fallback: ${e.message}")
            0
        }
    }

    // Booking & Dispatch operations
    suspend fun createTripBooking(
        serviceMode: ServiceMode,
        originLga: String,
        originMarket: String,
        destLga: String,
        destMarket: String,
        intermediateStops: List<String>,
        proposedFare: Int,
        cargoSacks: Int,
        cargoBasins: Int,
        isWaitAndReturn: Boolean,
        waitMinutes: Int,
        recipientName: String = "",
        recipientPhone: String = "",
        isRecurring: Boolean = false
    ): String {
        val uid = requireUserId()
        val tripId = "TRIP_" + UUID.randomUUID().toString().take(8).uppercase()
        val user = _currentUserAccount.value
        val otp = if (serviceMode == ServiceMode.WAYBILL) (100000..999999).random().toString() else ""

        val trip = Trip(
            id = tripId,
            passengerId = uid,
            passengerName = user?.displayName ?: "Commuter",
            passengerPhone = user?.phoneNumber ?: "08031234567",
            serviceMode = serviceMode,
            status = TripStatus.REQUESTED,
            originLgaId = originLga,
            originMarket = originMarket,
            destinationLgaId = destLga,
            destinationMarket = destMarket,
            intermediateStops = intermediateStops,
            proposedFareNaira = proposedFare,
            finalAgreedFareNaira = proposedFare,
            cargoSacks = cargoSacks,
            cargoBasins = cargoBasins,
            passengerSeatsOccupied = CargoCalculator.calculateAvailableSeats(cargoSacks, cargoBasins),
            isWaitAndReturn = isWaitAndReturn,
            waitMinutes = waitMinutes,
            waybillRecipientName = recipientName,
            waybillRecipientPhone = recipientPhone,
            waybillOtp = otp,
            isRecurring = isRecurring,
            createdAtEpoch = System.currentTimeMillis()
        )

        // Cache locally first (Offline Resilience)
        dao.insertOrUpdateTrip(trip.toEntity(isSynced = false))

        // Sync with Cloud Firestore if authenticated
        if (FirebaseManager.getCurrentUser() != null) {
            try {
                val db = FirebaseManager.getFirestore(context)
                val tripMap = hashMapOf(
                    "tripId" to trip.id,
                    "passengerId" to trip.passengerId,
                    "passengerName" to trip.passengerName,
                    "passengerPhone" to trip.passengerPhone,
                    "serviceMode" to trip.serviceMode.name,
                    "status" to trip.status.name,
                    "originLgaId" to trip.originLgaId,
                    "originMarket" to trip.originMarket,
                    "destinationLgaId" to trip.destinationLgaId,
                    "destinationMarket" to trip.destinationMarket,
                    "intermediateStops" to trip.intermediateStops,
                    "proposedFareNaira" to trip.proposedFareNaira,
                    "finalAgreedFareNaira" to trip.finalAgreedFareNaira,
                    "cargoSacks" to trip.cargoSacks,
                    "cargoBasins" to trip.cargoBasins,
                    "isWaitAndReturn" to trip.isWaitAndReturn,
                    "waitMinutes" to trip.waitMinutes,
                    "waybillRecipientName" to trip.waybillRecipientName,
                    "waybillRecipientPhone" to trip.waybillRecipientPhone,
                    "waybillOtp" to trip.waybillOtp,
                    "isOtpVerified" to false,
                    "isPaidDigitally" to false,
                    "createdAtEpoch" to trip.createdAtEpoch
                )
                db.collection("trips").document(trip.id).set(tripMap).await()
                dao.insertOrUpdateTrip(trip.toEntity(isSynced = true))
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, "trips/${trip.id}")
                dao.enqueueOfflineAction(
                    OfflineQueueEntity(
                        actionType = "CREATE_TRIP",
                        payloadJson = trip.id
                    )
                )
            }
        }

        return tripId
    }

    suspend fun submitDriverBid(tripId: String, counterFareNaira: Int) {
        val uid = requireUserId()
        val user = _currentUserAccount.value ?: return
        val bidId = "BID_" + UUID.randomUUID().toString().take(6).uppercase()

        val bid = BidOffer(
            id = bidId,
            tripId = tripId,
            driverId = uid,
            driverName = user.displayName,
            driverRating = 4.9,
            tricyclePlate = user.tricyclePlateNo,
            counterFareNaira = counterFareNaira,
            etaMinutes = (2..7).random()
        )

        // 1. Immediately update in-memory active bids for instantaneous UI responsiveness
        _activeBids.value = _activeBids.value + bid

        // 2. Immediately update local Room trip status
        val cachedTrip = dao.getTripById(tripId)
        if (cachedTrip != null) {
            dao.insertOrUpdateTrip(cachedTrip.copy(status = TripStatus.BIDDING.name))
        }

        // 3. Sync to Cloud Firestore if the session is authenticated
        if (FirebaseManager.getCurrentUser() != null) {
            try {
                val db = FirebaseManager.getFirestore(context)
                val bidMap = hashMapOf(
                    "bidId" to bid.id,
                    "tripId" to bid.tripId,
                    "driverId" to bid.driverId,
                    "driverName" to bid.driverName,
                    "driverRating" to bid.driverRating,
                    "tricyclePlate" to bid.tricyclePlate,
                    "counterFareNaira" to bid.counterFareNaira,
                    "etaMinutes" to bid.etaMinutes,
                    "createdAtEpoch" to bid.createdAtEpoch
                )
                db.collection("bids").document(bid.id).set(bidMap).await()

                val tripDoc = db.collection("trips").document(tripId).get().await()
                if (tripDoc.exists()) {
                    db.collection("trips").document(tripId).update("status", TripStatus.BIDDING.name).await()
                } else if (cachedTrip != null) {
                    val tripMap = hashMapOf<String, Any>(
                        "tripId" to cachedTrip.id,
                        "passengerId" to cachedTrip.passengerId,
                        "passengerName" to cachedTrip.passengerName,
                        "passengerPhone" to cachedTrip.passengerPhone,
                        "serviceMode" to cachedTrip.serviceMode,
                        "status" to TripStatus.BIDDING.name,
                        "originLgaId" to cachedTrip.originLgaId,
                        "originMarket" to cachedTrip.originMarket,
                        "destinationLgaId" to cachedTrip.destinationLgaId,
                        "destinationMarket" to cachedTrip.destinationMarket,
                        "intermediateStops" to if (cachedTrip.intermediateStopsRaw.isBlank()) emptyList<String>() else cachedTrip.intermediateStopsRaw.split(","),
                        "proposedFareNaira" to cachedTrip.proposedFareNaira,
                        "finalAgreedFareNaira" to cachedTrip.finalAgreedFareNaira,
                        "cargoSacks" to cachedTrip.cargoSacks,
                        "cargoBasins" to cachedTrip.cargoBasins,
                        "isWaitAndReturn" to cachedTrip.isWaitAndReturn,
                        "waitMinutes" to cachedTrip.waitMinutes,
                        "waybillRecipientName" to cachedTrip.waybillRecipientName,
                        "waybillRecipientPhone" to cachedTrip.waybillRecipientPhone,
                        "waybillOtp" to cachedTrip.waybillOtp,
                        "isOtpVerified" to cachedTrip.isOtpVerified,
                        "isPaidDigitally" to cachedTrip.isPaidDigitally,
                        "createdAtEpoch" to cachedTrip.createdAtEpoch
                    )
                    db.collection("trips").document(cachedTrip.id).set(tripMap).await()
                }
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, "bids/${bid.id}")
                Log.w(TAG, "Driver bid saved locally; cloud sync notice: ${e.message}")
            }
        }
    }

    suspend fun acceptBid(tripId: String, bid: BidOffer) {
        // Update local DB and in-memory bids first
        val cached = dao.getTripById(tripId)
        if (cached != null) {
            dao.insertOrUpdateTrip(
                cached.copy(
                    status = TripStatus.ACCEPTED.name,
                    driverId = bid.driverId,
                    driverName = bid.driverName,
                    tricycleRegNo = bid.tricyclePlate,
                    finalAgreedFareNaira = bid.counterFareNaira
                )
            )
        }
        _activeBids.value = _activeBids.value.filter { it.tripId != tripId }

        if (FirebaseManager.getCurrentUser() != null) {
            try {
                val db = FirebaseManager.getFirestore(context)
                val tripDoc = db.collection("trips").document(tripId).get().await()
                if (tripDoc.exists()) {
                    val updates = hashMapOf<String, Any>(
                        "status" to TripStatus.ACCEPTED.name,
                        "driverId" to bid.driverId,
                        "driverName" to bid.driverName,
                        "tricycleRegNo" to bid.tricyclePlate,
                        "finalAgreedFareNaira" to bid.counterFareNaira
                    )
                    db.collection("trips").document(tripId).update(updates).await()
                }
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, "trips/$tripId")
                Log.w(TAG, "Trip acceptance saved locally; cloud sync notice: ${e.message}")
            }
        }
    }

    suspend fun startTrip(tripId: String) {
        updateTripStatus(tripId, TripStatus.IN_PROGRESS)
    }

    suspend fun completeTrip(tripId: String, enteredOtp: String? = null): Boolean {
        val cached = dao.getTripById(tripId) ?: return false

        // If waybill, verify OTP
        if (cached.serviceMode == ServiceMode.WAYBILL.name) {
            if (enteredOtp != cached.waybillOtp) {
                return false // OTP mismatch
            }
        }

        updateTripStatus(tripId, TripStatus.COMPLETED)
        return true
    }

    private suspend fun updateTripStatus(tripId: String, newStatus: TripStatus) {
        val cached = dao.getTripById(tripId)
        if (cached != null) {
            dao.insertOrUpdateTrip(cached.copy(status = newStatus.name))
        }

        if (FirebaseManager.getCurrentUser() != null) {
            try {
                val db = FirebaseManager.getFirestore(context)
                val tripDoc = db.collection("trips").document(tripId).get().await()
                if (tripDoc.exists()) {
                    db.collection("trips").document(tripId).update("status", newStatus.name).await()
                }
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, "trips/$tripId")
                Log.w(TAG, "Trip status updated locally; cloud sync notice: ${e.message}")
            }
        }
    }

    // Paystack & Wallet Settlement
    suspend fun fundWalletPaystack(amountNaira: Int, cardLast4: String): String {
        val uid = requireUserId()
        val user = _currentUserAccount.value ?: return ""
        val paystackRef = "PSTK_REF_" + UUID.randomUUID().toString().take(12).uppercase()

        val updatedBalance = user.walletBalanceNaira + amountNaira
        val updated = user.copy(walletBalanceNaira = updatedBalance)
        _currentUserAccount.value = updated

        // Save ledger locally
        val ledger = LedgerEntity(
            id = UUID.randomUUID().toString(),
            userId = uid,
            type = TransactionType.WALLET_TOPUP.name,
            amountNaira = amountNaira,
            referenceToken = paystackRef,
            description = "Paystack Card Topup (•••• $cardLast4)",
            timestampEpoch = System.currentTimeMillis()
        )
        dao.insertLedgerEntry(ledger)

        // Save to Firestore if authenticated
        if (FirebaseManager.getCurrentUser() != null) {
            saveUserToFirestore(updated)
            try {
                val db = FirebaseManager.getFirestore(context)
                val txMap = hashMapOf(
                    "transactionId" to ledger.id,
                    "userId" to uid,
                    "amountNaira" to amountNaira,
                    "referenceToken" to paystackRef,
                    "description" to ledger.description,
                    "timestampEpoch" to ledger.timestampEpoch
                )
                db.collection("users").document(uid).collection("transactions").document(ledger.id).set(txMap).await()
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.WRITE, "users/$uid/transactions/${ledger.id}")
                Log.w(TAG, "Error writing ledger to Firestore: ${e.message}")
            }
        }

        return paystackRef
    }

    // Vault-to-Wallet Sweep Mechanism
    suspend fun sweepVaultToWallet(): Int {
        val uid = requireUserId()
        val user = _currentUserAccount.value ?: return 0
        val vaultSavings = user.vaultSavingsNaira
        if (vaultSavings <= 0) return 0

        val sweepRef = "VAULT_SWEEP_" + UUID.randomUUID().toString().take(8).uppercase()
        val updatedUser = user.copy(
            walletBalanceNaira = user.walletBalanceNaira + vaultSavings,
            vaultSavingsNaira = 0
        )
        _currentUserAccount.value = updatedUser

        val ledger = LedgerEntity(
            id = UUID.randomUUID().toString(),
            userId = uid,
            type = TransactionType.VAULT_SWEEP.name,
            amountNaira = vaultSavings,
            referenceToken = sweepRef,
            description = "KekeGo Vault fractional change sweep to main wallet",
            timestampEpoch = System.currentTimeMillis()
        )
        dao.insertLedgerEntry(ledger)
        saveUserToFirestore(updatedUser)

        return vaultSavings
    }

    // Driver Fractional Cash Change Handover to Commuter's KekeGo Vault
    suspend fun depositFractionalChangeToVault(amountNaira: Int): String {
        val uid = requireUserId()
        val user = _currentUserAccount.value ?: return ""
        val token = "VAULT_CHANGE_" + UUID.randomUUID().toString().take(8).uppercase()

        val updatedUser = user.copy(
            vaultSavingsNaira = user.vaultSavingsNaira + amountNaira
        )
        _currentUserAccount.value = updatedUser

        val ledger = LedgerEntity(
            id = UUID.randomUUID().toString(),
            userId = uid,
            type = TransactionType.WALLET_TOPUP.name,
            amountNaira = amountNaira,
            referenceToken = token,
            description = "₦$amountNaira fractional change credited to KekeGo Vault",
            timestampEpoch = System.currentTimeMillis()
        )
        dao.insertLedgerEntry(ledger)
        saveUserToFirestore(updatedUser)

        return token
    }

    // Google Sheets / Drive Export Format for ATRWAN / TOAN Daily Union Audit
    fun exportUnionAuditSpreadsheetData(): String {
        val dateString = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val header = "Tricycle Plate,Rider Permit ID,Union Branch Code,Park Sector,LGA,Daily Levy (NGN),Status,Audit Timestamp\n"
        val rows = listOf(
            "ABA-882-KU,AB/KKE/2026/8941,ATRWAN-ABA-ZONE-2,Bata Terminal,Aba South,200,COMPLIANT,${dateString}T08:14:22",
            "ABA-419-KU,AB/KKE/2026/1049,ATRWAN-ABA-ZONE-2,Bata Terminal,Aba South,200,COMPLIANT,${dateString}T08:22:10",
            "ABA-991-KU,AB/KKE/2026/4921,TOAN-ABA-ZONE-1,Ariaria Express,Aba North,200,COMPLIANT,${dateString}T08:45:00",
            "UMU-312-KU,AB/KKE/2026/7781,ATRWAN-UMUAHIA-1,Isi-Gate Central,Umuahia North,200,COMPLIANT,${dateString}T09:05:40",
            "UMU-884-KU,AB/KKE/2026/3319,ATRWAN-UMUAHIA-2,Ubani Market,Umuahia South,200,COMPLIANT,${dateString}T09:18:15"
        ).joinToString("\n")

        return header + rows
    }

    // Driver Micro-Advance Credit Float
    suspend fun requestCreditFloat(amountNaira: Int): Boolean {
        val uid = requireUserId()
        val user = _currentUserAccount.value ?: return false
        if (amountNaira > (user.maxCreditLimitNaira - user.creditFloatBalanceNaira)) return false

        val updatedUser = user.copy(
            creditFloatBalanceNaira = user.creditFloatBalanceNaira + amountNaira,
            walletBalanceNaira = user.walletBalanceNaira + amountNaira
        )
        _currentUserAccount.value = updatedUser

        val ledger = LedgerEntity(
            id = UUID.randomUUID().toString(),
            userId = uid,
            type = TransactionType.FLOAT_DISBURSEMENT.name,
            amountNaira = amountNaira,
            referenceToken = "FLOAT_DISBURSE_" + UUID.randomUUID().toString().take(8),
            description = "Fuel & Maintenance float advance approved",
            timestampEpoch = System.currentTimeMillis()
        )
        dao.insertLedgerEntry(ledger)
        saveUserToFirestore(updatedUser)
        return true
    }

    // Dispute Logging with Photo Evidence
    suspend fun logDispute(
        tripId: String,
        reasonCategory: String,
        description: String,
        locationLga: String,
        photoUriString: String? = null
    ) {
        val uid = requireUserId()
        val user = _currentUserAccount.value ?: return
        val disputeId = "DISPUTE_" + UUID.randomUUID().toString().take(8)

        val dispute = DisputeEntity(
            id = disputeId,
            tripId = tripId,
            reportedByUserId = uid,
            reporterName = user.displayName,
            reasonCategory = reasonCategory,
            description = description,
            locationLga = locationLga,
            timestampEpoch = System.currentTimeMillis(),
            status = "INVESTIGATING",
            photoUriString = photoUriString
        )
        dao.insertDispute(dispute)

        if (FirebaseManager.getCurrentUser() != null) {
            try {
                val db = FirebaseManager.getFirestore(context)
                val disputeMap = hashMapOf(
                    "disputeId" to dispute.id,
                    "tripId" to dispute.tripId,
                    "reportedBy" to dispute.reportedByUserId,
                    "reporterName" to dispute.reporterName,
                    "reasonCategory" to dispute.reasonCategory,
                    "description" to dispute.description,
                    "locationLga" to dispute.locationLga,
                    "timestampEpoch" to dispute.timestampEpoch,
                    "status" to dispute.status,
                    "photoEvidenceAttached" to (photoUriString != null),
                    "photoUriString" to (photoUriString ?: "")
                )
                db.collection("disputes").document(dispute.id).set(disputeMap).await()
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, "disputes/${dispute.id}")
            }
        }
    }

    // Real Firestore Telemetry & Audit Pipeline for Statewide Admin
    suspend fun pushStatewideTelemetryAudit(
        activeFleetCount: Int = 1182,
        dailyGmvNaira: Long = 4820000L,
        otpSuccessRate: Double = 99.8
    ): String {
        val uid = requireUserId()
        val logId = "AUDIT_" + UUID.randomUUID().toString().take(10).uppercase()
        val timestamp = System.currentTimeMillis()

        if (FirebaseManager.getCurrentUser() != null) {
            try {
                val db = FirebaseManager.getFirestore(context)
                val auditData = hashMapOf(
                    "auditLogId" to logId,
                    "adminUserId" to uid,
                    "timestampEpoch" to timestamp,
                    "statewideLgasCovered" to 17,
                    "activeTricycles" to activeFleetCount,
                    "dailyGmvNaira" to dailyGmvNaira,
                    "otpWaybillSuccessRate" to otpSuccessRate,
                    "systemStatus" to "OPERATIONAL_CELLULAR_MESH",
                    "syncedCorridors" to listOf("Aba North", "Aba South", "Umuahia North", "Umuahia South", "Osisioma"),
                    "complianceVersion" to "ABIA-TRANSIT-V1.4"
                )
                db.collection("telemetry_audit_logs").document(logId).set(auditData).await()
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, "telemetry_audit_logs/$logId")
            }
        }
        return logId
    }

    // Emergency SOS Trigger
    suspend fun triggerEmergencySos(lat: Double, lng: Double, lgaId: String): String {
        val uid = requireUserId()
        val user = _currentUserAccount.value
        val sosId = "SOS_" + UUID.randomUUID().toString().take(8)
        val compressedLogs = "LOG:BATTERY_OK;NET_CELLULAR;SPEED_18KMH;LGA_${lgaId.uppercase()};TS_${System.currentTimeMillis()}"

        if (FirebaseManager.getCurrentUser() != null) {
            try {
                val db = FirebaseManager.getFirestore(context)
                val sosMap = hashMapOf(
                    "sosId" to sosId,
                    "userId" to uid,
                    "userName" to (user?.displayName ?: "Unknown"),
                    "userRole" to (user?.role?.name ?: "PASSENGER"),
                    "lgaZone" to lgaId,
                    "lat" to lat,
                    "lng" to lng,
                    "operationalLog" to compressedLogs,
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("sos_alerts").document(sosId).set(sosMap).await()
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, "sos_alerts/$sosId")
            }
        }

        return sosId
    }

    fun getLedgerFlow(): Flow<List<LedgerEntry>> {
        val uid = FirebaseManager.getCurrentUser()?.uid ?: ""
        return dao.getLedgerFlow(uid).map { list ->
            list.map {
                LedgerEntry(
                    id = it.id,
                    userId = it.userId,
                    type = try { TransactionType.valueOf(it.type) } catch (_: Exception) { TransactionType.WALLET_TOPUP },
                    amountNaira = it.amountNaira,
                    referenceToken = it.referenceToken,
                    description = it.description,
                    timestampEpoch = it.timestampEpoch
                )
            }
        }
    }

    fun getDisputesFlow(): Flow<List<DisputeRecord>> {
        return dao.getAllDisputesFlow().map { list ->
            list.map {
                DisputeRecord(
                    id = it.id,
                    tripId = it.tripId,
                    reportedByUserId = it.reportedByUserId,
                    reporterName = it.reporterName,
                    reasonCategory = it.reasonCategory,
                    description = it.description,
                    locationLga = it.locationLga,
                    timestampEpoch = it.timestampEpoch,
                    status = it.status,
                    photoEvidenceAttached = it.photoUriString != null,
                    photoUriString = it.photoUriString
                )
            }
        }
    }
}

// Entity mappings
fun TripEntity.toDomainTrip(): Trip {
    return Trip(
        id = this.id,
        passengerId = this.passengerId,
        passengerName = this.passengerName,
        passengerPhone = this.passengerPhone,
        driverId = this.driverId,
        driverName = this.driverName,
        driverPhone = this.driverPhone,
        tricycleRegNo = this.tricycleRegNo,
        serviceMode = try { ServiceMode.valueOf(this.serviceMode) } catch (_: Exception) { ServiceMode.PASSENGER },
        status = try { TripStatus.valueOf(this.status) } catch (_: Exception) { TripStatus.REQUESTED },
        originLgaId = this.originLgaId,
        originMarket = this.originMarket,
        destinationLgaId = this.destinationLgaId,
        destinationMarket = this.destinationMarket,
        intermediateStops = if (this.intermediateStopsRaw.isBlank()) emptyList() else this.intermediateStopsRaw.split(","),
        proposedFareNaira = this.proposedFareNaira,
        finalAgreedFareNaira = this.finalAgreedFareNaira,
        cargoSacks = this.cargoSacks,
        cargoBasins = this.cargoBasins,
        isWaitAndReturn = this.isWaitAndReturn,
        waitMinutes = this.waitMinutes,
        waybillRecipientName = this.waybillRecipientName,
        waybillRecipientPhone = this.waybillRecipientPhone,
        waybillOtp = this.waybillOtp,
        isOtpVerified = this.isOtpVerified,
        isPaidDigitally = this.isPaidDigitally,
        paymentRef = this.paymentRef,
        createdAtEpoch = this.createdAtEpoch
    )
}

fun Trip.toEntity(isSynced: Boolean): TripEntity {
    return TripEntity(
        id = this.id,
        passengerId = this.passengerId,
        passengerName = this.passengerName,
        passengerPhone = this.passengerPhone,
        driverId = this.driverId,
        driverName = this.driverName,
        driverPhone = this.driverPhone,
        tricycleRegNo = this.tricycleRegNo,
        serviceMode = this.serviceMode.name,
        status = this.status.name,
        originLgaId = this.originLgaId,
        originMarket = this.originMarket,
        destinationLgaId = this.destinationLgaId,
        destinationMarket = this.destinationMarket,
        intermediateStopsRaw = this.intermediateStops.joinToString(","),
        proposedFareNaira = this.proposedFareNaira,
        finalAgreedFareNaira = this.finalAgreedFareNaira,
        cargoSacks = this.cargoSacks,
        cargoBasins = this.cargoBasins,
        isWaitAndReturn = this.isWaitAndReturn,
        waitMinutes = this.waitMinutes,
        waybillRecipientName = this.waybillRecipientName,
        waybillRecipientPhone = this.waybillRecipientPhone,
        waybillOtp = this.waybillOtp,
        isOtpVerified = this.isOtpVerified,
        isPaidDigitally = this.isPaidDigitally,
        paymentRef = this.paymentRef,
        createdAtEpoch = this.createdAtEpoch,
        isSyncedWithCloud = isSynced
    )
}

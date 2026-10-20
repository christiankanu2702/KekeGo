# Cloud Firestore Security Rules Lockdown Plan

Transition Cloud Firestore from permissive mode to strict, zero-trust Role-Based Access Control (RBAC).

---

## 1. Zero-Trust Access Matrix

| Collection | Create | Read | Update | Delete |
| :--- | :--- | :--- | :--- | :--- |
| `/users/{userId}` | Authenticated user (`uid == userId`) | Authenticated | Owner or Super Admin | Super Admin only |
| `/trips/{tripId}` | Passenger (`request.resource.data.passengerId == auth.uid`) | Booking Passenger, Assigned Driver, Open Dispatchers, Super Admin | Passenger (cancel/rate/tip) OR Assigned Driver (accept/telemetry/status) | Booking Passenger (if unaccepted) or Super Admin |
| `/trips/{tripId}/messages/{msgId}` | Trip Participants (Passenger or Driver) | Trip Participants only | None (immutable audit trail) | Super Admin only |
| `/bids/{bidId}` | Driver (`request.resource.data.driverId == auth.uid`) | Bidding Driver, Target Passenger, Super Admin | Bidding Driver only | Bidding Driver only |
| `/parks/{parkId}` | Park Captain / Super Admin | All Authenticated | Park Captain / Super Admin | Super Admin only |
| `/sos_alerts/{sosId}` | Any Authenticated user in distress | Regulators, Patrols, Super Admin | Regulators, Super Admin | Super Admin only |

---

## 2. Super Admin Master Authority
`request.auth.token.email == 'christiankanu2702@gmail.com'` retains unrestricted global administrative access across all collections for emergency intervention, dispute resolution, and platform audits.

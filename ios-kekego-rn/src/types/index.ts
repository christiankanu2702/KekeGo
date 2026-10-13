export type UserRole = 'PASSENGER' | 'DRIVER' | 'PARK_CAPTAIN' | 'ADMIN';

export type DriverServiceFilter = 'PASSENGERS_ONLY' | 'WAYBILL_ONLY' | 'BOTH';

export interface UserAccount {
  uid: string;
  email: string;
  displayName: string;
  phoneNumber: string;
  role: UserRole;
  homeLgaId: string;
  walletBalanceNaira: number;
  vaultSavingsNaira: number;
  // Driver compliance fields
  stateRiderPermitId?: string;
  unionBranchCode?: string;
  chassisNumber?: string;
  tricyclePlateNo?: string;
  isComplianceVerified?: boolean;
  driverServiceFilter?: DriverServiceFilter;
  // Park Captain fields
  managedParkName?: string;
  assignedLgaId?: string;
}

export type TripStatus =
  | 'REQUESTED'
  | 'BIDDING'
  | 'ACCEPTED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED';

export interface Trip {
  id: string;
  passengerId: string;
  passengerName: string;
  passengerPhone: string;
  driverId?: string;
  driverName?: string;
  driverPhone?: string;
  tricycleRegNo?: string;
  status: TripStatus;
  originLgaId: string;
  originMarket: string;
  destinationLgaId: string;
  destinationMarket: string;
  intermediateStops: string[];
  proposedFareNaira: number;
  finalAgreedFareNaira: number;
  createdAtEpoch: number;
}

export interface TripChatMessage {
  id: string;
  tripId: string;
  senderId: string;
  senderName: string;
  senderRole: UserRole;
  messageText: string;
  timestampEpoch: number;
}

export type KekeRideTier = 'STANDARD' | 'CHARTER' | 'CARGO';

export interface RideTierOption {
  key: KekeRideTier;
  title: string;
  subtitle: string;
  multiplier: number;
  capacitySeats: number;
  etaMins: number;
}

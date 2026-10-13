import {
  collection,
  doc,
  setDoc,
  getDoc,
  onSnapshot,
  query,
  where,
  addDoc,
  updateDoc
} from 'firebase/firestore';
import { db } from './config';
import { UserAccount, Trip, TripChatMessage, TripStatus } from '../types';

export const FirestoreService = {
  // Authoritative User Profile Synchronization
  async saveUserProfile(user: UserAccount): Promise<void> {
    const userRef = doc(db, 'users', user.uid);
    await setDoc(userRef, user, { merge: true });

    // Sync to respective role partition
    const roleCollection = user.role.toLowerCase() + 's';
    const roleRef = doc(db, roleCollection, user.uid);
    await setDoc(roleRef, user, { merge: true });
  },

  async getUserProfile(uid: string): Promise<UserAccount | null> {
    const userRef = doc(db, 'users', uid);
    const snap = await getDoc(userRef);
    if (snap.exists()) {
      return snap.data() as UserAccount;
    }
    return null;
  },

  // Real-Time Trip Operations
  async createTripRequest(trip: Omit<Trip, 'id'>): Promise<string> {
    const tripsColl = collection(db, 'trips');
    const docRef = await addDoc(tripsColl, trip);
    return docRef.id;
  },

  listenActiveTrips(onUpdate: (trips: Trip[]) => void) {
    const tripsColl = collection(db, 'trips');
    return onSnapshot(tripsColl, (snap) => {
      const trips: Trip[] = [];
      snap.forEach((doc) => {
        trips.push({ id: doc.id, ...(doc.data() as Omit<Trip, 'id'>) });
      });
      onUpdate(trips);
    });
  },

  async updateTripStatus(tripId: string, status: TripStatus): Promise<void> {
    const tripRef = doc(db, 'trips', tripId);
    await updateDoc(tripRef, { status });
  },

  // Cross-Platform In-App Chat
  async sendTripMessage(tripId: string, message: Omit<TripChatMessage, 'id'>): Promise<void> {
    const messagesColl = collection(db, 'trips', tripId, 'messages');
    await addDoc(messagesColl, message);
  },

  listenTripMessages(tripId: string, onUpdate: (messages: TripChatMessage[]) => void) {
    const messagesColl = collection(db, 'trips', tripId, 'messages');
    return onSnapshot(messagesColl, (snap) => {
      const msgs: TripChatMessage[] = [];
      snap.forEach((doc) => {
        msgs.push({ id: doc.id, ...(doc.data() as Omit<TripChatMessage, 'id'>) });
      });
      msgs.sort((a, b) => a.timestampEpoch - b.timestampEpoch);
      onUpdate(msgs);
    });
  }
};

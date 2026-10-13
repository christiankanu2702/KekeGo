import { initializeApp, getApps, getApp } from 'firebase/app';
import { getFirestore } from 'firebase/firestore';
import { getAuth } from 'firebase/auth';

/**
 * Shared Firebase Configuration - Mirrors Android Applet exactly:
 * Project: gen-lang-client-0639805966 (Project Number: 715487607162)
 */
export const firebaseConfig = {
  apiKey: "AIzaSyDuGkc8JDLrIc2B9fgC6dnH26BaKS1tZP8",
  authDomain: "gen-lang-client-0639805966.firebaseapp.com",
  projectId: "gen-lang-client-0639805966",
  storageBucket: "gen-lang-client-0639805966.appspot.com",
  messagingSenderId: "715487607162",
  appId: "1:715487607162:web:87babc36870e85381cbec3"
};

export const app = getApps().length === 0 ? initializeApp(firebaseConfig) : getApp();
export const db = getFirestore(app);
export const auth = getAuth(app);

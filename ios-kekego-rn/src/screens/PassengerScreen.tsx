import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  SafeAreaView,
  Alert,
} from 'react-native';
import { Colors } from '../theme/colors';
import { UserAccount, Trip, BoltRideTier, TripChatMessage } from '../types';
import { BoltRideMapView } from '../components/BoltRideMapView';
import { BoltSavedPlacesRow } from '../components/BoltSavedPlacesRow';
import { BoltRideTierSelector } from '../components/BoltRideTierSelector';
import { BoltActiveTripCard } from '../components/BoltActiveTripCard';
import { BoltChatModal } from '../components/BoltChatModal';
import { FirestoreService } from '../firebase/firestoreService';

interface PassengerScreenProps {
  user: UserAccount;
}

export const PassengerScreen: React.FC<PassengerScreenProps> = ({ user }) => {
  const [originMarket, setOriginMarket] = useState('Bata Junction Aba');
  const [destMarket, setDestMarket] = useState('Ariaria International Market');
  const [selectedTier, setSelectedTier] = useState<BoltRideTier>('STANDARD');
  const [activeTrip, setActiveTrip] = useState<Trip | null>({
    id: 'TRIP_DEMO_IOS_01',
    passengerId: user.uid,
    passengerName: user.displayName,
    passengerPhone: user.phoneNumber,
    driverName: 'Kalu Driver',
    driverPhone: '08039821033',
    tricycleRegNo: 'ABA-882-KU',
    status: 'ACCEPTED',
    originLgaId: 'aba_south',
    originMarket: 'Bata Junction Aba',
    destinationLgaId: 'aba_north',
    destinationMarket: 'Ariaria International Market',
    intermediateStops: [],
    proposedFareNaira: 400,
    finalAgreedFareNaira: 400,
    createdAtEpoch: Date.now(),
  });

  const [chatVisible, setChatVisible] = useState(false);
  const [chatMessages, setChatMessages] = useState<TripChatMessage[]>([
    {
      id: 'MSG_1',
      tripId: 'TRIP_DEMO_IOS_01',
      senderId: 'DRV_1',
      senderName: 'Kalu Driver',
      senderRole: 'DRIVER',
      messageText: 'I am arriving Bata Junction in 2 minutes.',
      timestampEpoch: Date.now() - 120000,
    },
  ]);

  const handleSendMessage = (text: string) => {
    const newMsg: TripChatMessage = {
      id: 'MSG_' + Date.now(),
      tripId: 'TRIP_DEMO_IOS_01',
      senderId: user.uid,
      senderName: user.displayName,
      senderRole: 'PASSENGER',
      messageText: text,
      timestampEpoch: Date.now(),
    };
    setChatMessages((prev) => [...prev, newMsg]);
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      {/* Top Bar with Wallet & SOS */}
      <View style={styles.topBar}>
        <View>
          <Text style={styles.greetingText}>Hello, {user.displayName.split(' ')[0]}</Text>
          <Text style={styles.lgaText}>Abia State • Corridor Transit</Text>
        </View>

        <View style={styles.topBarRight}>
          <View style={styles.walletBadge}>
            <Text style={styles.walletText}>₦{user.walletBalanceNaira}</Text>
          </View>
          <TouchableOpacity
            style={styles.sosBtn}
            onPress={() => Alert.alert('SOS Broadcasted', 'Emergency beacon transmitted to Abia State Police & ATRWAN rapid patrol.')}
          >
            <Text style={styles.sosText}>SOS</Text>
          </TouchableOpacity>
        </View>
      </View>

      <ScrollView contentContainerStyle={styles.container}>
        {/* Saved Places Shortcuts */}
        <BoltSavedPlacesRow
          onSelectPlace={(market) => {
            setDestMarket(market);
          }}
        />

        {/* 3D Isometric Cityscape Transit Radar */}
        <BoltRideMapView
          originMarket={originMarket}
          destinationMarket={destMarket}
        />

        {/* Multi-Category Ride Tier Selector */}
        <BoltRideTierSelector
          baseFareNaira={400}
          selectedTier={selectedTier}
          onTierSelected={setSelectedTier}
        />

        {/* Active Trip Status Card with Waiting Timer Countdown */}
        {activeTrip && (
          <BoltActiveTripCard
            trip={activeTrip}
            onOpenChat={() => setChatVisible(true)}
            onCancelTrip={() => setActiveTrip(null)}
          />
        )}
      </ScrollView>

      {/* Bolt In-App Chat Modal */}
      <BoltChatModal
        visible={chatVisible}
        onClose={() => setChatVisible(false)}
        messages={chatMessages}
        onSendMessage={handleSendMessage}
      />
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: Colors.asphaltBlack,
  },
  topBar: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderBottomColor: Colors.asphaltDivider,
    backgroundColor: Colors.asphaltDark,
  },
  greetingText: {
    color: '#FFF',
    fontSize: 16,
    fontWeight: '900',
  },
  lgaText: {
    color: Colors.mutedSilver,
    fontSize: 10,
  },
  topBarRight: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  walletBadge: {
    backgroundColor: Colors.asphaltCard,
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: Colors.safetyAmber,
  },
  walletText: {
    color: Colors.safetyAmber,
    fontSize: 11,
    fontWeight: 'bold',
  },
  sosBtn: {
    backgroundColor: '#D32F2F',
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 8,
  },
  sosText: {
    color: '#FFF',
    fontSize: 11,
    fontWeight: '900',
  },
  container: {
    padding: 16,
  },
});

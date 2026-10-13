import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  SafeAreaView,
  Switch,
} from 'react-native';
import { Colors } from '../theme/colors';
import { UserAccount } from '../types';
import { BoltRideMapView } from '../components/BoltRideMapView';

interface DriverScreenProps {
  user: UserAccount;
}

export const DriverScreen: React.FC<DriverScreenProps> = ({ user }) => {
  const [isOnline, setIsOnline] = useState(true);

  return (
    <SafeAreaView style={styles.safeArea}>
      {/* Header */}
      <View style={styles.topBar}>
        <View>
          <Text style={styles.title}>Rider Console (iOS)</Text>
          <Text style={styles.subtitle}>{user.tricyclePlateNo || 'ABA-882-KU'} • ATRWAN Zone 2</Text>
        </View>

        <View style={styles.switchRow}>
          <Text style={[styles.statusLabel, isOnline ? styles.onlineText : styles.offlineText]}>
            {isOnline ? 'ONLINE' : 'OFFLINE'}
          </Text>
          <Switch
            value={isOnline}
            onValueChange={setIsOnline}
            trackColor={{ false: '#333', true: '#00D664' }}
            thumbColor={'#FFF'}
          />
        </View>
      </View>

      <ScrollView contentContainerStyle={styles.container}>
        {/* Compliance Banner */}
        <View style={styles.complianceCard}>
          <Text style={styles.complianceTitle}>✓ State Commercial Permit Verified</Text>
          <Text style={styles.complianceSubtitle}>Permit: {user.stateRiderPermitId || 'AB/KKE/2026/8941'}</Text>
        </View>

        {/* Live Transit Map */}
        <BoltRideMapView
          originMarket="Bata Junction Aba"
          destinationMarket="Ariaria International Market"
        />

        {/* Live Passenger Request Card */}
        <Text style={styles.sectionHeader}>INCOMING COMMUTER REQUEST</Text>
        <View style={styles.requestCard}>
          <View style={styles.requestTop}>
            <View>
              <Text style={styles.passengerName}>Chidi Merchant</Text>
              <Text style={styles.routeText}>Bata Junction ➔ Ariaria Market</Text>
            </View>
            <Text style={styles.fareOffer}>₦500</Text>
          </View>

          <View style={styles.requestActions}>
            <TouchableOpacity style={styles.acceptBtn}>
              <Text style={styles.acceptBtnText}>Accept Bid (₦500)</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.counterBtn}>
              <Text style={styles.counterBtnText}>Counter (₦600)</Text>
            </TouchableOpacity>
          </View>
        </View>
      </ScrollView>
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
    padding: 16,
    backgroundColor: Colors.asphaltDark,
    borderBottomWidth: 1,
    borderBottomColor: Colors.asphaltDivider,
  },
  title: {
    color: '#FFF',
    fontSize: 16,
    fontWeight: 'bold',
  },
  subtitle: {
    color: Colors.safetyAmber,
    fontSize: 11,
    marginTop: 2,
  },
  switchRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  statusLabel: {
    fontSize: 11,
    fontWeight: 'bold',
  },
  onlineText: {
    color: '#00D664',
  },
  offlineText: {
    color: Colors.mutedSilver,
  },
  container: {
    padding: 16,
  },
  complianceCard: {
    backgroundColor: '#0F291B',
    borderRadius: 12,
    padding: 12,
    borderWidth: 1,
    borderColor: '#00D664',
    marginBottom: 10,
  },
  complianceTitle: {
    color: '#00D664',
    fontSize: 12,
    fontWeight: 'bold',
  },
  complianceSubtitle: {
    color: '#FFF',
    fontSize: 10,
    marginTop: 2,
  },
  sectionHeader: {
    color: Colors.safetyAmber,
    fontSize: 11,
    fontWeight: 'bold',
    marginTop: 14,
    marginBottom: 8,
  },
  requestCard: {
    backgroundColor: Colors.asphaltCard,
    borderRadius: 14,
    padding: 14,
    borderWidth: 1,
    borderColor: Colors.asphaltDivider,
  },
  requestTop: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  passengerName: {
    color: '#FFF',
    fontSize: 14,
    fontWeight: 'bold',
  },
  routeText: {
    color: Colors.mutedSilver,
    fontSize: 11,
    marginTop: 2,
  },
  fareOffer: {
    color: '#00D664',
    fontSize: 18,
    fontWeight: '900',
  },
  requestActions: {
    flexDirection: 'row',
    gap: 10,
  },
  acceptBtn: {
    flex: 1.5,
    backgroundColor: '#00D664',
    paddingVertical: 10,
    borderRadius: 8,
    alignItems: 'center',
  },
  acceptBtnText: {
    color: Colors.asphaltDark,
    fontSize: 12,
    fontWeight: 'bold',
  },
  counterBtn: {
    flex: 1,
    backgroundColor: Colors.asphaltSurface,
    paddingVertical: 10,
    borderRadius: 8,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: Colors.safetyAmber,
  },
  counterBtnText: {
    color: Colors.safetyAmber,
    fontSize: 12,
    fontWeight: 'bold',
  },
});

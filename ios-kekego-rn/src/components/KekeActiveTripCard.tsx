import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Linking, Share } from 'react-native';
import { Colors } from '../theme/colors';
import { Trip } from '../types';

interface KekeActiveTripCardProps {
  trip: Trip;
  onOpenChat: () => void;
  onCancelTrip: () => void;
}

export const KekeActiveTripCard: React.FC<KekeActiveTripCardProps> = ({
  trip,
  onOpenChat,
  onCancelTrip,
}) => {
  const [secondsRemaining, setSecondsRemaining] = useState(299);

  useEffect(() => {
    const timer = setInterval(() => {
      setSecondsRemaining((prev) => (prev > 0 ? prev - 1 : 0));
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  const mins = Math.floor(secondsRemaining / 60);
  const secs = secondsRemaining % 60;
  const timeFormatted = `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;

  const driverPhone = trip.driverPhone || '08039821033';
  const driverName = trip.driverName || 'Kalu Driver';
  const plateNo = trip.tricycleRegNo || 'ABA-882-KU';

  const handleCall = () => {
    Linking.openURL(`tel:${driverPhone}`);
  };

  const handleShare = async () => {
    try {
      await Share.share({
        message: `Follow my live KekeGo ride on iOS with ${driverName} (${plateNo}) from ${trip.originMarket} to ${trip.destinationMarket}. Live ETA: 2 mins.`,
      });
    } catch {}
  };

  return (
    <View style={styles.card}>
      {/* Status & Free Wait Timer */}
      <View style={styles.topRow}>
        <View style={styles.statusIndicator}>
          <View style={styles.statusDot} />
          <Text style={styles.statusText}>DRIVER ARRIVING • 2 MINS</Text>
        </View>
        <View style={styles.timerBadge}>
          <Text style={styles.timerText}>Free Wait: {timeFormatted}</Text>
        </View>
      </View>

      {/* Driver Info & Fare */}
      <View style={styles.driverRow}>
        <View style={styles.driverLeft}>
          <View style={styles.avatar}>
            <Text style={styles.avatarText}>🛺</Text>
          </View>
          <View>
            <View style={{ flexDirection: 'row', alignItems: 'center', gap: 6 }}>
              <Text style={styles.driverName}>{driverName}</Text>
              <View style={styles.ratingBadge}>
                <Text style={styles.ratingText}>★ 4.9</Text>
              </View>
            </View>
            <Text style={styles.vehicleDetails}>Bajaj RE 4-Stroke • {plateNo}</Text>
          </View>
        </View>

        <View style={{ alignItems: 'flex-end' }}>
          <Text style={styles.fareAmount}>₦{trip.finalAgreedFareNaira || trip.proposedFareNaira}</Text>
          <Text style={styles.fareType}>Cash / Wallet</Text>
        </View>
      </View>

      <View style={styles.divider} />

      {/* Action Buttons: Chat, Call, Share */}
      <View style={styles.actionsRow}>
        <TouchableOpacity style={styles.chatBtn} onPress={onOpenChat}>
          <Text style={styles.chatBtnText}>💬 Chat</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.outlineBtn} onPress={handleCall}>
          <Text style={styles.outlineBtnText}>📞 Call</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.outlineBtn} onPress={handleShare}>
          <Text style={styles.outlineBtnText}>📤 Share</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.cancelBtn} onPress={onCancelTrip}>
          <Text style={styles.cancelBtnText}>✕</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  card: {
    backgroundColor: Colors.asphaltDark,
    borderRadius: 20,
    padding: 16,
    borderWidth: 1.5,
    borderColor: 'rgba(0, 214, 100, 0.7)',
    marginVertical: 10,
  },
  topRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  statusIndicator: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  statusDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: '#00D664',
    marginRight: 6,
  },
  statusText: {
    color: '#00D664',
    fontSize: 11,
    fontWeight: '900',
  },
  timerBadge: {
    backgroundColor: '#1E2633',
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: '#323F54',
  },
  timerText: {
    color: '#FFF',
    fontSize: 10,
    fontWeight: '700',
  },
  driverRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  driverLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  avatar: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: Colors.safetyAmber,
    justifyContent: 'center',
    alignItems: 'center',
  },
  avatarText: {
    fontSize: 22,
  },
  driverName: {
    color: Colors.highContrastWhite,
    fontSize: 15,
    fontWeight: '900',
  },
  ratingBadge: {
    backgroundColor: 'rgba(255, 179, 0, 0.2)',
    paddingHorizontal: 4,
    paddingVertical: 1,
    borderRadius: 4,
  },
  ratingText: {
    color: Colors.safetyAmber,
    fontSize: 9,
    fontWeight: 'bold',
  },
  vehicleDetails: {
    color: Colors.mutedSilver,
    fontSize: 11,
    marginTop: 2,
  },
  fareAmount: {
    color: '#00D664',
    fontSize: 18,
    fontWeight: '900',
  },
  fareType: {
    color: Colors.mutedSilver,
    fontSize: 9,
  },
  divider: {
    height: 0.8,
    backgroundColor: Colors.asphaltDivider,
    marginVertical: 12,
  },
  actionsRow: {
    flexDirection: 'row',
    gap: 8,
  },
  chatBtn: {
    flex: 1.2,
    backgroundColor: '#00D664',
    paddingVertical: 10,
    borderRadius: 10,
    alignItems: 'center',
  },
  chatBtnText: {
    color: Colors.asphaltDark,
    fontSize: 12,
    fontWeight: '900',
  },
  outlineBtn: {
    flex: 1,
    borderWidth: 1,
    borderColor: Colors.asphaltDivider,
    paddingVertical: 10,
    borderRadius: 10,
    alignItems: 'center',
  },
  outlineBtnText: {
    color: Colors.highContrastWhite,
    fontSize: 12,
    fontWeight: '700',
  },
  cancelBtn: {
    width: 36,
    borderWidth: 1,
    borderColor: Colors.errorRed,
    paddingVertical: 10,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  cancelBtnText: {
    color: Colors.errorRed,
    fontSize: 12,
    fontWeight: 'bold',
  },
});

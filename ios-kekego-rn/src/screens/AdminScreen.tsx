import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  SafeAreaView,
  Linking,
} from 'react-native';
import { Colors } from '../theme/colors';
import { UserAccount, UserRole } from '../types';

interface AdminScreenProps {
  user: UserAccount;
  onSwitchViewRole: (role: UserRole) => void;
}

const REGISTERED_USERS = [
  { name: 'Christian Kanu', role: 'SUPER ADMIN', contact: 'christiankanu2702@gmail.com', isSuper: true },
  { name: 'Kalu Driver', role: 'RIDER • ABA-882-KU', contact: '08039821033', isSuper: false },
  { name: 'Chinedu Okeke', role: 'RIDER • ABA-419-KU', contact: '08149811234', isSuper: false },
  { name: 'Captain Eze', role: 'PARK CAPTAIN • Bata Terminal', contact: '08051122334', isSuper: false },
  { name: 'Ngozi Trader', role: 'PASSENGER • Aba South', contact: '07089900112', isSuper: false },
];

export const AdminScreen: React.FC<AdminScreenProps> = ({ user, onSwitchViewRole }) => {
  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.header}>
        <View>
          <Text style={styles.title}>Super Admin Command Tower</Text>
          <Text style={styles.subtitle}>Abia State Ministry of Transport Operations</Text>
        </View>
        <View style={styles.masterBadge}>
          <Text style={styles.masterBadgeText}>SUPER ADMIN</Text>
        </View>
      </View>

      <ScrollView contentContainerStyle={styles.container}>
        {/* Role Impersonation Switcher */}
        <Text style={styles.sectionTitle}>1-TAP ROLE IMPERSONATION (VIEW AS):</Text>
        <View style={styles.impersonateRow}>
          {(['PASSENGER', 'DRIVER', 'PARK_CAPTAIN'] as UserRole[]).map((r) => (
            <TouchableOpacity
              key={r}
              style={styles.impersonateBtn}
              onPress={() => onSwitchViewRole(r)}
            >
              <Text style={styles.impersonateBtnText}>
                {r === 'DRIVER' ? 'Rider' : r === 'PARK_CAPTAIN' ? 'Captain' : 'Passenger'}
              </Text>
            </TouchableOpacity>
          ))}
        </View>

        {/* High-level Statewide KPIs */}
        <View style={styles.kpiRow}>
          <View style={styles.kpiCard}>
            <Text style={styles.kpiVal}>142</Text>
            <Text style={styles.kpiLabel}>Active Kekes</Text>
          </View>
          <View style={styles.kpiCard}>
            <Text style={[styles.kpiVal, { color: '#00D664' }]}>₦842k</Text>
            <Text style={styles.kpiLabel}>Daily GMV</Text>
          </View>
          <View style={styles.kpiCard}>
            <Text style={[styles.kpiVal, { color: Colors.safetyAmber }]}>0</Text>
            <Text style={styles.kpiLabel}>Open Disputes</Text>
          </View>
        </View>

        {/* Master User & Role Directory */}
        <Text style={styles.sectionTitle}>MASTER USER & FLEET DIRECTORY</Text>
        {REGISTERED_USERS.map((u, i) => (
          <View key={i} style={styles.userCard}>
            <View style={{ flex: 1 }}>
              <Text style={styles.userName}>{u.name}</Text>
              <Text style={[styles.userRole, u.isSuper && { color: Colors.goldMaster }]}>{u.role}</Text>
              <Text style={styles.userContact}>{u.contact}</Text>
            </View>

            {u.contact.startsWith('0') && (
              <TouchableOpacity
                style={styles.callCircle}
                onPress={() => Linking.openURL(`tel:${u.contact}`)}
              >
                <Text style={{ fontSize: 14 }}>📞</Text>
              </TouchableOpacity>
            )}
          </View>
        ))}
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: Colors.asphaltBlack,
  },
  header: {
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
    fontSize: 15,
    fontWeight: '900',
  },
  subtitle: {
    color: Colors.mutedSilver,
    fontSize: 10,
    marginTop: 2,
  },
  masterBadge: {
    backgroundColor: 'rgba(255, 215, 0, 0.2)',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: Colors.goldMaster,
  },
  masterBadgeText: {
    color: Colors.goldMaster,
    fontSize: 9,
    fontWeight: '900',
  },
  container: {
    padding: 16,
  },
  sectionTitle: {
    color: Colors.safetyAmber,
    fontSize: 11,
    fontWeight: 'bold',
    marginBottom: 8,
    marginTop: 8,
  },
  impersonateRow: {
    flexDirection: 'row',
    gap: 8,
    marginBottom: 16,
  },
  impersonateBtn: {
    flex: 1,
    backgroundColor: Colors.asphaltCard,
    paddingVertical: 10,
    borderRadius: 10,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: Colors.safetyAmber,
  },
  impersonateBtnText: {
    color: Colors.safetyAmber,
    fontSize: 11,
    fontWeight: 'bold',
  },
  kpiRow: {
    flexDirection: 'row',
    gap: 8,
    marginBottom: 16,
  },
  kpiCard: {
    flex: 1,
    backgroundColor: Colors.asphaltCard,
    borderRadius: 12,
    padding: 12,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: Colors.asphaltDivider,
  },
  kpiVal: {
    color: '#FFF',
    fontSize: 18,
    fontWeight: '900',
  },
  kpiLabel: {
    color: Colors.mutedSilver,
    fontSize: 9,
    marginTop: 2,
  },
  userCard: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: Colors.asphaltCard,
    borderRadius: 12,
    padding: 12,
    borderWidth: 1,
    borderColor: Colors.asphaltDivider,
    marginBottom: 8,
  },
  userName: {
    color: '#FFF',
    fontSize: 13,
    fontWeight: 'bold',
  },
  userRole: {
    color: Colors.safetyAmber,
    fontSize: 11,
    fontWeight: '600',
    marginTop: 2,
  },
  userContact: {
    color: Colors.mutedSilver,
    fontSize: 10,
    marginTop: 2,
  },
  callCircle: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: 'rgba(0, 200, 83, 0.2)',
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: Colors.emeraldGreen,
  },
});

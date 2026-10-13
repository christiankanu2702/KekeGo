import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  TextInput,
  ScrollView,
  SafeAreaView,
} from 'react-native';
import { Colors } from '../theme/colors';
import { UserRole, UserAccount } from '../types';

interface AuthScreenProps {
  onSignInSuccess: (user: UserAccount) => void;
}

export const AuthScreen: React.FC<AuthScreenProps> = ({ onSignInSuccess }) => {
  const [selectedRole, setSelectedRole] = useState<UserRole>('PASSENGER');
  const [fullName, setFullName] = useState('Christian Kanu');
  const [phone, setPhone] = useState('08031234567');
  const [riderPermitId, setRiderPermitId] = useState('AB/KKE/2026/8941');
  const [plateNo, setPlateNo] = useState('ABA-882-KU');
  const [managedPark, setManagedPark] = useState('Bata Central Terminal Aba');

  const handleDemoSignIn = () => {
    const isOwner = fullName.toLowerCase().includes('christian') || selectedRole === 'ADMIN';
    const user: UserAccount = {
      uid: 'USER_' + Date.now(),
      email: isOwner ? 'christiankanu2702@gmail.com' : 'commuter@kekego.ng',
      displayName: fullName,
      phoneNumber: phone,
      role: isOwner ? 'ADMIN' : selectedRole,
      homeLgaId: 'aba_south',
      walletBalanceNaira: selectedRole === 'DRIVER' ? 5000 : 3500,
      vaultSavingsNaira: 450,
      stateRiderPermitId: riderPermitId,
      tricyclePlateNo: plateNo,
      managedParkName: managedPark,
    };
    onSignInSuccess(user);
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <ScrollView contentContainerStyle={styles.container}>
        {/* Brand Banner */}
        <View style={styles.brandCard}>
          <View style={styles.kekeIconCircle}>
            <Text style={{ fontSize: 30 }}>🛺</Text>
          </View>
          <View style={styles.tagRow}>
            <View style={styles.certifiedTag}>
              <Text style={styles.tagText}>17 LGAs CERTIFIED</Text>
            </View>
            <Text style={styles.subTagText}>ATRWAN & TOAN COMPLIANT</Text>
          </View>
        </View>

        <Text style={styles.appTitle}>KekeGo iOS</Text>
        <Text style={styles.appSubtitle}>Abia State Commercial Tricycle Transit Authority</Text>

        {/* Role Selector Chips */}
        <Text style={styles.sectionLabel}>SELECT OPERATING ROLE AT REGISTRATION:</Text>
        <View style={styles.roleTabs}>
          {(['PASSENGER', 'DRIVER', 'PARK_CAPTAIN', 'ADMIN'] as UserRole[]).map((role) => (
            <TouchableOpacity
              key={role}
              style={[styles.roleTab, selectedRole === role && styles.roleTabSelected]}
              onPress={() => setSelectedRole(role)}
            >
              <Text style={[styles.roleTabText, selectedRole === role && styles.roleTabTextSelected]}>
                {role === 'DRIVER' ? 'Rider' : role === 'PARK_CAPTAIN' ? 'Captain' : role.charAt(0) + role.slice(1).toLowerCase()}
              </Text>
            </TouchableOpacity>
          ))}
        </View>

        {/* Dynamic Compliance Form */}
        <View style={styles.formCard}>
          <Text style={styles.formTitle}>
            {selectedRole === 'DRIVER'
              ? 'Rider Permit & Vehicle Compliance'
              : selectedRole === 'PARK_CAPTAIN'
              ? 'Terminal & Union Assignment'
              : selectedRole === 'ADMIN'
              ? 'Super Admin Governance Credentials'
              : 'Passenger Onboarding Details'}
          </Text>

          <TextInput
            style={styles.input}
            placeholder="Full Name / Alias"
            placeholderTextColor={Colors.mutedSilver}
            value={fullName}
            onChangeText={setFullName}
          />
          <TextInput
            style={styles.input}
            placeholder="Phone Number (e.g. 08031234567)"
            placeholderTextColor={Colors.mutedSilver}
            value={phone}
            onChangeText={setPhone}
          />

          {selectedRole === 'DRIVER' && (
            <>
              <TextInput
                style={styles.input}
                placeholder="Abia State Permit ID (AB/KKE/...)"
                placeholderTextColor={Colors.mutedSilver}
                value={riderPermitId}
                onChangeText={setRiderPermitId}
              />
              <TextInput
                style={styles.input}
                placeholder="Tricycle Plate (e.g. ABA-882-KU)"
                placeholderTextColor={Colors.mutedSilver}
                value={plateNo}
                onChangeText={setPlateNo}
              />
            </>
          )}

          {selectedRole === 'PARK_CAPTAIN' && (
            <TextInput
              style={styles.input}
              placeholder="Assigned Transit Park Name"
              placeholderTextColor={Colors.mutedSilver}
              value={managedPark}
              onChangeText={setManagedPark}
            />
          )}
        </View>

        {/* Instant Demo Launch Button */}
        <TouchableOpacity style={styles.primaryBtn} onPress={handleDemoSignIn}>
          <Text style={styles.primaryBtnText}>
            Launch KekeGo ({selectedRole})
          </Text>
        </TouchableOpacity>
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: Colors.asphaltBlack,
  },
  container: {
    padding: 20,
    alignItems: 'center',
  },
  brandCard: {
    width: '100%',
    backgroundColor: Colors.asphaltCard,
    borderRadius: 18,
    padding: 16,
    alignItems: 'center',
    borderWidth: 1.5,
    borderColor: 'rgba(255, 179, 0, 0.6)',
    marginBottom: 16,
  },
  kekeIconCircle: {
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: Colors.safetyAmber,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 10,
  },
  tagRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  certifiedTag: {
    backgroundColor: Colors.emeraldGreenDark,
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 4,
  },
  tagText: {
    color: '#FFF',
    fontSize: 9,
    fontWeight: 'bold',
  },
  subTagText: {
    color: Colors.safetyAmber,
    fontSize: 10,
    fontWeight: 'bold',
  },
  appTitle: {
    color: Colors.safetyAmber,
    fontSize: 26,
    fontWeight: '900',
  },
  appSubtitle: {
    color: Colors.mutedSilver,
    fontSize: 11,
    textAlign: 'center',
    marginBottom: 16,
  },
  sectionLabel: {
    alignSelf: 'flex-start',
    color: Colors.safetyAmber,
    fontSize: 10,
    fontWeight: 'bold',
    marginBottom: 6,
  },
  roleTabs: {
    flexDirection: 'row',
    width: '100%',
    gap: 6,
    marginBottom: 14,
  },
  roleTab: {
    flex: 1,
    backgroundColor: Colors.asphaltCard,
    paddingVertical: 10,
    borderRadius: 10,
    alignItems: 'center',
  },
  roleTabSelected: {
    backgroundColor: Colors.safetyAmber,
  },
  roleTabText: {
    color: '#FFF',
    fontSize: 11,
    fontWeight: 'bold',
  },
  roleTabTextSelected: {
    color: Colors.asphaltDark,
  },
  formCard: {
    width: '100%',
    backgroundColor: Colors.asphaltCard,
    borderRadius: 14,
    padding: 14,
    borderWidth: 1,
    borderColor: Colors.asphaltDivider,
    gap: 10,
    marginBottom: 18,
  },
  formTitle: {
    color: '#FFF',
    fontSize: 13,
    fontWeight: 'bold',
    marginBottom: 4,
  },
  input: {
    backgroundColor: Colors.asphaltSurface,
    borderRadius: 10,
    paddingHorizontal: 12,
    paddingVertical: 10,
    color: '#FFF',
    fontSize: 13,
    borderWidth: 1,
    borderColor: Colors.asphaltDivider,
  },
  primaryBtn: {
    width: '100%',
    backgroundColor: Colors.safetyAmber,
    paddingVertical: 14,
    borderRadius: 12,
    alignItems: 'center',
  },
  primaryBtnText: {
    color: Colors.asphaltDark,
    fontSize: 14,
    fontWeight: '900',
  },
});

import React, { useState } from 'react';
import { View, StyleSheet, StatusBar } from 'react-native';
import { Colors } from './src/theme/colors';
import { UserAccount, UserRole } from './src/types';
import { AuthScreen } from './src/screens/AuthScreen';
import { PassengerScreen } from './src/screens/PassengerScreen';
import { DriverScreen } from './src/screens/DriverScreen';
import { AdminScreen } from './src/screens/AdminScreen';

export default function App() {
  const [currentUser, setCurrentUser] = useState<UserAccount | null>(null);
  const [activeViewRole, setActiveViewRole] = useState<UserRole>('PASSENGER');

  const handleSignIn = (user: UserAccount) => {
    setCurrentUser(user);
    setActiveViewRole(user.role);
  };

  if (!currentUser) {
    return (
      <View style={styles.container}>
        <StatusBar barStyle="light-content" backgroundColor={Colors.asphaltBlack} />
        <AuthScreen onSignInSuccess={handleSignIn} />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor={Colors.asphaltBlack} />
      {activeViewRole === 'ADMIN' && (
        <AdminScreen user={currentUser} onSwitchViewRole={setActiveViewRole} />
      )}
      {activeViewRole === 'DRIVER' && (
        <DriverScreen user={currentUser} />
      )}
      {activeViewRole === 'PASSENGER' && (
        <PassengerScreen user={currentUser} />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.asphaltBlack,
  },
});

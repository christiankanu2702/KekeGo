import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView } from 'react-native';
import { Colors } from '../theme/colors';

interface BoltSavedPlacesRowProps {
  onSelectPlace: (marketName: string, lgaId: string) => void;
}

const SHORTCUTS = [
  { label: 'Home', marketName: 'Bata Junction Aba', lgaId: 'aba_south', icon: '🏠' },
  { label: 'Work', marketName: 'Factory Road Industrial Layout', lgaId: 'aba_north', icon: '💼' },
  { label: 'Ariaria Market', marketName: 'Ariaria International Market', lgaId: 'aba_north', icon: '🛍️' },
  { label: 'Ekeoha Plaza', marketName: 'Ekeoha Shopping Centre', lgaId: 'aba_south', icon: '🏪' },
  { label: 'Umuahia Terminal', marketName: 'Isi Gate Central Market', lgaId: 'umuahia_north', icon: '🚌' },
];

export const BoltSavedPlacesRow: React.FC<BoltSavedPlacesRowProps> = ({ onSelectPlace }) => {
  return (
    <View style={styles.container}>
      <Text style={styles.headerLabel}>SAVED PLACES SHORTCUTS</Text>
      <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.scrollContent}>
        {SHORTCUTS.map((place) => (
          <TouchableOpacity
            key={place.label}
            style={styles.pill}
            onPress={() => onSelectPlace(place.marketName, place.lgaId)}
          >
            <Text style={styles.icon}>{place.icon}</Text>
            <View>
              <Text style={styles.title}>{place.label}</Text>
              <Text style={styles.subtitle}>{place.marketName.slice(0, 14)}</Text>
            </View>
          </TouchableOpacity>
        ))}
      </ScrollView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    marginVertical: 6,
  },
  headerLabel: {
    color: Colors.safetyAmber,
    fontSize: 10,
    fontWeight: 'bold',
    marginBottom: 6,
  },
  scrollContent: {
    gap: 8,
  },
  pill: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: Colors.asphaltCard,
    borderRadius: 12,
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderWidth: 1,
    borderColor: Colors.asphaltDivider,
    gap: 8,
  },
  icon: {
    fontSize: 14,
  },
  title: {
    color: Colors.highContrastWhite,
    fontSize: 11,
    fontWeight: 'bold',
  },
  subtitle: {
    color: Colors.mutedSilver,
    fontSize: 9,
  },
});

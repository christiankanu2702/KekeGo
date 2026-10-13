import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Colors } from '../theme/colors';
import { BoltRideTier, RideTierOption } from '../types';

interface BoltRideTierSelectorProps {
  baseFareNaira: number;
  selectedTier: BoltRideTier;
  onTierSelected: (tier: BoltRideTier) => void;
}

const TIERS: RideTierOption[] = [
  { key: 'STANDARD', title: 'Keke Standard', subtitle: 'Shared passenger', multiplier: 1.0, capacitySeats: 3, etaMins: 2 },
  { key: 'CHARTER', title: 'Express Charter', subtitle: 'Private direct ride', multiplier: 1.8, capacitySeats: 3, etaMins: 1 },
  { key: 'CARGO', title: 'Cargo & Freight', subtitle: 'Sacks & market cargo', multiplier: 1.5, capacitySeats: 1, etaMins: 4 },
];

export const BoltRideTierSelector: React.FC<BoltRideTierSelectorProps> = ({
  baseFareNaira,
  selectedTier,
  onTierSelected,
}) => {
  return (
    <View style={styles.container}>
      <Text style={styles.headerLabel}>SELECT RIDE CATEGORY</Text>
      <View style={styles.tierRow}>
        {TIERS.map((tier) => {
          const isSelected = tier.key === selectedTier;
          const fare = Math.round(baseFareNaira * tier.multiplier);

          return (
            <TouchableOpacity
              key={tier.key}
              style={[
                styles.tierCard,
                isSelected && styles.tierCardSelected,
              ]}
              onPress={() => onTierSelected(tier.key)}
            >
              <View style={styles.tierTop}>
                <Text style={styles.tierIcon}>
                  {tier.key === 'STANDARD' ? '🛺' : tier.key === 'CHARTER' ? '⚡' : '📦'}
                </Text>
                <Text style={styles.seatsText}>👤 {tier.capacitySeats}</Text>
              </View>

              <Text style={styles.tierTitle} numberOfLines={1}>{tier.title}</Text>
              <Text style={styles.etaText}>{tier.etaMins} mins</Text>
              <Text style={[styles.fareText, isSelected && styles.fareTextSelected]}>₦{fare}</Text>
            </TouchableOpacity>
          );
        })}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    marginVertical: 8,
  },
  headerLabel: {
    color: Colors.safetyAmber,
    fontSize: 10,
    fontWeight: 'bold',
    marginBottom: 6,
  },
  tierRow: {
    flexDirection: 'row',
    gap: 8,
  },
  tierCard: {
    flex: 1,
    backgroundColor: Colors.asphaltCard,
    borderRadius: 14,
    padding: 10,
    borderWidth: 1.5,
    borderColor: Colors.asphaltDivider,
  },
  tierCardSelected: {
    backgroundColor: '#1E2838',
    borderColor: '#00D664',
  },
  tierTop: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 4,
  },
  tierIcon: {
    fontSize: 16,
  },
  seatsText: {
    color: Colors.mutedSilver,
    fontSize: 9,
    fontWeight: 'bold',
  },
  tierTitle: {
    color: Colors.highContrastWhite,
    fontSize: 11,
    fontWeight: 'bold',
  },
  etaText: {
    color: '#00D664',
    fontSize: 9,
    fontWeight: '600',
    marginTop: 2,
  },
  fareText: {
    color: Colors.safetyAmber,
    fontSize: 13,
    fontWeight: '900',
    marginTop: 4,
  },
  fareTextSelected: {
    color: '#00D664',
  },
});

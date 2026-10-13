import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  Modal,
  TouchableOpacity,
  TextInput,
  ScrollView,
} from 'react-native';
import { Colors } from '../theme/colors';

interface KekeRatingModalProps {
  visible: boolean;
  driverName?: string;
  plateNo?: string;
  onSubmit: (stars: number, compliments: string[], comment: string, tip: number) => void;
  onDismiss: () => void;
}

const COMPLIMENT_TAGS = [
  'Clean Keke',
  'Safe Driving',
  'Polite Rider',
  'Punctual Arrival',
  'Fair Corridor Price',
  'Helpful with Cargo',
  'Good Music',
];

const TIP_OPTIONS = [0, 100, 200, 500];

export const KekeRatingModal: React.FC<KekeRatingModalProps> = ({
  visible,
  driverName = 'Kalu Driver',
  plateNo = 'ABA-882-KU',
  onSubmit,
  onDismiss,
}) => {
  const [stars, setStars] = useState(5);
  const [selectedTags, setSelectedTags] = useState<string[]>(['Safe Driving', 'Polite Rider']);
  const [selectedTip, setSelectedTip] = useState(0);
  const [comment, setComment] = useState('');

  const toggleTag = (tag: string) => {
    setSelectedTags((prev) =>
      prev.includes(tag) ? prev.filter((t) => t !== tag) : [...prev, tag]
    );
  };

  return (
    <Modal visible={visible} transparent animationType="fade">
      <View style={styles.overlay}>
        <View style={styles.modalCard}>
          {/* Header Icon & Title */}
          <View style={styles.header}>
            <View style={styles.iconCircle}>
              <Text style={{ fontSize: 24 }}>🛺</Text>
            </View>
            <Text style={styles.title}>How was your ride?</Text>
            <Text style={styles.subtitle}>{driverName} • {plateNo}</Text>
          </View>

          {/* Star Rating Row */}
          <View style={styles.starsRow}>
            {[1, 2, 3, 4, 5].map((starVal) => (
              <TouchableOpacity key={starVal} onPress={() => setStars(starVal)}>
                <Text style={[styles.starIcon, starVal <= stars && styles.starIconActive]}>
                  ★
                </Text>
              </TouchableOpacity>
            ))}
          </View>
          <Text style={styles.starRemark}>
            {stars === 5 ? 'Excellent trip!' : stars >= 4 ? 'Good ride' : stars >= 3 ? 'Average ride' : 'Could be better'}
          </Text>

          {/* Compliment Tags */}
          <Text style={styles.sectionLabel}>Select Compliments:</Text>
          <View style={styles.tagsWrap}>
            {COMPLIMENT_TAGS.map((tag) => {
              const isSelected = selectedTags.includes(tag);
              return (
                <TouchableOpacity
                  key={tag}
                  style={[styles.tagPill, isSelected && styles.tagPillSelected]}
                  onPress={() => toggleTag(tag)}
                >
                  <Text style={[styles.tagText, isSelected && styles.tagTextSelected]}>
                    {tag}
                  </Text>
                </TouchableOpacity>
              );
            })}
          </View>

          {/* Driver Tip Options */}
          <Text style={styles.sectionLabel}>Add Tip for Rider (Optional):</Text>
          <View style={styles.tipRow}>
            {TIP_OPTIONS.map((tipAmount) => {
              const isSelected = selectedTip === tipAmount;
              return (
                <TouchableOpacity
                  key={tipAmount}
                  style={[styles.tipBtn, isSelected && styles.tipBtnSelected]}
                  onPress={() => setSelectedTip(tipAmount)}
                >
                  <Text style={[styles.tipBtnText, isSelected && styles.tipBtnTextSelected]}>
                    {tipAmount === 0 ? 'No Tip' : `₦${tipAmount}`}
                  </Text>
                </TouchableOpacity>
              );
            })}
          </View>

          {/* Optional Review Comment */}
          <TextInput
            style={styles.input}
            placeholder="Write feedback for the rider..."
            placeholderTextColor={Colors.mutedSilver}
            value={comment}
            onChangeText={setComment}
          />

          {/* Buttons */}
          <TouchableOpacity
            style={styles.submitBtn}
            onPress={() => onSubmit(stars, selectedTags, comment, selectedTip)}
          >
            <Text style={styles.submitBtnText}>Submit Review & Tip</Text>
          </TouchableOpacity>

          <TouchableOpacity onPress={onDismiss} style={styles.skipBtn}>
            <Text style={styles.skipBtnText}>Skip for Now</Text>
          </TouchableOpacity>
        </View>
      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.75)',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  modalCard: {
    width: '100%',
    backgroundColor: Colors.asphaltDark,
    borderRadius: 22,
    padding: 20,
    borderWidth: 1.5,
    borderColor: 'rgba(255, 179, 0, 0.4)',
    alignItems: 'center',
  },
  header: {
    alignItems: 'center',
    marginBottom: 10,
  },
  iconCircle: {
    width: 52,
    height: 52,
    borderRadius: 26,
    backgroundColor: 'rgba(255, 179, 0, 0.2)',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 8,
  },
  title: {
    color: '#FFF',
    fontSize: 18,
    fontWeight: '900',
  },
  subtitle: {
    color: Colors.mutedSilver,
    fontSize: 11,
    marginTop: 2,
  },
  starsRow: {
    flexDirection: 'row',
    gap: 8,
    marginVertical: 6,
  },
  starIcon: {
    fontSize: 32,
    color: Colors.asphaltDivider,
  },
  starIconActive: {
    color: Colors.safetyAmber,
  },
  starRemark: {
    color: Colors.safetyAmber,
    fontSize: 12,
    fontWeight: 'bold',
    marginBottom: 12,
  },
  sectionLabel: {
    alignSelf: 'flex-start',
    color: Colors.mutedSilver,
    fontSize: 11,
    fontWeight: 'bold',
    marginBottom: 6,
  },
  tagsWrap: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 6,
    marginBottom: 14,
  },
  tagPill: {
    backgroundColor: Colors.asphaltCard,
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: Colors.asphaltDivider,
  },
  tagPillSelected: {
    backgroundColor: Colors.safetyAmber,
    borderColor: Colors.safetyAmber,
  },
  tagText: {
    color: '#FFF',
    fontSize: 10,
    fontWeight: '600',
  },
  tagTextSelected: {
    color: Colors.asphaltDark,
    fontWeight: 'bold',
  },
  tipRow: {
    flexDirection: 'row',
    width: '100%',
    gap: 6,
    marginBottom: 14,
  },
  tipBtn: {
    flex: 1,
    backgroundColor: Colors.asphaltCard,
    paddingVertical: 8,
    borderRadius: 8,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: Colors.asphaltDivider,
  },
  tipBtnSelected: {
    backgroundColor: '#00D664',
    borderColor: '#00D664',
  },
  tipBtnText: {
    color: '#FFF',
    fontSize: 11,
    fontWeight: 'bold',
  },
  tipBtnTextSelected: {
    color: Colors.asphaltDark,
  },
  input: {
    width: '100%',
    backgroundColor: Colors.asphaltCard,
    borderRadius: 10,
    paddingHorizontal: 12,
    paddingVertical: 8,
    color: '#FFF',
    borderWidth: 1,
    borderColor: Colors.asphaltDivider,
    fontSize: 12,
    marginBottom: 14,
  },
  submitBtn: {
    width: '100%',
    backgroundColor: Colors.safetyAmber,
    paddingVertical: 12,
    borderRadius: 10,
    alignItems: 'center',
    marginBottom: 8,
  },
  submitBtnText: {
    color: Colors.asphaltDark,
    fontSize: 13,
    fontWeight: '900',
  },
  skipBtn: {
    paddingVertical: 4,
  },
  skipBtnText: {
    color: Colors.mutedSilver,
    fontSize: 11,
  },
});

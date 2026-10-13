import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  Modal,
  TouchableOpacity,
  TextInput,
  ScrollView,
  Linking,
} from 'react-native';
import { Colors } from '../theme/colors';
import { TripChatMessage } from '../types';

interface BoltChatModalProps {
  visible: boolean;
  onClose: () => void;
  driverName?: string;
  driverPhone?: string;
  plateNo?: string;
  messages: TripChatMessage[];
  onSendMessage: (text: string) => void;
}

const QUICK_CHIPS = [
  "I'm here waiting",
  'Where are you?',
  'On my way now',
  'Traffic at Bata Junction',
  'Please wait 2 mins',
];

export const BoltChatModal: React.FC<BoltChatModalProps> = ({
  visible,
  onClose,
  driverName = 'Kalu Driver',
  driverPhone = '08039821033',
  plateNo = 'ABA-882-KU',
  messages,
  onSendMessage,
}) => {
  const [inputText, setInputText] = useState('');

  const handleSend = () => {
    if (inputText.trim()) {
      onSendMessage(inputText.trim());
      setInputText('');
    }
  };

  return (
    <Modal visible={visible} transparent animationType="slide">
      <View style={styles.overlay}>
        <View style={styles.modalContent}>
          {/* Header */}
          <View style={styles.header}>
            <View>
              <Text style={styles.driverTitle}>{driverName}</Text>
              <Text style={styles.plateSubtitle}>Assigned Rider • {plateNo}</Text>
            </View>

            <View style={{ flexDirection: 'row', gap: 8, alignItems: 'center' }}>
              <TouchableOpacity
                style={styles.callCircle}
                onPress={() => Linking.openURL(`tel:${driverPhone}`)}
              >
                <Text style={{ fontSize: 14 }}>📞</Text>
              </TouchableOpacity>
              <TouchableOpacity onPress={onClose} style={styles.closeBtn}>
                <Text style={{ color: '#FFF', fontSize: 16 }}>✕</Text>
              </TouchableOpacity>
            </View>
          </View>

          {/* Messages Feed */}
          <ScrollView style={styles.messagesList} contentContainerStyle={{ gap: 8 }}>
            {messages.map((msg) => {
              const isMe = msg.senderRole === 'PASSENGER';
              return (
                <View
                  key={msg.id}
                  style={[styles.bubble, isMe ? styles.bubbleMe : styles.bubbleDriver]}
                >
                  <Text style={[styles.bubbleText, isMe && styles.bubbleTextMe]}>
                    {msg.messageText}
                  </Text>
                </View>
              );
            })}
          </ScrollView>

          {/* Quick Reply Chips */}
          <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.chipsRow}>
            {QUICK_CHIPS.map((chip) => (
              <TouchableOpacity
                key={chip}
                style={styles.chip}
                onPress={() => onSendMessage(chip)}
              >
                <Text style={styles.chipText}>{chip}</Text>
              </TouchableOpacity>
            ))}
          </ScrollView>

          {/* Input Bar */}
          <View style={styles.inputBar}>
            <TextInput
              style={styles.textInput}
              placeholder="Message rider..."
              placeholderTextColor={Colors.mutedSilver}
              value={inputText}
              onChangeText={setInputText}
            />
            <TouchableOpacity style={styles.sendBtn} onPress={handleSend}>
              <Text style={styles.sendText}>➤</Text>
            </TouchableOpacity>
          </View>
        </View>
      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.7)',
    justifyContent: 'flex-end',
  },
  modalContent: {
    backgroundColor: Colors.asphaltDark,
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    height: '75%',
    padding: 16,
    borderWidth: 1,
    borderColor: 'rgba(255, 179, 0, 0.4)',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingBottom: 12,
    borderBottomWidth: 1,
    borderBottomColor: Colors.asphaltDivider,
  },
  driverTitle: {
    color: '#FFF',
    fontSize: 16,
    fontWeight: 'bold',
  },
  plateSubtitle: {
    color: Colors.emeraldGreen,
    fontSize: 11,
    marginTop: 2,
  },
  callCircle: {
    width: 34,
    height: 34,
    borderRadius: 17,
    backgroundColor: 'rgba(0, 200, 83, 0.2)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  closeBtn: {
    padding: 6,
  },
  messagesList: {
    flex: 1,
    marginVertical: 10,
  },
  bubble: {
    padding: 10,
    borderRadius: 12,
    maxWidth: '75%',
  },
  bubbleMe: {
    backgroundColor: Colors.safetyAmber,
    alignSelf: 'flex-end',
  },
  bubbleDriver: {
    backgroundColor: Colors.asphaltSurface,
    alignSelf: 'flex-start',
    borderWidth: 1,
    borderColor: Colors.asphaltDivider,
  },
  bubbleText: {
    color: '#FFF',
    fontSize: 13,
  },
  bubbleTextMe: {
    color: Colors.asphaltDark,
    fontWeight: '600',
  },
  chipsRow: {
    maxHeight: 36,
    marginBottom: 8,
  },
  chip: {
    backgroundColor: Colors.asphaltCard,
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 14,
    marginRight: 6,
    borderWidth: 1,
    borderColor: Colors.asphaltDivider,
  },
  chipText: {
    color: '#FFF',
    fontSize: 11,
    fontWeight: '600',
  },
  inputBar: {
    flexDirection: 'row',
    gap: 8,
  },
  textInput: {
    flex: 1,
    backgroundColor: Colors.asphaltCard,
    borderRadius: 20,
    paddingHorizontal: 14,
    paddingVertical: 8,
    color: '#FFF',
    borderWidth: 1,
    borderColor: Colors.asphaltDivider,
    fontSize: 13,
  },
  sendBtn: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: Colors.safetyAmber,
    justifyContent: 'center',
    alignItems: 'center',
  },
  sendText: {
    color: Colors.asphaltDark,
    fontSize: 16,
    fontWeight: 'bold',
  },
});

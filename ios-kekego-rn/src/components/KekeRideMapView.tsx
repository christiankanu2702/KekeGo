import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Dimensions } from 'react-native';
import Svg, { Rect, Line, Circle, Path, Defs, RadialGradient, Stop } from 'react-native-svg';
import { Colors } from '../theme/colors';

interface KekeRideMapViewProps {
  originMarket: string;
  destinationMarket: string;
}

export const KekeRideMapView: React.FC<KekeRideMapViewProps> = ({
  originMarket,
  destinationMarket,
}) => {
  const [scale, setScale] = useState(1.0);
  const [kekeProgress, setKekeProgress] = useState(0.0);

  // Animated continuous tricycle traffic flow along the corridor
  useEffect(() => {
    const interval = setInterval(() => {
      setKekeProgress((prev) => (prev >= 1.0 ? 0.0 : prev + 0.02));
    }, 150);
    return () => clearInterval(interval);
  }, []);

  const width = Dimensions.get('window').width - 32;
  const height = 280;

  // Key node points
  const pOrigin = { x: 50, y: 190 };
  const pIntermediate = { x: width * 0.45, y: 140 };
  const pDest = { x: width - 60, y: 70 };

  // Live moving keke coordinate
  const currentKekeX = pOrigin.x + (pDest.x - pOrigin.x) * kekeProgress;
  const currentKekeY = pOrigin.y + (pDest.y - pOrigin.y) * kekeProgress;

  return (
    <View style={styles.container}>
      <Svg width={width} height={height} viewBox={`0 0 ${width} ${height}`}>
        <Defs>
          <RadialGradient id="lampGlow" cx="50%" cy="50%" r="50%">
            <Stop offset="0%" stopColor="#FFE082" stopOpacity="0.45" />
            <Stop offset="100%" stopColor="#FFE082" stopOpacity="0" />
          </RadialGradient>
        </Defs>

        {/* 1. Deep Midnight Asphalt Ground */}
        <Rect x="0" y="0" width={width} height={height} fill="#13171F" />

        {/* 2. Urban Blocks & Green Parks */}
        <Rect x="15" y="20" width="100" height="70" rx="12" fill="#181D26" />
        <Rect x={width - 120} y="150" width="105" height="85" rx="12" fill="#181D26" />
        <Rect x="130" y="180" width="85" height="60" rx="14" fill="#10281E" />

        {/* 3. Secondary Street Network Grid */}
        <Line x1="0" y1="95" x2={width} y2="95" stroke="#1F2633" strokeWidth="2" />
        <Line x1="0" y1="185" x2={width} y2="185" stroke="#1F2633" strokeWidth="2" />
        <Line x1={width * 0.3} y1="0" x2={width * 0.3} y2={height} stroke="#1F2633" strokeWidth="2" />
        <Line x1={width * 0.7} y1="0" x2={width * 0.7} y2={height} stroke="#1F2633" strokeWidth="2" />

        {/* 4. Dual-Lane Boulevard (Faulks / Aba-Owerri Expressway) */}
        <Line x1="0" y1="135" x2={width} y2="135" stroke="#0C0E14" strokeWidth="24" />
        <Line x1="0" y1="135" x2={width} y2="135" stroke="#222936" strokeWidth="18" />
        <Line x1="0" y1="135" x2={width} y2="135" stroke="#FFD54F" strokeWidth="1.8" strokeDasharray="12,10" />

        {/* 5. Aba Waterside River Promenade */}
        <Path
          d={`M 0 240 Q ${width * 0.4} 260 ${width * 0.7} 210 T ${width} 220`}
          stroke="#122E42"
          strokeWidth="16"
          fill="none"
        />

        {/* 6. Keke Glowing Emerald Transit Corridor */}
        {/* Glow halo */}
        <Path
          d={`M ${pOrigin.x} ${pOrigin.y} Q ${pIntermediate.x} ${pIntermediate.y} ${pDest.x} ${pDest.y}`}
          stroke="#00E676"
          strokeWidth="20"
          strokeOpacity="0.25"
          fill="none"
          strokeLinecap="round"
        />
        {/* Route border */}
        <Path
          d={`M ${pOrigin.x} ${pOrigin.y} Q ${pIntermediate.x} ${pIntermediate.y} ${pDest.x} ${pDest.y}`}
          stroke="#003816"
          strokeWidth="10"
          fill="none"
          strokeLinecap="round"
        />
        {/* Keke Core Line */}
        <Path
          d={`M ${pOrigin.x} ${pOrigin.y} Q ${pIntermediate.x} ${pIntermediate.y} ${pDest.x} ${pDest.y}`}
          stroke="#00D664"
          strokeWidth="6"
          fill="none"
          strokeLinecap="round"
        />

        {/* 7. Live Moving Keke with Headlight Beam */}
        <Circle cx={currentKekeX} cy={currentKekeY} r="10" fill={Colors.safetyAmber} />
        <Circle cx={currentKekeX} cy={currentKekeY} r="5.5" fill="#1E222A" />
        {/* Headlight cone */}
        <Path
          d={`M ${currentKekeX + 8} ${currentKekeY - 2} L ${currentKekeX + 24} ${currentKekeY - 10} L ${currentKekeX + 24} ${currentKekeY + 8} Z`}
          fill="#FFEE58"
          fillOpacity="0.32"
        />

        {/* 8. Pickup Marker (Keke Signature Green) */}
        <Circle cx={pOrigin.x} cy={pOrigin.y} r="22" fill="#00E676" fillOpacity="0.25" />
        <Circle cx={pOrigin.x} cy={pOrigin.y} r="9" fill="#FFFFFF" />
        <Circle cx={pOrigin.x} cy={pOrigin.y} r="6.5" fill="#00D664" />

        {/* 9. Destination Marker (Dark Chrome) */}
        <Circle cx={pDest.x} cy={pDest.y} r="18" fill="#000000" fillOpacity="0.35" />
        <Circle cx={pDest.x} cy={pDest.y} r="9" fill="#161A22" />
        <Circle cx={pDest.x} cy={pDest.y} r="4.5" fill="#FFFFFF" />
      </Svg>

      {/* Floating Status Badges */}
      <View style={styles.topHeader}>
        <View style={styles.statusDot} />
        <Text style={styles.topHeaderText}>KEKEGO TRANSIT RADAR • ABA URBAN (iOS)</Text>
      </View>

      <View style={styles.zoomControls}>
        <TouchableOpacity style={styles.zoomBtn} onPress={() => setScale((s) => Math.min(s + 0.2, 2.5))}>
          <Text style={styles.zoomText}>+</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.zoomBtn} onPress={() => setScale((s) => Math.max(s - 0.2, 0.7))}>
          <Text style={styles.zoomText}>−</Text>
        </TouchableOpacity>
      </View>

      <View style={styles.bottomCorridorCard}>
        <Text style={styles.corridorTitle}>
          {originMarket.slice(0, 16)} ➔ {destinationMarket.slice(0, 16)}
        </Text>
        <Text style={styles.corridorSubtitle}>3 Kekes Active • 2-4 mins ETA</Text>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    borderRadius: 22,
    overflow: 'hidden',
    borderWidth: 1.2,
    borderColor: '#2B3342',
    backgroundColor: '#13171F',
    position: 'relative',
    marginVertical: 10,
  },
  topHeader: {
    position: 'absolute',
    top: 12,
    left: 12,
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: 'rgba(20, 25, 36, 0.94)',
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: '#2E384D',
  },
  statusDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: '#00D664',
    marginRight: 6,
  },
  topHeaderText: {
    color: Colors.highContrastWhite,
    fontSize: 9,
    fontWeight: '900',
  },
  zoomControls: {
    position: 'absolute',
    top: 12,
    right: 12,
    gap: 6,
  },
  zoomBtn: {
    width: 34,
    height: 34,
    borderRadius: 17,
    backgroundColor: 'rgba(26, 33, 46, 0.95)',
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#2E384D',
  },
  zoomText: {
    color: '#FFF',
    fontSize: 16,
    fontWeight: 'bold',
  },
  bottomCorridorCard: {
    position: 'absolute',
    bottom: 12,
    left: 12,
    backgroundColor: 'rgba(20, 25, 36, 0.95)',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#2E384D',
  },
  corridorTitle: {
    color: '#FFF',
    fontSize: 11,
    fontWeight: 'bold',
  },
  corridorSubtitle: {
    color: '#00D664',
    fontSize: 9,
    fontWeight: '600',
  },
});

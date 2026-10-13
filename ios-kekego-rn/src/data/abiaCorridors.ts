export interface MarketLocation {
  name: string;
  lgaId: string;
  latitude: number;
  longitude: number;
}

export const ABIA_MARKETS: MarketLocation[] = [
  { name: 'Bata Junction Aba', lgaId: 'aba_south', latitude: 5.1118, longitude: 7.3689 },
  { name: 'Ariaria International Market', lgaId: 'aba_north', latitude: 5.1325, longitude: 7.3382 },
  { name: 'Ekeoha Shopping Centre', lgaId: 'aba_south', latitude: 5.1082, longitude: 7.3721 },
  { name: 'Factory Road Industrial Layout', lgaId: 'aba_north', latitude: 5.1220, longitude: 7.3760 },
  { name: 'Ahia Ohuru (New Market)', lgaId: 'aba_south', latitude: 5.1021, longitude: 7.3620 },
  { name: 'Cemetery Market Aba', lgaId: 'aba_north', latitude: 5.1190, longitude: 7.3645 },
  { name: 'Osisioma Flyover Junction', lgaId: 'osisioma', latitude: 5.1582, longitude: 7.3290 },
  { name: 'Isi Gate Central Market', lgaId: 'umuahia_north', latitude: 5.5265, longitude: 7.4895 },
  { name: 'Orie Ugba Market', lgaId: 'umuahia_north', latitude: 5.5340, longitude: 7.4980 },
  { name: 'Ubani Ultra Modern Market', lgaId: 'umuahia_north', latitude: 5.5680, longitude: 7.4620 },
];

export function resolveMarketCoordinates(marketName: string): { latitude: number; longitude: number } {
  const found = ABIA_MARKETS.find((m) => m.name.toLowerCase() === marketName.toLowerCase());
  if (found) return { latitude: found.latitude, longitude: found.longitude };
  return { latitude: 5.1118, longitude: 7.3689 };
}

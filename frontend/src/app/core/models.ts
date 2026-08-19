export type Gender = 'MALE' | 'FEMALE';

export const GENDERS: readonly { id: Gender; label: string }[] = [
  { id: 'MALE', label: 'Männlich' },
  { id: 'FEMALE', label: 'Weiblich' },
];

export interface Competitor {
  id: number;
  name: string;
  gender: Gender;
}

export interface Boulder {
  id: number;
  number: number;
}

export interface Ascent {
  boulderNumber: number;
  flashed: boolean;
}

export interface RankingEntry {
  rank: number;
  name: string;
  points: number;
}

export interface BoulderPoints {
  boulderNumber: number;
  points: number;
}

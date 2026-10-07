export type LightIntensityLevel = 30 | 60 | 100;

export type ConcertProgramType = 'wave' | 'strobe' | 'supernova' | 'aurora';

export interface ConcertProgramInfo {
  id: ConcertProgramType;
  name: string;
  bpm: number;
  description: string;
  iconName: string;
}

export interface AndroidFileEntry {
  path: string;
  filename: string;
  language: string;
  content: string;
  description: string;
}

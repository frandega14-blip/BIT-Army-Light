import React, { useState } from 'react';
import { Waves, Zap, Sparkles, Orbit, Music } from 'lucide-react';
import { ConcertProgramType } from '../types/light';
import { triggerHaptic, hapticPatterns } from '../utils/haptics';

interface ConcertChoreoSelectorProps {
  currentProgram: ConcertProgramType;
  onSelectProgram: (prog: ConcertProgramType) => void;
  bpm: number;
  onBpmChange: (bpm: number) => void;
  isVibrationEnabled: boolean;
}

export const ConcertChoreoSelector: React.FC<ConcertChoreoSelectorProps> = ({
  currentProgram,
  onSelectProgram,
  bpm,
  onBpmChange,
  isVibrationEnabled,
}) => {
  const [tapTimes, setTapTimes] = useState<number[]>([]);
  const [tapFeedback, setTapFeedback] = useState(false);

  const programs: Array<{
    id: ConcertProgramType;
    name: string;
    icon: React.ReactNode;
    desc: string;
  }> = [
    {
      id: 'wave',
      name: 'Onda Púrpura',
      icon: <Waves className="w-3.5 h-3.5" />,
      desc: 'Flujo rítmico continuo'
    },
    {
      id: 'strobe',
      name: 'Strobe Beat',
      icon: <Zap className="w-3.5 h-3.5" />,
      desc: 'Destellos dinámicos'
    },
    {
      id: 'supernova',
      name: 'Supernova',
      icon: <Sparkles className="w-3.5 h-3.5" />,
      desc: 'Explosión de luz'
    },
    {
      id: 'aurora',
      name: 'Aurora',
      icon: <Orbit className="w-3.5 h-3.5" />,
      desc: 'Transición mística'
    },
  ];

  const handleTapTempo = (e: React.MouseEvent) => {
    e.stopPropagation();
    const now = Date.now();
    if (isVibrationEnabled) triggerHaptic(hapticPatterns.click);

    setTapFeedback(true);
    setTimeout(() => setTapFeedback(false), 120);

    const newTimes = [...tapTimes.slice(-4), now];
    setTapTimes(newTimes);

    if (newTimes.length >= 2) {
      const intervals = [];
      for (let i = 1; i < newTimes.length; i++) {
        intervals.push(newTimes[i] - newTimes[i - 1]);
      }
      const avgInterval = intervals.reduce((a, b) => a + b, 0) / intervals.length;
      if (avgInterval > 250 && avgInterval < 2000) {
        const calculatedBpm = Math.round(60000 / avgInterval);
        onBpmChange(Math.min(180, Math.max(50, calculatedBpm)));
      }
    }
  };

  return (
    <div className="w-full mt-3 pt-3 border-t border-purple-500/20">
      <div className="flex items-center justify-between mb-2">
        <span className="text-[11px] font-semibold uppercase tracking-wider text-purple-300/80">
          Coreografía de Concierto
        </span>
        <button
          onClick={handleTapTempo}
          className={`flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-medium border transition-all ${
            tapFeedback
              ? 'bg-purple-500 text-white border-purple-300 scale-95 shadow-[0_0_12px_rgba(168,85,247,0.8)]'
              : 'bg-purple-950/40 text-purple-200 border-purple-700/40 hover:bg-purple-900/50'
          }`}
          title="Toca al ritmo de la música para calibrar el tempo"
        >
          <Music className="w-3 h-3 text-purple-400" />
          <span>Tap Tempo: {bpm} BPM</span>
        </button>
      </div>

      <div className="grid grid-cols-4 gap-1.5">
        {programs.map((p) => {
          const isSelected = currentProgram === p.id;
          return (
            <button
              key={p.id}
              onClick={(e) => {
                e.stopPropagation();
                if (isVibrationEnabled) triggerHaptic(hapticPatterns.click);
                onSelectProgram(p.id);
              }}
              className={`flex flex-col items-center justify-center p-2 rounded-xl transition-all border text-center ${
                isSelected
                  ? 'bg-purple-600/35 border-purple-400 text-white shadow-[0_0_14px_rgba(168,85,247,0.4)]'
                  : 'bg-black/30 border-white/10 text-white/70 hover:bg-white/5 hover:text-white'
              }`}
            >
              <div
                className={`p-1.5 rounded-lg mb-1 ${
                  isSelected ? 'bg-purple-500 text-white' : 'bg-white/5 text-purple-300'
                }`}
              >
                {p.icon}
              </div>
              <span className="text-[11px] font-semibold leading-tight line-clamp-1">
                {p.name}
              </span>
            </button>
          );
        })}
      </div>
    </div>
  );
};

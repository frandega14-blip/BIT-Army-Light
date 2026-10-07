import React from 'react';
import {
  Activity,
  Music,
  Vibrate,
  BatteryCharging,
  Maximize2,
  Minimize2,
  Smartphone,
  Sparkles
} from 'lucide-react';
import { ConcertProgramType, LightIntensityLevel } from '../types/light';
import { triggerHaptic, hapticPatterns } from '../utils/haptics';
import { ConcertChoreoSelector } from './ConcertChoreoSelector';

interface ControlsPanelProps {
  isPulseActive: boolean;
  onTogglePulse: () => void;
  isConcertActive: boolean;
  onToggleConcert: () => void;
  concertProgram: ConcertProgramType;
  onSelectConcertProgram: (p: ConcertProgramType) => void;
  intensity: LightIntensityLevel;
  onSelectIntensity: (level: LightIntensityLevel) => void;
  isVibrationEnabled: boolean;
  onToggleVibration: () => void;
  isBatterySaver: boolean;
  onToggleBatterySaver: () => void;
  isFullscreen: boolean;
  onToggleFullscreen: () => void;
  onOpenAndroidCode: () => void;
  bpm: number;
  onBpmChange: (bpm: number) => void;
}

export const ControlsPanel: React.FC<ControlsPanelProps> = ({
  isPulseActive,
  onTogglePulse,
  isConcertActive,
  onToggleConcert,
  concertProgram,
  onSelectConcertProgram,
  intensity,
  onSelectIntensity,
  isVibrationEnabled,
  onToggleVibration,
  isBatterySaver,
  onToggleBatterySaver,
  isFullscreen,
  onToggleFullscreen,
  onOpenAndroidCode,
  bpm,
  onBpmChange
}) => {
  const intensityLevels: LightIntensityLevel[] = [30, 60, 100];

  return (
    <div
      onClick={(e) => e.stopPropagation()}
      className="w-full max-w-md mx-auto rounded-3xl bg-[#090412]/85 backdrop-blur-2xl border border-white/12 p-4 shadow-[0_20px_50px_rgba(0,0,0,0.8),0_0_30px_rgba(147,51,234,0.15)] select-none transition-all"
    >
      {/* Selector de Intensidad */}
      <div className="flex items-center justify-between pb-3.5 border-b border-purple-500/15">
        <div className="flex flex-col">
          <span className="text-[10px] font-bold tracking-widest uppercase text-purple-300/80">
            Intensidad de Luz
          </span>
          <span className="text-xs text-white/60">
            {intensity === 30
              ? 'Ecológico · Ahorro'
              : intensity === 60
              ? 'Concierto Estándar'
              : 'Estadio Máximo (100%)'}
          </span>
        </div>

        <div className="flex items-center gap-1.5 p-1 bg-black/40 border border-white/10 rounded-2xl">
          {intensityLevels.map((level) => {
            const isSelected = intensity === level;
            return (
              <button
                key={level}
                onClick={() => {
                  if (isVibrationEnabled) triggerHaptic(hapticPatterns.click);
                  onSelectIntensity(level);
                }}
                className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all ${
                  isSelected
                    ? 'bg-gradient-to-r from-purple-600 to-fuchsia-600 text-white shadow-[0_0_12px_rgba(192,132,252,0.6)] scale-102'
                    : 'text-white/60 hover:text-white hover:bg-white/5'
                }`}
              >
                {level}%
              </button>
            );
          })}
        </div>
      </div>

      {/* Botones Primarios: PULSO & CONCIERTO */}
      <div className="grid grid-cols-2 gap-3 pt-3.5">
        {/* Botón Pulso */}
        <button
          onClick={() => {
            if (isVibrationEnabled) triggerHaptic(hapticPatterns.click);
            onTogglePulse();
          }}
          className={`flex items-center gap-3 p-3 rounded-2xl border transition-all text-left relative overflow-hidden group ${
            isPulseActive
              ? 'bg-purple-900/40 border-purple-400/80 shadow-[0_0_20px_rgba(168,85,247,0.35)]'
              : 'bg-white/4 border-white/10 text-white/60 hover:bg-white/8'
          }`}
        >
          {isPulseActive && (
            <div className="absolute inset-0 bg-gradient-to-r from-purple-500/15 to-transparent pointer-events-none" />
          )}
          <div
            className={`w-11 h-11 rounded-xl flex items-center justify-center transition-all ${
              isPulseActive
                ? 'bg-gradient-to-br from-purple-500 to-fuchsia-600 text-white shadow-[0_0_15px_rgba(168,85,247,0.8)] animate-pulse'
                : 'bg-white/6 text-white/50 group-hover:text-white'
            }`}
          >
            <Activity className="w-5 h-5" />
          </div>
          <div>
            <div className="text-sm font-bold text-white leading-tight">Pulso</div>
            <div className="text-[11px] text-purple-300/80 font-medium">
              {isPulseActive ? 'Respiración ON' : 'Pausado'}
            </div>
          </div>
        </button>

        {/* Botón Concierto */}
        <button
          onClick={() => {
            if (isVibrationEnabled) triggerHaptic(hapticPatterns.click);
            onToggleConcert();
          }}
          className={`flex items-center gap-3 p-3 rounded-2xl border transition-all text-left relative overflow-hidden group ${
            isConcertActive
              ? 'bg-gradient-to-br from-fuchsia-950/70 to-purple-900/50 border-fuchsia-400 shadow-[0_0_20px_rgba(217,70,239,0.35)]'
              : 'bg-white/4 border-white/10 text-white/60 hover:bg-white/8'
          }`}
        >
          {isConcertActive && (
            <div className="absolute inset-0 bg-gradient-to-r from-fuchsia-500/15 to-transparent pointer-events-none" />
          )}
          <div
            className={`w-11 h-11 rounded-xl flex items-center justify-center transition-all ${
              isConcertActive
                ? 'bg-gradient-to-br from-fuchsia-500 to-purple-600 text-white shadow-[0_0_15px_rgba(217,70,239,0.8)]'
                : 'bg-white/6 text-white/50 group-hover:text-white'
            }`}
          >
            <Music className="w-5 h-5" />
          </div>
          <div>
            <div className="text-sm font-bold text-white leading-tight">Concierto</div>
            <div className="text-[11px] text-fuchsia-300/80 font-medium">
              {isConcertActive ? 'Show Dinámico' : 'Modo Concierto'}
            </div>
          </div>
        </button>
      </div>

      {/* Submenú de Coreografías si el Modo Concierto está activo */}
      {isConcertActive && (
        <ConcertChoreoSelector
          currentProgram={concertProgram}
          onSelectProgram={onSelectConcertProgram}
          bpm={bpm}
          onBpmChange={onBpmChange}
          isVibrationEnabled={isVibrationEnabled}
        />
      )}

      {/* Opciones secundarias rápidas: Vibración, Ahorro OLED, Pantalla Completa y Código Android */}
      <div className="grid grid-cols-4 gap-2 pt-3.5 mt-3 border-t border-purple-500/15">
        {/* Toggle Vibración */}
        <button
          onClick={() => {
            if (!isVibrationEnabled) triggerHaptic(hapticPatterns.click);
            onToggleVibration();
          }}
          className={`flex flex-col items-center justify-center p-2 rounded-xl border text-center transition-all ${
            isVibrationEnabled
              ? 'bg-purple-900/40 border-purple-400/60 text-purple-200'
              : 'bg-white/4 border-white/10 text-white/50 hover:bg-white/8'
          }`}
          title="Vibración sincronizada al ritmo"
        >
          <Vibrate className={`w-4 h-4 mb-1 ${isVibrationEnabled ? 'text-purple-300' : 'text-white/40'}`} />
          <span className="text-[10px] font-medium leading-none">
            {isVibrationEnabled ? 'Vibra ON' : 'Vibra OFF'}
          </span>
        </button>

        {/* Toggle Ahorro Batería OLED */}
        <button
          onClick={() => {
            if (isVibrationEnabled) triggerHaptic(hapticPatterns.click);
            onToggleBatterySaver();
          }}
          className={`flex flex-col items-center justify-center p-2 rounded-xl border text-center transition-all ${
            isBatterySaver
              ? 'bg-emerald-950/50 border-emerald-500/60 text-emerald-200 shadow-[0_0_12px_rgba(16,185,129,0.3)]'
              : 'bg-white/4 border-white/10 text-white/50 hover:bg-white/8'
          }`}
          title="Ahorro de batería para pantallas OLED (Negro absoluto)"
        >
          <BatteryCharging className={`w-4 h-4 mb-1 ${isBatterySaver ? 'text-emerald-400' : 'text-white/40'}`} />
          <span className="text-[10px] font-medium leading-none">
            {isBatterySaver ? 'OLED Eco' : 'Eco OFF'}
          </span>
        </button>

        {/* Toggle Pantalla Completa */}
        <button
          onClick={() => {
            if (isVibrationEnabled) triggerHaptic(hapticPatterns.click);
            onToggleFullscreen();
          }}
          className={`flex flex-col items-center justify-center p-2 rounded-xl border text-center transition-all ${
            isFullscreen
              ? 'bg-purple-900/40 border-purple-400/60 text-purple-200'
              : 'bg-white/4 border-white/10 text-white/50 hover:bg-white/8'
          }`}
          title="Modo pantalla completa"
        >
          {isFullscreen ? (
            <Minimize2 className="w-4 h-4 mb-1 text-purple-300" />
          ) : (
            <Maximize2 className="w-4 h-4 mb-1 text-white/40" />
          )}
          <span className="text-[10px] font-medium leading-none">
            {isFullscreen ? 'Salir' : 'Full scr'}
          </span>
        </button>

        {/* Explorador de Código Android Nativo */}
        <button
          onClick={() => {
            if (isVibrationEnabled) triggerHaptic(hapticPatterns.click);
            onOpenAndroidCode();
          }}
          className="flex flex-col items-center justify-center p-2 rounded-xl border border-fuchsia-500/40 bg-fuchsia-950/30 text-fuchsia-200 hover:bg-fuchsia-900/40 transition-all text-center"
          title="Ver y descargar proyecto nativo Android (Kotlin + Compose)"
        >
          <Smartphone className="w-4 h-4 mb-1 text-fuchsia-300" />
          <span className="text-[10px] font-medium leading-none">
            Android
          </span>
        </button>
      </div>
    </div>
  );
};

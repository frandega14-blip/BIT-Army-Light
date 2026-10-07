/**
 * BIT | ARMY LIGHT
 * Aplicación Fan-Made No Oficial de Luz para Conciertos
 * Desarrollada para Android (Kotlin + Jetpack Compose) y Web/PWA
 */

import React, { useState, useEffect, useCallback, useRef } from 'react';
import {
  Smartphone,
  Eye,
  EyeOff,
  Maximize2,
  Minimize2,
  Sparkles,
  Zap,
  Battery
} from 'lucide-react';
import { LightCore } from './components/LightCore';
import { ControlsPanel } from './components/ControlsPanel';
import { AndroidCodeModal } from './components/AndroidCodeModal';
import { ConcertProgramType, LightIntensityLevel } from './types/light';
import { requestScreenWakeLock, releaseScreenWakeLock } from './utils/wakeLock';
import { triggerHaptic, hapticPatterns } from './utils/haptics';

export default function App() {
  // Estados de control de luz
  const [isPulseActive, setIsPulseActive] = useState<boolean>(true);
  const [isConcertActive, setIsConcertActive] = useState<boolean>(false);
  const [concertProgram, setConcertProgram] = useState<ConcertProgramType>('wave');
  const [intensity, setIntensity] = useState<LightIntensityLevel>(100);
  const [isVibrationEnabled, setIsVibrationEnabled] = useState<boolean>(true);
  const [isBatterySaver, setIsBatterySaver] = useState<boolean>(false);
  const [bpm, setBpm] = useState<number>(110);

  // Estados de UI
  const [areControlsVisible, setAreControlsVisible] = useState<boolean>(true);
  const [isFullscreen, setIsFullscreen] = useState<boolean>(false);
  const [isAndroidModalOpen, setIsAndroidModalOpen] = useState<boolean>(false);
  const [showHint, setShowHint] = useState<boolean>(true);

  const hideTimerRef = useRef<NodeJS.Timeout | null>(null);

  // Mantener pantalla encendida (Wake Lock)
  useEffect(() => {
    requestScreenWakeLock();
    return () => {
      releaseScreenWakeLock();
    };
  }, []);

  // Ocultar hint inicial después de 4 segundos
  useEffect(() => {
    const timer = setTimeout(() => setShowHint(false), 4500);
    return () => clearTimeout(timer);
  }, []);

  // Temporizador para auto-ocultar controles tras inactividad
  const resetAutoHideTimer = useCallback(() => {
    if (hideTimerRef.current) clearTimeout(hideTimerRef.current);
    hideTimerRef.current = setTimeout(() => {
      setAreControlsVisible(false);
    }, 7000);
  }, []);

  useEffect(() => {
    if (areControlsVisible) {
      resetAutoHideTimer();
    } else {
      if (hideTimerRef.current) clearTimeout(hideTimerRef.current);
    }
    return () => {
      if (hideTimerRef.current) clearTimeout(hideTimerRef.current);
    };
  }, [areControlsVisible, resetAutoHideTimer]);

  // Manejo de toques en la pantalla (Modo Puro de Luz)
  const handleScreenTap = () => {
    if (isVibrationEnabled) triggerHaptic(hapticPatterns.click);
    setAreControlsVisible((prev) => !prev);
  };

  // Toggle de pantalla completa nativa del navegador
  const handleToggleFullscreen = () => {
    if (!document.fullscreenElement) {
      document.documentElement.requestFullscreen().then(() => {
        setIsFullscreen(true);
      }).catch(() => {
        // Fallback
      });
    } else {
      if (document.exitFullscreen) {
        document.exitFullscreen().then(() => {
          setIsFullscreen(false);
        }).catch(() => {});
      }
    }
  };

  useEffect(() => {
    const onFullscreenChange = () => {
      setIsFullscreen(!!document.fullscreenElement);
    };
    document.addEventListener('fullscreenchange', onFullscreenChange);
    return () => document.removeEventListener('fullscreenchange', onFullscreenChange);
  }, []);

  const handleTogglePulse = () => {
    setIsPulseActive((prev) => !prev);
    resetAutoHideTimer();
  };

  const handleToggleConcert = () => {
    setIsConcertActive((prev) => {
      const next = !prev;
      if (next) setIsPulseActive(true);
      return next;
    });
    resetAutoHideTimer();
  };

  const handleSelectConcertProgram = (p: ConcertProgramType) => {
    setConcertProgram(p);
    setIsConcertActive(true);
    resetAutoHideTimer();
  };

  const handleSelectIntensity = (level: LightIntensityLevel) => {
    setIntensity(level);
    resetAutoHideTimer();
  };

  const handleToggleVibration = () => {
    setIsVibrationEnabled((prev) => !prev);
    resetAutoHideTimer();
  };

  const handleToggleBatterySaver = () => {
    setIsBatterySaver((prev) => !prev);
    resetAutoHideTimer();
  };

  // Color de fondo: negro absoluto OLED si está activo el modo ahorro
  const backgroundColor = isBatterySaver ? '#000000' : '#04010a';

  return (
    <main
      onClick={handleScreenTap}
      style={{ backgroundColor }}
      className="relative w-screen h-screen overflow-hidden cursor-pointer select-none transition-colors duration-500 font-sans"
    >
      {/* 1. MOTOR DE LUZ PÚRPURA CENTRAL */}
      <LightCore
        isPulseActive={isPulseActive}
        isConcertActive={isConcertActive}
        concertProgram={concertProgram}
        intensity={intensity}
        isBatterySaver={isBatterySaver}
        isVibrationEnabled={isVibrationEnabled}
        bpm={bpm}
      />

      {/* 2. CABECERA SUPERIOR ELEGANTE (Marca + Fan-made badge) */}
      <header
        className={`absolute top-0 inset-x-0 z-30 transition-all duration-500 ease-out pointer-events-none ${
          areControlsVisible
            ? 'opacity-100 translate-y-0'
            : 'opacity-0 -translate-y-8'
        }`}
      >
        <div className="max-w-md mx-auto pt-6 px-6 flex flex-col items-center justify-center text-center">
          <div className="pointer-events-auto flex items-center justify-between w-full mb-1">
            {/* Botón flotante para ver código Android */}
            <button
              onClick={(e) => {
                e.stopPropagation();
                if (isVibrationEnabled) triggerHaptic(hapticPatterns.click);
                setIsAndroidModalOpen(true);
              }}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-white/5 hover:bg-white/10 active:scale-95 border border-white/10 text-white/80 hover:text-white text-xs transition-all shadow-lg"
              title="Ver código fuente nativo Android en Kotlin"
            >
              <Smartphone className="w-3.5 h-3.5 text-purple-400" />
              <span className="font-medium text-[11px]">Android Nativo</span>
            </button>

            {/* Botón de pantalla completa / Ocultar controles */}
            <div className="flex items-center gap-1.5">
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  handleToggleFullscreen();
                }}
                className="p-2 rounded-full bg-white/5 hover:bg-white/10 active:scale-95 border border-white/10 text-white/70 hover:text-white transition-all"
                title={isFullscreen ? 'Salir de pantalla completa' : 'Pantalla completa'}
              >
                {isFullscreen ? (
                  <Minimize2 className="w-3.5 h-3.5" />
                ) : (
                  <Maximize2 className="w-3.5 h-3.5" />
                )}
              </button>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  setAreControlsVisible(false);
                }}
                className="p-2 rounded-full bg-white/5 hover:bg-white/10 active:scale-95 border border-white/10 text-white/70 hover:text-white transition-all"
                title="Ocultar interfaz (Modo Solo Luz)"
              >
                <EyeOff className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>

          <h1 className="text-2xl sm:text-3xl font-extrabold tracking-widest text-white drop-shadow-[0_0_25px_rgba(192,132,252,0.8)] font-['Space_Grotesk']">
            BIT | ARMY LIGHT
          </h1>

          <div className="mt-1.5 flex items-center gap-2">
            <span className="inline-flex items-center gap-1 px-3 py-0.5 rounded-full bg-purple-900/40 border border-purple-400/40 text-purple-200 text-[11px] font-semibold tracking-wider uppercase shadow-[0_0_12px_rgba(168,85,247,0.3)]">
              <Sparkles className="w-3 h-3 text-purple-300" />
              Fan-made · No oficial
            </span>

            {isBatterySaver && (
              <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-emerald-950/60 border border-emerald-500/40 text-emerald-300 text-[10px] font-medium">
                <Battery className="w-2.5 h-2.5" />
                OLED Eco
              </span>
            )}
          </div>
        </div>
      </header>

      {/* 3. PISTA DE INTERACCIÓN INFERIOR CUANDO LOS CONTROLES ESTÁN OCULTOS */}
      {!areControlsVisible && (
        <div className="absolute bottom-6 inset-x-0 z-20 flex justify-center pointer-events-none animate-pulse">
          <div className="flex items-center gap-2 px-4 py-1.5 rounded-full bg-black/50 border border-white/10 backdrop-blur-md text-xs text-white/60">
            <Eye className="w-3.5 h-3.5 text-purple-400" />
            <span>Toca la pantalla para ver controles</span>
          </div>
        </div>
      )}

      {/* 4. HINT INICIAL SUAVEMENTE FLOTANTE */}
      {showHint && areControlsVisible && (
        <div className="absolute top-28 inset-x-0 z-20 flex justify-center pointer-events-none animate-fade-in transition-opacity">
          <div className="px-3 py-1 rounded-full bg-purple-950/70 border border-purple-500/30 backdrop-blur-md text-[11px] text-purple-200 shadow-md">
            Tip: Toca el fondo para ocultar controles y ondear la luz en concierto
          </div>
        </div>
      )}

      {/* 5. PANEL DE CONTROLES INFERIOR */}
      <footer
        className={`absolute bottom-0 inset-x-0 z-30 p-4 pb-6 transition-all duration-500 ease-out pointer-events-none ${
          areControlsVisible
            ? 'opacity-100 translate-y-0'
            : 'opacity-0 translate-y-12 pointer-events-none'
        }`}
      >
        <div className="pointer-events-auto">
          <ControlsPanel
            isPulseActive={isPulseActive}
            onTogglePulse={handleTogglePulse}
            isConcertActive={isConcertActive}
            onToggleConcert={handleToggleConcert}
            concertProgram={concertProgram}
            onSelectConcertProgram={handleSelectConcertProgram}
            intensity={intensity}
            onSelectIntensity={handleSelectIntensity}
            isVibrationEnabled={isVibrationEnabled}
            onToggleVibration={handleToggleVibration}
            isBatterySaver={isBatterySaver}
            onToggleBatterySaver={handleToggleBatterySaver}
            isFullscreen={isFullscreen}
            onToggleFullscreen={handleToggleFullscreen}
            onOpenAndroidCode={() => setIsAndroidModalOpen(true)}
            bpm={bpm}
            onBpmChange={setBpm}
          />
        </div>
      </footer>

      {/* 6. MODAL DEL PROYECTO ANDROID NATIVO (KOTLIN + JETPACK COMPOSE) */}
      <AndroidCodeModal
        isOpen={isAndroidModalOpen}
        onClose={() => setIsAndroidModalOpen(false)}
      />
    </main>
  );
}

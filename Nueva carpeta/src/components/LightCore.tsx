import React, { useEffect, useRef } from 'react';
import { ConcertProgramType, LightIntensityLevel } from '../types/light';
import { triggerHaptic, hapticPatterns } from '../utils/haptics';

interface LightCoreProps {
  isPulseActive: boolean;
  isConcertActive: boolean;
  concertProgram: ConcertProgramType;
  intensity: LightIntensityLevel;
  isBatterySaver: boolean;
  isVibrationEnabled: boolean;
  bpm: number;
}

export const LightCore: React.FC<LightCoreProps> = ({
  isPulseActive,
  isConcertActive,
  concertProgram,
  intensity,
  isBatterySaver,
  isVibrationEnabled,
  bpm
}) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const animFrameRef = useRef<number | null>(null);
  const lastPeakRef = useRef<number>(0);

  // Intensidad normalizada: 0.3, 0.6, 1.0
  const intensityFactor = intensity === 30 ? 0.35 : intensity === 60 ? 0.65 : 1.0;

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let width = (canvas.width = window.innerWidth);
    let height = (canvas.height = window.innerHeight);

    const handleResize = () => {
      if (!canvas) return;
      width = canvas.width = window.innerWidth;
      height = canvas.height = window.innerHeight;
    };
    window.addEventListener('resize', handleResize);

    // Partículas de luz ambiente (polvo de concierto reflectante)
    const particleCount = isBatterySaver ? 0 : 36;
    const particles = Array.from({ length: particleCount }, () => ({
      x: Math.random() * width,
      y: Math.random() * height,
      size: Math.random() * 2.2 + 0.8,
      speedX: (Math.random() - 0.5) * 0.4,
      speedY: (Math.random() - 0.5) * 0.4 - 0.2,
      alpha: Math.random() * 0.6 + 0.2,
      pulseOffset: Math.random() * Math.PI * 2,
    }));

    let startTime = performance.now();

    const render = (time: number) => {
      const elapsed = (time - startTime) / 1000;
      ctx.clearRect(0, 0, width, height);

      const centerX = width / 2;
      const centerY = height / 2;
      const minDim = Math.min(width, height);

      // Calcular factor de pulso y ritmo según modo
      let pulse = 1.0;
      let alphaMod = 1.0;
      let hueShift = 0; // para aurora

      if (isConcertActive) {
        if (concertProgram === 'strobe') {
          // Destello rítmico estroboscópico sincronizado al BPM
          const strobeFreq = (bpm / 60) * 2; // doble tempo
          const phase = (elapsed * strobeFreq) % 1;
          pulse = phase < 0.25 ? 1.35 : 0.8;
          alphaMod = phase < 0.25 ? 1.0 : 0.3;
          if (phase < 0.08 && time - lastPeakRef.current > 200) {
            lastPeakRef.current = time;
            if (isVibrationEnabled) triggerHaptic(hapticPatterns.strobeBeat);
          }
        } else if (concertProgram === 'supernova') {
          // Explosión de pulso potente
          const period = 60 / bpm;
          const cycle = (elapsed / period) % 1;
          const burst = Math.pow(Math.sin(cycle * Math.PI), 2.5);
          pulse = 0.85 + burst * 0.55;
          alphaMod = 0.6 + burst * 0.4;
          if (burst > 0.95 && time - lastPeakRef.current > 400) {
            lastPeakRef.current = time;
            if (isVibrationEnabled) triggerHaptic(hapticPatterns.supernovaBurst);
          }
        } else if (concertProgram === 'aurora') {
          // Fluctuación mística suave con ligero cambio de coloración
          const period = (60 / bpm) * 2;
          pulse = 0.9 + 0.3 * Math.sin((elapsed / period) * Math.PI * 2);
          alphaMod = 0.8 + 0.2 * Math.cos((elapsed / period) * Math.PI * 2);
          hueShift = Math.sin(elapsed * 0.8) * 15; // cambia entre violeta y orquídea
        } else {
          // Modo Wave continuo y orgánico
          const period = 60 / bpm;
          const sinVal = Math.sin((elapsed / period) * Math.PI * 2);
          pulse = 0.88 + 0.32 * (sinVal * 0.5 + 0.5);
          alphaMod = 0.75 + 0.25 * (sinVal * 0.5 + 0.5);
          if (sinVal > 0.98 && time - lastPeakRef.current > 500) {
            lastPeakRef.current = time;
            if (isVibrationEnabled) triggerHaptic(hapticPatterns.pulsePeak);
          }
        }
      } else if (isPulseActive) {
        // Pulso suave de respiración calma (2.4 seg)
        const breatheCycle = Math.sin(elapsed * 2.6);
        pulse = 0.85 + 0.30 * (breatheCycle * 0.5 + 0.5);
        alphaMod = 0.7 + 0.3 * (breatheCycle * 0.5 + 0.5);

        // Disparo háptico en la cima del pulso
        if (breatheCycle > 0.98 && time - lastPeakRef.current > 1200) {
          lastPeakRef.current = time;
          if (isVibrationEnabled) triggerHaptic(hapticPatterns.pulsePeak);
        }
      }

      const baseRadius = minDim * 0.26 * pulse;

      // 1. Capa ambiental difusa (sólo si no está activado ahorro estricto)
      if (!isBatterySaver) {
        const ambientRadius = minDim * 0.85 * pulse;
        const ambientGrad = ctx.createRadialGradient(
          centerX,
          centerY,
          0,
          centerX,
          centerY,
          ambientRadius
        );
        ambientGrad.addColorStop(
          0,
          `hsla(${285 + hueShift}, 85%, 35%, ${0.5 * intensityFactor * alphaMod})`
        );
        ambientGrad.addColorStop(
          0.5,
          `hsla(${280 + hueShift}, 80%, 20%, ${0.2 * intensityFactor * alphaMod})`
        );
        ambientGrad.addColorStop(1, 'rgba(3, 1, 7, 0)');
        ctx.fillStyle = ambientGrad;
        ctx.beginPath();
        ctx.arc(centerX, centerY, ambientRadius, 0, Math.PI * 2);
        ctx.fill();
      }

      // 2. Halo y Corona de resplandor exterior
      const haloRadius = baseRadius * 1.85;
      const haloGrad = ctx.createRadialGradient(
        centerX,
        centerY,
        baseRadius * 0.4,
        centerX,
        centerY,
        haloRadius
      );
      haloGrad.addColorStop(
        0,
        `hsla(${286 + hueShift}, 95%, 60%, ${0.85 * intensityFactor * alphaMod})`
      );
      haloGrad.addColorStop(
        0.55,
        `hsla(${282 + hueShift}, 90%, 48%, ${0.45 * intensityFactor * alphaMod})`
      );
      haloGrad.addColorStop(1, 'rgba(139, 37, 198, 0)');

      ctx.fillStyle = haloGrad;
      ctx.beginPath();
      ctx.arc(centerX, centerY, haloRadius, 0, Math.PI * 2);
      ctx.fill();

      // 3. Orbe de Luz Púrpura Intenso Primario
      const orbGrad = ctx.createRadialGradient(
        centerX,
        centerY,
        0,
        centerX,
        centerY,
        baseRadius
      );
      orbGrad.addColorStop(0, `rgba(255, 255, 255, ${0.98 * intensityFactor})`);
      orbGrad.addColorStop(
        0.25,
        `hsla(${285 + hueShift}, 100%, 86%, ${0.95 * intensityFactor * alphaMod})`
      );
      orbGrad.addColorStop(
        0.58,
        `hsla(${288 + hueShift}, 95%, 68%, ${0.92 * intensityFactor * alphaMod})`
      );
      orbGrad.addColorStop(
        0.88,
        `hsla(${282 + hueShift}, 90%, 50%, ${0.80 * intensityFactor * alphaMod})`
      );
      orbGrad.addColorStop(1, 'rgba(100, 15, 150, 0)');

      ctx.fillStyle = orbGrad;
      ctx.beginPath();
      ctx.arc(centerX, centerY, baseRadius, 0, Math.PI * 2);
      ctx.fill();

      // 4. Núcleo central ultra luminoso (Core de alta concentración)
      const coreRadius = baseRadius * 0.32;
      const coreGrad = ctx.createRadialGradient(
        centerX,
        centerY,
        0,
        centerX,
        centerY,
        coreRadius
      );
      coreGrad.addColorStop(0, `rgba(255, 255, 255, ${1.0 * intensityFactor})`);
      coreGrad.addColorStop(
        0.4,
        `rgba(243, 232, 255, ${0.95 * intensityFactor})`
      );
      coreGrad.addColorStop(
        1,
        `hsla(${285 + hueShift}, 100%, 78%, ${0.1 * intensityFactor})`
      );

      ctx.fillStyle = coreGrad;
      ctx.beginPath();
      ctx.arc(centerX, centerY, coreRadius, 0, Math.PI * 2);
      ctx.fill();

      // 5. Partículas stardust flotantes
      if (!isBatterySaver) {
        particles.forEach((p) => {
          p.x += p.speedX;
          p.y += p.speedY;
          if (p.x < 0) p.x = width;
          if (p.x > width) p.x = 0;
          if (p.y < 0) p.y = height;
          if (p.y > height) p.y = 0;

          const pAlpha =
            (Math.sin(elapsed * 2 + p.pulseOffset) * 0.3 + 0.7) *
            p.alpha *
            intensityFactor;

          ctx.fillStyle = `rgba(233, 213, 255, ${pAlpha})`;
          ctx.beginPath();
          ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
          ctx.fill();
        });
      }

      animFrameRef.current = requestAnimationFrame(render);
    };

    animFrameRef.current = requestAnimationFrame(render);

    return () => {
      window.removeEventListener('resize', handleResize);
      if (animFrameRef.current) cancelAnimationFrame(animFrameRef.current);
    };
  }, [
    isPulseActive,
    isConcertActive,
    concertProgram,
    intensity,
    isBatterySaver,
    isVibrationEnabled,
    bpm,
    intensityFactor
  ]);

  return (
    <div className="absolute inset-0 pointer-events-none select-none overflow-hidden">
      <canvas
        ref={canvasRef}
        className="w-full h-full block"
        style={{
          filter: isBatterySaver ? 'none' : 'contrast(1.05)',
        }}
      />
    </div>
  );
};

/**
 * Utilidad segura de vibración háptica para navegadores móviles y Android WebViews
 */
export const triggerHaptic = (pattern: number | number[] = 20) => {
  if (typeof window !== 'undefined' && 'vibrate' in navigator) {
    try {
      navigator.vibrate(pattern);
    } catch {
      // Navegadores sin permiso o en plataformas que restringen vibración
    }
  }
};

export const hapticPatterns = {
  click: 15,
  pulsePeak: [30, 40, 20],
  strobeBeat: [40],
  supernovaBurst: [80, 50, 40],
  doubleTap: [20, 50, 20],
};

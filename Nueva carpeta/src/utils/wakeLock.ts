/**
 * Wake Lock API para mantener la pantalla encendida sin apagarse durante conciertos
 */
let wakeLockSentinel: any = null;

export const requestScreenWakeLock = async (): Promise<boolean> => {
  if (typeof window !== 'undefined' && 'wakeLock' in navigator) {
    try {
      wakeLockSentinel = await (navigator as any).wakeLock.request('screen');
      wakeLockSentinel.addEventListener('release', () => {
        wakeLockSentinel = null;
      });
      return true;
    } catch {
      return false;
    }
  }
  return false;
};

export const releaseScreenWakeLock = async () => {
  if (wakeLockSentinel) {
    try {
      await wakeLockSentinel.release();
      wakeLockSentinel = null;
    } catch {
      // Ignorar
    }
  }
};

import React, { useState } from 'react';
import JSZip from 'jszip';
import {
  X,
  Download,
  Copy,
  Check,
  Smartphone,
  FolderGit2,
  Terminal,
  FileCode,
  ShieldCheck,
  Battery
} from 'lucide-react';
import { ANDROID_FILES } from '../data/androidProjectFiles';

interface AndroidCodeModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const AndroidCodeModal: React.FC<AndroidCodeModalProps> = ({
  isOpen,
  onClose,
}) => {
  const [selectedFileIdx, setSelectedFileIdx] = useState(0);
  const [hasCopied, setHasCopied] = useState(false);
  const [isDownloading, setIsDownloading] = useState(false);

  if (!isOpen) return null;

  const currentFile = ANDROID_FILES[selectedFileIdx];

  const handleCopyCode = async () => {
    try {
      await navigator.clipboard.writeText(currentFile.content);
      setHasCopied(true);
      setTimeout(() => setHasCopied(false), 2000);
    } catch {
      // Fallback
    }
  };

  const handleDownloadZip = async () => {
    setIsDownloading(true);
    try {
      const zip = new JSZip();

      // Añadir todos los archivos del proyecto Android estructurado
      ANDROID_FILES.forEach((f) => {
        // Remover el prefijo 'android/' si lo tiene para la raíz del zip
        const relativeZipPath = f.path.startsWith('android/')
          ? f.path.replace('android/', '')
          : f.path;
        zip.file(relativeZipPath, f.content);
      });

      // Archivos complementarios esenciales
      zip.file(
        'settings.gradle.kts',
        `rootProject.name = "BitArmyLight"\ninclude(":app")`
      );
      zip.file(
        'build.gradle.kts',
        `plugins {\n    alias(libs.plugins.android.application) apply false\n    alias(libs.plugins.kotlin.android) apply false\n    alias(libs.plugins.kotlin.compose) apply false\n}`
      );
      zip.file(
        'gradle.properties',
        `org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8\nandroid.useAndroidX=true\nkotlin.code.style=official`
      );
      zip.file(
        'README.md',
        `# BIT | ARMY LIGHT - Android Nativo (Kotlin + Jetpack Compose)\n\n1. Abre Android Studio.\n2. Archivo > Abrir (File > Open) y selecciona esta carpeta extraída.\n3. Presiona 'Run' o ejecuta:\n   ./gradlew assembleDebug\n\nEl APK resultante se generará en app/build/outputs/apk/debug/app-debug.apk`
      );

      const content = await zip.generateAsync({ type: 'blob' });
      const url = URL.createObjectURL(content);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'bit-army-light-android.zip';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
    } catch (e) {
      console.error('Error generando ZIP:', e);
    } finally {
      setIsDownloading(false);
    }
  };

  return (
    <div
      onClick={onClose}
      className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-6 bg-black/80 backdrop-blur-xl animate-fade-in select-text"
    >
      <div
        onClick={(e) => e.stopPropagation()}
        className="w-full max-w-4xl h-[90vh] bg-[#0c0617] border border-purple-500/30 rounded-3xl shadow-[0_25px_60px_rgba(0,0,0,0.9),0_0_40px_rgba(168,85,247,0.2)] flex flex-col overflow-hidden"
      >
        {/* Cabecera del Modal */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-purple-500/20 bg-purple-950/20">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-purple-600 to-fuchsia-500 flex items-center justify-center text-white shadow-[0_0_15px_rgba(168,85,247,0.5)]">
              <Smartphone className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-lg font-bold text-white tracking-wide">
                  Código Android Nativo
                </h2>
                <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-purple-500/20 text-purple-300 border border-purple-400/30">
                  Kotlin + Jetpack Compose
                </span>
              </div>
              <p className="text-xs text-white/60">
                100% Offline · Cero Internet/GPS · Optimizado para pantallas OLED
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={handleDownloadZip}
              disabled={isDownloading}
              className="flex items-center gap-2 px-4 py-2 rounded-xl bg-purple-600 hover:bg-purple-500 active:scale-95 text-white text-xs font-bold transition-all shadow-[0_0_15px_rgba(147,51,234,0.4)] disabled:opacity-50"
            >
              <Download className="w-4 h-4" />
              <span>{isDownloading ? 'Generando ZIP...' : 'Descargar Proyecto .ZIP'}</span>
            </button>
            <button
              onClick={onClose}
              className="p-2 rounded-xl bg-white/5 hover:bg-white/10 text-white/70 hover:text-white transition-colors"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Mini barra de especificaciones técnicas */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 px-6 py-2.5 bg-black/40 border-b border-purple-500/10 text-xs">
          <div className="flex items-center gap-2 text-white/70">
            <ShieldCheck className="w-4 h-4 text-emerald-400" />
            <span>Permisos: Solo VIBRATE</span>
          </div>
          <div className="flex items-center gap-2 text-white/70">
            <Battery className="w-4 h-4 text-purple-400" />
            <span>Negro puro AMOLED #000000</span>
          </div>
          <div className="flex items-center gap-2 text-white/70">
            <Terminal className="w-4 h-4 text-fuchsia-400" />
            <span>minSdk 26 · targetSdk 35</span>
          </div>
          <div className="flex items-center gap-2 text-white/70">
            <FolderGit2 className="w-4 h-4 text-cyan-400" />
            <span>Gradle 8.7 · Kotlin 2.0</span>
          </div>
        </div>

        {/* Cuerpo del explorador: Sidebar de archivos + Visor de código */}
        <div className="flex-1 flex flex-col md:flex-row overflow-hidden">
          {/* Lista de archivos */}
          <div className="w-full md:w-64 border-r border-purple-500/15 bg-black/30 p-2 overflow-y-auto space-y-1">
            <div className="px-3 py-1.5 text-[10px] font-bold uppercase tracking-wider text-purple-400/80">
              Archivos del Proyecto
            </div>
            {ANDROID_FILES.map((f, idx) => {
              const isSelected = selectedFileIdx === idx;
              return (
                <button
                  key={f.path}
                  onClick={() => setSelectedFileIdx(idx)}
                  className={`w-full flex items-center gap-2 px-3 py-2 rounded-xl text-left text-xs transition-all ${
                    isSelected
                      ? 'bg-purple-900/50 text-white font-semibold border border-purple-500/40'
                      : 'text-white/60 hover:text-white hover:bg-white/5'
                  }`}
                >
                  <FileCode
                    className={`w-4 h-4 shrink-0 ${
                      isSelected ? 'text-purple-300' : 'text-white/40'
                    }`}
                  />
                  <div className="truncate">
                    <div className="truncate">{f.filename}</div>
                  </div>
                </button>
              );
            })}
          </div>

          {/* Visor de código del archivo seleccionado */}
          <div className="flex-1 flex flex-col overflow-hidden bg-[#07030e]">
            {/* Cabecera del archivo actual */}
            <div className="flex items-center justify-between px-4 py-2.5 border-b border-purple-500/15 bg-black/50">
              <div className="truncate pr-2">
                <span className="text-xs font-mono text-purple-300">
                  {currentFile.path}
                </span>
                <p className="text-[11px] text-white/50 truncate">
                  {currentFile.description}
                </p>
              </div>

              <button
                onClick={handleCopyCode}
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-white/5 hover:bg-white/10 active:scale-95 text-xs text-white/80 hover:text-white transition-all shrink-0 border border-white/10"
              >
                {hasCopied ? (
                  <>
                    <Check className="w-3.5 h-3.5 text-emerald-400" />
                    <span className="text-emerald-300 font-semibold">Copiado</span>
                  </>
                ) : (
                  <>
                    <Copy className="w-3.5 h-3.5" />
                    <span>Copiar</span>
                  </>
                )}
              </button>
            </div>

            {/* Contenido con scroll y numeración */}
            <div className="flex-1 overflow-auto p-4 font-mono text-xs leading-relaxed text-purple-100 bg-[#06020c]">
              <pre className="overflow-x-auto whitespace-pre font-mono selection:bg-purple-600 selection:text-white">
                {currentFile.content}
              </pre>
            </div>
          </div>
        </div>

        {/* Pie con instrucciones de compilación */}
        <div className="px-6 py-3 border-t border-purple-500/20 bg-black/60 flex flex-col sm:flex-row items-start sm:items-center justify-between text-xs text-white/60 gap-2">
          <div>
            <span className="font-semibold text-white">Instrucciones de Compilación:</span> Abre la carpeta en Android Studio y ejecuta <code className="text-purple-300 bg-purple-950/50 px-1.5 py-0.5 rounded font-mono">./gradlew assembleDebug</code>
          </div>
          <div className="text-[11px] text-purple-300/80">
            Diseño fan-made no oficial · Cero elementos propietarios
          </div>
        </div>
      </div>
    </div>
  );
};

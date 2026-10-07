# BIT | ARMY LIGHT - Proyecto Android Nativo (Kotlin + Jetpack Compose)

Aplicación fan-made no oficial para experiencia de luz y pulso en conciertos.

## Características Técnicas:
- **Lenguaje:** Kotlin 2.0.21
- **UI:** Jetpack Compose (Material 3)
- **Modo Offline:** 100% offline tras la instalación.
- **Permisos:** Solo `VIBRATE` (Cero permisos de red/Internet, Cero GPS, Cero cámara).
- **Pantalla Completa:** `WindowInsetsControllerCompat` con modo inmersivo sticky y `FLAG_KEEP_SCREEN_ON`.
- **Batería:** Optimización para pantallas OLED/AMOLED con negros absolutos (`#000000`).

## Instrucciones para compilar en Android Studio:
1. Abrir Android Studio (Hedgehog, Iguana, Jellyfish, Koala, Ladybug o superior).
2. Seleccionar **File > Open** y elegir la carpeta `/android`.
3. Esperar que Gradle sincronice las dependencias.
4. Conectar un teléfono Android físico o iniciar un emulador con API 26+.
5. Presionar **Run 'app'** (Shift + F10) o compilar el APK con:
   ```bash
   ./gradlew assembleDebug
   ```
El APK se genera en: `app/build/outputs/apk/debug/app-debug.apk`.

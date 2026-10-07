import { AndroidFileEntry } from '../types/light';

export const ANDROID_FILES: AndroidFileEntry[] = [
  {
    path: 'android/app/src/main/java/com/bit/armylight/MainActivity.kt',
    filename: 'MainActivity.kt',
    language: 'kotlin',
    description: 'Punto de entrada nativo: activa pantalla completa inmersiva, FLAG_KEEP_SCREEN_ON y arranca Jetpack Compose.',
    content: `package com.bit.armylight

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bit.armylight.ui.screens.ArmyLightScreen
import com.bit.armylight.ui.theme.BitArmyLightTheme
import com.bit.armylight.util.HapticController
import com.bit.armylight.viewmodel.LightViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LightViewModel by viewModels()
    private lateinit var hapticController: HapticController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Mantener pantalla siempre encendida para el concierto
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 2. Modo inmersivo total (oculta barras de estado y navegación)
        enableImmersiveFullScreen()

        // 3. Inicializar hápticos
        hapticController = HapticController(this)

        setContent {
            BitArmyLightTheme {
                ArmyLightScreen(
                    viewModel = viewModel,
                    hapticController = hapticController
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        enableImmersiveFullScreen()
    }

    private fun enableImmersiveFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { insetsController ->
                insetsController.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                insetsController.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hapticController.cancel()
    }
}`
  },
  {
    path: 'android/app/src/main/java/com/bit/armylight/ui/screens/ArmyLightScreen.kt',
    filename: 'ArmyLightScreen.kt',
    language: 'kotlin',
    description: 'Pantalla Jetpack Compose principal: orbe central, botones de Pulso/Concierto, selector 30/60/100% y autohide.',
    content: `package com.bit.armylight.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bit.armylight.ui.components.LightCoreVisualizer
import com.bit.armylight.ui.theme.*
import com.bit.armylight.util.HapticController
import com.bit.armylight.viewmodel.*

@Composable
fun ArmyLightScreen(
    viewModel: LightViewModel,
    hapticController: HapticController
) {
    val uiState by viewModel.uiState.collectAsState()
    val bgColor = if (uiState.isBatterySaverEnabled) BackgroundAmoled else BackgroundDark

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                hapticController.vibrateClick()
                viewModel.toggleControlsVisibility()
            }
    ) {
        // Visualizador de luz central con pulso animado
        LightCoreVisualizer(
            isPulseActive = uiState.isPulseActive,
            isConcertActive = uiState.isConcertActive,
            concertProgram = uiState.concertProgram,
            intensity = uiState.intensity,
            isBatterySaver = uiState.isBatterySaverEnabled,
            onPeakPulse = {
                if (uiState.isVibrationEnabled) {
                    if (uiState.isConcertActive) {
                        hapticController.vibrateBeat(uiState.intensity.factor)
                    } else {
                        hapticController.vibratePulsePeak()
                    }
                }
            }
        )

        // Cabecera superior
        AnimatedVisibility(
            visible = uiState.areControlsVisible,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 18.dp, start = 20.dp, end = 20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "BIT | ARMY LIGHT",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 3.sp
                    )
                )
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PurplePrimary.copy(alpha = 0.20f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBright.copy(alpha = 0.35f)),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "Fan-made · No oficial",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(color = PurpleBright)
                    )
                }
            }
        }

        // Panel de controles inferior
        AnimatedVisibility(
            visible = uiState.areControlsVisible,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xE6080312))
                    .border(1.dp, GlassSurfaceBorder, RoundedCornerShape(28.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Selector de Intensidad (30%, 60%, 100%)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("INTENSIDAD", style = MaterialTheme.typography.labelSmall.copy(color = TextTertiary))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LightIntensity.values().forEach { level ->
                            val isSelected = uiState.intensity == level
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) PurpleVibrant else GlassSurface)
                                    .border(1.dp, if (isSelected) PurpleBright else GlassSurfaceBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        hapticController.vibrateClick()
                                        viewModel.setIntensity(level)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = level.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Botones Pulso y Concierto
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ButtonMode("Pulso", if (uiState.isPulseActive) "Activo" else "Apagado", Icons.Default.Bolt, uiState.isPulseActive, Modifier.weight(1f)) {
                        hapticController.vibrateClick()
                        viewModel.togglePulse()
                    }
                    ButtonMode("Concierto", if (uiState.isConcertActive) uiState.concertProgram.title else "Modo Show", Icons.Default.MusicNote, uiState.isConcertActive, Modifier.weight(1f)) {
                        hapticController.vibrateClick()
                        viewModel.toggleConcert()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Opciones secundarias: Vibración & Ahorro OLED
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ChipOption(Icons.Default.Vibration, "Vibración", uiState.isVibrationEnabled) {
                        hapticController.vibrateClick()
                        viewModel.toggleVibration()
                    }
                    ChipOption(Icons.Default.Eco, "Ahorro OLED", uiState.isBatterySaverEnabled) {
                        hapticController.vibrateClick()
                        viewModel.toggleBatterySaver()
                    }
                }
            }
        }
    }
}`
  },
  {
    path: 'android/app/src/main/java/com/bit/armylight/ui/components/LightCoreVisualizer.kt',
    filename: 'LightCoreVisualizer.kt',
    language: 'kotlin',
    description: 'Visualizador gráfico customizado: gradientes radiales púrpuras multicapa, brillo bloom y respiración animada.',
    content: `package com.bit.armylight.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.bit.armylight.ui.theme.*
import com.bit.armylight.viewmodel.ConcertProgram
import com.bit.armylight.viewmodel.LightIntensity

@Composable
fun LightCoreVisualizer(
    modifier: Modifier = Modifier,
    isPulseActive: Boolean,
    isConcertActive: Boolean,
    concertProgram: ConcertProgram,
    intensity: LightIntensity,
    isBatterySaver: Boolean,
    onPeakPulse: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LightEngine")

    val pulseDuration = if (isConcertActive) {
        when (concertProgram) {
            ConcertProgram.STROBE -> 450
            ConcertProgram.WAVE -> 1200
            ConcertProgram.SUPERNOVA -> 800
            ConcertProgram.AURORA -> 2000
        }
    } else {
        2400
    }

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = if (isPulseActive) 0.82f else 1.0f,
        targetValue = if (isPulseActive) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = pulseDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val pulseGlowAlpha by infiniteTransition.animateFloat(
        initialValue = if (isPulseActive) 0.45f else 0.85f,
        targetValue = if (isPulseActive) 1.0f else 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = pulseDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    LaunchedEffect(pulseScale) {
        if (isPulseActive && pulseScale > 1.23f) {
            onPeakPulse()
        }
    }

    val factor = intensity.factor

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val minDim = minOf(size.width, size.height)
        val baseRadius = (minDim * 0.40f) * pulseScale

        // Fondo ambiental
        if (!isBatterySaver) {
            val ambientRadius = (minDim * 0.95f) * pulseScale
            val ambientBrush = Brush.radialGradient(
                colors = listOf(
                    PurpleDeep.copy(alpha = 0.55f * factor * pulseGlowAlpha),
                    PurpleAmbient.copy(alpha = 0.25f * factor),
                    Color.Transparent
                ),
                center = center,
                radius = ambientRadius
            )
            drawCircle(brush = ambientBrush, radius = ambientRadius, center = center)
        }

        // Corona de brillo exterior (Bloom)
        val coronaRadius = baseRadius * 1.55f
        val coronaBrush = Brush.radialGradient(
            colors = listOf(
                PurpleGlow.copy(alpha = 0.75f * factor * pulseGlowAlpha),
                PurplePrimary.copy(alpha = 0.40f * factor * pulseGlowAlpha),
                Color.Transparent
            ),
            center = center,
            radius = coronaRadius
        )
        drawCircle(brush = coronaBrush, radius = coronaRadius, center = center)

        // Orbe púrpura principal
        val bodyBrush = Brush.radialGradient(
            colors = listOf(
                PurpleBright.copy(alpha = 0.95f * factor),
                PurpleVibrant.copy(alpha = 0.90f * factor),
                PurplePrimary.copy(alpha = 0.70f * factor),
                Color.Transparent
            ),
            center = center,
            radius = baseRadius
        )
        drawCircle(brush = bodyBrush, radius = baseRadius, center = center)

        // Núcleo blanco-violeta central
        val coreRadius = baseRadius * 0.38f
        val coreBrush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.98f * factor),
                PurpleCore.copy(alpha = 0.90f * factor),
                PurpleBright.copy(alpha = 0.50f * factor),
                Color.Transparent
            ),
            center = center,
            radius = coreRadius
        )
        drawCircle(brush = coreBrush, radius = coreRadius, center = center)
    }
}`
  },
  {
    path: 'android/app/src/main/java/com/bit/armylight/viewmodel/LightViewModel.kt',
    filename: 'LightViewModel.kt',
    language: 'kotlin',
    description: 'Gestor de estado reactivo: pulsos, ritmos de concierto, intensidades y temporizador de autohide.',
    content: `package com.bit.armylight.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LightIntensity(val factor: Float, val label: String) {
    THIRTY(0.30f, "30%"),
    SIXTY(0.60f, "60%"),
    HUNDRED(1.00f, "100%")
}

enum class ConcertProgram(val title: String, val bpm: Int) {
    WAVE("Onda Púrpura", 70),
    STROBE("Strobe Beat", 130),
    SUPERNOVA("Supernova", 90),
    AURORA("Aurora", 60)
}

data class LightUiState(
    val isPulseActive: Boolean = true,
    val isConcertActive: Boolean = false,
    val intensity: LightIntensity = LightIntensity.HUNDRED,
    val isVibrationEnabled: Boolean = true,
    val isBatterySaverEnabled: Boolean = false,
    val concertProgram: ConcertProgram = ConcertProgram.WAVE,
    val areControlsVisible: Boolean = true
)

class LightViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LightUiState())
    val uiState: StateFlow<LightUiState> = _uiState.asStateFlow()

    private var autoHideJob: Job? = null

    init {
        scheduleControlsAutoHide()
    }

    fun togglePulse() {
        _uiState.update { it.copy(isPulseActive = !it.isPulseActive) }
        pingUserActivity()
    }

    fun toggleConcert() {
        _uiState.update { current ->
            val next = !current.isConcertActive
            current.copy(isConcertActive = next, isPulseActive = if (next) true else current.isPulseActive)
        }
        pingUserActivity()
    }

    fun setConcertProgram(program: ConcertProgram) {
        _uiState.update { it.copy(concertProgram = program, isConcertActive = true) }
        pingUserActivity()
    }

    fun setIntensity(intensity: LightIntensity) {
        _uiState.update { it.copy(intensity = intensity) }
        pingUserActivity()
    }

    fun toggleVibration() {
        _uiState.update { it.copy(isVibrationEnabled = !it.isVibrationEnabled) }
        pingUserActivity()
    }

    fun toggleBatterySaver() {
        _uiState.update { it.copy(isBatterySaverEnabled = !it.isBatterySaverEnabled) }
        pingUserActivity()
    }

    fun toggleControlsVisibility() {
        _uiState.update { it.copy(areControlsVisible = !it.areControlsVisible) }
        if (_uiState.value.areControlsVisible) scheduleControlsAutoHide()
    }

    fun pingUserActivity() {
        if (_uiState.value.areControlsVisible) scheduleControlsAutoHide()
    }

    private fun scheduleControlsAutoHide() {
        autoHideJob?.cancel()
        autoHideJob = viewModelScope.launch {
            delay(6000L)
            _uiState.update { it.copy(areControlsVisible = false) }
        }
    }
}`
  },
  {
    path: 'android/app/src/main/java/com/bit/armylight/util/HapticController.kt',
    filename: 'HapticController.kt',
    language: 'kotlin',
    description: 'Controlador de vibración nativo para Android: soporta API 26 a 35 con amplitudes dinámicas.',
    content: `package com.bit.armylight.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticController(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun vibrateClick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15)
            }
        } catch (_: Exception) {}
    }

    fun vibratePulsePeak() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(35, 120)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(35)
            }
        } catch (_: Exception) {}
    }

    fun vibrateBeat(intensity: Float) {
        try {
            val amplitude = (intensity.coerceIn(0.1f, 1.0f) * 255).toInt().coerceIn(1, 255)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(50, amplitude)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(40)
            }
        } catch (_: Exception) {}
    }

    fun cancel() {
        try { vibrator?.cancel() } catch (_: Exception) {}
    }
}`
  },
  {
    path: 'android/app/src/main/AndroidManifest.xml',
    filename: 'AndroidManifest.xml',
    language: 'xml',
    description: 'Manifiesto de la app: 100% offline (CERO permisos de red/GPS/cámara), únicamente permiso de vibración.',
    content: `<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Único permiso requerido para vibración en conciertos -->
    <!-- CERO permisos de Internet, GPS o Cámara -->
    <uses-permission android:name="android.permission.VIBRATE" />

    <application
        android:allowBackup="false"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="false"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.BitArmyLight">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
            android:theme="@style/Theme.BitArmyLight">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>`
  },
  {
    path: 'android/app/build.gradle.kts',
    filename: 'app/build.gradle.kts',
    language: 'kotlin',
    description: 'Configuración Gradle del módulo app con Jetpack Compose y optimizaciones release R8.',
    content: `plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.bit.armylight"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bit.armylight"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
}`
  },
  {
    path: 'android/gradle/libs.versions.toml',
    filename: 'libs.versions.toml',
    language: 'toml',
    description: 'Catálogo de dependencias moderno de Gradle con Android Gradle Plugin 8.7 y Kotlin 2.0.',
    content: `[versions]
agp = "8.7.3"
kotlin = "2.0.21"
coreKtx = "1.15.0"
lifecycleRuntimeKtx = "2.8.7"
activityCompose = "1.9.3"
composeBom = "2024.12.01"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }`
  },
  {
    path: 'android/app/src/main/java/com/bit/armylight/ui/theme/Color.kt',
    filename: 'Color.kt',
    language: 'kotlin',
    description: 'Paleta cromática: tonos púrpuras de alta intensidad para conciertos, negro AMOLED #000000 y superficies vítreas.',
    content: `package com.bit.armylight.ui.theme

import androidx.compose.ui.graphics.Color

val BackgroundDark = Color(0xFF030107)
val BackgroundAmoled = Color(0xFF000000)

val PurplePrimary = Color(0xFF9D44B5)
val PurpleVibrant = Color(0xFFBF55EC)
val PurpleGlow = Color(0xFF8B25C6)
val PurpleBright = Color(0xFFC084FC)
val PurpleCore = Color(0xFFF3E8FF)
val PurpleDeep = Color(0xFF38084A)
val PurpleAmbient = Color(0xFF1E0427)

val GlassSurface = Color(0x2AFFFFFF)
val GlassSurfaceBorder = Color(0x3DFFFFFF)
val GlassSurfaceActive = Color(0x529D44B5)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xB3FFFFFF)
val TextTertiary = Color(0x80D8B4FE)`
  },
  {
    path: 'android/gradle/wrapper/gradle-wrapper.properties',
    filename: 'gradle-wrapper.properties',
    language: 'properties',
    description: 'Configuración oficial de Gradle Wrapper 8.11.1.',
    content: `distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\\://services.gradle.org/distributions/gradle-8.11.1-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists`
  }
];

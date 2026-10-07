package com.bit.armylight.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.bit.armylight.ui.components.LightCoreVisualizer
import com.bit.armylight.ui.theme.BackgroundAmoled
import com.bit.armylight.ui.theme.BackgroundDark
import com.bit.armylight.ui.theme.GlassSurface
import com.bit.armylight.ui.theme.GlassSurfaceActive
import com.bit.armylight.ui.theme.GlassSurfaceBorder
import com.bit.armylight.ui.theme.PurpleBright
import com.bit.armylight.ui.theme.PurplePrimary
import com.bit.armylight.ui.theme.PurpleVibrant
import com.bit.armylight.ui.theme.TextPrimary
import com.bit.armylight.ui.theme.TextSecondary
import com.bit.armylight.ui.theme.TextTertiary
import com.bit.armylight.util.AudioBeatDetector
import com.bit.armylight.util.HapticController
import com.bit.armylight.viewmodel.ConcertProgram
import com.bit.armylight.viewmodel.LightIntensity
import com.bit.armylight.viewmodel.LightViewModel

@Composable
fun ArmyLightScreen(
    viewModel: LightViewModel,
    hapticController: HapticController
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var isRhythmActive by remember {
        mutableStateOf(false)
    }

    var rhythmLevel by remember {
        mutableFloatStateOf(0f)
    }

    val audioDetector = remember {
        AudioBeatDetector { level ->
            rhythmLevel = level
        }
    }

    val microphonePermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                isRhythmActive = true
                audioDetector.start()
            } else {
                isRhythmActive = false
                audioDetector.stop()
                rhythmLevel = 0f
            }
        }

    /*
     * Limpieza del detector al salir de la pantalla.
     */
    DisposableEffect(Unit) {
        onDispose {
            audioDetector.stop()
        }
    }

    /*
     * Arranque/parada del detector.
     */
    LaunchedEffect(isRhythmActive) {

        if (isRhythmActive) {

            val permissionGranted =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED

            if (permissionGranted) {
                audioDetector.start()
            }

        } else {
            audioDetector.stop()
            rhythmLevel = 0f
        }
    }

    val bgColor =
        if (uiState.isBatterySaverEnabled) {
            BackgroundAmoled
        } else {
            BackgroundDark
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null
            ) {
                hapticController.vibrateClick()
                viewModel.toggleControlsVisibility()
            }
    ) {

        /*
         * VISUALIZADOR
         */
        LightCoreVisualizer(
            isPulseActive = uiState.isPulseActive,
            isConcertActive = uiState.isConcertActive,
            concertProgram = uiState.concertProgram,
            intensity = uiState.intensity,
            isBatterySaver = uiState.isBatterySaverEnabled,
            isRhythmActive = isRhythmActive,
            rhythmLevel = rhythmLevel,
            onPeakPulse = {

                if (uiState.isVibrationEnabled) {

                    if (uiState.isConcertActive) {
                        hapticController.vibrateBeat(
                            uiState.intensity.factor
                        )
                    } else {
                        hapticController.vibratePulsePeak()
                    }
                }
            }
        )

        /*
         * CABECERA
         */
        AnimatedVisibility(
            visible = uiState.areControlsVisible,
            enter = fadeIn() +
                    slideInVertically(
                        initialOffsetY = { -it }
                    ),
            exit = fadeOut() +
                    slideOutVertically(
                        targetOffsetY = { -it }
                    ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(
                    top = 18.dp,
                    start = 20.dp,
                    end = 20.dp
                )
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = "BIT | ARMY LIGHT",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 3.sp
                    )
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PurplePrimary.copy(alpha = 0.20f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        PurpleBright.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                ) {

                    Text(
                        text = "Fan-made · No oficial",
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 3.dp
                        ),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PurpleBright,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        /*
         * PANEL DE CONTROLES
         */
        AnimatedVisibility(
            visible = uiState.areControlsVisible,
            enter = fadeIn() +
                    slideInVertically(
                        initialOffsetY = { it }
                    ),
            exit = fadeOut() +
                    slideOutVertically(
                        targetOffsetY = { it }
                    ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(
                    horizontal = 16.dp,
                    vertical = 18.dp
                )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(28.dp)
                    )
                    .background(
                        Color(0xE6080312)
                    )
                    .border(
                        1.dp,
                        GlassSurfaceBorder,
                        RoundedCornerShape(28.dp)
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                /*
                 * INTENSIDAD
                 */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text = "INTENSIDAD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextTertiary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    )

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        LightIntensity.values()
                            .forEach { level ->

                                val isSelected =
                                    uiState.intensity == level

                                Box(
                                    modifier = Modifier
                                        .clip(
                                            RoundedCornerShape(12.dp)
                                        )
                                        .background(
                                            if (isSelected) {
                                                PurpleVibrant
                                            } else {
                                                GlassSurface
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) {
                                                PurpleBright
                                            } else {
                                                GlassSurfaceBorder
                                            },
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            hapticController
                                                .vibrateClick()

                                            viewModel
                                                .setIntensity(level)
                                        }
                                        .padding(
                                            horizontal = 14.dp,
                                            vertical = 7.dp
                                        )
                                ) {

                                    Text(
                                        text = level.label,
                                        fontSize = 12.sp,
                                        fontWeight =
                                            if (isSelected) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Medium
                                            },
                                        color =
                                            if (isSelected) {
                                                Color.White
                                            } else {
                                                TextSecondary
                                            }
                                    )
                                }
                            }
                    }
                }

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                /*
                 * PULSO + CONCIERTO
                 */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    ModeToggleButton(
                        title = "Pulso",
                        subtitle =
                            if (uiState.isPulseActive) {
                                "Activo"
                            } else {
                                "Apagado"
                            },
                        icon = Icons.Default.Bolt,
                        isActive =
                            uiState.isPulseActive,
                        modifier =
                            Modifier.weight(1f),
                        onClick = {
                            hapticController.vibrateClick()
                            viewModel.togglePulse()
                        }
                    )

                    ModeToggleButton(
                        title = "Concierto",
                        subtitle =
                            if (uiState.isConcertActive) {
                                uiState.concertProgram.title
                            } else {
                                "Modo Show"
                            },
                        icon = Icons.Default.MusicNote,
                        isActive =
                            uiState.isConcertActive,
                        modifier =
                            Modifier.weight(1f),
                        onClick = {
                            hapticController.vibrateClick()
                            viewModel.toggleConcert()
                        }
                    )
                }

                /*
                 * PROGRAMAS
                 */
                if (uiState.isConcertActive) {

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(6.dp)
                    ) {

                        ConcertProgram.values()
                            .forEach { program ->

                                val isCurrent =
                                    uiState.concertProgram == program

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(
                                            RoundedCornerShape(10.dp)
                                        )
                                        .background(
                                            if (isCurrent) {
                                                GlassSurfaceActive
                                            } else {
                                                Color.Transparent
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            if (isCurrent) {
                                                PurpleBright.copy(
                                                    alpha = 0.8f
                                                )
                                            } else {
                                                GlassSurfaceBorder.copy(
                                                    alpha = 0.3f
                                                )
                                            },
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            hapticController
                                                .vibrateClick()

                                            viewModel
                                                .setConcertProgram(
                                                    program
                                                )
                                        }
                                        .padding(
                                            vertical = 6.dp
                                        ),
                                    contentAlignment =
                                        Alignment.Center
                                ) {

                                    Text(
                                        text = program.title,
                                        fontSize = 10.sp,
                                        fontWeight =
                                            if (isCurrent) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Normal
                                            },
                                        color =
                                            if (isCurrent) {
                                                TextPrimary
                                            } else {
                                                TextSecondary
                                            },
                                        maxLines = 1
                                    )
                                }
                            }
                    }
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                /*
                 * RITMO + VIBRACIÓN + OLED
                 */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    MiniOptionChip(
                        icon = Icons.Default.Mic,
                        label = "Ritmo",
                        isEnabled = isRhythmActive,
                        onClick = {

                            hapticController.vibrateClick()

                            if (isRhythmActive) {

                                isRhythmActive = false
                                audioDetector.stop()

                            } else {

                                val granted =
                                    ContextCompat
                                        .checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        ) ==
                                        PackageManager.PERMISSION_GRANTED

                                if (granted) {

                                    isRhythmActive = true

                                } else {

                                    microphonePermissionLauncher
                                        .launch(
                                            Manifest.permission.RECORD_AUDIO
                                        )
                                }
                            }
                        }
                    )

                    MiniOptionChip(
                        icon = Icons.Default.Vibration,
                        label = "Vibración",
                        isEnabled =
                            uiState.isVibrationEnabled,
                        onClick = {
                            hapticController.vibrateClick()
                            viewModel.toggleVibration()
                        }
                    )

                    MiniOptionChip(
                        icon = Icons.Default.Eco,
                        label = "Ahorro OLED",
                        isEnabled =
                            uiState.isBatterySaverEnabled,
                        onClick = {
                            hapticController.vibrateClick()
                            viewModel.toggleBatterySaver()
                        }
                    )
                }

                /*
                 * INDICADOR DE NIVEL DEL RITMO
                 */
                if (isRhythmActive) {

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                       text = "🎤 RITMO: ${(rhythmLevel * 100).toInt()}%"
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurpleBright
                    )
                }
            }
        }

        /*
         * CONTROLES OCULTOS
         */
        if (!uiState.areControlsVisible) {

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 20.dp)
                    .clip(CircleShape)
                    .background(
                        Color.Black.copy(alpha = 0.4f)
                    )
                    .padding(
                        horizontal = 14.dp,
                        vertical = 6.dp
                    )
            ) {

                Text(
                    text = "Toca para controles",
                    fontSize = 11.sp,
                    color = Color.White.copy(
                        alpha = 0.45f
                    ),
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}

@Composable
private fun ModeToggleButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Box(
        modifier = modifier
            .clip(
                RoundedCornerShape(18.dp)
            )
            .background(
                if (isActive) {
                    PurpleVibrant.copy(alpha = 0.35f)
                } else {
                    GlassSurface
                }
            )
            .border(
                1.5.dp,
                if (isActive) {
                    PurpleBright
                } else {
                    GlassSurfaceBorder
                },
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(
                vertical = 12.dp,
                horizontal = 14.dp
            )
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) {
                            PurpleVibrant
                        } else {
                            Color.White.copy(alpha = 0.08f)
                        }
                    ),
                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint =
                        if (isActive) {
                            Color.White
                        } else {
                            TextSecondary
                        },
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(
                modifier = Modifier.width(10.dp)
            )

            Column {

                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color =
                        if (isActive) {
                            PurpleBright
                        } else {
                            TextSecondary
                        }
                )
            }
        }
    }
}

@Composable
private fun MiniOptionChip(
    icon: ImageVector,
    label: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .clip(
                RoundedCornerShape(14.dp)
            )
            .background(
                if (isEnabled) {
                    PurplePrimary.copy(alpha = 0.20f)
                } else {
                    Color.White.copy(alpha = 0.04f)
                }
            )
            .border(
                1.dp,
                if (isEnabled) {
                    PurpleBright.copy(alpha = 0.5f)
                } else {
                    Color.White.copy(alpha = 0.08f)
                },
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = 10.dp,
                vertical = 7.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Icon(
            imageVector = icon,
            contentDescription = label,
            tint =
                if (isEnabled) {
                    PurpleBright
                } else {
                    TextSecondary.copy(alpha = 0.6f)
                },
            modifier = Modifier.size(15.dp)
        )

        Spacer(
            modifier = Modifier.width(5.dp)
        )

        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight =
                if (isEnabled) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                },
            color =
                if (isEnabled) {
                    TextPrimary
                } else {
                    TextSecondary
                }
        )
    }
}

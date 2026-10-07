package com.bit.armylight.ui.screens

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
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.bit.armylight.util.HapticController
import com.bit.armylight.viewmodel.ConcertProgram
import com.bit.armylight.viewmodel.LightIntensity
import com.bit.armylight.viewmodel.LightViewModel

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
        // Visualizador de luz central
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

        // Cabecera superior con marca y badge no oficial
        AnimatedVisibility(
            visible = uiState.areControlsVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 18.dp, start = 20.dp, end = 20.dp)
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

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PurplePrimary.copy(alpha = 0.20f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBright.copy(alpha = 0.35f)),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "Fan-made · No oficial",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PurpleBright,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        // Panel de controles inferior
        AnimatedVisibility(
            visible = uiState.areControlsVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
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
                    Text(
                        text = "INTENSIDAD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextTertiary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LightIntensity.values().forEach { level ->
                            val isSelected = uiState.intensity == level
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) PurpleVibrant else GlassSurface)
                                    .border(
                                        1.dp,
                                        if (isSelected) PurpleBright else GlassSurfaceBorder,
                                        RoundedCornerShape(12.dp)
                                    )
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

                // Botones principales: PULSO y CONCIERTO
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón Pulso
                    ModeToggleButton(
                        title = "Pulso",
                        subtitle = if (uiState.isPulseActive) "Activo" else "Apagado",
                        icon = Icons.Default.Bolt,
                        isActive = uiState.isPulseActive,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            hapticController.vibrateClick()
                            viewModel.togglePulse()
                        }
                    )

                    // Botón Concierto
                    ModeToggleButton(
                        title = "Concierto",
                        subtitle = if (uiState.isConcertActive) uiState.concertProgram.title else "Modo Show",
                        icon = Icons.Default.MusicNote,
                        isActive = uiState.isConcertActive,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            hapticController.vibrateClick()
                            viewModel.toggleConcert()
                        }
                    )
                }

                // Si modo concierto está activo, mostrar programas de concierto
                if (uiState.isConcertActive) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ConcertProgram.values().forEach { program ->
                            val isCurrent = uiState.concertProgram == program
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isCurrent) GlassSurfaceActive else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (isCurrent) PurpleBright.copy(alpha = 0.8f) else GlassSurfaceBorder.copy(alpha = 0.3f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        hapticController.vibrateClick()
                                        viewModel.setConcertProgram(program)
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = program.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) TextPrimary else TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Opciones secundarias: Vibración & Ahorro de batería OLED
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón Vibración
                    MiniOptionChip(
                        icon = Icons.Default.Vibration,
                        label = "Vibración",
                        isEnabled = uiState.isVibrationEnabled,
                        onClick = {
                            hapticController.vibrateClick()
                            viewModel.toggleVibration()
                        }
                    )

                    // Botón Ahorro OLED
                    MiniOptionChip(
                        icon = Icons.Default.Eco,
                        label = "Ahorro OLED",
                        isEnabled = uiState.isBatterySaverEnabled,
                        onClick = {
                            hapticController.vibrateClick()
                            viewModel.toggleBatterySaver()
                        }
                    )
                }
            }
        }

        // Pista de interacción sutil para volver a mostrar controles cuando están ocultos
        if (!uiState.areControlsVisible) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 20.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Toca para controles",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.45f),
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
            .clip(RoundedCornerShape(18.dp))
            .background(if (isActive) PurpleVibrant.copy(alpha = 0.35f) else GlassSurface)
            .border(
                1.5.dp,
                if (isActive) PurpleBright else GlassSurfaceBorder,
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isActive) PurpleVibrant else Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isActive) Color.White else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

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
                    color = if (isActive) PurpleBright else TextSecondary
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
            .clip(RoundedCornerShape(14.dp))
            .background(if (isEnabled) PurplePrimary.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.04f))
            .border(
                1.dp,
                if (isEnabled) PurpleBright.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isEnabled) PurpleBright else TextSecondary.copy(alpha = 0.6f),
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isEnabled) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isEnabled) TextPrimary else TextSecondary
        )
    }
}

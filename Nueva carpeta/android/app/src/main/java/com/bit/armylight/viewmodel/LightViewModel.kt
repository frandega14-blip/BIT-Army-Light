package com.bit.armylight.viewmodel

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
    val areControlsVisible: Boolean = true,
    val customBpm: Int = 110,
    val lastTapTime: Long = 0L
)

class LightViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LightUiState())
    val uiState: StateFlow<LightUiState> = _uiState.asStateFlow()

    private var autoHideJob: Job? = null

    init {
        scheduleControlsAutoHide()
    }

    fun togglePulse() {
        _uiState.update { current ->
            val nextPulse = !current.isPulseActive
            // Si desactivamos concierto y activamos pulso
            current.copy(isPulseActive = nextPulse)
        }
        pingUserActivity()
    }

    fun toggleConcert() {
        _uiState.update { current ->
            val nextConcert = !current.isConcertActive
            current.copy(
                isConcertActive = nextConcert,
                // Al activar concierto, aseguramos que el pulso esté encendido
                isPulseActive = if (nextConcert) true else current.isPulseActive
            )
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
        if (_uiState.value.areControlsVisible) {
            scheduleControlsAutoHide()
        } else {
            autoHideJob?.cancel()
        }
    }

    fun pingUserActivity() {
        if (_uiState.value.areControlsVisible) {
            scheduleControlsAutoHide()
        }
    }

    private fun scheduleControlsAutoHide() {
        autoHideJob?.cancel()
        autoHideJob = viewModelScope.launch {
            delay(6000L) // Ocultar suavemente después de 6 segundos de inactividad
            _uiState.update { it.copy(areControlsVisible = false) }
        }
    }
}

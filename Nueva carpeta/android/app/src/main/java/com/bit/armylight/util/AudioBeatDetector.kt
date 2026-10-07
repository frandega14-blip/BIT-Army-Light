package com.bit.armylight.util

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlin.math.sqrt

class AudioBeatDetector(
    private val onLevelChanged: (Float) -> Unit
) {

    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    @Volatile
    private var isRunning = false

    fun start() {
        if (isRunning) return

        val sampleRate = 44100

        val minBuffer = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        if (minBuffer <= 0) {
            onLevelChanged(0f)
            return
        }

        val bufferSize = maxOf(
            minBuffer,
            sampleRate / 5
        )

        try {
            val recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            audioRecord = recorder
            isRunning = true

            recorder.startRecording()

            recordingThread = Thread {
                val buffer = ShortArray(bufferSize / 2)

                var smoothedLevel = 0f
                var previousLevel = 0f

                while (isRunning) {

                    val read = try {
                        recorder.read(
                            buffer,
                            0,
                            buffer.size
                        )
                    } catch (_: Exception) {
                        -1
                    }

                    if (read <= 0) {
                        continue
                    }

                    var sum = 0.0

                    for (i in 0 until read) {
                        val sample = buffer[i].toDouble()
                        sum += sample * sample
                    }

                    val rms = sqrt(sum / read)

                    /*
                     * Normalizamos el volumen.
                     *
                     * 0 = silencio
                     * 1 = sonido muy fuerte
                     */
                    val normalized = (
                        rms / 7000.0
                    ).coerceIn(0.0, 1.0)

                    /*
                     * Suavizado para evitar que la luz
                     * tiemble demasiado.
                     */
                    smoothedLevel =
                        (smoothedLevel * 0.72f) +
                        (normalized.toFloat() * 0.28f)

                    /*
                     * Detectamos golpes fuertes.
                     */
                    val beatBoost =
                        if (
                            smoothedLevel > 0.42f &&
                            smoothedLevel > previousLevel + 0.08f
                        ) {
                            0.35f
                        } else {
                            0f
                        }

                    val finalLevel = (
                        smoothedLevel + beatBoost
                    ).coerceIn(0f, 1f)

                    previousLevel = smoothedLevel

                    onLevelChanged(finalLevel)

                    try {
                        Thread.sleep(45)
                    } catch (_: InterruptedException) {
                        break
                    }
                }

                try {
                    recorder.stop()
                } catch (_: Exception) {
                }

                recorder.release()
            }

            recordingThread?.start()

        } catch (_: SecurityException) {
            isRunning = false
            onLevelChanged(0f)
        } catch (_: Exception) {
            isRunning = false
            onLevelChanged(0f)
        }
    }

    fun stop() {
        isRunning = false

        recordingThread?.interrupt()
        recordingThread = null

        try {
            audioRecord?.stop()
        } catch (_: Exception) {
        }

        try {
            audioRecord?.release()
        } catch (_: Exception) {
        }

        audioRecord = null

        onLevelChanged(0f)
    }
}

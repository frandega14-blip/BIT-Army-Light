package com.bit.armylight.util

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import kotlin.math.sqrt

class AudioBeatDetector(
    private val onLevelChanged: (Float) -> Unit
) {

    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null

    @Volatile
    private var isRunning = false

    private val mainHandler =
        Handler(Looper.getMainLooper())


    fun start() {

        if (isRunning) return

        val sampleRate = 44100

        val minBuffer =
            AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

        if (minBuffer <= 0) {
            sendLevel(0f)
            return
        }

        val bufferSize =
            maxOf(
                minBuffer,
                sampleRate / 10
            )

        try {

            val recorder =
                AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )

            if (
                recorder.state !=
                AudioRecord.STATE_INITIALIZED
            ) {
                recorder.release()
                sendLevel(0f)
                return
            }

            audioRecord = recorder
            isRunning = true

            recorder.startRecording()


            recordingThread =
                Thread {

                    val buffer =
                        ShortArray(
                            bufferSize / 2
                        )

                    var smoothedLevel = 0f
                    var previousLevel = 0f

                    while (isRunning) {

                        val read =
                            try {
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


                        /*
                         * CALCULAR RMS
                         */

                        var sum = 0.0

                        for (i in 0 until read) {

                            val sample =
                                buffer[i].toDouble()

                            sum +=
                                sample * sample
                        }

                        val rms =
                            sqrt(
                                sum / read
                            )


                        /*
                         * SENSIBILIDAD MUSICAL
                         *
                         * Más sensible que la versión
                         * original, pero sin exagerar.
                         */

                        val normalized =
                            (
                                rms / 700.0
                            )
                                .coerceIn(
                                    0.0,
                                    1.0
                                )
                                .toFloat()


                        /*
                         * SUAVIZADO
                         *
                         * La música tiene más influencia
                         * que el valor anterior, pero la
                         * transición sigue siendo suave.
                         */

                        smoothedLevel =
                            (
                                smoothedLevel * 0.40f
                            ) +
                            (
                                normalized * 0.60f
                            )


                        /*
                         * DETECCIÓN DE CAMBIOS DE RITMO
                         */

                        val difference =
                            smoothedLevel -
                            previousLevel


                        /*
                         * PEQUEÑO IMPULSO EN LOS GOLPES
                         *
                         * No domina la señal.
                         * Solo hace destacar los golpes.
                         */

                        val beatBoost =
                            when {

                                difference > 0.08f ->
                                    0.20f

                                difference > 0.035f ->
                                    0.08f

                                else ->
                                    0f
                            }


                        /*
                         * NIVEL FINAL
                         */

                        val finalLevel =
                            (
                                smoothedLevel +
                                beatBoost
                            )
                                .coerceIn(
                                    0f,
                                    1f
                                )


                        previousLevel =
                            smoothedLevel


                        /*
                         * ACTUALIZAR LA INTERFAZ
                         */

                        sendLevel(
                            finalLevel
                        )


                        /*
                         * FRECUENCIA DE ACTUALIZACIÓN
                         */

                        try {
                            Thread.sleep(30)
                        } catch (_: InterruptedException) {
                            break
                        }
                    }


                    /*
                     * LIMPIEZA DEL GRABADOR
                     */

                    try {
                        recorder.stop()
                    } catch (_: Exception) {
                    }

                    try {
                        recorder.release()
                    } catch (_: Exception) {
                    }
                }


            recordingThread?.start()

        } catch (_: SecurityException) {

            isRunning = false
            sendLevel(0f)

        } catch (_: Exception) {

            isRunning = false
            sendLevel(0f)
        }
    }


    /*
     * ENVIAR EL NIVEL AL HILO PRINCIPAL
     */

    private fun sendLevel(
        level: Float
    ) {

        mainHandler.post {

            onLevelChanged(
                level.coerceIn(
                    0f,
                    1f
                )
            )
        }
    }


    /*
     * DETENER DETECTOR
     */

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

        sendLevel(0f)
    }
}

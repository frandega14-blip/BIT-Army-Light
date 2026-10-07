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

                    var energyLevel = 0f
                    var displayLevel = 0f
                    var previousEnergy = 0f

                    var averageEnergy = 0f
                    var beatEnergy = 0f

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
                         * ENERGÍA DE LA SEÑAL
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
                         * NIVEL BASE DE LA MÚSICA
                         *
                         * Sensibilidad alta, pero
                         * todavía controlada.
                         */

                        val normalized =
                            (
                                rms / 650.0
                            )
                                .coerceIn(
                                    0.0,
                                    1.0
                                )
                                .toFloat()


                        /*
                         * SEGUIMIENTO DE LA ENERGÍA
                         *
                         * La música reciente tiene
                         * bastante peso.
                         */

                        energyLevel =
                            (
                                energyLevel * 0.35f
                            ) +
                            (
                                normalized * 0.65f
                            )


                        /*
                         * PROMEDIO LENTO
                         *
                         * Sirve para distinguir un
                         * golpe musical de un ruido
                         * constante.
                         */

                        averageEnergy =
                            (
                                averageEnergy * 0.92f
                            ) +
                            (
                                energyLevel * 0.08f
                            )


                        /*
                         * DIFERENCIA CONTRA EL NIVEL BASE
                         */

                        val musicRise =
                            (
                                energyLevel -
                                averageEnergy
                            )
                                .coerceAtLeast(0f)


                        /*
                         * CAMBIO ENTRE MUESTRAS
                         */

                        val instantRise =
                            (
                                energyLevel -
                                previousEnergy
                            )
                                .coerceAtLeast(0f)


                        /*
                         * DETECCIÓN DE GOLPE
                         *
                         * Los cambios rápidos reciben
                         * más importancia.
                         */

                        val beatStrength =
                            (
                                musicRise * 2.2f
                            ) +
                            (
                                instantRise * 1.8f
                            )


                        /*
                         * LIMITAR EL BEAT
                         */

                        beatEnergy =
                            beatStrength
                                .coerceIn(
                                    0f,
                                    1f
                                )


                        /*
                         * NIVEL MUSICAL FINAL
                         *
                         * 75% energía de música
                         * 25% golpes
                         */

                        val targetLevel =
                            (
                                energyLevel * 0.75f
                            ) +
                            (
                                beatEnergy * 0.25f
                            )
                                .coerceIn(
                                    0f,
                                    1f
                                )


                        /*
                         * RESPUESTA RÁPIDA AL SUBIR
                         * Y SUAVE AL BAJAR.
                         *
                         * Esto hace que la luz
                         * siga mejor la música.
                         */

                        displayLevel =
                            if (
                                targetLevel >
                                displayLevel
                            ) {

                                (
                                    displayLevel * 0.25f
                                ) +
                                (
                                    targetLevel * 0.75f
                                )

                            } else {

                                (
                                    displayLevel * 0.65f
                                ) +
                                (
                                    targetLevel * 0.35f
                                )
                            }


                        /*
                         * PEQUEÑO IMPULSO FINAL
                         *
                         * Evita que los golpes fuertes
                         * queden demasiado apagados.
                         */

                        val finalLevel =
                            (
                                displayLevel +
                                beatEnergy * 0.10f
                            )
                                .coerceIn(
                                    0f,
                                    1f
                                )


                        previousEnergy =
                            energyLevel


                        /*
                         * ACTUALIZAR LA INTERFAZ
                         */

                        sendLevel(
                            finalLevel
                        )


                        try {
                            Thread.sleep(30)
                        } catch (_: InterruptedException) {
                            break
                        }
                    }


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
     * ENVIAR NIVEL AL HILO PRINCIPAL
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

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
                sampleRate / 5
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
                         * MAYOR SENSIBILIDAD
                         *
                         * Antes:
                         * rms / 7000
                         *
                         * Ahora:
                         * rms / 2500
                         */

                        val normalized =
                            (
                                rms / 2500.0
                            )
                                .coerceIn(
                                    0.0,
                                    1.0
                                )
                                .toFloat()


                        /*
                         * SUAVIZADO
                         */

                        smoothedLevel =
                            (
                                smoothedLevel * 0.55f
                            ) +
                            (
                                normalized * 0.45f
                            )


                        /*
                         * DETECCIÓN DE GOLPE
                         */

                        val difference =
                            smoothedLevel -
                            previousLevel

                        val beatBoost =
                            if (
                                smoothedLevel > 0.18f &&
                                difference > 0.025f
                            ) {
                                0.30f
                            } else {
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
                         * ENVIAR AL HILO PRINCIPAL
                         */

                        sendLevel(
                            finalLevel
                        )


                        try {
                            Thread.sleep(35)
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

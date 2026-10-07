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
                         * RMS DEL AUDIO
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
                         * SENSIBILIDAD MUY ALTA
                         *
                         * Un valor pequeño de RMS
                         * ahora produce una respuesta
                         * mucho mayor.
                         */

                        val normalized =
                            (
                                rms / 900.0
                            )
                                .coerceIn(
                                    0.0,
                                    1.0
                                )
                                .toFloat()


                        /*
                         * RESPUESTA RÁPIDA
                         */

                        smoothedLevel =
                            (
                                smoothedLevel * 0.30f
                            ) +
                            (
                                normalized * 0.70f
                            )


                        /*
                         * RESPUESTA EXTRA A LOS GOLPES
                         */

                        val difference =
                            smoothedLevel -
                            previousLevel

                        val beatBoost =
                            when {

                                difference > 0.08f ->
                                    0.40f

                                difference > 0.035f ->
                                    0.20f

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
                         * ENVIAR A COMPOSE
                         */

                        sendLevel(
                            finalLevel
                        )


                        try {
                            Thread.sleep(25)
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

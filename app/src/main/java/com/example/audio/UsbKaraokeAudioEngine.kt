package com.example.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.os.Process
import androidx.core.content.ContextCompat
import com.example.model.UsbMicStatus
import com.example.model.VocalSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tanh

/**
 * Real-time USB Microphone & Vocal Effects DSP Engine for Koro Koro TV.
 *
 * Features:
 * - Automatic USB Audio Input detection (TYPE_USB_DEVICE / TYPE_USB_HEADSET / TYPE_USB_ACCESSORY)
 * - Low-latency AudioRecord -> DSP -> AudioTrack pipeline
 * - Real-time Echo, Multi-tap Reverb, Bass/Treble EQ, Pitch Shift (-6..+6 semitones)
 * - Mic Monitor ON/OFF & Adjustable Monitor Volume
 * - Noise Gate & Anti-Feedback Soft Limiter
 * - Complete isolation from Voice Search (pauseForVoiceSearch / resumeAfterVoiceSearch)
 */
class UsbKaraokeAudioEngine(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    @Volatile
    private var currentSettings = VocalSettings()

    @Volatile
    private var isRunning = false

    @Volatile
    private var isPausedForVoiceSearch = false

    private var workerThread: Thread? = null
    private var preferredUsbInputDevice: AudioDeviceInfo? = null

    private val _status = MutableStateFlow(UsbMicStatus())
    val status: StateFlow<UsbMicStatus> = _status.asStateFlow()

    private val deviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            refreshUsbMicDetection()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            refreshUsbMicDetection()
        }
    }

    init {
        audioManager.registerAudioDeviceCallback(deviceCallback, null)
        refreshUsbMicDetection()
    }

    fun refreshUsbMicDetection() {
        val inputs = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
        val usbMic = inputs.firstOrNull { device ->
            device.type == AudioDeviceInfo.TYPE_USB_DEVICE ||
                device.type == AudioDeviceInfo.TYPE_USB_HEADSET ||
                device.type == AudioDeviceInfo.TYPE_USB_ACCESSORY
        }
        preferredUsbInputDevice = usbMic

        val fallbackMic = inputs.firstOrNull {
            it.type != AudioDeviceInfo.TYPE_TELEPHONY && it.type != AudioDeviceInfo.TYPE_FM_TUNER
        }

        val activeDevice = usbMic ?: fallbackMic
        val sampleRate = activeDevice?.sampleRates?.firstOrNull { it == 48000 || it == 44100 } ?: 48000
        val channels = activeDevice?.channelCounts?.maxOrNull()?.coerceIn(1, 2) ?: 1

        _status.update {
            it.copy(
                isUsbConnected = usbMic != null,
                deviceName = usbMic?.productName?.toString()?.ifBlank { "USB Karaoke Microphone" }
                    ?: activeDevice?.productName?.toString()?.ifBlank { "Mikrofon Sistem / Remote" }
                    ?: "Tidak ada mikrofon terdeteksi",
                sampleRateHz = sampleRate,
                channelCount = channels,
                estimatedLatencyMs = if (usbMic != null) 14 else 22,
                routingNote = if (usbMic != null) {
                    "USB Mic aktif -> DSP Vokal -> Output TV"
                } else {
                    "Colokkan USB Microphone ke port USB TV Box untuk latensi terendah"
                }
            )
        }
    }

    fun updateSettings(settings: VocalSettings) {
        currentSettings = settings
    }

    fun startEngine() {
        if (isRunning) return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            _status.update {
                it.copy(
                    isEngineRunning = false,
                    routingNote = "Izin RECORD_AUDIO diperlukan untuk mengaktifkan Mic Monitor"
                )
            }
            return
        }

        isRunning = true
        _status.update { it.copy(isEngineRunning = true) }

        workerThread = Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
            runAudioLoop()
        }, "KoroKoro-USB-DSP").apply { start() }
    }

    fun stopEngine() {
        isRunning = false
        workerThread?.interrupt()
        workerThread = null
        _status.update { it.copy(isEngineRunning = false, inputPeakLevel = 0f) }
    }

    /**
     * Isolates USB Karaoke Mic when Voice Search is active so Voice Search
     * never alters vocal effects, Mic Monitor, or karaoke mic settings.
     */
    fun pauseForVoiceSearch() {
        isPausedForVoiceSearch = true
        _status.update { it.copy(isIsolatedForVoiceSearch = true, inputPeakLevel = 0f) }
    }

    fun resumeAfterVoiceSearch() {
        isPausedForVoiceSearch = false
        _status.update { it.copy(isIsolatedForVoiceSearch = false) }
    }

    fun release() {
        stopEngine()
        try {
            audioManager.unregisterAudioDeviceCallback(deviceCallback)
        } catch (_: Exception) {
        }
    }

    private fun runAudioLoop() {
        val sampleRate = 48000
        val minRecBuf = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(2048)

        val minPlayBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(2048)

        val bufferSamples = 512
        var recorder: AudioRecord? = null
        var track: AudioTrack? = null

        try {
            recorder = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minRecBuf * 2
            )

            preferredUsbInputDevice?.let { usbDev ->
                recorder.setPreferredDevice(usbDev)
            }

            val trackBuilder = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minPlayBuf * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                trackBuilder.setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
            }
            track = trackBuilder.build()

            if (recorder.state != AudioRecord.STATE_INITIALIZED || track.state != AudioTrack.STATE_INITIALIZED) {
                _status.update {
                    it.copy(
                        isEngineRunning = false,
                        routingNote = "Perangkat audio tidak mendukung inisialisasi low-latency"
                    )
                }
                return
            }

            recorder.startRecording()
            track.play()

            val inBuf = ShortArray(bufferSamples)
            val outBuf = ShortArray(bufferSamples)

            // DSP state buffers
            val echoDelaySamples = (sampleRate * 0.24f).toInt() // 240ms karaoke echo
            val echoBuffer = FloatArray(echoDelaySamples)
            var echoIdx = 0

            // Multi-tap Schroeder-inspired reverb comb buffers
            val rev1 = FloatArray(1687)
            val rev2 = FloatArray(2053)
            val rev3 = FloatArray(2251)
            var r1Idx = 0
            var r2Idx = 0
            var r3Idx = 0

            // Pitch shifter ring buffer
            val pitchBufSize = 4096
            val pitchBuffer = FloatArray(pitchBufSize)
            var pitchWriteIdx = 0
            var pitchReadIdx = 0f

            // Simple 1-pole low/high shelf filter states
            var lowShelfState = 0f
            var highShelfPrevIn = 0f
            var highShelfState = 0f
            var gateEnvelope = 0f

            var peakCounter = 0

            while (isRunning && !Thread.currentThread().isInterrupted) {
                if (isPausedForVoiceSearch) {
                    Thread.sleep(40)
                    continue
                }

                val read = recorder.read(inBuf, 0, bufferSamples)
                if (read <= 0) continue

                val cfg = currentSettings
                val pitchRatio = 2.0.pow(cfg.pitchSemitones / 12.0).toFloat()
                val bassGain = 10.0.pow(cfg.bassDb / 20.0).toFloat() - 1f
                val trebleGain = 10.0.pow(cfg.trebleDb / 20.0).toFloat() - 1f

                var maxAbs = 0f

                for (i in 0 until read) {
                    var sample = (inBuf[i] / 32768f) * cfg.micVolume

                    val absSample = abs(sample)
                    if (absSample > maxAbs) maxAbs = absSample

                    // 1. Noise Gate
                    if (cfg.noiseGateEnabled) {
                        val target = if (absSample > 0.012f) 1f else 0.05f
                        gateEnvelope += (target - gateEnvelope) * 0.08f
                        sample *= gateEnvelope
                    }

                    // 2. Pitch Shifter (Ring Buffer)
                    pitchBuffer[pitchWriteIdx] = sample
                    if (cfg.pitchSemitones != 0) {
                        val idx0 = pitchReadIdx.toInt() and (pitchBufSize - 1)
                        val idx1 = (idx0 + 1) and (pitchBufSize - 1)
                        val frac = pitchReadIdx - pitchReadIdx.toInt()
                        sample = pitchBuffer[idx0] * (1f - frac) + pitchBuffer[idx1] * frac
                        pitchReadIdx = (pitchReadIdx + pitchRatio) % pitchBufSize
                    } else {
                        pitchReadIdx = pitchWriteIdx.toFloat()
                    }
                    pitchWriteIdx = (pitchWriteIdx + 1) and (pitchBufSize - 1)

                    // 3. Bass & Treble EQ
                    lowShelfState += (sample - lowShelfState) * 0.045f
                    val highPass = sample - highShelfPrevIn + 0.85f * highShelfState
                    highShelfPrevIn = sample
                    highShelfState = highPass
                    sample += lowShelfState * bassGain * 0.65f + highPass * trebleGain * 0.45f

                    // 4. Echo Delay Line
                    val delayedEcho = echoBuffer[echoIdx]
                    val echoOut = sample + delayedEcho * (cfg.echoLevel * 0.55f)
                    echoBuffer[echoIdx] = echoOut
                    echoIdx = (echoIdx + 1) % echoDelaySamples

                    // 5. Multi-tap Reverb
                    val c1 = rev1[r1Idx]
                    val c2 = rev2[r2Idx]
                    val c3 = rev3[r3Idx]
                    val reverbWet = (c1 + c2 + c3) * 0.33f
                    val fb = 0.45f + cfg.reverbLevel * 0.35f
                    rev1[r1Idx] = sample + c1 * fb
                    rev2[r2Idx] = sample + c2 * fb
                    rev3[r3Idx] = sample + c3 * fb
                    r1Idx = (r1Idx + 1) % rev1.size
                    r2Idx = (r2Idx + 1) % rev2.size
                    r3Idx = (r3Idx + 1) % rev3.size

                    var mixed = sample +
                        delayedEcho * (cfg.echoLevel * 0.6f) +
                        reverbWet * (cfg.reverbLevel * 0.65f)

                    // 6. Anti-Feedback Soft Limiter
                    if (cfg.antiFeedbackEnabled) {
                        mixed = tanh(mixed * 1.15f)
                    }

                    // 7. Mic Monitor Output
                    val monitorOut = if (cfg.micMonitorEnabled) {
                        (mixed * cfg.micMonitorVolume).coerceIn(-0.98f, 0.98f)
                    } else {
                        0f
                    }

                    outBuf[i] = (monitorOut * 32767f).toInt().toShort()
                }

                track.write(outBuf, 0, read)

                peakCounter++
                if (peakCounter % 4 == 0) {
                    _status.update { it.copy(inputPeakLevel = maxAbs.coerceIn(0f, 1f)) }
                }
            }
        } catch (_: InterruptedException) {
            // Normal stop
        } catch (e: Exception) {
            _status.update {
                it.copy(
                    isEngineRunning = false,
                    routingNote = "Audio engine berhenti: ${e.localizedMessage ?: "Periksa koneksi mic"}"
                )
            }
        } finally {
            try {
                recorder?.stop()
                recorder?.release()
            } catch (_: Exception) {
            }
            try {
                track?.stop()
                track?.release()
            } catch (_: Exception) {
            }
            isRunning = false
            _status.update { it.copy(isEngineRunning = false, inputPeakLevel = 0f) }
        }
    }
}

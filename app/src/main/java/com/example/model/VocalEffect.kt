package com.example.model

enum class VocalPreset(
    val keyNumber: Int,
    val displayName: String,
    val shortDesc: String,
    val echoLevel: Float,
    val reverbLevel: Float,
    val bassDb: Float,
    val trebleDb: Float,
    val pitchSemitones: Int
) {
    NORMAL(0, "Normal", "Suara natural tanpa efek", 0.0f, 0.0f, 0f, 0f, 0),
    STUDIO_REVERB(1, "Studio Reverb", "Ruang rekaman jernih", 0.12f, 0.45f, 1.5f, 2.0f, 0),
    ECHO(2, "Echo", "Gema klasik karaoke", 0.55f, 0.20f, 0f, 1.0f, 0),
    CONCERT_HALL(3, "Concert Hall", "Panggung konser megah", 0.30f, 0.78f, 2.5f, 1.5f, 0),
    KARAOKE_POP(4, "Karaoke Pop", "Vokal cerah & seimbang", 0.25f, 0.38f, 1.0f, 3.5f, 0),
    DEEP_VOCAL(5, "Deep Vocal", "Suara tebal & hangat", 0.18f, 0.32f, 6.0f, -1.5f, -1),
    BRIGHT_VOCAL(6, "Bright Vocal", "Treble tajam & jelas", 0.20f, 0.30f, -1.0f, 6.0f, 0),
    WARM_VOCAL(7, "Warm Vocal", "Lembut untuk lagu nostalgia", 0.22f, 0.42f, 4.0f, 0.5f, 0),
    POWERFUL_VOCAL(8, "Powerful Vocal", "Dorongan penuh untuk nada tinggi", 0.32f, 0.52f, 4.5f, 4.5f, 0),
    CUSTOM_EFFECT(9, "Custom Effect", "Pengaturan kustom tersimpan", 0.25f, 0.35f, 2.0f, 2.0f, 0);

    companion object {
        fun fromKeyNumber(num: Int): VocalPreset? = entries.find { it.keyNumber == num }
    }
}

data class VocalSettings(
    val preset: VocalPreset = VocalPreset.KARAOKE_POP,
    val micVolume: Float = 1.15f,        // 0.0f .. 2.0f
    val echoLevel: Float = 0.25f,        // 0.0f .. 1.0f
    val reverbLevel: Float = 0.38f,      // 0.0f .. 1.0f
    val bassDb: Float = 1.0f,            // -12.0f .. +12.0f
    val trebleDb: Float = 3.5f,          // -12.0f .. +12.0f
    val pitchSemitones: Int = 0,         // -6 .. +6
    val micMonitorEnabled: Boolean = true,
    val micMonitorVolume: Float = 0.85f, // 0.0f .. 1.0f
    val noiseGateEnabled: Boolean = true,
    val antiFeedbackEnabled: Boolean = true
)

data class UsbMicStatus(
    val isUsbConnected: Boolean = false,
    val deviceName: String = "Mendeteksi perangkat audio...",
    val sampleRateHz: Int = 48000,
    val channelCount: Int = 1,
    val estimatedLatencyMs: Int = 18,
    val isEngineRunning: Boolean = false,
    val isIsolatedForVoiceSearch: Boolean = false,
    val inputPeakLevel: Float = 0f,
    val routingNote: String = "Siap untuk USB Microphone & Output TV"
)

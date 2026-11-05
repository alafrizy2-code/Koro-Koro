package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.VocalPreset
import com.example.model.VocalSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.vocalDataStore: DataStore<Preferences> by preferencesDataStore(name = "vocal_settings")

class VocalSettingsStore(private val context: Context) {

    private object Keys {
        val PRESET = intPreferencesKey("preset_key_number")
        val MIC_VOLUME = floatPreferencesKey("mic_volume")
        val ECHO_LEVEL = floatPreferencesKey("echo_level")
        val REVERB_LEVEL = floatPreferencesKey("reverb_level")
        val BASS_DB = floatPreferencesKey("bass_db")
        val TREBLE_DB = floatPreferencesKey("treble_db")
        val PITCH_SEMITONES = intPreferencesKey("pitch_semitones")
        val MIC_MONITOR_ENABLED = booleanPreferencesKey("mic_monitor_enabled")
        val MIC_MONITOR_VOLUME = floatPreferencesKey("mic_monitor_volume")
        val NOISE_GATE_ENABLED = booleanPreferencesKey("noise_gate_enabled")
        val ANTI_FEEDBACK_ENABLED = booleanPreferencesKey("anti_feedback_enabled")

        // Custom preset (#9) saved parameters
        val CUSTOM_ECHO = floatPreferencesKey("custom_echo")
        val CUSTOM_REVERB = floatPreferencesKey("custom_reverb")
        val CUSTOM_BASS = floatPreferencesKey("custom_bass")
        val CUSTOM_TREBLE = floatPreferencesKey("custom_treble")
        val CUSTOM_PITCH = intPreferencesKey("custom_pitch")

        // Voice search language ("id-ID" or "en-US")
        val VOICE_LANGUAGE = stringPreferencesKey("voice_language")
    }

    val vocalSettingsFlow: Flow<VocalSettings> = context.vocalDataStore.data.map { prefs ->
        val presetNum = prefs[Keys.PRESET] ?: VocalPreset.KARAOKE_POP.keyNumber
        val preset = VocalPreset.fromKeyNumber(presetNum) ?: VocalPreset.KARAOKE_POP
        VocalSettings(
            preset = preset,
            micVolume = prefs[Keys.MIC_VOLUME] ?: 1.15f,
            echoLevel = prefs[Keys.ECHO_LEVEL] ?: preset.echoLevel,
            reverbLevel = prefs[Keys.REVERB_LEVEL] ?: preset.reverbLevel,
            bassDb = prefs[Keys.BASS_DB] ?: preset.bassDb,
            trebleDb = prefs[Keys.TREBLE_DB] ?: preset.trebleDb,
            pitchSemitones = prefs[Keys.PITCH_SEMITONES] ?: preset.pitchSemitones,
            micMonitorEnabled = prefs[Keys.MIC_MONITOR_ENABLED] ?: true,
            micMonitorVolume = prefs[Keys.MIC_MONITOR_VOLUME] ?: 0.85f,
            noiseGateEnabled = prefs[Keys.NOISE_GATE_ENABLED] ?: true,
            antiFeedbackEnabled = prefs[Keys.ANTI_FEEDBACK_ENABLED] ?: true
        )
    }

    val voiceLanguageFlow: Flow<String> = context.vocalDataStore.data.map { prefs ->
        prefs[Keys.VOICE_LANGUAGE] ?: "id-ID"
    }

    suspend fun setVoiceLanguage(langTag: String) {
        context.vocalDataStore.edit { prefs ->
            prefs[Keys.VOICE_LANGUAGE] = langTag
        }
    }

    suspend fun applyPreset(preset: VocalPreset) {
        context.vocalDataStore.edit { prefs ->
            prefs[Keys.PRESET] = preset.keyNumber
            if (preset == VocalPreset.CUSTOM_EFFECT) {
                prefs[Keys.ECHO_LEVEL] = prefs[Keys.CUSTOM_ECHO] ?: preset.echoLevel
                prefs[Keys.REVERB_LEVEL] = prefs[Keys.CUSTOM_REVERB] ?: preset.reverbLevel
                prefs[Keys.BASS_DB] = prefs[Keys.CUSTOM_BASS] ?: preset.bassDb
                prefs[Keys.TREBLE_DB] = prefs[Keys.CUSTOM_TREBLE] ?: preset.trebleDb
                prefs[Keys.PITCH_SEMITONES] = prefs[Keys.CUSTOM_PITCH] ?: preset.pitchSemitones
            } else {
                prefs[Keys.ECHO_LEVEL] = preset.echoLevel
                prefs[Keys.REVERB_LEVEL] = preset.reverbLevel
                prefs[Keys.BASS_DB] = preset.bassDb
                prefs[Keys.TREBLE_DB] = preset.trebleDb
                prefs[Keys.PITCH_SEMITONES] = preset.pitchSemitones
            }
        }
    }

    suspend fun updateSettings(transform: (VocalSettings) -> VocalSettings) {
        context.vocalDataStore.edit { prefs ->
            val currentPreset = VocalPreset.fromKeyNumber(prefs[Keys.PRESET] ?: 4) ?: VocalPreset.KARAOKE_POP
            val current = VocalSettings(
                preset = currentPreset,
                micVolume = prefs[Keys.MIC_VOLUME] ?: 1.15f,
                echoLevel = prefs[Keys.ECHO_LEVEL] ?: currentPreset.echoLevel,
                reverbLevel = prefs[Keys.REVERB_LEVEL] ?: currentPreset.reverbLevel,
                bassDb = prefs[Keys.BASS_DB] ?: currentPreset.bassDb,
                trebleDb = prefs[Keys.TREBLE_DB] ?: currentPreset.trebleDb,
                pitchSemitones = prefs[Keys.PITCH_SEMITONES] ?: currentPreset.pitchSemitones,
                micMonitorEnabled = prefs[Keys.MIC_MONITOR_ENABLED] ?: true,
                micMonitorVolume = prefs[Keys.MIC_MONITOR_VOLUME] ?: 0.85f,
                noiseGateEnabled = prefs[Keys.NOISE_GATE_ENABLED] ?: true,
                antiFeedbackEnabled = prefs[Keys.ANTI_FEEDBACK_ENABLED] ?: true
            )
            val updated = transform(current)
            prefs[Keys.PRESET] = updated.preset.keyNumber
            prefs[Keys.MIC_VOLUME] = updated.micVolume
            prefs[Keys.ECHO_LEVEL] = updated.echoLevel
            prefs[Keys.REVERB_LEVEL] = updated.reverbLevel
            prefs[Keys.BASS_DB] = updated.bassDb
            prefs[Keys.TREBLE_DB] = updated.trebleDb
            prefs[Keys.PITCH_SEMITONES] = updated.pitchSemitones
            prefs[Keys.MIC_MONITOR_ENABLED] = updated.micMonitorEnabled
            prefs[Keys.MIC_MONITOR_VOLUME] = updated.micMonitorVolume
            prefs[Keys.NOISE_GATE_ENABLED] = updated.noiseGateEnabled
            prefs[Keys.ANTI_FEEDBACK_ENABLED] = updated.antiFeedbackEnabled

            if (updated.preset == VocalPreset.CUSTOM_EFFECT) {
                prefs[Keys.CUSTOM_ECHO] = updated.echoLevel
                prefs[Keys.CUSTOM_REVERB] = updated.reverbLevel
                prefs[Keys.CUSTOM_BASS] = updated.bassDb
                prefs[Keys.CUSTOM_TREBLE] = updated.trebleDb
                prefs[Keys.CUSTOM_PITCH] = updated.pitchSemitones
            }
        }
    }
}

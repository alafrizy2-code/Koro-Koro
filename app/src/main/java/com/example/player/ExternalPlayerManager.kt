package com.example.player

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build

data class ExternalPlayerApp(
    val packageName: String,
    val appName: String,
    val isTizenTubeCobalt: Boolean
)

sealed interface LaunchResult {
    data class LaunchedTizenTube(val appName: String) : LaunchResult
    data class TizenTubeNotInstalled(val alternatives: List<ExternalPlayerApp>) : LaunchResult
    data class LaunchFailed(val reason: String, val alternatives: List<ExternalPlayerApp>) : LaunchResult
}

/**
 * Inspects installed Android TV packages & Intent capabilities to launch YouTube karaoke
 * videos via TizenTube Cobalt without hardcoding unverified package names or schemes.
 */
class ExternalPlayerManager(private val context: Context) {

    fun discoverYouTubeCapablePlayers(): List<ExternalPlayerApp> {
        val pm = context.packageManager
        val sampleIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
        ).addCategory(Intent.CATEGORY_BROWSABLE)

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PackageManager.MATCH_ALL
        } else {
            PackageManager.MATCH_DEFAULT_ONLY
        }

        val resolved: List<ResolveInfo> = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(
                    sampleIntent,
                    PackageManager.ResolveInfoFlags.of(flags.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(sampleIntent, flags)
            }
        } catch (_: Exception) {
            emptyList()
        }

        return resolved
            .mapNotNull { info ->
                val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
                if (pkg == context.packageName) return@mapNotNull null
                val label = info.loadLabel(pm)?.toString()?.ifBlank { pkg } ?: pkg
                val isTizenTube = pkg.contains("tizentube", ignoreCase = true) ||
                    pkg.contains("cobalt", ignoreCase = true) ||
                    label.contains("tizentube", ignoreCase = true) ||
                    label.contains("cobalt", ignoreCase = true)
                ExternalPlayerApp(
                    packageName = pkg,
                    appName = label,
                    isTizenTubeCobalt = isTizenTube
                )
            }
            .distinctBy { it.packageName }
            .sortedByDescending { it.isTizenTubeCobalt }
    }

    fun findTizenTubeCobalt(): ExternalPlayerApp? {
        return discoverYouTubeCapablePlayers().firstOrNull { it.isTizenTubeCobalt }
    }

    fun launchInTizenTubeCobalt(videoId: String): LaunchResult {
        val players = discoverYouTubeCapablePlayers()
        val tizenTube = players.firstOrNull { it.isTizenTubeCobalt }
            ?: return LaunchResult.TizenTubeNotInstalled(alternatives = players)

        return try {
            val intent = buildYouTubeWatchIntent(videoId, tizenTube.packageName)
            context.startActivity(intent)
            LaunchResult.LaunchedTizenTube(tizenTube.appName)
        } catch (e: Exception) {
            LaunchResult.LaunchFailed(
                reason = e.localizedMessage ?: "Gagal membuka Intent pada ${tizenTube.appName}",
                alternatives = players.filter { it.packageName != tizenTube.packageName }
            )
        }
    }

    fun launchInSpecificPlayer(videoId: String, packageName: String): Boolean {
        return try {
            val intent = buildYouTubeWatchIntent(videoId, packageName)
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun launchWithSystemChooser(videoId: String): Boolean {
        return try {
            val baseIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/watch?v=$videoId")
            )
            val chooser = Intent.createChooser(baseIntent, "Pilih Pemutar Video Karaoke")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun buildYouTubeWatchIntent(videoId: String, packageName: String): Intent {
        return Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.youtube.com/watch?v=$videoId")
        ).apply {
            setPackage(packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}

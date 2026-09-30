package app.trollfoss.update

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.core.content.edit
import app.trollfoss.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

enum class UpdateMessage { CURRENT, NETWORK, INVALID, STORAGE, PERMISSION, INSTALL, ACCESS, RATE }

data class UpdateState(
    val release: AppRelease? = null,
    val checking: Boolean = false,
    val downloading: Boolean = false,
    val progress: Float = 0f,
    val ready: Boolean = false,
    val message: UpdateMessage? = null,
    val automatic: Boolean = true,
    val previews: Boolean = false,
)

/**
 * Looks for a newer Trollfoss on GitHub, downloads it on request and hands it to Android's installer
 * only after size, SHA-256, package, version and signature all match. Nothing installs silently:
 * a parent starts the download and Android asks before installing.
 */
class AppUpdater(context: Context, private val scope: CoroutineScope) {
    private val app = context.applicationContext
    private val prefs = app.getSharedPreferences("trollfoss_updates", Context.MODE_PRIVATE)
    private val folder = File(app.cacheDir, "updates").apply { mkdirs() }
    private val apk = File(folder, "trollfoss-update.apk")
    private var job: Job? = null

    var state by mutableStateOf(
        UpdateState(
            release = prefs.getString("release", null)?.let(AppRelease::decode)?.takeIf(::isNewer),
            automatic = prefs.getBoolean("automatic", !BuildConfig.DEBUG),
            previews = prefs.getBoolean("previews", ReleaseVersion.parse(BuildConfig.VERSION_NAME)?.preview == true),
        ),
    )
        private set

    init {
        val release = state.release
        if (release != null && apk.exists()) {
            scope.launch {
                val valid = withContext(Dispatchers.IO) { runCatching { verify(apk, release) }.isSuccess }
                if (state.release == release) state = state.copy(ready = valid)
            }
        }
    }

    private fun isNewer(release: AppRelease): Boolean {
        val installed = ReleaseVersion.parse(BuildConfig.VERSION_NAME) ?: return false
        val candidate = ReleaseVersion.parse(release.tag) ?: return false
        return candidate > installed
    }

    fun setAutomatic(value: Boolean) {
        prefs.edit { putBoolean("automatic", value) }
        state = state.copy(automatic = value)
    }

    fun setPreviews(value: Boolean) {
        cancel()
        prefs.edit { putBoolean("previews", value); remove("release"); remove("last_check") }
        state = state.copy(previews = value, release = null, ready = false)
        check(manual = true)
    }

    /** Called when the app comes to the front. At most one automatic check per twelve hours. */
    fun checkIfDue() = check(manual = false)

    fun check(manual: Boolean = true) {
        if (job?.isActive == true) return
        val due = System.currentTimeMillis() - prefs.getLong("last_check", 0L) >= CHECK_INTERVAL_MS
        if (!manual && (!state.automatic || !due)) return
        job = scope.launch {
            prefs.edit { putLong("last_check", System.currentTimeMillis()) }
            state = state.copy(checking = true, message = null)
            try {
                val release = withContext(Dispatchers.IO) {
                    val connection = connect(RELEASE_API, "application/vnd.github+json")
                    try {
                        val body = connection.inputStream.use { it.readBounded(4 * 1024 * 1024) }
                        newerRelease(body.toString(Charsets.UTF_8), BuildConfig.VERSION_NAME, state.previews)
                    } finally {
                        connection.disconnect()
                    }
                }
                ensureActive()
                prefs.edit { putString("release", release?.encode()) }
                state = state.copy(
                    checking = false,
                    release = release,
                    ready = state.ready && state.release == release,
                    message = if (release == null && manual) UpdateMessage.CURRENT else null,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (failure: UpdateFailure) {
                state = state.copy(checking = false, message = if (manual) failure.reason else null)
            } catch (_: Exception) {
                state = state.copy(checking = false, message = if (manual) UpdateMessage.NETWORK else null)
            }
        }
    }

    fun cancel() {
        job?.cancel()
        state = state.copy(checking = false, downloading = false, progress = 0f)
    }

    fun download() {
        val release = state.release ?: return
        if (job?.isActive == true) return
        job = scope.launch {
            state = state.copy(downloading = true, ready = false, progress = 0f, message = null)
            try {
                withContext(Dispatchers.IO) {
                    val part = File(folder, "download.part")
                    try {
                        if (folder.usableSpace < release.size + 4 * 1024 * 1024) throw UpdateFailure(UpdateMessage.STORAGE)
                        val connection = connect("https://api.github.com/repos/$RELEASE_REPO/releases/assets/${release.assetId}", "application/octet-stream")
                        try {
                            connection.inputStream.use { input ->
                                part.outputStream().use { output ->
                                    val buffer = ByteArray(64 * 1024)
                                    var total = 0L
                                    var lastPercent = -1
                                    while (true) {
                                        ensureActive()
                                        val count = input.read(buffer)
                                        if (count < 0) break
                                        total += count
                                        if (total > release.size || total > MAX_APK_BYTES) throw UpdateFailure(UpdateMessage.INVALID)
                                        output.write(buffer, 0, count)
                                        val percent = (100 * total / release.size).toInt()
                                        if (percent != lastPercent) {
                                            lastPercent = percent
                                            val progress = total.toFloat() / release.size
                                            withContext(Dispatchers.Main) { state = state.copy(progress = progress) }
                                        }
                                    }
                                }
                            }
                        } finally {
                            connection.disconnect()
                        }
                        verify(part, release)
                        ensureActive()
                        if (apk.exists() && !apk.delete()) throw UpdateFailure(UpdateMessage.STORAGE)
                        if (!part.renameTo(apk)) throw UpdateFailure(UpdateMessage.STORAGE)
                    } finally {
                        part.delete()
                    }
                }
                state = state.copy(downloading = false, ready = true, progress = 1f)
            } catch (error: CancellationException) {
                throw error
            } catch (failure: UpdateFailure) {
                state = state.copy(downloading = false, message = failure.reason)
            } catch (_: Exception) {
                state = state.copy(downloading = false, message = UpdateMessage.NETWORK)
            }
        }
    }

    fun install() {
        val release = state.release ?: return
        if (!state.ready || job?.isActive == true) return
        job = scope.launch {
            try {
                withContext(Dispatchers.IO) { verify(apk, release) }
                if (!app.packageManager.canRequestPackageInstalls()) {
                    state = state.copy(message = UpdateMessage.PERMISSION)
                    app.startActivity(
                        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${app.packageName}"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                } else {
                    val uri = FileProvider.getUriForFile(app, "${app.packageName}.files", apk)
                    app.startActivity(
                        Intent(Intent.ACTION_VIEW)
                            .setDataAndType(uri, "application/vnd.android.package-archive")
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (failure: UpdateFailure) {
                state = state.copy(message = failure.reason, ready = false)
            } catch (_: Exception) {
                state = state.copy(message = UpdateMessage.INSTALL)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun verify(file: File, release: AppRelease) {
        if (file.length() != release.size) throw UpdateFailure(UpdateMessage.INVALID)
        val sha = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(65_536)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                sha.update(buffer, 0, count)
            }
        }
        if (sha.digest().joinToString("") { "%02x".format(it) } != release.digest) throw UpdateFailure(UpdateMessage.INVALID)
        val flags = if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
        val installed = app.packageManager.getPackageInfo(app.packageName, flags)
        val candidate = app.packageManager.getPackageArchiveInfo(file.path, flags) ?: throw UpdateFailure(UpdateMessage.INVALID)
        if (!compatibleUpdate(identity(installed), identity(candidate), release.tag, Build.VERSION.SDK_INT)) throw UpdateFailure(UpdateMessage.INVALID)
    }

    @Suppress("DEPRECATION")
    private fun identity(info: PackageInfo): UpdatePackageIdentity = UpdatePackageIdentity(
        name = info.packageName,
        versionCode = if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else info.versionCode.toLong(),
        versionName = info.versionName.orEmpty(),
        minSdk = info.applicationInfo?.minSdkVersion ?: Int.MAX_VALUE,
        debug = ((info.applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_DEBUGGABLE) != 0,
        signers = (if (Build.VERSION.SDK_INT >= 28) info.signingInfo?.apkContentsSigners else info.signatures)
            .orEmpty().map { it.toCharsString() }.toSet(),
    )

    private fun connect(initial: String, accept: String): HttpURLConnection {
        var target = initial
        repeat(5) {
            if (!trustedUpdateUrl(target)) throw UpdateFailure(UpdateMessage.INVALID)
            val url = URL(target)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = false
                connectTimeout = 15_000
                readTimeout = 20_000
                setRequestProperty("Accept", accept)
                setRequestProperty("User-Agent", "Trollfoss/${BuildConfig.VERSION_NAME}")
            }
            val code = connection.responseCode
            when {
                code in listOf(301, 302, 303, 307, 308) -> {
                    val next = connection.getHeaderField("Location")
                    connection.disconnect()
                    target = next?.let { URL(url, it).toString() } ?: throw UpdateFailure(UpdateMessage.INVALID)
                }
                code == 200 -> return connection
                else -> {
                    connection.disconnect()
                    throw UpdateFailure(
                        when (code) {
                            401, 404 -> UpdateMessage.ACCESS
                            403, 429 -> UpdateMessage.RATE
                            else -> UpdateMessage.NETWORK
                        },
                    )
                }
            }
        }
        throw UpdateFailure(UpdateMessage.INVALID)
    }

    private class UpdateFailure(val reason: UpdateMessage) : Exception()

    private fun InputStream.readBounded(max: Int): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            val count = read(buffer)
            if (count < 0) break
            if (output.size() + count > max) throw UpdateFailure(UpdateMessage.INVALID)
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }

    private companion object {
        const val CHECK_INTERVAL_MS = 12 * 60 * 60 * 1000L
    }
}


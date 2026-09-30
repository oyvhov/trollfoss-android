package app.trollfoss.update

import org.json.JSONArray
import org.json.JSONObject
import java.net.URI

/*
 * The same update contract as Spole: one public GitHub repository, one signed universal APK per
 * release, and a SHA-256 digest from GitHub that the download must match before Android is asked
 * to install anything.
 */

const val RELEASE_REPO = "oyvhov/trollfoss-android"
const val RELEASE_API = "https://api.github.com/repos/$RELEASE_REPO/releases?per_page=100"
const val MAX_APK_BYTES = 100L * 1024 * 1024

data class ReleaseVersion(val major: Int, val minor: Int, val patch: Int, val stage: Int, val number: Int) : Comparable<ReleaseVersion> {
    override fun compareTo(other: ReleaseVersion): Int = compareValuesBy(
        this, other,
        { it.major }, { it.minor }, { it.patch }, { it.stage }, { it.number },
    )

    val preview: Boolean get() = stage < STABLE

    companion object {
        private const val STABLE = 3
        private val pattern = Regex("^v?(\\d+)\\.(\\d+)\\.(\\d+)(?:-(alpha|beta|rc)[.-]?(\\d+))?(?:-debug)?$")

        fun parse(raw: String): ReleaseVersion? {
            val match = pattern.matchEntire(raw.trim()) ?: return null
            return runCatching {
                ReleaseVersion(
                    major = match.groupValues[1].toInt(),
                    minor = match.groupValues[2].toInt(),
                    patch = match.groupValues[3].toInt(),
                    stage = when (match.groupValues[4]) {
                        "alpha" -> 0
                        "beta" -> 1
                        "rc" -> 2
                        else -> STABLE
                    },
                    number = match.groupValues[5].toIntOrNull() ?: 0,
                )
            }.getOrNull()
        }
    }
}

data class AppRelease(val tag: String, val notes: String, val assetId: Long, val size: Long, val digest: String) {
    fun encode(): String = JSONObject()
        .put("tag", tag)
        .put("notes", notes)
        .put("id", assetId)
        .put("size", size)
        .put("digest", digest)
        .toString()

    companion object {
        fun decode(raw: String): AppRelease? = runCatching {
            val o = JSONObject(raw)
            AppRelease(o.getString("tag"), o.getString("notes"), o.getLong("id"), o.getLong("size"), o.getString("digest"))
        }.getOrNull()
    }
}

/** Picks the newest usable release. Versions are compared, never GitHub's publication order. */
fun newerRelease(raw: String, current: String, previews: Boolean): AppRelease? {
    val installed = ReleaseVersion.parse(current) ?: return null
    val releases = JSONArray(raw)
    var best: Pair<ReleaseVersion, AppRelease>? = null
    for (i in 0 until releases.length()) {
        val o = releases.optJSONObject(i) ?: continue
        if (o.optBoolean("draft", false)) continue
        val tag = o.optString("tag_name", "")
        if (tag.isBlank() || tag.contains("debug", ignoreCase = true)) continue
        val version = ReleaseVersion.parse(tag) ?: continue
        if (version <= installed) continue
        if (!previews && (version.preview || o.optBoolean("prerelease", false))) continue
        val assets = o.optJSONArray("assets") ?: JSONArray()
        val apks = (0 until assets.length()).mapNotNull { assets.optJSONObject(it) }.filter {
            val name = it.optString("name", "")
            name.endsWith(".apk", ignoreCase = true) && !name.contains("debug", ignoreCase = true) && it.optString("state") == "uploaded"
        }
        // Trollfoss publishes exactly one universal APK. Anything else is ambiguous and skipped.
        val asset = apks.singleOrNull() ?: continue
        val id = asset.optLong("id", -1)
        val size = asset.optLong("size", -1)
        val digest = asset.optString("digest", "").removePrefix("sha256:").lowercase()
        if (id <= 0 || size !in 1..MAX_APK_BYTES || !digest.matches(Regex("[0-9a-f]{64}"))) continue
        val candidate = AppRelease(tag, o.optString("body", "").take(12_000), id, size, digest)
        if (best == null || version > best.first) best = version to candidate
    }
    return best?.second
}

/** Only GitHub's API for this repository and GitHub's asset hosts, over plain HTTPS on 443. */
fun trustedUpdateUrl(url: String): Boolean = runCatching {
    val uri = URI(url)
    uri.scheme == "https" && uri.rawUserInfo == null && uri.port in setOf(-1, 443) &&
        (
            (uri.host == "api.github.com" && uri.path.startsWith("/repos/$RELEASE_REPO/releases")) ||
                uri.host in setOf("release-assets.githubusercontent.com", "objects.githubusercontent.com", "github-releases.githubusercontent.com")
            )
}.getOrDefault(false)

data class UpdatePackageIdentity(
    val name: String,
    val versionCode: Long,
    val versionName: String,
    val minSdk: Int,
    val debug: Boolean,
    val signers: Set<String>,
)

/** A download may only replace this app with a newer, non-debug build signed by the same key. */
fun compatibleUpdate(installed: UpdatePackageIdentity, candidate: UpdatePackageIdentity, tag: String, sdk: Int): Boolean {
    val expected = ReleaseVersion.parse(tag) ?: return false
    return candidate.name == installed.name &&
        candidate.versionCode > installed.versionCode &&
        candidate.minSdk <= sdk &&
        !candidate.debug &&
        ReleaseVersion.parse(candidate.versionName) == expected &&
        installed.signers.isNotEmpty() &&
        installed.signers == candidate.signers
}


package app.trollfoss.update

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleasePolicyTest {

    private val digest = "a".repeat(64)

    private fun release(
        tag: String,
        prerelease: Boolean = false,
        draft: Boolean = false,
        assets: List<JSONObject> = listOf(asset("Trollfoss-$tag.apk")),
    ): JSONObject = JSONObject()
        .put("tag_name", tag)
        .put("prerelease", prerelease)
        .put("draft", draft)
        .put("body", "Nytt i $tag")
        .put("assets", JSONArray(assets))

    private fun asset(name: String, id: Long = 7, size: Long = 1_500_000, state: String = "uploaded", sha: String = "sha256:$digest") =
        JSONObject().put("name", name).put("id", id).put("size", size).put("state", state).put("digest", sha)

    private fun list(vararg releases: JSONObject) = JSONArray(releases.toList()).toString()

    @Test
    fun `versions compare numerically and previews sort before the stable release`() {
        assertTrue(ReleaseVersion.parse("1.10.0")!! > ReleaseVersion.parse("1.9.3")!!)
        assertTrue(ReleaseVersion.parse("v1.1.0")!! > ReleaseVersion.parse("1.1.0-rc2")!!)
        assertTrue(ReleaseVersion.parse("1.1.0-beta1")!! > ReleaseVersion.parse("1.1.0-alpha9")!!)
        assertTrue(ReleaseVersion.parse("1.1.0-alpha2")!!.preview)
        assertEquals(ReleaseVersion.parse("1.0.0"), ReleaseVersion.parse("1.0.0-debug"))
        assertNull(ReleaseVersion.parse("latest"))
    }

    @Test
    fun `the newest stable release with one apk wins`() {
        val raw = list(release("v1.1.0"), release("v1.3.0"), release("v1.2.0"))
        val found = newerRelease(raw, "1.1.0", previews = false)
        assertEquals("v1.3.0", found?.tag)
        assertEquals(digest, found?.digest)
        assertEquals(1_500_000L, found?.size)
    }

    @Test
    fun `nothing newer means no update`() {
        assertNull(newerRelease(list(release("v1.0.0"), release("v1.1.0")), "1.1.0", previews = false))
    }

    @Test
    fun `drafts, previews, debug builds and ambiguous assets are skipped`() {
        val raw = list(
            release("v2.0.0", draft = true),
            release("v1.9.0", prerelease = true),
            release("v1.8.0-beta1"),
            release("v1.7.0-debug"),
            release("v1.6.0", assets = listOf(asset("a.apk"), asset("b.apk", id = 8))),
            release("v1.5.0", assets = listOf(asset("Trollfoss.apk", state = "starter"))),
            release("v1.4.0", assets = listOf(asset("Trollfoss.apk", sha = ""))),
            release("v1.3.0", assets = listOf(asset("Trollfoss.apk", size = MAX_APK_BYTES + 1))),
            release("v1.2.0", assets = listOf(asset("Trollfoss.apk"), asset("SHA256SUMS.txt", id = 9))),
        )
        assertEquals("v1.2.0", newerRelease(raw, "1.1.0", previews = false)?.tag)
        assertEquals("v1.9.0", newerRelease(raw, "1.1.0", previews = true)?.tag)
    }

    @Test
    fun `a stored release survives a round trip`() {
        val original = AppRelease("v1.2.0", "Notat", 42, 1234, digest)
        assertEquals(original, AppRelease.decode(original.encode()))
        assertNull(AppRelease.decode("{not json"))
    }

    @Test
    fun `only github hosts for this repository are trusted`() {
        assertTrue(trustedUpdateUrl(RELEASE_API))
        assertTrue(trustedUpdateUrl("https://api.github.com/repos/$RELEASE_REPO/releases/assets/7"))
        assertTrue(trustedUpdateUrl("https://release-assets.githubusercontent.com/github-production-release-asset/1/2"))
        assertFalse(trustedUpdateUrl("http://api.github.com/repos/$RELEASE_REPO/releases"))
        assertFalse(trustedUpdateUrl("https://api.github.com/repos/someone/else/releases"))
        assertFalse(trustedUpdateUrl("https://evil.example/repos/$RELEASE_REPO/releases"))
        assertFalse(trustedUpdateUrl("https://user@api.github.com/repos/$RELEASE_REPO/releases"))
        assertFalse(trustedUpdateUrl("https://api.github.com:8443/repos/$RELEASE_REPO/releases"))
    }

    @Test
    fun `an update must be the same app, newer, signed by the same key and not debuggable`() {
        val installed = UpdatePackageIdentity("app.trollfoss", 2, "1.1.0", 26, debug = false, signers = setOf("key"))
        val good = installed.copy(versionCode = 3, versionName = "1.2.0")
        assertTrue(compatibleUpdate(installed, good, "v1.2.0", sdk = 36))
        assertFalse(compatibleUpdate(installed, good.copy(name = "app.other"), "v1.2.0", 36))
        assertFalse(compatibleUpdate(installed, good.copy(versionCode = 2), "v1.2.0", 36))
        assertFalse(compatibleUpdate(installed, good.copy(signers = setOf("other")), "v1.2.0", 36))
        assertFalse(compatibleUpdate(installed, good.copy(debug = true), "v1.2.0", 36))
        assertFalse(compatibleUpdate(installed, good.copy(minSdk = 40), "v1.2.0", 36))
        assertFalse(compatibleUpdate(installed, good, "v1.3.0", 36))
        assertFalse(compatibleUpdate(installed.copy(signers = emptySet()), good.copy(signers = emptySet()), "v1.2.0", 36))
    }
}



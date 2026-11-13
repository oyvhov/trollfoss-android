package app.trollfoss.update

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@Suppress("DEPRECATION")
class ArchiveSignersTest {
    @Test
    fun stagedApkReportsTheSameCurrentSignersAndVersionAsTheInstalledApp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val manager = context.packageManager
        val flags = if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
        val installed = manager.getPackageInfo(context.packageName, flags)
        val signers = currentSigners(installed)
        assertTrue("The installed test app must have a signing certificate", signers.isNotEmpty())

        val staged = File.createTempFile("update-", ".apk", context.cacheDir)
        try {
            File(context.applicationInfo.sourceDir).inputStream().use { source ->
                staged.outputStream().use { target -> source.copyTo(target) }
            }
            val archive = checkNotNull(readUpdateArchive(manager, staged)) { "Android could not read the staged APK" }
            assertEquals(installed.packageName, archive.packageName)
            assertEquals(installed.versionName, archive.versionName)
            if (Build.VERSION.SDK_INT >= 28) {
                assertEquals(installed.longVersionCode, archive.longVersionCode)
            } else {
                assertEquals(installed.versionCode, archive.versionCode)
            }
            assertEquals(signers, currentSigners(archive))
        } finally {
            staged.delete()
        }
    }

    private fun currentSigners(info: PackageInfo): Set<Signature> {
        val signers = if (Build.VERSION.SDK_INT >= 28) info.signingInfo?.apkContentsSigners else info.signatures
        return signers.orEmpty().toSet()
    }
}

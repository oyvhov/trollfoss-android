// Same toolchain as Komet and Spole (AGP 9 with built-in Kotlin), so the projects share one Gradle cache.
plugins {
    id("com.android.application") version "9.2.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.10" apply false
}

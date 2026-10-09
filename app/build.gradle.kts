import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val signingProperties = Properties().apply {
    val propertiesFile = rootProject.file("signing.properties")
    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use(::load)
    }
}

val hasReleaseSigning = listOf(
    "storeFile",
    "storePassword",
    "keyAlias",
    "keyPassword",
).all { signingProperties.getProperty(it).isNullOrBlank().not() }

// A release signed with a different key cannot update an installed Topa. Refuse the build instead
// of silently producing an APK that the tablet will reject.
tasks.matching { it.name == "validateSigningRelease" || it.name == "packageRelease" }.configureEach {
    doFirst {
        check(hasReleaseSigning) {
            "Release-bygg krev signing.properties og Trollfoss-nøkkelen i .signing/. Ikkje lag ein ny nøkkel."
        }
    }
}

android {
    namespace = "app.trollfoss"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "app.trollfoss"
        minSdk = 26
        targetSdk = 36
        // The published release sets these. Only the update test in docs/RELEASE_WORKFLOW.md overrides
        // them, to build an older local copy that the release must be able to replace.
        versionCode = (findProperty("trollfossVersionCode") as String?)?.toInt() ?: 21
        versionName = (findProperty("trollfossVersionName") as String?) ?: "1.16.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        debug {
            // Helpers building in parallel worktrees give each debug build its own package (-PtrollfossIdSuffix=.stova),
            // so they never overwrite each other on a shared emulator.
            applicationIdSuffix = (findProperty("trollfossIdSuffix") as String?) ?: ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.create("trollfossRelease") {
                    storeFile = rootProject.file(signingProperties.getProperty("storeFile"))
                    storePassword = signingProperties.getProperty("storePassword")
                    keyAlias = signingProperties.getProperty("keyAlias")
                    keyPassword = signingProperties.getProperty("keyPassword")
                }
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.06.00")

    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    // android.jar only ships stubs for org.json; the real implementation lets the store be tested on the JVM.
    testImplementation("org.json:json:20240303")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}

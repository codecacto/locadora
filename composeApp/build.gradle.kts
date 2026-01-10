import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

// Load local.properties for release signing credentials
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

fun getLocalProperty(key: String): String? =
    localProperties.getProperty(key)
        ?: project.findProperty(key) as String?
        ?: System.getenv(key)

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            binaryOption("bundleId", "br.com.codecacto.locadora.ComposeApp")
            export(libs.androidx.lifecycle.viewmodel)
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.android)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.androidx.core.splashscreen)
            implementation(libs.androidx.work.runtime)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // ViewModel
            api(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Serialization
            implementation(libs.kotlinx.serialization.json)

            // Navigation
            implementation(libs.navigation.compose)

            // DateTime
            implementation(libs.kotlinx.datetime)

            // DataStore
            implementation(libs.androidx.datastore.preferences.core)

            // GitLive Firebase SDK
            implementation(libs.gitlive.firebase.auth)
            implementation(libs.gitlive.firebase.common)
            implementation(libs.gitlive.firebase.firestore)
            implementation(libs.gitlive.firebase.crashlytics)

            // Image Loading
            implementation(libs.coil.compose)
            implementation(libs.coil.compose.core)
            implementation(libs.coil.network.ktor)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }

    sourceSets.all {
        languageSettings.optIn("kotlin.time.ExperimentalTime")
    }
}

android {
    namespace = "br.com.codecacto.locadora"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "br.com.codecacto.locadora"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
    }
    // Signing configurations for debug and release builds
    // IMPORTANT: Release credentials must be in local.properties (gitignored) or environment variables
    signingConfigs {
        getByName("debug") {
            // Prefer the default Android debug keystore unless explicit credentials are provided.
            val ksFileProp = getLocalProperty("DEBUG_KEYSTORE_FILE")
            val ksPassword = getLocalProperty("DEBUG_KEYSTORE_PASSWORD")
            val keyAliasProp = getLocalProperty("DEBUG_KEY_ALIAS")
            val keyPasswordProp = getLocalProperty("DEBUG_KEY_PASSWORD")
            if (!ksFileProp.isNullOrBlank()
                && !ksPassword.isNullOrBlank()
                && !keyAliasProp.isNullOrBlank()
                && !keyPasswordProp.isNullOrBlank()
            ) {
                storeFile = file(ksFileProp)
                storePassword = ksPassword
                keyAlias = keyAliasProp
                keyPassword = keyPasswordProp
            }
        }

        // Release signing config - reads from local.properties, gradle.properties, or environment variables
        // IMPORTANT: Never commit the keystore or credentials to the repository!
        // Set these via: local.properties (gitignored) or environment variables for CI/CD
        create("release") {
            val releaseKsFile = getLocalProperty("RELEASE_KEYSTORE_FILE")
            val releaseKsPassword = getLocalProperty("RELEASE_KEYSTORE_PASSWORD")
            val releaseKeyAlias = getLocalProperty("RELEASE_KEY_ALIAS")
            val releaseKeyPassword = getLocalProperty("RELEASE_KEY_PASSWORD")

            if (!releaseKsFile.isNullOrBlank() && file(releaseKsFile).exists()) {
                storeFile = file(releaseKsFile)
                storePassword = releaseKsPassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"

            // Ensure debug uses the configured debug signing config
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Use release signing config if available, otherwise unsigned
            val releaseSigningConfig = signingConfigs.findByName("release")
            if (releaseSigningConfig?.storeFile != null) {
                signingConfig = releaseSigningConfig
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

compose.resources {
    publicResClass = true
}

dependencies {
    debugImplementation(compose.uiTooling)

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
}

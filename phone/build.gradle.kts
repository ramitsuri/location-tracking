plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

android {
    val appVersion = libs.versions.appVersion.get()
    namespace = "com.ramitsuri.locationtracking"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.ramitsuri.locationtracking"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = appVersion.toDouble().times(100).toInt()
        versionName = appVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Enable if testing
            // signingConfig signingConfigs.debug
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(compose.components.resources)
    implementation(compose.components.uiToolingPreview)
    implementation(compose.materialIconsExtended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(compose.preview)
    implementation(libs.maps.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.play.services.location)
    implementation(libs.koin.android)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.koin.workmanager)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.android)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlin.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.splash)
    implementation(libs.kermit)
    implementation(libs.room.ktx)
    implementation(libs.room.runtime)

    debugImplementation(compose.uiTooling)
}

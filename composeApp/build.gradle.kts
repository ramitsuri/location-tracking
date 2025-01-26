import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.room)
}

kotlin {
    jvm()

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.lifecycle.service)
            implementation(libs.play.services.location)
            implementation(libs.koin.android)
            implementation(libs.koin.workmanager)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.ktor.client.android)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.kotlin.datetime)
            implementation(libs.splash)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.datastore)
            implementation(libs.kermit)
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.content.negotation)
            implementation(libs.ktor.logging)
            implementation(libs.ktor.serialization)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlin.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.room.ktx)
            implementation(libs.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.androidx.work.runtime.ktx)
        }

        jvmTest.dependencies {
            implementation(libs.room.testing)
            implementation(libs.koin.test)
            implementation(libs.koin.test.junit)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
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
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspJvm", libs.room.compiler)
    add("kspJvmTest", libs.room.compiler)
    debugImplementation(compose.uiTooling)
}

room {
    schemaDirectory("$projectDir/schemas")
}

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
        exclude { element -> element.file.toString().contains("generated/") }
        exclude { element -> element.file.toString().contains("build/") }
    }
}

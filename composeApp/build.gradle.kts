import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "1.9.0"
    alias(libs.plugins.googleGmsGoogleServices)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // Configure XCFramework
    val xcframework = XCFramework("ComposeApp")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true

            // Add to XCFramework
            xcframework.add(this)

            // Export dependencies
            export(libs.androidx.lifecycle.viewmodelCompose)
            export(libs.androidx.lifecycle.runtimeCompose)
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                api(libs.androidx.lifecycle.viewmodelCompose)
                api(libs.androidx.lifecycle.runtimeCompose)
                implementation(libs.compose.material.icons.extended)
                implementation("io.github.qdsfdhvh:image-loader:1.7.8")
                implementation("com.russhwolf:multiplatform-settings-no-arg:1.1.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
                implementation("io.ktor:ktor-client-core:2.3.9")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.9")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.9")
                implementation("io.ktor:ktor-client-logging:2.3.5")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
                implementation("cafe.adriel.voyager:voyager-navigator:1.0.0")
                implementation("cafe.adriel.voyager:voyager-screenmodel:1.0.0")
                implementation("cafe.adriel.voyager:voyager-koin:1.0.0")
                implementation("cafe.adriel.voyager:voyager-tab-navigator:1.0.0")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test:2.0.21")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
                implementation("io.ktor:ktor-client-okhttp:2.3.9")
                implementation(files("libs/qring_sdk_20250516.aar"))
                implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
                implementation("org.greenrobot:eventbus:3.2.0")
                implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
                implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
                implementation("androidx.compose.runtime:runtime-livedata:1.6.1")
                implementation("androidx.navigation:navigation-compose:2.8.5")
                implementation("com.google.android.exoplayer:exoplayer:2.19.1")
                implementation("com.google.android.exoplayer:exoplayer-ui:2.19.1")
                implementation("app.cash.sqldelight:android-driver:2.0.1")
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting

        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                implementation("io.ktor:ktor-client-darwin:2.3.9")
            }
        }
    }
}

android {
    namespace = "dev.infa.page3"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.infa.page3"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.firebase.auth)
    debugImplementation(compose.uiTooling)
}
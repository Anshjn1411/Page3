import org.gradle.kotlin.dsl.implementation
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

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

    val xcframework = XCFramework("ComposeApp")

    // ✅ FIX: Add iosSimulatorArm64
    val iosTargets = listOf(
        iosX64(),
        iosArm64(),
    )

    iosTargets.forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true

            xcframework.add(this)
            binaryOption("bundleId", "dev.infa.page3")

            export(libs.androidx.lifecycle.viewmodelCompose)
            export(libs.androidx.lifecycle.runtimeCompose)

            linkerOpts(
                "-framework", "SystemConfiguration",
                "-ObjC"
            )
        }
    }

    // ✅ FIX: Compiler flag for Xcode integration
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.all {
            freeCompilerArgs += listOf(
                "-Xbinary=ios_use_xcode_message_style=true"
            )
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
                implementation("io.github.qdsfdhvh:image-loader:1.10.0")
                implementation("com.russhwolf:multiplatform-settings-no-arg:1.3.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

                implementation("io.ktor:ktor-client-core:3.3.3")
                implementation("io.ktor:ktor-client-content-negotiation:3.3.3")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.3")
                implementation("io.ktor:ktor-client-logging:3.3.3")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

                implementation("cafe.adriel.voyager:voyager-navigator:1.0.1")
                implementation("cafe.adriel.voyager:voyager-screenmodel:1.0.1")
                implementation("cafe.adriel.voyager:voyager-koin:1.0.1")
                implementation("cafe.adriel.voyager:voyager-tab-navigator:1.0.1")

                implementation("io.insert-koin:koin-core:3.5.6")
                implementation("io.insert-koin:koin-compose:1.1.5")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("io.coil-kt:coil-compose:2.7.0")   // rememberAsyncImagePainter, ImageLoader
                implementation("io.coil-kt:coil-gif:2.7.0")
                implementation("io.insert-koin:koin-android:3.5.6")
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)

                implementation("io.ktor:ktor-client-okhttp:3.3.3")

                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
                implementation("androidx.fragment:fragment-ktx:1.6.0")

                implementation("io.ktor:ktor-client-okhttp:3.3.3")
                implementation(files("libs/QWatchPro__sdk_20251120.aar"))
                // VeePoo SDK (V-Band smart watch)
                implementation(files("libs/vpprotocol-2.3.27.15.aar"))
                implementation(files("libs/vpbluetooth-1.18.aar"))
                implementation(files("libs/libble-0.5.aar"))
                implementation(files("libs/libcomx-0.5.jar"))
                implementation(files("libs/libdfu-1.5.aar"))
                implementation(files("libs/libfastdfu-0.5.aar"))
                // JieLi OTA/Watch libs (required by VeePoo SDK internally)
                implementation(files("libs/jl_bt_ota_V1.10.0_10931-release.aar"))
                implementation(files("libs/jl_rcsp_V0.7.2_527-release.aar"))
                implementation(files("libs/JL_Watch_V1.13.1_11214-release.aar"))
                implementation(files("libs/BmpConvert_V1.6.0_10604-release.aar"))
                implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
                implementation("org.greenrobot:eventbus:3.3.1")
                implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
                implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
                implementation("androidx.compose.runtime:runtime-livedata:1.9.5")
                implementation("androidx.navigation:navigation-compose:2.9.6")
                implementation("com.google.android.exoplayer:exoplayer:2.19.1")
                implementation("com.google.android.exoplayer:exoplayer-ui:2.19.1")
                implementation("app.cash.sqldelight:android-driver:2.2.1")
                implementation("com.airbnb.android:lottie-compose:6.6.0")
                implementation("com.google.code.gson:gson:2.10.1")
                // PhonePe SDK
                implementation("phonepe.intentsdk.android.release:IntentSDK:5.3.0")
            }
        }

        // ✅ FIX: Proper iOS hierarchy
        val iosX64Main by getting
        val iosArm64Main by getting

        val iosMain by creating {
            dependsOn(commonMain)

            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)

            dependencies {
                implementation("io.ktor:ktor-client-darwin:3.3.3")
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
        versionCode = 24
        versionName = "2.4"

        // PhonePe SDK credentials from gradle.properties
        buildConfigField("String", "PHONEPE_CLIENT_ID", "\"${project.findProperty("PHONEPE_CLIENT_ID") ?: ""}\"")
        buildConfigField("String", "PHONEPE_CLIENT_SECRET", "\"${project.findProperty("PHONEPE_CLIENT_SECRET") ?: ""}\"")
        buildConfigField("String", "PHONEPE_CLIENT_VERSION", "\"${project.findProperty("PHONEPE_CLIENT_VERSION") ?: "1"}\"")
        buildConfigField("String", "PHONEPE_MERCHANT_ID", "\"${project.findProperty("PHONEPE_MERCHANT_ID") ?: ""}\"")
    }
    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            ndk {
                debugSymbolLevel = "FULL"
            }
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
    implementation(libs.firebase.auth)
    debugImplementation(compose.uiTooling)
}

compose {
    resources {
        publicResClass = true
    }
}

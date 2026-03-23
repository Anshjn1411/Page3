package dev.infa.page3.ui.orderscreen

import dev.infa.page3.BuildConfig

/**
 * Android actual implementations – read PhonePe credentials from BuildConfig
 * (populated from gradle.properties via buildConfigField in build.gradle.kts).
 */
actual fun getPhonePeClientId(): String = BuildConfig.PHONEPE_CLIENT_ID
actual fun getPhonePeClientSecret(): String = BuildConfig.PHONEPE_CLIENT_SECRET
actual fun getPhonePeClientVersion(): String = BuildConfig.PHONEPE_CLIENT_VERSION
actual fun getPhonePeMerchantId(): String = BuildConfig.PHONEPE_MERCHANT_ID

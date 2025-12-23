package dev.infa.page3.SDK.ui.utils

import dev.infa.page3.Page3Application

// androidMain/PlatformContext.android.kt
actual object PlatformContext {
    actual fun get(): Any = Page3Application.instance
}
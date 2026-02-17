package dev.infa.page3.SDK.ui.utils

import platform.UIKit.UIApplication

actual object PlatformContext {
    actual fun get(): Any {
        return UIApplication.sharedApplication
    }
}

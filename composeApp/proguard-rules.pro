# PhonePe SDK ProGuard Rules
-keep class com.phonepe.intent.sdk.api.** { *; }
-keep class com.phonepe.intent.sdk.api.models.** { *; }
-dontwarn com.phonepe.**

# PhonePe SDK data models
-keep class dev.infa.page3.data.model.PhonePe* { *; }
-keep class dev.infa.page3.payment.PhonePeSDKHelper { *; }

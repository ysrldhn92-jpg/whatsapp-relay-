plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "il.example.whatsappgrouprelay"
    compileSdk = 35

    defaultConfig {
        applicationId = "il.example.whatsappgrouprelay"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1-prototype"
    }
}

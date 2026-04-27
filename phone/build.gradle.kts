plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace  = "com.gesturecomm"
    compileSdk = 34
    defaultConfig {
        applicationId  = "com.gesturecomm"
        minSdk         = 26
        targetSdk      = 34
        versionCode    = 1
        versionName    = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(libs.play.services.wearable)
    implementation(libs.core.ktx)
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.play.services)
}

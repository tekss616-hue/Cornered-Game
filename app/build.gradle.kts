plugins {
    id("com.android.application")
}

android {
    namespace = "com.studio.ai"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.studio.ai"
        minSdk = 26
        targetSdk = 35
        versionCode = 10
        versionName = "0.10.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

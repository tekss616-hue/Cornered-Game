plugins {
    id("com.android.application")
}

android {
    namespace = "com.cornered.game"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.cornered.game"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

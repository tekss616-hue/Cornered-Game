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
        versionCode = 14
        versionName = "0.14.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        getByName("debug") { isMinifyEnabled = false }
        getByName("release") { isMinifyEnabled = false }
    }
}

dependencies {
    implementation("androidx.media3:media3-transformer:1.11.0")
    implementation("androidx.media3:media3-effect:1.11.0")
    implementation("androidx.media3:media3-common:1.11.0")
}

plugins {
    id("com.android.application")
}

android {
    namespace = "com.cornered.game"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cornered.game"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "0.1.1"

        buildConfigField("String", "FIREBASE_API_KEY", "\"${System.getenv("CORNERED_FIREBASE_API_KEY") ?: ""}\"")
        buildConfigField("String", "FIREBASE_APP_ID", "\"1:228318611339:android:0dc033187e6921e974518e\"")
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"veilmark-d2480\"")
        buildConfigField("String", "FIREBASE_DATABASE_URL", "\"https://veilmark-d2480-default-rtdb.firebaseio.com\"")
        buildConfigField("String", "FIREBASE_STORAGE_BUCKET", "\"veilmark-d2480.firebasestorage.app\"")
        buildConfigField("String", "FIREBASE_WEB_CLIENT_ID", "\"228318611339-aehctm7ggfreblqfpddd8efglgh87rtt.apps.googleusercontent.com\"")
        buildConfigField("String", "SERVER_BASE_URL", "\"${System.getenv("CORNERED_SERVER_BASE_URL") ?: ""}\"")
    }

    buildFeatures {
        buildConfig = true
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

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.18.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
}

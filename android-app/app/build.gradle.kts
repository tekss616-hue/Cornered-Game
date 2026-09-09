plugins {
    id("com.android.application")
}

fun javaString(value: String): String = "\"" + value
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")
    .replace("\n", "\\n")
    .replace("\r", "\\r") + "\""

val firebaseSecret = System.getenv("CORNERED_FIREBASE_API_KEY")?.trim().orEmpty()
val firebaseApiKey = sequenceOf(
    Regex("\"current_key\"\\s*:\\s*\"([^\"]+)\""),
    Regex("\\\\\"current_key\\\\\"\\s*:\\s*\\\\\"([^\\\\\"]+)\\\\\"")
).mapNotNull { it.find(firebaseSecret)?.groupValues?.getOrNull(1) }
 .firstOrNull()
 ?: firebaseSecret.trim().trim('"')

android {
    namespace = "com.cornered.game"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cornered.game"
        minSdk = 24
        targetSdk = 36
        versionCode = 9
        versionName = "0.1.8"

        buildConfigField("String", "FIREBASE_API_KEY", javaString(firebaseApiKey))
        buildConfigField("String", "FIREBASE_APP_ID", javaString("1:228318611339:android:0dc033187e6921e974518e"))
        buildConfigField("String", "FIREBASE_PROJECT_ID", javaString("veilmark-d2480"))
        buildConfigField("String", "FIREBASE_DATABASE_URL", javaString("https://veilmark-d2480-default-rtdb.firebaseio.com"))
        buildConfigField("String", "FIREBASE_STORAGE_BUCKET", javaString("veilmark-d2480.firebasestorage.app"))
        buildConfigField("String", "FIREBASE_WEB_CLIENT_ID", javaString("228318611339-aehctm7ggfreblqfpddd8efglgh87rtt.apps.googleusercontent.com"))
        buildConfigField("String", "SERVER_BASE_URL", javaString("https://cornered-server.onrender.com"))
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

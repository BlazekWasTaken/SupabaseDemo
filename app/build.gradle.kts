plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.example.supabasedemo"
    compileSdk = 35

    val key: String = com.android.build.gradle.internal.cxx.configure.gradleLocalProperties(rootDir, providers)
        .getProperty("supabaseKey")
    val url: String = com.android.build.gradle.internal.cxx.configure.gradleLocalProperties(rootDir, providers)
        .getProperty("supabaseUrl")

    defaultConfig {
        applicationId = "com.example.supabasedemo"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String","supabaseKey","\"$key\"")
        buildConfigField("String","supabaseUrl","\"$url\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.bom))
    implementation(libs.auth.kt)
    implementation(libs.realtime.kt)

    implementation(libs.ktor.client.okhttp)

    //noinspection UseTomlInstead
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

}
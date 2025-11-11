plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    kotlin("plugin.serialization") version "1.9.10"
}

android {
    namespace = "com.example.parkwise"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.parkwise"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // 1. Define custom field for BuildConfig
        buildConfigField("String", "MAPS_API_KEY", "\"${project.findProperty("MAPS_API_KEY")}\"")

        // 2. Define Manifest Placeholder (CRITICAL FIX for Manifest merger)
        manifestPlaceholders["MAPS_API_KEY"] = project.findProperty("MAPS_API_KEY").toString()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.database)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Compose UI
    implementation("androidx.compose.ui:ui:1.5.3")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.3")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.3")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.6.0")
    implementation("com.google.accompanist:accompanist-navigation-animation:0.32.0")

    // Retrofit + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    // Lifecycle / ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    // Ktor client for realtime
    implementation("io.ktor:ktor-client-okhttp:3.3.1")

    // JSON (Gson)
    implementation("com.google.code.gson:gson:2.10.1")

    // Coil for image loading
    implementation(libs.coil.compose)
    implementation(libs.coil.compose.core)
    implementation("io.coil-kt.coil3:coil-compose:3.3.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")

    // Sceneform
    implementation("com.gorisse.thomas.sceneform:sceneform:1.21.0")

    // Google Play Services
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // Firebase
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.4")

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    // ✅ MAPBOX - Using only Maps SDK (publicly available)
    implementation("com.mapbox.maps:android:11.16.0")

    // 2. Navigation SDK
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-services:7.8.0")



    // 3. GeoJSON
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-geojson:7.8.0")
    


    // 4. Gson
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.google.android.gms:play-services-location:21.0.1")


    // We already have Retrofit, so we'll use it for Mapbox API calls directly
}
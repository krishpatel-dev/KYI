plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    kotlin("kapt")
}

android {
    namespace = "com.krishhh.knowyouringredients"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.krishhh.knowyouringredients"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
    kotlinOptions { jvmTarget = "11" }
    buildFeatures { viewBinding = true }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation("com.google.android.material:material:1.12.0")
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Firebase platform & libs
    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // CameraX
    val camerax = "1.3.1"
    implementation("androidx.camera:camera-core:$camerax")
    implementation("androidx.camera:camera-camera2:$camerax")
    implementation("androidx.camera:camera-lifecycle:$camerax")
    implementation("androidx.camera:camera-view:$camerax")

    // Needed for await() support with ListenableFuture
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.8.1")

    // Optional but okay to leave
    implementation("com.google.guava:guava:32.1.3-jre")
    implementation("androidx.concurrent:concurrent-futures-ktx:1.1.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    /* ML Kit on‑device text recognition */
    implementation("com.google.mlkit:text-recognition:16.0.0")

    /* Room (pre‑loaded from CSV) */
    val room = "2.6.1"
    implementation("androidx.room:room-ktx:$room")
    kapt("androidx.room:room-compiler:$room")

    /* Coroutines for Room & MLKit background work */
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    implementation("com.opencsv:opencsv:5.9")
}


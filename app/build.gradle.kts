plugins {

    alias(libs.plugins.android.application)

    alias(libs.plugins.kotlin.compose)

    id("com.google.devtools.ksp")
}

android {

    namespace = "com.example.sathiai"

    compileSdk = 36

    defaultConfig {

        applicationId = "com.example.sathiai"

        minSdk = 29

        targetSdk = 36

        versionCode = 1

        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {

        release {

            isMinifyEnabled = false

            proguardFiles(

                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),

                "proguard-rules.pro"
            )
        }
    }

    compileOptions {

        sourceCompatibility =
            JavaVersion.VERSION_11

        targetCompatibility =
            JavaVersion.VERSION_11
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

    // ── Compose ───────────────────────────────

    implementation(
        platform(libs.androidx.compose.bom)
    )

    implementation(
        libs.androidx.activity.compose
    )

    implementation(
        libs.androidx.compose.material3
    )

    implementation(
        libs.androidx.compose.ui
    )

    implementation(
        libs.androidx.compose.ui.graphics
    )

    implementation(
        libs.androidx.compose.ui.tooling.preview
    )

    implementation(
        libs.androidx.compose.material.icons.core
    )

    implementation(
        "androidx.compose.material:material-icons-extended"
    )

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )

    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )

    // ── Android Core ─────────────────────────

    implementation(
        libs.androidx.core.ktx
    )

    implementation(
        libs.androidx.lifecycle.runtime.ktx
    )

    implementation(
        "androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2"
    )

    // ── Coroutines ──────────────────────────

    implementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1"
    )

    // ── Retrofit ────────────────────────────

    implementation(
        "com.squareup.retrofit2:retrofit:2.11.0"
    )

    implementation(
        "com.squareup.retrofit2:converter-gson:2.11.0"
    )

    implementation(
        "com.squareup.okhttp3:logging-interceptor:4.12.0"
    )

    // ── Room Database ───────────────────────

    implementation(
        "androidx.room:room-runtime:2.6.1"
    )

    implementation(
        "androidx.room:room-ktx:2.6.1"
    )

    ksp(
        "androidx.room:room-compiler:2.6.1"
    )

    // ── Testing ─────────────────────────────

    testImplementation(
        libs.junit
    )

    androidTestImplementation(
        platform(libs.androidx.compose.bom)
    )

    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )

    androidTestImplementation(
        libs.androidx.espresso.core
    )

    androidTestImplementation(
        libs.androidx.junit
    )
}
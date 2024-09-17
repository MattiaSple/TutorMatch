plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.tutormatch"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tutormatch"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.mediarouter)
    implementation(libs.androidx.ui.desktop)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)

    ////////////////////////////////////////////////////////////////////////////////
    implementation (libs.google.firebase.database.ktx)
    implementation(platform(libs.firebase.bom.v3102))
    implementation (libs.com.google.firebase.firebase.database.ktx)
    implementation (libs.firebase.messaging)
////////////////////////////////////////////////////////////////////////////////
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.recyclerview)

    //Fragments dependencies
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    //PER LA MAPPA
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    //per la localizzazione dello studente
    implementation ("com.google.android.gms:play-services-location:21.0.1")

    // Dipendenza per OkHttp Logging Interceptor
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")

    // OSMDroid core library
    implementation ("org.osmdroid:osmdroid-android:6.1.11")
    implementation(libs.androidx.preference.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.messaging.ktx)
    //implementation(libs.osmdroid.android)
    //implementation(libs.osmbonuspack)
    //implementation(libs.androidx.preference.ktx)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}


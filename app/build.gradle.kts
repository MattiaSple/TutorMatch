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

    packaging {
        resources {
            excludes.add("META-INF/LICENSE.md")
            excludes.add("META-INF/LICENSE")
            excludes.add("META-INF/NOTICE")
            excludes.add("META-INF/LICENSE-notice.md")
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
    //implementation (libs.com.google.firebase.firebase.database.ktx)
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
    implementation (libs.play.services.location)


    // Dipendenza per OkHttp Logging Interceptor
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")

    // OSMDroid core library
    implementation ("org.osmdroid:osmdroid-android:6.1.11")
    implementation(libs.androidx.preference.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.androidx.junit.ktx)

    // JUnit per i test unitari
    testImplementation (libs.junit)

    // Per testare LiveData
    testImplementation (libs.androidx.core.testing)

    // Coroutine test (per testare le coroutine)
    testImplementation (libs.kotlinx.coroutines.test)
    testImplementation (libs.mockito.kotlin)
    // Libreria MockK per mocking (opzione 1)
    testImplementation (libs.mockk)
    testImplementation (libs.robolectric)
    // Strumentazione dei test (solo se fai test su emulatore/dispositivo)
    androidTestImplementation (libs.androidx.junit.v121)
    androidTestImplementation (libs.androidx.espresso.core.v361)

    androidTestImplementation (libs.mockk.android)

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2") {
        exclude(group = "org.junit.platform", module = "junit-platform-commons")
    }
}


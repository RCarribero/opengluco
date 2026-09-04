plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.example.opengluco.wear"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.opengluco"
        minSdk = 29
        targetSdk = 34
        versionCode = 12
        versionName = "1.2.0"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(project(":core:data"))

    // Wear OS Core & Compose
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("com.google.android.gms:play-services-wearable:18.2.0")
    implementation("androidx.percentlayout:percentlayout:1.0.0")

    // Compose for Wear OS & Material 3
    implementation("androidx.wear.compose:compose-material3:1.0.0-alpha24")
    implementation("androidx.wear.compose:compose-foundation:1.4.0")
    implementation("androidx.wear.compose:compose-navigation:1.4.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.8")
    implementation("androidx.compose.material:material-icons-extended:1.6.8")

    // Watch Face Complications
    implementation("androidx.wear.watchface:watchface-complications-data-source:1.2.1")

    // Wear OS Tiles & ProtoLayout
    implementation("androidx.wear.tiles:tiles:1.4.0")
    implementation("androidx.wear.protolayout:protolayout:1.2.0")
    implementation("androidx.wear.protolayout:protolayout-material:1.2.0")
    implementation("com.google.guava:guava:33.2.1-android")

    // ZXing para renderizado de QR en pantalla del reloj
    implementation("com.google.zxing:core:3.5.3")

    // WorkManager (Segundo plano)
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
}

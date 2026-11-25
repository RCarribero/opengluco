import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val keystoreProps = Properties().apply {
    val propsFile = rootProject.file("keystore.properties")
    if (propsFile.exists()) {
        propsFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.example.opengluco.auto"
    compileSdk = 35

    signingConfigs {
        create("release") {
            val storePath = keystoreProps.getProperty("storeFile")
                ?: System.getenv("OPENGLUCO_KEYSTORE_PATH")
            if (!storePath.isNullOrBlank()) {
                storeFile = file(storePath)
            }
            storePassword = keystoreProps.getProperty("storePassword")
                ?: System.getenv("OPENGLUCO_KEYSTORE_PASSWORD")
            keyAlias = keystoreProps.getProperty("keyAlias")
                ?: System.getenv("OPENGLUCO_KEY_ALIAS")
            keyPassword = keystoreProps.getProperty("keyPassword")
                ?: System.getenv("OPENGLUCO_KEY_PASSWORD")
        }
    }

    defaultConfig {
        applicationId = "com.example.opengluco.auto"
        minSdk = 29
        targetSdk = 34
        versionCode = 22
        versionName = "1.3.0"
    }

    buildTypes {
        release {
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null && releaseSigning.storeFile!!.exists()) {
                signingConfig = releaseSigning
            }
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
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(project(":core:data"))

    // Android Auto (Car App Library)
    implementation("androidx.car.app:app:1.4.0")

    // Core & Coroutines
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ZXing para renderizado de QR en pantalla de Android Auto
    implementation("com.google.zxing:core:3.5.3")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
}

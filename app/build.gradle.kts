plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val envVersionName = System.getenv("VERSION_NAME") ?: "1.0.0.0"
val envVersionCode = System.getenv("VERSION_CODE")?.toInt() ?: 1

android {
    namespace = "com.hereliesaz.reup"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hereliesaz.reup"
        minSdk = 26
        targetSdk = 34
        versionCode = envVersionCode
        versionName = envVersionName
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    // The compiler must not crush the neural network's weights.
    androidResources {
        noCompress.add("tflite")
    }
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            output.outputFileName.set("ReUp-${variant.name}-${envVersionName}.apk")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    
    // The artificial cortex
    implementation("org.tensorflow:tensorflow-lite-task-text:0.4.4")
}

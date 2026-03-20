// hereliesaz/reup/ReUp-9db2805a9ede9350d55e55d72acf9c1535bb70f4/app/build.gradle.kts

import java.net.URI
import java.io.FileOutputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("base")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Interrogating the source of truth for versioning
val versionProps = Properties().apply {
    val file = rootProject.file("version.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

val major = versionProps.getProperty("MAJOR", "0")
val minor = versionProps.getProperty("MINOR", "0")
val envVersionName = "$major.$minor.0"
val envVersionCode = (major.toInt() * 100) + minor.toInt()

android {
    namespace = "com.hereliesaz.reup"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.hereliesaz.reup"
        minSdk = 26
        targetSdk = 36
        versionCode = envVersionCode
        versionName = envVersionName
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
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

    androidResources {
        noCompress.add("tflite")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildToolsVersion = "36.1.0"
}

base {
    archivesName.set("ReUp-$envVersionName")
}

tasks.register("downloadCortex") {
    val modelUrl = "[https://storage.googleapis.com/download.tensorflow.org/models/tflite/text_classification/text_classification_v2.tflite](https://storage.googleapis.com/download.tensorflow.org/models/tflite/text_classification/text_classification_v2.tflite)"
    val destDir = file("src/main/assets")
    val destFile = file("$destDir/sentiment_classifier.tflite")

    doLast {
        if (!destFile.exists()) {
            println("Awaiting neural network geometry...")
            destDir.mkdirs()
            URI(modelUrl).toURL().openStream().use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            println("Cortex successfully grafted into the panopticon.")
        } else {
            println("The machine already possesses a mind. Skipping assimilation.")
        }
    }
}

tasks.named("preBuild") {
    dependsOn("downloadCortex")
}

dependencies {
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation(platform("androidx.compose:compose-bom:2026.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    
    // LiteRT (TFLite) Task Library for natural language interpretation
    // Redundant litert-core removed to prevent dependency hallucinations/collisions
    implementation("org.tensorflow:tensorflow-lite-task-text:0.4.4") {
        exclude(group = "org.tensorflow", module = "tensorflow-lite")
        exclude(group = "org.tensorflow", module = "tensorflow-lite-api")
    }
    
    implementation("com.google.auto.value:auto-value-annotations:1.11.1")
}

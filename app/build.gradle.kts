plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-parcelize")
    kotlin("kapt")
    id("idea")
}

android {
    // This is pretty default Android project setup
    compileSdk = 31
    buildToolsVersion = "31.0.0"

    defaultConfig {
        applicationId = "com.example.smartpossample"
        minSdk = 24
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    kotlinOptions {
        this.jvmTarget = "1.8"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // This sets extra source sets, not necessary but nice to separate Kotlin and Java code
    sourceSets {
        map {
            it.java.srcDirs(
                "src/${it.name}/kotlin",
                "src-get/${it.name}/kotlin",
                "src-gen/${it.name}/java",
                "src/${it.name}/java"
            )
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        viewBinding = true
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

dependencies {
    // Default implementations from Android Studio template project
    implementation(kotlin("stdlib-jdk8", version = ProjectConstants.KOTLIN_VERSION))
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.4.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.4.0")
    implementation("androidx.preference:preference:1.2.0")

    implementation("com.google.zxing:core:3.5.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    // Implementation for Nets SmartPOS SDK
    implementation(group = "eu.nets.lab.smartpos", name = "sdk-core", version = ProjectConstants.SDK_VERSION)
    implementation(group = "eu.nets.lab.smartpos", name = "sdk-utilities", version = ProjectConstants.UTILITIES_VERSION)
    implementation(group = "eu.nets.lab.smartpos", name = "sdk-printer", version = ProjectConstants.PRINTER_VERSION)
    implementation(group = "eu.nets.lab.smartpos", name = "sdk-printer-castles", version = ProjectConstants.PRINTER_VERSION)
    implementation(group = "eu.nets.lab.smartpos", name = "sdk-room-extensions-jackson", version = ProjectConstants.ROOM_VERSION)
    // Not in tutorial
    implementation(group = "eu.nets.lab.smartpos", name = "sdk-scanner-castles", version = ProjectConstants.SCANNER_VERSION)

    // Kotlin support for Room
    implementation("androidx.room:room-ktx:2.4.1")
    kapt("androidx.room:room-compiler:2.4.1")
}
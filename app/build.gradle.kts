plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-parcelize")
    kotlin("kapt")
}

android {
    // This is pretty default Android project setup
    compileSdkVersion(31)
    buildToolsVersion("31.0.0")

    defaultConfig {
        applicationId("com.example.smartpossample")
        minSdkVersion(26)
        targetSdkVersion(31)
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

dependencies {
    // Default implementations from Android Studio template project
    implementation(kotlin("stdlib-jdk8", version = ProjectConstants.KOTLIN_VERSION))
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.5")

    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    // Implementation for Nets SmartPOS SDK
    implementation(group = "eu.nets.lab.smartpos", name = "nets-smartpos-sdk", version = ProjectConstants.SDK_VERSION)

    // Kotlin support for Room
    implementation("androidx.room:room-ktx:2.3.0")
    kapt("androidx.room:room-compiler:2.3.0")
}
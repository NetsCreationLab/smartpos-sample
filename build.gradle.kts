// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.2")
        classpath(kotlin("gradle-plugin", ProjectConstants.KOTLIN_VERSION))

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    apply(from = "$rootDir/secure.gradle.kts")
    repositories {
        google()
        mavenCentral()
        // Repository containing SmartPOS SDK
        maven(project.extra["sdkUri"] ?: "")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
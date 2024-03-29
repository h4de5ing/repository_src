import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
//    id("maven-publish")
    `maven-publish`
}

android {
    namespace = "com.github.h4de5ing.document"
    compileSdk = 33

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    implementation("org.nanohttpd:nanohttpd:2.3.1")
}
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.h4de5ing.document"
            artifactId = "document"
            val today = SimpleDateFormat("yyyyMMdd").format(Date())
            version = "1.0-${today}"
            artifact("$buildDir/outputs/aar/Document-release.aar")
        }
    }
    repositories {
        mavenLocal()
        maven("D://repository/repository/repository")
    }
}
//gradlew publish
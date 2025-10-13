import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "com.github.h4de5ing.document"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_22
        targetCompatibility = JavaVersion.VERSION_22
    }
    publishing { singleVariant("release") {} }
}

dependencies {
    api("org.nanohttpd:nanohttpd:2.3.1")
}
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.h4de5ing.document"
            artifactId = "document"
            val today = SimpleDateFormat("yyyyMMdd").format(Date())
            version = "1.0-${today}"
            artifact("${layout.buildDirectory.get().asFile}/outputs/aar/Document-release.aar")
        }
    }
    repositories {
        mavenLocal()
        maven("D://repository/repository/repository")
    }
}
//gradlew publish
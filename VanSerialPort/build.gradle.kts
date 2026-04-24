import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.library)
    id("maven-publish")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "com.github.h4de5ing.vanserialport"

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        //noinspection ChromeOsAbiSupport
        ndk {
            abiFilters += listOf("armeabi", "armeabi-v7a", "arm64-v8a")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_22
        targetCompatibility = JavaVersion.VERSION_22
    }

    buildFeatures {
        buildConfig = true
    }

    publishing {
        singleVariant("release") {}
    }
}

afterEvaluate {
    publishing {
        publications {
            val today = SimpleDateFormat("yyyyMMdd").format(Date())
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.github.h4de5ing.vanserialport"
                artifactId = "vanserialport"
                version = "1.0-$today"
            }
        }
        repositories {
            mavenLocal()
            maven {
                url = uri(project.findProperty("mavenRepositoryPath") as String)
            }
        }
    }
}
//gradlew publish

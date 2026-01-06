import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "com.github.h4de5ing.netlib"
    
    defaultConfig {
        minSdk = 26
        targetSdk = libs.versions.targetSdk.get().toInt()
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_22
        targetCompatibility = JavaVersion.VERSION_22
    }
    
    publishing {
        singleVariant("release") {
            //withSourcesJar()
        }
    }
}

dependencies {
    implementation(libs.java.websocket)
    implementation(libs.okhttp)
}

afterEvaluate {
    publishing {
        publications {
            val today = SimpleDateFormat("yyyyMMdd").format(Date())
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.github.h4de5ing.netlib"
                artifactId = "netlib"
                version = "2.0-$today"
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

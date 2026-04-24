import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.library)
    id("maven-publish")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "com.github.h4de5ing.netlib"
    
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
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
    implementation(libs.kotlinx.coroutines.android)
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

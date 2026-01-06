import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.kotlin)
    id("maven-publish")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "com.github.h4de5ing.license"
    
    defaultConfig {
        minSdk = 21
        targetSdk = libs.versions.targetSdk.get().toInt()
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_22
        targetCompatibility = JavaVersion.VERSION_22
    }
    
    publishing { 
        singleVariant("release") {} 
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
}

afterEvaluate {
    publishing {
        publications {
            val today = SimpleDateFormat("yyyyMMdd").format(Date())
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.github.h4de5ing.license"
                artifactId = "license"
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

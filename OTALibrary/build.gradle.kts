import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.library)
    id("maven-publish")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "com.android.otalibrary"
    
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
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

dependencies {
    implementation(libs.androidx.startup.runtime)
    implementation(libs.androidx.appcompat)
    implementation(libs.netlib)
    implementation(libs.kotlinx.serialization.json)
}

afterEvaluate {
    publishing {
        publications {
            val today = SimpleDateFormat("yyyyMMdd").format(Date())
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.github.h4de5ing.ota"
                artifactId = "ota"
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

plugins {
    alias(libs.plugins.library)
    id("maven-publish")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "com.serenegiant.uvccamera"
    
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_22
        targetCompatibility = JavaVersion.VERSION_22
    }
    
    sourceSets {
        getByName("main") { 
            jniLibs.srcDirs("/src/main/libs")
        }
    }
    
    buildFeatures { 
        buildConfig = true 
    }
    
    publishing { 
        singleVariant("release") {} 
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(project(":libcommon"))
    implementation(project(":usbCameraCommon"))
}

afterEvaluate {
    publishing {
        publications {
//            val today = SimpleDateFormat("yyyyMMdd").format(Date())
//            create<MavenPublication>("release") {
//                from(components["release"])
//                groupId = "com.serenegiant.uvccamera"
//                artifactId = "uvccamera"
//                version = "2.12.4"
//            }
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

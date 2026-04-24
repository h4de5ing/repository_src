plugins {
    alias(libs.plugins.library)
    id("maven-publish")
}

/*
 * libcommon
 * utility/helper classes for myself
 *
 * Copyright (c) 2014-2024 saki t_saki@serenegiant.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "com.serenegiant.common"
    
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        buildConfigField("int", "VERSION_CODE", "2124")
        buildConfigField("String", "VERSION_NAME", "\"2.12.4\"")

        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true
    }
    
    buildFeatures {
        buildConfig = true
        viewBinding = true
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
    // androidx
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.exifinterface)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.databinding.common)
    
    // ktx
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.lifecycle.common.java8)
    implementation(libs.kotlinx.coroutines.android)
}

afterEvaluate {
    publishing {
        publications {
//			val today = SimpleDateFormat("yyyyMMdd").format(Date())
//			create<MavenPublication>("release") {
//				from(components["release"])
//				groupId = "com.serenegiant"
//				artifactId = "common"
//				version = "2.12.4"
//			}
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

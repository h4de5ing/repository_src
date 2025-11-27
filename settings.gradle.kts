pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://github.com/h4de5ing/repository/raw/master/repository") }
    }
}

rootProject.name = "repository"
include(":Base")
include(":BaseUI")
include(":Document")
include(":filepicker")
include(":License")
include(":NetLib")
include(":OTALibrary")
include(":SerialPortLib")
include(":SerialPortLibrary")
include(":VanSerialPort")
include(":Ymode")
include(":zxing")
//include(":usbCameraCommon")
//include(":usbserial")
//include(":usbSerialForAndroid")

//include(":libcommon")
//include(":libuvccamera")


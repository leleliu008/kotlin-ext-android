plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
}

android {
    compileSdkVersion(28)

    defaultConfig {
        minSdkVersion(18)
        targetSdkVersion(28)
        applicationId = "com.fpliu.newton.util.app"
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDir("src/main/libs")
            java.srcDirs("src/main/kotlin")
        }
        getByName("androidTest") {
            java.srcDirs("src/androidTest/kotlin")
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    lintOptions {
        isAbortOnError = false
    }

    compileOptions {
        //使用JAVA8语法解析
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    //api(project(":library"))

    api("com.fpliu:Android-BaseUI:2.0.12")
    api("com.fpliu:Android-CustomDimen:1.0.0")
    api("com.fpliu:Android-StatusBar-Util:1.0.1")
    api("com.fpliu:Android-Font-Config-api:1.0.0")
    api("com.fpliu:Android-Font-Assets-Alibaba_PuHuiTi_Light:1.0.0")

    testApi("junit:junit:4.12")
    //https://developer.android.google.cn/training/testing/espresso/setup
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
    androidTestImplementation("androidx.test:runner:1.2.0")
    androidTestImplementation ("androidx.test:rules:1.2.0")
}

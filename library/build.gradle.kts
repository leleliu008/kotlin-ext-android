val kotlinVersion: String = rootProject.extra["kotlinVersion"] as String

buildscript {
    repositories {
        jcenter { url = uri("https://maven.aliyun.com/repository/jcenter") }
    }
    dependencies {
        classpath("com.fpliu:BintrayUploadGradlePlugin:1.0.7")
    }
}

apply {
    plugin("com.fpliu.bintray")
}

plugins {
    id("com.android.library")
    id("kotlin-android")

    //用于构建jar和pom
    //https://github.com/dcendents/android-maven-gradle-plugin
    id("com.github.dcendents.android-maven").version("2.0")

    //用于上传到jCenter中
    //https://github.com/bintray/gradle-bintray-plugin
    id("com.jfrog.bintray").version("1.7.3")
}

android {
    compileSdkVersion(28)

    defaultConfig {
        minSdkVersion(19)
        targetSdkVersion(28)
        versionCode = 1
        versionName = "1.0.1"
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDir("src/main/libs")
            java.srcDirs("src/main/kotlin")
        }
    }

    buildTypes {
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
    api(kotlin("stdlib", kotlinVersion))

    //https://dl.google.com/dl/android/maven2/index.html
    //https://developer.android.google.cn/reference/androidx/classes
    api("androidx.appcompat:appcompat:1.1.0")

    //https://github.com/leleliu008/kotlin-ext-jdk
    api("com.fpliu:kotlin-ext-jdk:1.0.1")
}

// 这里是groupId，必须填写,一般填你唯一的包名
group = "com.fpliu"

//这个是版本号，必须填写
version = android.defaultConfig.versionName ?: "1.0.0"

val rootProjectName = rootProject.name

(project.extensions.getByName("bintrayUploadExtension") as com.fpliu.gradle.BintrayUploadExtension).apply {
    developerName = "leleliu008"
    developerEmail = "leleliu008@gamil.com"

    projectSiteUrl = "https://github.com/$developerName/$rootProjectName"
    projectGitUrl = "https://github.com/$developerName/$rootProjectName"

    bintrayOrganizationName = "fpliu"
    bintrayRepositoryName = "newton"
}

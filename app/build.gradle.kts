plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = Project.CompileSdkVersion
    buildToolsVersion = Project.BuildToolVersion

    defaultConfig {
        applicationId = Project.ApplicationId
        minSdk = Project.MinSdkVersion
        targetSdk = Project.TargetSdkVersion
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(Libraries.Kotlin.KotlinStdLib)
    implementation(Libraries.Kotlin.KtxCore)
    implementation(Libraries.AndroidX.AppCompat)
    implementation(Libraries.Google.Material)

    implementation(Libraries.Google.Hilt)
    kapt(Libraries.Google.HiltCompiler)

    implementation(Libraries.Others.RxAndroid)
    implementation(Libraries.Others.RxJava)

    testImplementation(Libraries.Test.JUnit)
    testImplementation(Libraries.Test.Truth)

    androidTestImplementation(Libraries.AndroidTest.JUnitExt)
    androidTestImplementation(Libraries.AndroidTest.EspressoCore)
}


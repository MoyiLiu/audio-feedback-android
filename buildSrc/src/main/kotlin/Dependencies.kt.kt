object Versions {
    const val AndroidGradlePlugin = "7.2.1"

    object Kotlin {
        const val Main = "1.7.0"
        const val Core = "1.8.0"
        const val Coroutine = "1.6.2"
    }

    object AndroidX {
        const val AppCompat = "1.4.2"
    }

    object Google {
        const val Material = "1.6.1"
        const val Hilt = "2.40.5"
    }

    object Others {
        const val RxJava = "3.1.3"
        const val RxAndroid = "3.0.0"
    }

    object Test {
        const val JUnit = "4.13.2"
        const val Truth = "1.1.2"
    }

    object AndroidTest {
        const val EspressoCore = "3.4.0"
        const val JUnitExt = "1.1.3"
    }
}

object BuildPlugins {
    const val AndroidGradlePlugin = "com.android.tools.build:gradle:${Versions.AndroidGradlePlugin}"
    const val KotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.Kotlin.Main}"
    const val HiltGradlePlugin =
        "com.google.dagger:hilt-android-gradle-plugin:${Versions.Google.Hilt}"
}

object Project {
    const val CompileSdkVersion = 31
    const val BuildToolVersion = "31.0.0"
    const val MinSdkVersion = 26
    const val TargetSdkVersion = 31
    const val ApplicationId = "com.moyi.liu.audiofeedback"
}

object Libraries {
    object Kotlin {
        const val StdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.Kotlin.Main}"
        const val KtxCore = "androidx.core:core-ktx:${Versions.Kotlin.Core}"
        const val CoroutineCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.Kotlin.Coroutine}"
        const val CoroutineAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android::${Versions.Kotlin.Coroutine}"
    }

    object AndroidX {
        const val AppCompat = "androidx.appcompat:appcompat:${Versions.AndroidX.AppCompat}"
    }

    object Google {
        const val Material = "com.google.android.material:material:${Versions.Google.Material}"
        const val Hilt = "com.google.dagger:hilt-android:${Versions.Google.Hilt}"
        const val HiltCompiler = "com.google.dagger:hilt-compiler:${Versions.Google.Hilt}"
    }

    object Others {
        const val RxAndroid = "io.reactivex.rxjava3:rxandroid:${Versions.Others.RxAndroid}"
        const val RxJava = "io.reactivex.rxjava3:rxjava:${Versions.Others.RxJava}"
    }

    object Test {
        const val JUnit = "junit:junit:${Versions.Test.JUnit}"
        const val Truth = "com.google.truth:truth:${Versions.Test.Truth}"
    }

    object AndroidTest {
        const val JUnitExt = "androidx.test.ext:junit:${Versions.AndroidTest.JUnitExt}"
        const val EspressoCore =
            "androidx.test.espresso:espresso-core:${Versions.AndroidTest.EspressoCore}"
    }
}
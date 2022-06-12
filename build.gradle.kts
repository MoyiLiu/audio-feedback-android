buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(BuildPlugins.AndroidGradlePlugin)
        classpath(BuildPlugins.KotlinGradlePlugin)
        classpath(BuildPlugins.HiltGradlePlugin)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://oss.jfrog.org/libs-snapshot")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
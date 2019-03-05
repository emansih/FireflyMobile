buildscript {
    repositories {
        google()
        jcenter()
        maven("https://dl.bintray.com/florent37/maven")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.3.2")
        classpath(kotlin("gradle-plugin", Dependencies.kotlinVersion))
        classpath("com.akaita.android:easylauncher:1.3.1")
        classpath("com.github.ben-manes:gradle-versions-plugin:0.20.0")
    }
}

allprojects {
    repositories {
        google()
        maven("https://jitpack.io")
        jcenter()
        maven("https://dl.bintray.com/florent37/maven")
    }
}
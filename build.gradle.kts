buildscript {
    repositories {
        google()
        jcenter()
        maven("https://dl.bintray.com/florent37/maven")
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.0.0")
        classpath(kotlin("gradle-plugin", Dependencies.kotlinVersion))
        classpath("com.akaita.android:easylauncher:1.3.1")
        classpath("com.github.ben-manes:gradle-versions-plugin:0.28.0")
		classpath("org.owasp:dependency-check-gradle:5.1.0")
        classpath("de.mannodermaus.gradle.plugins:android-junit5:1.6.2.0")
    }
}

allprojects {
    repositories {
        google {
            content {
                includeGroupByRegex("com.android.*")
                includeGroupByRegex ("androidx.*")
                includeGroup("com.google.android.material")
            }
        }
        maven("https://jitpack.io")
        jcenter()
        maven("https://dl.bintray.com/florent37/maven") {
            content{
                includeGroup("com.github.florent37")
            }
        }
    }
}
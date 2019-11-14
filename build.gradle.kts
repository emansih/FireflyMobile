buildscript {
    repositories {
        google()
        jcenter()
        maven("https://dl.bintray.com/florent37/maven")
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.5.2")
        classpath(kotlin("gradle-plugin", Dependencies.kotlinVersion))
        classpath("com.akaita.android:easylauncher:1.3.1")
        classpath("com.github.ben-manes:gradle-versions-plugin:0.20.0")
		classpath("org.owasp:dependency-check-gradle:5.1.0")
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
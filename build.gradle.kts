buildscript {
    repositories {
        google()
        jcenter()
        maven("https://dl.bintray.com/florent37/maven")
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.2")
        classpath(kotlin("gradle-plugin", Dependencies.kotlinVersion))
        classpath("com.project.starter:easylauncher:3.2.1")
        classpath("com.github.ben-manes:gradle-versions-plugin:0.36.0")
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
        dependencies{
            apply("$rootDir/buildSrc/src/main/java/Version.gradle")
        }
    }
}

subprojects {
    apply("$rootDir/buildSrc/src/main/java/DependencyReport.gradle")
}
buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.3")
        classpath(kotlin("gradle-plugin", Dependencies.kotlinVersion))
        classpath("com.project.starter:easylauncher:3.2.1")
        classpath("com.github.ben-manes:gradle-versions-plugin:0.39.0")
        classpath("de.mannodermaus.gradle.plugins:android-junit5:1.7.1.1")
    }
}

allprojects {
    repositories {
        google {
            content {
                includeGroupByRegex("com.android.*")
                includeGroupByRegex ("androidx.*")
                includeGroup("com.google.android.material")
                includeGroup("com.google.devtools.ksp")
            }
        }
        mavenCentral()
        maven("https://jitpack.io")
        // TODO: Remove by end of Dec 2021
        // https://jfrog.com/blog/into-the-sunset-bintray-jcenter-gocenter-and-chartcenter/
        /*maven("https://dl.bintray.com/florent37/maven") {
            content{
                includeGroup("com.github.florent37")
            }
        }*/
        dependencies{
            apply("$rootDir/buildSrc/src/main/java/Version.gradle")
        }
    }
}

subprojects {
    apply("$rootDir/buildSrc/src/main/java/DependencyReport.gradle")
}
// hereliesaz/reup/ReUp-c714b8692ef249c9d91ed57a33a63f43f5c8c59d/settings.gradle.kts

/**
 * The epistemological foundation of the project.
 * Dictates where the machine is allowed to search for its required truths.
 */
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Teaches the machine how to dynamically resolve toolchain requests 
    // instead of choking on dead hardcoded links.
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.8.0")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ReUp"
include(":app")

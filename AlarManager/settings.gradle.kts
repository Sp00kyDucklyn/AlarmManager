pluginManagement {
    repositories {
        google()
        mavenCentral()

    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Add JitPack repository
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "AlarManager"
include(":app")
 
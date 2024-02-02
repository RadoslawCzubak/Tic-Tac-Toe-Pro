pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TicTacToe"
include(":app")
include(":home:implementation")
include(":game:implementation")
include(":home:api")
include(":game:api")
include(":build-logic")
include(":bluetooth")
include(":core")
include(":core-ui")

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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

rootProject.name = "OpenGlucoEcosystem"

// Módulos Core compartidos
include(":core:model")
include(":core:network")
include(":core:data")

// Módulos de Aplicaciones
include(":app-mobile")
include(":app-wear")
include(":app-auto")

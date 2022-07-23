pluginManagement {
    val kotlinVersion: String by settings
    plugins {
        kotlin("jvm") version kotlinVersion
    }
    repositories {
        mavenCentral()
    }
}


rootProject.name = "blaze"

include(
    "blaze-core",
    "blaze-annotations",
    "blaze-processor"
)
plugins {
    kotlin("jvm") apply false
}


subprojects {
    val libraryVersion: String by project

    group = "com.jacobtread.blaze"
    version = libraryVersion

    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }

    configure<PublishingExtension> {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/jacobtread/BlazeKt")
                credentials {
                    username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
                    password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
                }
            }
        }
        publications {
            register<MavenPublication>("gpr") {
                from(components["java"])
            }
        }
    }
}
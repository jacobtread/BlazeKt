plugins {
    kotlin("jvm") apply false
}

subprojects {
    val libraryVersion: String by project

    group = "com.jacobtread.blaze"
    version = libraryVersion

    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }

    configure<JavaPluginExtension> {
        withSourcesJar()
        withJavadocJar()
    }

    configure<PublishingExtension> {
        repositories {
            maven {
                name = "Sonatype"
                setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                val sonatypeUser: String? by project
                val sonatypeKey: String? by project
                credentials {
                    username = sonatypeUser
                    password = sonatypeKey
                }
            }
        }
    }

    configure<SigningExtension> {
        useGpgCmd()
    }
}

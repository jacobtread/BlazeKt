plugins {
    kotlin("jvm") apply false
}

val libraryVersion: String by project

group = "com.jacobtread.blaze"
version = libraryVersion

allprojects {
    repositories {
        mavenCentral()
    }
}
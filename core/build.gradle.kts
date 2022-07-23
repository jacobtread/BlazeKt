plugins {
    kotlin("jvm")
    `maven-publish`
}

dependencies {
    val nettyVersion: String by project
    implementation("io.netty:netty-buffer:$nettyVersion")
    implementation("io.netty:netty-handler:$nettyVersion")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}


publishing {
    publications {
        create<MavenPublication>("maven") {
            val libraryVersion: String by project
            groupId = "com.jacobtread"
            artifactId = "blaze-core"
            version = libraryVersion
            from(components["java"])
        }
    }
}
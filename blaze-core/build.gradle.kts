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
        register<MavenPublication>("sonatype") {
            from(components["java"])

            pom {
                name.set("Blaze Networking Core")
                description.set("Core library for blaze networking")
                url.set("https://github.com/jacobtread/BlazeKT/blaze-core")

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://github.com/jacobtread/BlazeKt/blob/master/LICENSE.md")
                    }
                }

                developers {
                    developer {
                        id.set("jacobtread")
                        name.set("Jacobtread")
                        email.set("jacobtread@gmail.com")
                    }
                }

                scm {
                    url.set("https://github.com/jacobtread/BlazeKt/blaze-core")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications)
}
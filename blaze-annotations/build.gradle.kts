publishing {
    publications {
        register<MavenPublication>("sonatype") {
            from(components["java"])

            pom {
                name.set("Blaze Networking Annotations")
                description.set("Library containing annotations used by blaze networking")
                url.set("https://github.com/jacobtread/BlazeKT/blaze-annotations")

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
                    url.set("https://github.com/jacobtread/BlazeKt/blaze-annotations")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications)
}
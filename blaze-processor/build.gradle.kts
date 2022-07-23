dependencies {
    // The blaze project for annotations
    implementation(project(":blaze-annotations"))

    // Symbol processing api for annotation processing
    val kspVersion: String by project
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")

    // Kotlin Poet for generating source code
    val kotlinPoetVersion: String by project
    implementation("com.squareup:kotlinpoet-ksp:$kotlinPoetVersion")
    implementation("com.squareup:kotlinpoet:$kotlinPoetVersion")
}

publishing {
    publications {
        register<MavenPublication>("sonatype") {
            from(components["java"])

            pom {
                name.set("Blaze Networking Processor")
                description.set("Annotation processing library for the core com.jacobtread:blaze-annotations library")
                url.set("https://github.com/jacobtread/BlazeKT/blaze-processor")

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
                    url.set("https://github.com/jacobtread/BlazeKt/blaze-processor")
                }
            }
        }
    }
}


signing {
    sign(publishing.publications)
}
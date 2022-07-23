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

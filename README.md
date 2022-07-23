# BlazeKt
![Latest Version](https://img.shields.io/maven-central/v/com.jacobtread.blaze/blaze-core?label=LATEST%20VERSION&style=for-the-badge)
![License](https://img.shields.io/github/license/jacobtread/BlazeKt?style=for-the-badge)
[![Gradle Build](https://img.shields.io/github/workflow/status/jacobtread/BlazeKt/gradle-build?style=for-the-badge)](https://github.com/jacobtread/BlazeKt/actions/workflows/gradle.yml)
![Total Lines](https://img.shields.io/tokei/lines/github/jacobtread/BlazeKt?style=for-the-badge)

This is an implementation of the Blaze packet system in Kotlin


## Using this dependency

### Without Annotation Routing

**Maven**:

```xml
<dependency>
    <groupId>com.jacobtread.blaze</groupId>
    <artifactId>blaze-core</artifactId>
    <version>{VERSION}</version>
</dependency>
```

**Groovy**:

```kotlin

repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.jacobtread.blaze:blaze-core:{VERSION}'
}
```

**Kotlin DSL**:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.jacobtread.blaze:blaze-core:{VERSION}")
}
```

### With Annotation routing

**Maven**:

_**NOT SUPPORTED**_

**Groovy**:

```groovy
plugins {
    id 'com.google.devtools.ksp' version '1.7.10-1.0.6'
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.jacobtread.blaze:blaze-core:{VERSION}'
    implementation 'com.jacobtread.blaze:blaze-annotations:{VERSION}'
    ksp 'com.jacobtread.blaze:blaze-processor:{VERSION}'
}
```

**Kotlin DSL**:

```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.7.10-1.0.6"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.jacobtread.blaze:blaze-core:{VERSION}")
    implementation("com.jacobtread.blaze:blaze-annotations:{VERSION}")
    ksp("com.jacobtread.blaze:blaze-processor:{VERSION}")
}
```

If you are using Intellij IDEA you may run into the following error
```
Execution optimizations have been disabled for task ':publishPluginJar' to ensure correctness due to the following reasons:
Gradle detected a problem with the following location: '../build/generated/ksp/main/kotlin'.
Reason: Task ':publishPluginJar' uses this output of task ':kspKotlin' without declaring an explicit or implicit dependency.
```

You can fix this by adding the following to your build script

**Kotlin DSL**:
```kotlin
plugins {
   // ...
   idea
}

idea {
   module {
      // Not using += due to https://github.com/gradle/gradle/issues/8749
      sourceDirs = sourceDirs + file("build/generated/ksp/main/kotlin") // or tasks["kspKotlin"].destination
      testSourceDirs = testSourceDirs + file("build/generated/ksp/test/kotlin")
      generatedSourceDirs = generatedSourceDirs + file("build/generated/ksp/main/kotlin") + file("build/generated/ksp/test/kotlin")
   }
}
```

**Groovy**:
```groovy
plugins {
   // ...
   id 'idea'
}

idea {
   module {
      // Not using += due to https://github.com/gradle/gradle/issues/8749
      sourceDirs = sourceDirs + file('build/generated/ksp/main/kotlin') // or tasks["kspKotlin"].destination
      testSourceDirs = testSourceDirs + file('build/generated/ksp/test/kotlin')
      generatedSourceDirs = generatedSourceDirs + file('build/generated/ksp/main/kotlin') + file('build/generated/ksp/test/kotlin')
   }
}
```

> TODO: More information and guides

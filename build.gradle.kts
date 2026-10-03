import org.gradle.api.plugins.quality.CheckstyleExtension

plugins {
    java
}

allprojects {
    group = "org.fife.ui.hex"
    version = "0.2"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "checkstyle")

    java {
        toolchain {
            // Build with a modern JDK regardless of what's on PATH.
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        // Use only Java 8 APIs and generate Java 8-compatible bytecode.
        options.release.set(8)
    }

    configure<CheckstyleExtension> {
        toolVersion = "13.8.0"
        configDirectory.set(file("$rootDir/config/checkstyle"))
    }
}

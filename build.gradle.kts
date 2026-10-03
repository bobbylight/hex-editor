import org.gradle.api.plugins.quality.CheckstyleExtension
import com.github.spotbugs.snom.SpotBugsExtension
import com.github.spotbugs.snom.SpotBugsTask

plugins {
    java
    id("com.github.spotbugs") version "6.5.9" apply false
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
    apply(plugin = "com.github.spotbugs")

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

    configure<SpotBugsExtension> {
        excludeFilter.set(file("$rootDir/config/spotbugs-exclude.xml"))
    }

    tasks.withType<SpotBugsTask>().configureEach {
        reports.create("html") {
            required.set(true)
        }
        reports.create("xml") {
            required.set(false)
        }
    }
}

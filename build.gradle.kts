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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    configure<CheckstyleExtension> {
        toolVersion = "13.8.0"
        configDirectory.set(file("$rootDir/config/checkstyle"))
    }
}

plugins {
    application
}

description = "HexEditorDemo build file"

dependencies {
    implementation(project(":HexEditor"))
}

application {
    mainClass.set("org.fife.ui.hex.swing.demo.HexEditorDemoApp")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "org.fife.ui.hex.swing.demo.HexEditorDemoApp",
            "Class-Path" to configurations.runtimeClasspath.get().files.joinToString(" ") { it.name }
        )
    }
}

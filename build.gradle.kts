plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.15.0"
}

version = "1.4.1"

allprojects {
    repositories {
        mavenCentral()
    }

    apply {
        plugin("java")
        plugin("org.jetbrains.intellij")
    }

    java {
        toolchain {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
    }

    intellij {
        pluginName.set("zato")
        version.set("${project.property("ideVersion")}")
        plugins.set(listOf("PythonCore:${project.property("pythonCoreVersion")}"))

        downloadSources.set(true)
    }

    tasks {
        withType<JavaCompile> { options.encoding = "UTF-8" }

        patchPluginXml {
            sinceBuild.set("221.0")
            untilBuild.set("251.*")
        }

        runPluginVerifier {
            ideVersions.set(project.property("ideVerifierVersions").toString().split(","))
        }
    }

    dependencies {
        testImplementation("org.nanohttpd:nanohttpd:2.3.1")
        testImplementation("junit:junit:4.13.2")
    }
}

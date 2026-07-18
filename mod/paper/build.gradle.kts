plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("com.gradleup.shadow") version "8.3.6"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    implementation(project(":common"))
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    // Shadow bundles common (and any other implementation deps) into the plugin jar.
    // run-paper picks up shadowJar automatically when the shadow plugin is applied.
    shadowJar {
        // paper-api is provided by the server, so exclude it from the fat jar
        configurations = listOf(project.configurations.runtimeClasspath.get())
        mergeServiceFiles()
    }

    assemble {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion("1.21.1")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}

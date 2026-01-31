plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.10.4"
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea(rootProject.findProperty("platformVersion").toString())
    }
}

intellijPlatform {
    pluginConfiguration {
        name = "Concord Perf Tester"
    }
}

tasks {
    // Настраиваем runIde для этого модуля
    runIde {
        // Указываем путь к проекту для тестирования
        val perfProjectDir = project.file("perf-project")
        args = listOf(perfProjectDir.toString())

        // Настройки JFR
        val reportFile = rootProject.layout.buildDirectory.file("reports/perf.jfr").get().asFile
        doFirst {
            reportFile.parentFile.mkdirs()
        }

        jvmArgs = listOf(
            "-XX:StartFlightRecording=filename=${reportFile.absolutePath},duration=120s,settings=profile",
            "-Dconcord.perf.test=true",
            "-Didea.is.internal=true"
        )
    }

    named("prepareSandbox", org.jetbrains.intellij.platform.gradle.tasks.PrepareSandboxTask::class) {
        val buildPluginTask = rootProject.tasks.named<org.jetbrains.intellij.platform.gradle.tasks.BuildPluginTask>("buildPlugin")
        dependsOn(buildPluginTask)

        doLast {
            // Install main plugin
            val sandboxPluginsDir = sandboxDirectory.get().asFile.resolve("plugins")
            val pluginZip = buildPluginTask.get().archiveFile.get().asFile
            project.copy {
                from(zipTree(pluginZip))
                into(sandboxPluginsDir)
            }

            // Disable Kubernetes plugin
            val configDir = sandboxDirectory.get().asFile.resolve("config")
            configDir.mkdirs()
            val disabledPluginsFile = configDir.resolve("disabled_plugins.txt")
            if (!disabledPluginsFile.exists()) {
                disabledPluginsFile.createNewFile()
            }
            if (!disabledPluginsFile.readText().contains("com.intellij.kubernetes")) {
                disabledPluginsFile.appendText("com.intellij.kubernetes\n")
            }
        }
    }

    verifyPlugin {
        enabled = false
    }

    buildSearchableOptions {
        enabled = false
    }
}

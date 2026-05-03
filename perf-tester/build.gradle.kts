import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.FileSystemOperations
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

plugins {
    id("java")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intellijPlatform)
}

abstract class GradleServices @Inject constructor(
    val fs: FileSystemOperations,
    val archives: ArchiveOperations,
)

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
        // Disable Compose Hot Reload Agent — it intercepts every class load and adds
        // significant CPU noise (~45%) to JFR profiles with zero relevance to our plugin.
        composeHotReload.set(false)

        // Указываем путь к проекту для тестирования
        val perfProjectDir = project.file("perf-project")
        args = listOf(perfProjectDir.toString())

        // Настройки JFR
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
        val reportFile = rootProject.layout.buildDirectory.file("reports/perf-${timestamp}.jfr").get().asFile
        doFirst {
            reportFile.parentFile.mkdirs()
            // Fallback: filter out the hot-reload-agent if composeHotReload flag didn't prevent it
            jvmArgs = jvmArgs.orEmpty().filter { !it.contains("hot-reload-agent") }
        }

        jvmArgs = listOf(
            "-XX:StartFlightRecording=filename=${reportFile.absolutePath},duration=120s,settings=profile",
            "-Dconcord.perf.test=true",
            "-Didea.is.internal=true"
        )
    }

    named("prepareSandbox", org.jetbrains.intellij.platform.gradle.tasks.PrepareSandboxTask::class) {
        val buildPluginTask = rootProject.tasks.named<org.jetbrains.intellij.platform.gradle.tasks.BuildPluginTask>("buildPlugin")
        val services = objects.newInstance<GradleServices>()
        dependsOn(buildPluginTask)

        doLast {
            // Install main plugin
            val sandboxPluginsDir = sandboxDirectory.get().asFile.resolve("plugins")
            val pluginZip = buildPluginTask.get().archiveFile.get().asFile
            services.fs.copy {
                from(services.archives.zipTree(pluginZip))
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

    instrumentCode {
        enabled = false
    }

    publishPlugin {
        enabled = false
    }
}

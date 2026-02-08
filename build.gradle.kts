import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.changelog.Changelog

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.3.10"
    id("org.jetbrains.intellij.platform") version "2.11.0"
    id("org.jetbrains.changelog") version "2.5.0"
    id("org.jetbrains.qodana") version "2025.3.1"
    id("org.jetbrains.grammarkit") version "2023.3.0.1"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

// Configure project's dependencies
repositories {
    mavenCentral()
    maven("https://packages.jetbrains.team/maven/p/grazi/grazie-platform-public/")

    intellijPlatform {
        defaultRepositories()
    }
}

sourceSets {
    main {
        java {
            srcDirs("src/main/java")
            srcDir("src/main/gen")
        }
    }
}

dependencies {
    intellijPlatform {
        intellijIdea(properties("platformVersion"))

        bundledPlugins(providers.gradleProperty("platformPlugins").map { it.split(',') })

        pluginVerifier()
        zipSigner()

        testFramework(TestFrameworkType.Platform)
    }

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("junit:junit:4.13.2")

    implementation("com.cronutils:cron-utils:9.2.0")
    implementation("org.ow2.asm:asm:9.7")
    implementation("com.google.code.gson:gson:2.11.0")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// Configure Gradle IntelliJ Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
intellijPlatform {
    pluginConfiguration {
        name = properties("pluginName")
    }

    pluginVerification {
        ides {
            recommended()
        }
        freeArgs = listOf(
            "-mute",
            "TemplateWordInPluginId"
        )
    }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version = properties("pluginVersion")
    groups = emptyList()
    keepUnreleasedSection.set(false)
}

grammarKit {
    jflexRelease.set("1.9.2")
}

tasks {
    generateLexer {
        sourceFile.set(file("src/main/java/brig/concord/lexer/ConcordYaml.flex"))
        targetOutputDir.set(file("src/main/gen/brig/concord/lexer"))
        purgeOldFiles.set(true)
    }

    compileJava {
        dependsOn(generateLexer)
    }

    compileKotlin {
        dependsOn(generateLexer)
    }

    test {
        useJUnitPlatform()
    }

    buildSearchableOptions {
        enabled = false
    }

    wrapper {
        gradleVersion = properties("gradleVersion")
    }

    patchPluginXml {
        version = properties("pluginVersion")
        sinceBuild = properties("pluginSinceBuild")

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription.set(
            projectDir.resolve("README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run { markdownToHTML(this) }
        )

        // Get the latest available change notes from the changelog file
        changeNotes.set(provider {
            changelog.run {
                val item = getOrNull(properties("pluginVersion")) ?: getLatest()
                renderItem(item, Changelog.OutputType.HTML)
            }
        })
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels.set(listOf("default"))
    }

    register<DefaultTask>("printVersion") {
        group = "help"
        description = "Prints project version"
        doLast { println(version) }
    }

    register<DefaultTask>("printPluginName") {
        group = "help"
        description = "Prints IntelliJ plugin name"
        doLast {
            println(properties("pluginName"))
        }
    }

    register<DefaultTask>("printUnreleasedChangelog") {
        group = "help"
        description = "Prints unreleased changelog (plain text, no header)"
        doLast {
            val unreleased = changelog.getUnreleased()
            println(changelog.renderItem(unreleased, Changelog.OutputType.MARKDOWN))
        }
    }
}

val runIdeNoK8s by intellijPlatformTesting.runIde.registering {
    plugins {
        disablePlugin("com.intellij.kubernetes")
    }
}



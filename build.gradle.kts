import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.changelog.Changelog

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intellijPlatform)
    alias(libs.plugins.changelog)
    alias(libs.plugins.qodana)
    alias(libs.plugins.grammarkit)
    alias(libs.plugins.spotless)
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
        testFramework(TestFrameworkType.JUnit5)
    }

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.legacy)
    testImplementation(libs.assertj)

    implementation(project(":el-language"))
    implementation(libs.cron.utils)
    implementation(libs.asm)
    implementation(libs.gson)
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
    jflexRelease.set(libs.versions.jflex.get())
}

spotless {
    ratchetFrom("origin/main")
    val excludeDirs = listOf(".qodana/**", ".idea-sources/**", ".idea/**", ".gradle/**", "build/**", "**/build/**")
    // Files forked from JetBrains YAML plugin — keep their original copyright
    val jetbrainsFiles = listOf("**/brig/concord/yaml/**", "**/brig/concord/formatter/YAML*", "**/brig/concord/formatter/YamlInjectedBlockFactory*")
    java {
        target("**/src/main/java/**/*.java", "**/src/test/java/**/*.java")
        targetExclude(excludeDirs + jetbrainsFiles + "**/src/main/gen/**")
        licenseHeaderFile(file("gradle/license-header.txt"))
    }
    kotlin {
        target(
            "**/src/main/java/**/*.kt", "**/src/main/kotlin/**/*.kt",
            "**/src/test/java/**/*.kt", "**/src/test/kotlin/**/*.kt"
        )
        targetExclude(excludeDirs + jetbrainsFiles)
        licenseHeaderFile(file("gradle/license-header.txt"))
    }
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

// Configuration for extracting IntelliJ IDEA sources for local reference
val ideaSources by configurations.creating {
    isTransitive = false
}

dependencies {
    ideaSources("com.jetbrains.intellij.idea:idea:${properties("platformVersion")}:sources")
}

tasks.register<Sync>("extractIdeaSources") {
    group = "ide"
    description = "Extracts IntelliJ IDEA sources to .idea-sources/ for local reference"
    from(ideaSources.map { zipTree(it) })
    into(layout.projectDirectory.dir(".idea-sources"))
}



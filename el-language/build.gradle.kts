plugins {
    id("java")
    alias(libs.plugins.intellijPlatform)
    alias(libs.plugins.grammarkit)
}

repositories {
    mavenCentral()
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
        intellijIdea(rootProject.findProperty("platformVersion").toString())

        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.JUnit5)
    }

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.legacy)
    testImplementation(libs.assertj)
}

intellijPlatform {
    buildSearchableOptions = false
}

grammarKit {
    jflexRelease.set(libs.versions.jflex.get())
}

tasks {
    generateLexer {
        sourceFile.set(file("src/main/java/brig/concord/el/El.flex"))
        targetOutputDir.set(file("src/main/gen/brig/concord/el"))
        purgeOldFiles.set(true)
    }

    generateParser {
        sourceFile.set(file("src/main/java/brig/concord/el/El.bnf"))
        targetRootOutputDir.set(file("src/main/gen"))
        pathToParser.set("brig/concord/el/parser/ElParser.java")
        pathToPsiRoot.set("brig/concord/el/psi")
        purgeOldFiles.set(true)
    }

    compileJava {
        dependsOn(generateLexer, generateParser)
    }

    test {
        useJUnitPlatform()
    }

    verifyPlugin { enabled = false }
    buildSearchableOptions { enabled = false }
    instrumentCode { enabled = false }
    publishPlugin { enabled = false }
}

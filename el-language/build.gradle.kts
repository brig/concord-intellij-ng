plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.11.0"
    id("org.jetbrains.grammarkit") version "2023.3.0.1"
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

    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("junit:junit:4.13.2")
    testImplementation("org.assertj:assertj-core:3.27.7")
}

intellijPlatform {
    buildSearchableOptions = false
}

grammarKit {
    jflexRelease.set("1.9.2")
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

rootProject.name = "concord-intellij-plugin"

include("perf-tester")
include("el-language")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

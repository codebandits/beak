apply {
    plugin("org.jetbrains.kotlin.jvm")
    plugin("org.junit.platform.gradle.plugin")
    from("../gradle/scripts/versions.gradle.kts")
}

dependencies {
    val kotlinVersion: String by extra
    val h2Version: String by extra
    val arrowVersion: String by extra
    val exposedVersion: String by extra
    val junitVersion: String by extra
    val slf4jSimpleVersion: String by extra

    "compile"(kotlin(module = "stdlib", version = kotlinVersion))
    "compile"("io.arrow-kt:arrow-core:$arrowVersion")
    "compile"("io.arrow-kt:arrow-data:$arrowVersion")
    "compile"("org.jetbrains.exposed:exposed:$exposedVersion")

    "testCompile"("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    "testRuntime"("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    "testCompile"(kotlin(module = "test-junit", version = kotlinVersion))
    "testCompile"("com.h2database:h2:$h2Version")
    "testRuntime"("org.slf4j:slf4j-simple:$slf4jSimpleVersion")
}

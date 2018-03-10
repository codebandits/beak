apply {
    plugin("org.jetbrains.kotlin.jvm")
    plugin("org.junit.platform.gradle.plugin")
}

dependencies {
    "compile"("io.arrow-kt:arrow-core:0.6.1")
    "compile"("io.arrow-kt:arrow-data:0.6.1")
    "compile"("org.jetbrains.exposed:exposed:0.10.1")

    "testCompile"("org.junit.jupiter:junit-jupiter-api:5.1.0")
    "testRuntime"("org.junit.jupiter:junit-jupiter-engine:5.1.0")
    "testCompile"("org.jetbrains.kotlin:kotlin-test-junit:1.2.30")
    "testCompile"("com.h2database:h2:1.4.196")
}

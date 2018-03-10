apply {
    plugin("org.jetbrains.kotlin.jvm")
    plugin("org.junit.platform.gradle.plugin")
}

dependencies {
    "testCompile"("org.junit.jupiter:junit-jupiter-api:5.1.0")
    "testRuntime"("org.junit.jupiter:junit-jupiter-engine:5.1.0")
    "testCompile"("org.jetbrains.kotlin:kotlin-test-junit:1.2.30")
}

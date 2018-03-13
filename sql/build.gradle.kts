dependencies {
    val kotlinVersion: String by extra
    val h2Version: String by extra
    val arrowVersion: String by extra
    val exposedVersion: String by extra
    val junitVersion: String by extra

    compile(kotlin(module = "stdlib", version = kotlinVersion))
    compile("io.arrow-kt:arrow-core:$arrowVersion")
    compile("io.arrow-kt:arrow-data:$arrowVersion")
    compile("org.jetbrains.exposed:exposed:$exposedVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testCompile(kotlin(module = "test-junit", version = kotlinVersion))
    testCompile("com.h2database:h2:$h2Version")
}

publishing {
    publications.create<MavenPublication>(rootProject.name) {
        artifactId = rootProject.name
        from(components["java"])
    }
}

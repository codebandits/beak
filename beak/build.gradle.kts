dependencies {
    val kotlinVersion: String by extra
    val h2Version: String by extra
    val mysqlVersion: String by extra
    val netCrusherVersion: String by extra
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
    testCompile("mysql:mysql-connector-java:$mysqlVersion")
    testCompile("com.github.netcrusherorg:netcrusher-core:$netCrusherVersion")
}

publishing {
    publications.create<MavenPublication>(name) {
        from(components["java"])
    }

    repositories {
        maven {
            name = "release"

            setUrl("https://api.bintray.com/maven/codebandits/beak/beak/;publish=1")

            credentials {
                username = if (hasProperty("bintrayUser")) property("bintrayUser") as String else ""
                password = if (hasProperty("bintrayKey")) property("bintrayKey") as String else ""
            }
        }
    }
}

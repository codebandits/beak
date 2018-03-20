dependencies {
    val kotlinVersion: String by extra
    val h2Version: String by extra
    val mysqlVersion: String by extra
    val postgresqlVersion: String by extra
    val netCrusherVersion: String by extra
    val arrowVersion: String by extra
    val exposedVersion: String by extra
    val junitVersion: String by extra
    val httpclientVersion: String by extra

    compile(kotlin(module = "stdlib", version = kotlinVersion))
    compile("io.arrow-kt:arrow-core:$arrowVersion")
    compile("io.arrow-kt:arrow-data:$arrowVersion")
    compile("org.jetbrains.exposed:exposed:$exposedVersion")
    compileOnly("mysql:mysql-connector-java:$mysqlVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testCompile(kotlin(module = "test-junit", version = kotlinVersion))
    testCompile("com.h2database:h2:$h2Version")
    testCompile("mysql:mysql-connector-java:$mysqlVersion")
    testCompile("org.postgresql:postgresql:$postgresqlVersion")
    testCompile("com.github.netcrusherorg:netcrusher-core:$netCrusherVersion")
    testCompile("org.apache.httpcomponents:httpclient:$httpclientVersion")
}

publishing {
    val sourceJar = task("sourceJar", type = Jar::class) {
        classifier = "sources"
        from(the<JavaPluginConvention>().sourceSets.getByName("main").allSource)
    }

    publications.create<MavenPublication>(name) {
        from(components["java"])
        artifact(sourceJar)
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

tasks {
    withType<Test> {
        project.properties["test.db.mysql"]?.also { systemProperty("test.db.mysql", it) }
        project.properties["test.db.postgresql"]?.also { systemProperty("test.db.postgresql", it) }
    }

    "testMySQL"(Test::class) {
        useJUnitPlatform {
            filter {
                includeTags("mysql")
            }
        }
    }

    "testH2"(Test::class) {
        useJUnitPlatform {
            filter {
                includeTags("h2")
            }
        }
    }

    "testPostgreSQL"(Test::class) {
        useJUnitPlatform {
            filter {
                includeTags("postgresql")
            }
        }
    }
}

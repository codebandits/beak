import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    project.apply { from("gradle/scripts/versions.gradle.kts") }

    val kotlinVersion: String by extra
    val dokkaVersion: String by extra

    repositories {
        jcenter()
    }

    dependencies {
        classpath(kotlin(module = "gradle-plugin", version = kotlinVersion))
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:$dokkaVersion")
    }
}

allprojects {
    apply {
        from("${rootProject.projectDir}/gradle/scripts/versions.gradle.kts")
    }

    val jvmTarget: String by extra

    group = "io.github.codebandits"

    repositories {
        jcenter()
        maven { setUrl("https://dl.bintray.com/kotlin/exposed") }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            this.jvmTarget = jvmTarget
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()

        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    tasks.withType<AbstractPublishToMaven> {
        doFirst {
            when (version) {
                !is String, "unspecified" -> throw Throwable("${project.name} version is not specified (was \"$version\")")
            }
        }
    }
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.dokka")
        plugin("maven-publish")
    }
}

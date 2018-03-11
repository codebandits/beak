import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    project.apply { from("gradle/scripts/versions.gradle.kts") }

    val kotlinVersion: String by extra
    val junitPluginVersion: String by extra

    repositories {
        jcenter()
    }

    dependencies {
        classpath("org.junit.platform:junit-platform-gradle-plugin:$junitPluginVersion")
        classpath(kotlin(module = "gradle-plugin", version = kotlinVersion))
    }
}

allprojects {
    apply { from("${rootProject.projectDir}/gradle/scripts/versions.gradle.kts") }
    val jvmTarget: String by extra

    repositories {
        jcenter()
        maven { setUrl("https://dl.bintray.com/kotlin/exposed") }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            this.jvmTarget = jvmTarget
        }
    }
}

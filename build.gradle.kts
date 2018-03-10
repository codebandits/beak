buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.1.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.2.30")
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
}

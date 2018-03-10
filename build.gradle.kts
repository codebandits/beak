buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.1.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.2.30")
    }
}

allprojects {
    repositories {
        jcenter()
        maven { setUrl("https://dl.bintray.com/kotlin/exposed") }
    }
}

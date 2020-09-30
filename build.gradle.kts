buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
        google()
    }
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        google()
    }

    group = "dev.valvassori"
    version = "1.0.0-dev"
}
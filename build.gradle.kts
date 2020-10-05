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

    group = "dev.valvassori.fluks"
    version = "1.0.0"
}

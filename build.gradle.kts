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
    version = System.getenv("RELEASE_VERSION")
}

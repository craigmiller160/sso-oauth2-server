group = "io.craigmiller160"
version = "1.8.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.6.21"
    application
    `maven-publish`
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://craigmiller160.ddns.net:30003/repository/maven-public")
    }
}
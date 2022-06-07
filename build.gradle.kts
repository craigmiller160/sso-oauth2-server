import org.springframework.boot.gradle.tasks.run.BootRun

group = "io.craigmiller160"
version = "1.8.0-SNAPSHOT"

plugins {
    val kotlinVersion = "1.6.21"

    kotlin("jvm") version kotlinVersion
    id("org.springframework.boot") version "2.7.0"
    `maven-publish`
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
}
apply(plugin = "io.spring.dependency-management")

repositories {
    mavenCentral()
    maven {
        url = uri("https://craigmiller160.ddns.net:30003/repository/maven-public")
    }
}

dependencies {
    val assertjVersion = "3.22.0"
    val kotestArrowVersion = "1.2.5"
    val arrowVersion = "1.0.1"
    val legacyDateVersion = "1.1.2"
    val hamcrestDateVersion = "2.0.7"
    val apiTestProcessorVersion = "1.2.2"
    val webUtilsVersion = "1.1.3"
    val postgresVersion = "42.3.1"
    val nimbusJoseVersion = "9.22"
    val mockitoVersion = "4.0.0"

    testImplementation("org.assertj:assertj-core:$assertjVersion")
    implementation("io.kotest.extensions:kotest-assertions-arrow-jvm:$kotestArrowVersion")
    implementation("io.arrow-kt:arrow-core-jvm:$arrowVersion")
    implementation("io.craigmiller160:legacy-date-converter:$legacyDateVersion")
    testImplementation("org.exparity:hamcrest-date:$hamcrestDateVersion")
    testImplementation("io.craigmiller160:api-test-processor:$apiTestProcessorVersion")
    implementation("io.craigmiller160:spring-web-utils:$webUtilsVersion")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("com.nimbusds:nimbus-jose-jwt:$nimbusJoseVersion")
    implementation("org.mockito.kotlin:mockito-kotlin:$mockitoVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("org.junit.vintage", "junit-vintage-engine")
    }
    testImplementation("com.h2database:h2")
    implementation("org.springframework.boot:spring-boot-starter-security")
}

tasks.named<BootRun>("bootRun") {
    systemProperties = project.properties["jvmArguments"]
        ?.toString()
        ?.split(" ")
        ?.asSequence()
        ?.map {
            val parts = it.split("=")
            Pair(parts[0].substring(2), parts[1])
        }
        ?.toMap()
        ?: mapOf()
}
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.dokka")
    application
}

group = "org.itmo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val kotlinVersion: String by project
    val koinVersion: String by project
    val mockkVersion: String by project
    val junitVersion: String by project
    val serializationVersion: String by project
    val kotlinLoggingVersion: String by project
    val slf4Version: String by project
    val coroutinesVersion: String by project

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:${mockkVersion}")
    testImplementation("io.insert-koin:koin-test-junit5:$koinVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    implementation(kotlin("serialization", version = kotlinVersion))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
    implementation("io.insert-koin:koin-core:$koinVersion")
    implementation("org.slf4j:slf4j-log4j12:$slf4Version")
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    implementation(project(":common"))
}


tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "GatewayKt"
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}


application {
    mainClass.set("GatewayKt")
}
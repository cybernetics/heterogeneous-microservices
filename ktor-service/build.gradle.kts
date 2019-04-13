import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion: String by project
val ktorVersion: String by project
val koinVersion: String by project
val consulClientVersion: String by project
val logbackVersion: String by project
val junitVersion: String by project
val mockitoVersion: String by project

plugins {
    application
    id("com.github.johnrengelman.shadow")
    kotlin("jvm")
    jacoco
}

application {
    mainClassName = "io.heterogeneousmicroservices.ktorservice.KtorServiceApplicationKt"
//    mainClassName = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenCentral()
    maven("https://dl.bintray.com/kotlin/kotlinx")
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("org.koin:koin-ktor:$koinVersion")
    implementation("com.orbitz.consul:consul-client:$consulClientVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.koin:koin-test:$koinVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    // fixme temporary override mockito version that comes from koin-test
    testRuntime("org.mockito:mockito-core:$mockitoVersion")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "12"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }
}
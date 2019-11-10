rootProject.name = "heterogeneous-microservices"

include(
    "helidon-service",
    "ktor-service",
    "micronaut-service",
    "spring-boot-service"
)

pluginManagement {
    val kotlinVersion: String by settings
    val springBootPluginVersion: String by settings
    val springDependencyManagementPluginVersion: String by settings
    val shadowPluginVersion: String by settings

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "org.jetbrains.kotlin.jvm" -> useVersion(kotlinVersion)
                "org.jetbrains.kotlin.plugin.spring" -> useVersion(kotlinVersion)
                "org.jetbrains.kotlin.kapt" -> useVersion(kotlinVersion)
                "org.springframework.boot" -> useVersion(springBootPluginVersion)
                "io.spring.dependency-management" -> useVersion(springDependencyManagementPluginVersion)
                "com.github.johnrengelman.shadow" -> useVersion(shadowPluginVersion)
            }
        }
    }
}
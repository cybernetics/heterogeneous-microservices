package io.heterogeneousmicroservices.helidonservice

import com.orbitz.consul.Consul
import com.orbitz.consul.model.agent.ImmutableRegistration
import io.helidon.common.http.Http
import io.helidon.config.Config
import io.helidon.webserver.NotFoundException
import io.helidon.webserver.Routing
import io.helidon.webserver.ServerConfiguration
import io.helidon.webserver.WebServer
import io.helidon.webserver.json.JsonSupport
import io.heterogeneousmicroservices.helidonservice.config.ApplicationInfoProperties
import io.heterogeneousmicroservices.helidonservice.service.ApplicationInfoJsonService
import io.heterogeneousmicroservices.helidonservice.service.ApplicationInfoService
import io.heterogeneousmicroservices.helidonservice.service.KtorServiceClient
import org.koin.dsl.module.module
import org.koin.standalone.KoinComponent
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.inject
import org.slf4j.LoggerFactory

object HelidonServiceApplication : KoinComponent {

    private val log = LoggerFactory.getLogger(this::class.java)

    private val applicationContext = module {
        single { ApplicationInfoService(get(), get(), get()) }
        single { ApplicationInfoProperties() }
        single { ApplicationInfoJsonService() }
        single { KtorServiceClient() }
        single { Consul.builder().withUrl("http://localhost:8500").build() }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        startKoin(listOf(applicationContext))
        startServer()
    }

    fun startServer(): WebServer {
        // read config from application.yaml
        val config = Config.create()
        val serverConfig = ServerConfiguration.fromConfig(config.get("server"))

        val server: WebServer = WebServer
            .builder(createRouting())
            .configuration(serverConfig)
            .build()

        server.start().thenAccept { ws ->
            log.info("Service running at: http://localhost:" + ws.port())
            val applicationInfoProperties: ApplicationInfoProperties by inject()
            val serviceName = applicationInfoProperties.name
            registerInConsul(serviceName, ws.port())
        }

        return server
    }

    private fun createRouting(): Routing {
        val applicationInfoService: ApplicationInfoService by inject()
        return Routing.builder()
            // add JSON support to all end-points
            .register(JsonSupport.get())
            .register("/application-info", applicationInfoService)
            .error(NotFoundException::class.java) { req, res, ex ->
                log.error("NotFoundException:", ex)
                res.status(Http.Status.BAD_REQUEST_400).send()
            }
            .error(Exception::class.java) { req, res, ex ->
                log.error("Exception:", ex)
                res.status(Http.Status.INTERNAL_SERVER_ERROR_500).send()
            }
            .build()
    }

    private fun registerInConsul(serviceName: String, port: Int) {
        val consulClient: Consul by inject()
        consulClient.agentClient().register(buildConsulRegistration(serviceName, port))
    }

    private fun buildConsulRegistration(serviceName: String, port: Int) = ImmutableRegistration.builder()
        .id("$serviceName-$port")
        .name(serviceName)
        .address("localhost")
        .port(port)
        .build()
}

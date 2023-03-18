package dog.catfood

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import dog.catfood.controllers.AccountController
import dog.catfood.controllers.CertificatesController
import dog.catfood.controllers.DashboardController
import dog.catfood.controllers.DeviceApiController
import dog.catfood.controllers.DevicesController
import dog.catfood.controllers.HomeController
import dog.catfood.controllers.LocationsApiController
import dog.catfood.dao.CertificateDao
import dog.catfood.dao.DeviceDao
import dog.catfood.dao.LocationDao
import dog.catfood.dao.UserDao
import dog.catfood.logic.BcryptPasswordHasher
import dog.catfood.logic.CertificateService
import dog.catfood.logic.DeviceService
import dog.catfood.logic.LocationService
import dog.catfood.logic.PasswordHasher
import dog.catfood.logic.UserService
import dog.catfood.logic.buildCaKeyStore
import dog.catfood.logic.getHash
import dog.catfood.models.AppSession
import dog.catfood.models.CertificateStatus
import dog.catfood.models.DevicePrincipal
import dog.catfood.plugins.auth.clientcert.clientCert
import dog.catfood.plugins.modelbinding.csrf.CsrfContextProcessor
import dog.catfood.plugins.modelbinding.freemarker.FreeMarkerTemplateProvider
import dog.catfood.plugins.modelbinding.hibernate.HibernateModelValidator
import dog.catfood.plugins.controllers.Controllers
import dog.catfood.plugins.controllers.authentication.AuthenticationAnnotationProcessor
import dog.catfood.plugins.controllers.controller
import dog.catfood.plugins.controllers.routesHelper
import dog.catfood.plugins.csrf.Csrf
import dog.catfood.plugins.flash.Flash
import dog.catfood.plugins.flash.use
import dog.catfood.plugins.modelbinding.ModelBinding
import dog.catfood.plugins.modelbinding.RequestContextProcessor
import dog.catfood.plugins.modelbinding.routing.RouteContextProcessor
import dog.catfood.plugins.modelbinding.views
import dog.catfood.plugins.modelbinding.controllers.controllerActionPathResolver
import dog.catfood.plugins.sessions.RedisSessionStorage
import dog.catfood.utils.Converter
import dog.catfood.utils.get
import dog.catfood.utils.getOrNull
import dog.catfood.utils.readCertificate
import dog.catfood.utils.readPrivateKey
import freemarker.cache.ClassTemplateLoader
import freemarker.template.Configuration
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.session
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.freemarker.FreeMarker
import io.ktor.server.http.content.resources
import io.ktor.server.http.content.static
import io.ktor.server.http.content.staticBasePackage
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.doublereceive.DoubleReceive
import io.ktor.server.request.uri
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.sessions.DEFAULT_SESSION_MAX_AGE
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.ConnectionFactoryOptions.DATABASE
import io.r2dbc.spi.ConnectionFactoryOptions.DRIVER
import io.r2dbc.spi.ConnectionFactoryOptions.HOST
import io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD
import io.r2dbc.spi.ConnectionFactoryOptions.PORT
import io.r2dbc.spi.ConnectionFactoryOptions.PROTOCOL
import io.r2dbc.spi.ConnectionFactoryOptions.USER
import no.api.freemarker.java8.Java8ObjectWrapper
import no.api.freemarker.java8.time.DefaultFormatters
import org.flywaydb.core.Flyway
import org.jooq.impl.DSL
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.reflect.typeOf

fun main(args: Array<String>) {
    if (args.isNotEmpty() && args[0] == "migrate") {
        val config = commandLineEnvironment(args.copyOfRange(1, args.size))
            .config
        val host = config.get("db.host")
        val port = config.get("db.port")
        val name = config.get("db.name")
        val user = config.get("db.user")
        val password = config.get("db.password")
        Flyway.configure()
            .dataSource(
                "jdbc:postgresql://$host:$port/$name?sslmode=disable",
                user,
                password
            )
            .load()
            .migrate()
    } else {
        io.ktor.server.netty.EngineMain.main(args)
    }
}

fun Application.main() {
    install(CallLogging)
    install(Koin) {
        modules(appModule(environment.config))
    }
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
        objectWrapper = Java8ObjectWrapper(Configuration.VERSION_2_3_31)
        DefaultFormatters.setOffsetDateTimeFormatter(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))
    }
    val certificateDao = get<CertificateDao>()
    install(Authentication) {
        session<AppSession>("auth-session") {
            validate { it.userPrincipal }
            challenge {
                val requestUri = call.request.uri
                val redirectUrl = call.routesHelper.href("Account", "login") {
                    append("next", requestUri)
                }
                call.respondRedirect(redirectUrl)
            }
        }
        clientCert("auth-cert") {
            validate {x509Certificate ->
                certificateDao.getCertificateByHash(x509Certificate.getHash(), CertificateStatus.ACTIVE)
                    ?.let { DevicePrincipal(it.deviceId) }
            }
        }
    }
    install(Controllers) {
        annotationProcessors = listOf(AuthenticationAnnotationProcessor())
    }
    routing {
        // static assets
        static("/assets") {
            staticBasePackage = "assets"
            static("css") { resources("css") }
        }
        // app
        route("/") {
            val redissonClient = get<RedissonClient>()
            install(Sessions) {
                cookie<AppSession>(
                    "app_session",
                    RedisSessionStorage(redissonClient, Duration.ofSeconds(DEFAULT_SESSION_MAX_AGE))
                )
            }
            // app api routes
            // load before app plugins to keep routes lightweight
            route("/_api") {
                install(ContentNegotiation) {
                    jackson {
                        registerModule(JavaTimeModule())
                        setConfig(serializationConfig.withoutFeatures(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS))
                    }
                }
                controller(
                    get<LocationsApiController>()
                )
            }
            install(Flash) { use("app_session") }
            install(DoubleReceive)
            install(Csrf)
            install(ModelBinding) {
                validator = HibernateModelValidator()
                converters = mapOf(
                    typeOf<CertificateStatus>() to Converter { CertificateStatus.valueOf(it) }
                )
                views {
                    templateProvider = FreeMarkerTemplateProvider(::controllerActionPathResolver)
                    contextProcessors = listOf(
                        RequestContextProcessor(),
                        CsrfContextProcessor(),
                        RouteContextProcessor()
                    )
                }
            }
            controller(
                HomeController(),
                get<DashboardController>(),
                get<DevicesController>(),
                get<AccountController>(),
                get<CertificatesController>()
            )
        }

        // api
        route("/api/v1") {
            install(ContentNegotiation) {
                jackson {
                    registerModule(JavaTimeModule())
                    setConfig(serializationConfig.withoutFeatures(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS))
                }
            }
            controller(
                get<DeviceApiController>()
            )
        }
        get("/health") {
            call.respondText("ok")
        }
    }
}

fun appModule(config: ApplicationConfig) = module {
    val connectionFactory: ConnectionFactory = ConnectionFactories.get(
        ConnectionFactoryOptions.builder()
            .option(DRIVER,"pool")
            .option(PROTOCOL,"postgresql")
            .option(HOST, config["db.host"])
            .option(PORT, config["db.port"])
            .option(USER, config["db.user"])
            .option(PASSWORD, config["db.password"])
            .option(DATABASE, config["db.name"])
            .build()
    )
    val context = DSL.using(connectionFactory)

    val redissonClient = Redisson.create(
        Config().apply {
            useSingleServer().apply {
                address = config["redis.url"]
                password = config.getOrNull("redis.password", emptyAsNull = true)
            }
        }
    )

    val keystore = buildCaKeyStore(
        certificate = readCertificate(config["ca.certificate"]),
        key = readPrivateKey(config["ca.key"]),
        password = config["ca.password"]
    )

    single { context }
    single { redissonClient }
    single<PasswordHasher> { BcryptPasswordHasher() }
    single { keystore }
    singleOf(::UserDao)
    singleOf(::DeviceDao)
    singleOf(::CertificateDao)
    singleOf(::LocationDao)
    singleOf(::UserService)
    singleOf(::DeviceService)
    singleOf(::CertificateService)
    singleOf(::LocationService)
    singleOf(::DashboardController)
    singleOf(::DevicesController)
    singleOf(::AccountController)
    singleOf(::CertificatesController)
    singleOf(::DeviceApiController)
    singleOf(::LocationsApiController)
}

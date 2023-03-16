package dog.catfood.plugins.auth.clientcert

import io.ktor.http.decodeURLPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.AuthenticationContext
import io.ktor.server.auth.AuthenticationFailedCause
import io.ktor.server.auth.AuthenticationFunction
import io.ktor.server.auth.AuthenticationProvider
import io.ktor.server.auth.BearerAuthenticationProvider
import io.ktor.server.auth.Principal
import io.ktor.server.auth.UnauthorizedResponse
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

class ClientCertAuthenticationProvider internal constructor(
    config: Config
): AuthenticationProvider(config) {
    private val header = config.header
    private val validate = config.validationFunction

    class Config internal constructor(name: String?) : AuthenticationProvider.Config(name) {
        internal var validationFunction: AuthenticationFunction<X509Certificate> = { null }

        public var header: String = "X-Client-Cert"

        public fun validate(block: suspend ApplicationCall.(X509Certificate) -> Principal?) {
            validationFunction = block
        }

        internal fun build() = ClientCertAuthenticationProvider(this)
    }

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val call = context.call
        val certificateHeaderValue = call.request.headers[header]?.decodeURLPart()
        val x509Certificate = certificateHeaderValue?.let {
            val certificateFactory = CertificateFactory.getInstance("X.509")
            try {
                certificateFactory.generateCertificate(it.byteInputStream()) as X509Certificate
            } catch (ex: CertificateException) {
                logger.warn("Failed to parse header=$header as X.509 certificate", ex)
                null
            }
        }
        val principal = x509Certificate?.let { validate(call, it) }

        val cause = when {
            certificateHeaderValue == null -> AuthenticationFailedCause.NoCredentials
            x509Certificate == null -> AuthenticationFailedCause.Error("Failed to parse X.509 certificate from $header header")
            principal == null -> AuthenticationFailedCause.InvalidCredentials
            else -> null
        }

        if (cause != null) {
            @Suppress("NAME_SHADOWING")
            context.challenge(challengeKey, cause) { challenge, call ->
                call.respond(UnauthorizedResponse())
                challenge.complete()
            }
        }

        if (principal != null) { context.principal(name, principal) }
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(ClientCertAuthenticationProvider::class.java)
    }
}

public fun AuthenticationConfig.clientCert(
    name: String?,
    configure: ClientCertAuthenticationProvider.Config.() -> Unit
) {
    val provider = ClientCertAuthenticationProvider.Config(name).apply(configure).build()
    register(provider)
}

private val challengeKey: Any = "ClientCertificateAuth"
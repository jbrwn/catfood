package dog.catfood.plugins.csrf

import io.ktor.http.Cookie
import io.ktor.http.CookieEncoding
import io.ktor.http.HttpMethod
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.Hook
import io.ktor.server.application.call
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.auth.ForbiddenResponse
import io.ktor.server.request.httpMethod
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.ApplicationSendPipeline
import io.ktor.server.response.respond
import io.ktor.util.AttributeKey
import io.ktor.util.date.GMTDate
import io.ktor.util.date.plus
import java.security.MessageDigest
import java.util.Base64
import kotlin.random.Random

class TokenGenerator(private val secret: String, val generateFn: (String) -> String) {
    fun generate(): String = generateFn(secret)
}

interface TokenManager {
    fun generateSecret(): String
    fun generateToken(secret: String): String
    fun verifyToken(secret: String, token: String): Boolean
}

class MaskedTokenManager(
    private val saltLength: Int = 8,
    private val secretLength: Int = 18
): TokenManager {
    override fun generateSecret(): String {
        val randomBytes = Random.Default.nextBytes(secretLength)
        return toStringSafe(randomBytes)
    }
    override fun generateToken(secret: String): String {
        return tokenize(secret, randomString(saltLength))
    }
    override fun verifyToken(secret: String, token: String): Boolean {
        val index = token.indexOf("-")
        if (index == -1) return false
        val salt = token.substring(0, index)
        val expected = tokenize(secret, salt)
        return MessageDigest.isEqual(
            token.toByteArray(Charsets.US_ASCII),
            expected.toByteArray(Charsets.US_ASCII)
        )
    }

    private companion object {
        fun toStringSafe(bytes: ByteArray): String {
            return bytes.toBase64()
                .replace(EQUAL_END_REGEXP, "")
                .replace(PLUS_GLOBAL_REGEXP, "-")
                .replace(SLASH_GLOBAL_REGEXP, "_")
        }
        fun hash(value: String): String {
            return toStringSafe(
                MessageDigest.getInstance("SHA-1")
                    .digest(value.toByteArray(Charsets.US_ASCII))
            )
        }
        fun tokenize(secret: String, salt: String): String {
            return "$salt-${hash("$salt-$secret")}"
        }
        fun randomString(saltLength: Int): String {
            val builder = StringBuilder()
            repeat(saltLength) {
                builder.append(
                    RANDOM_CHARS[Random.nextInt(until = RANDOM_CHARS.length)]
                )
            }
            return builder.toString()
        }
        const val RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        const val EQUAL_END_REGEXP = "/=+$/"
        const val PLUS_GLOBAL_REGEXP = "/\\+/g"
        const val SLASH_GLOBAL_REGEXP = "/\\//g"
    }
}

fun ByteArray.toBase64(): String =
    String(Base64.getEncoder().encode(this))

class CsrfCookieManager(private val configuration: CsrfCookieConfiguration) {
    fun getOrSet(call: ApplicationCall, secret: () -> String): String {
        val result = get(call)
        if (result != null ) return result
        return secret().apply {
            set(call, this)
        }
    }
    fun get(call: ApplicationCall): String? {
        return call.request.cookies[configuration.name, configuration.encoding]
    }
    fun set(call: ApplicationCall, value: String) {
        val now = GMTDate()
        val maxAge = configuration.maxAgeInSeconds
        val expires = when (maxAge) {
            0L -> null
            else -> now + maxAge * 1000L
        }

        val cookie = Cookie(
            configuration.name,
            value,
            configuration.encoding,
            maxAge.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
            expires,
            configuration.domain,
            configuration.path,
            configuration.secure,
            configuration.httpOnly,
            configuration.extensions
        )

        call.response.cookies.append(cookie)
    }
}

const val DEFAULT_CSRF_FORM_FIELD: String = "_csrf"

class CsrfConfiguration {
    var ignoreMethods = listOf(HttpMethod.Get, HttpMethod.Head, HttpMethod.Options)
    var tokenManager: TokenManager = MaskedTokenManager()
    var cookieManager = CsrfCookieManager(CsrfCookieConfiguration())
    var parseTokenFn: suspend ApplicationCall.() -> String? = {
        receiveParameters()[DEFAULT_CSRF_FORM_FIELD]
    }
    fun parseToken(body: suspend ApplicationCall.() -> String?) {
        parseTokenFn = body
    }
}

const val DEFAULT_CSRF_COOKIE_AGE: Long = 14L * 24 * 3600 // 14 days
const val DEFAULT_CSRF_COOKIE_NAME: String = "csrf"

class CsrfCookieConfiguration {
    var maxAgeInSeconds: Long = DEFAULT_CSRF_COOKIE_AGE
    var name = DEFAULT_CSRF_COOKIE_NAME
    var encoding = CookieEncoding.URI_ENCODING
    var domain: String? = null
    var path: String? = "/"
    var secure: Boolean = false
    var httpOnly: Boolean = true
    val extensions: MutableMap<String, String?> = mutableMapOf()
}

fun CsrfConfiguration.cookie(block: CsrfCookieConfiguration.() -> Unit) {
    val config = CsrfCookieConfiguration().apply(block)
    cookieManager = CsrfCookieManager(config)
}

internal object BeforeSend : Hook<suspend (ApplicationCall) -> Unit> {
    override fun install(pipeline: ApplicationCallPipeline, handler: suspend (ApplicationCall) -> Unit) {
        pipeline.sendPipeline.intercept(ApplicationSendPipeline.Before) {
            handler(call)
        }
    }
}

val Csrf = createRouteScopedPlugin(
    name = "Csrf",
    createConfiguration = ::CsrfConfiguration
) {
    val ignoreMethods = pluginConfig.ignoreMethods
    val parseTokenFn = pluginConfig.parseTokenFn
    val tokenManager = pluginConfig.tokenManager
    val cookieManager = pluginConfig.cookieManager

    onCall { call ->
        val secret = cookieManager.getOrSet(call) { tokenManager.generateSecret() }

        if (call.request.httpMethod !in ignoreMethods) {
            val token = parseTokenFn(call)
            if (token == null || !tokenManager.verifyToken(secret, token)) {
                call.respond(ForbiddenResponse())
            }
        }

        val csrfData = CsrfData(
            tokenGenerator = TokenGenerator(secret, tokenManager::generateToken)
        )
        call.attributes.put(CsrfDataKey, csrfData)
    }

    on(BeforeSend) { call ->
        val csrfData = call.attributes.getOrNull(CsrfDataKey) ?: return@on
        if (csrfData.rotateCookie) {
            val secret = tokenManager.generateSecret()
            cookieManager.set(call, secret)
        }
    }
}

val CsrfDataKey = AttributeKey<CsrfData>("CsrfDataKey")

interface CsrfSession {
    fun getToken(): String
    fun rotateCookie()
}

class CsrfData(
    private val tokenGenerator: TokenGenerator,
    internal var rotateCookie: Boolean = false
): CsrfSession {
    override fun getToken(): String = tokenGenerator.generate()
    override fun rotateCookie() {
        rotateCookie = true
    }
}

val ApplicationCall.csrf
    get() = attributes[CsrfDataKey]
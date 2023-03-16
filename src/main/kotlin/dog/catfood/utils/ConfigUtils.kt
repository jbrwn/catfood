package dog.catfood.utils

import io.ktor.server.config.ApplicationConfig
import java.io.File

@Suppress("UNCHECKED_CAST")
operator fun <T> ApplicationConfig.get(property: String): T {
    return get(property) as T
}

@Suppress("UNCHECKED_CAST")
fun <T> ApplicationConfig.getOrNull(property: String, emptyAsNull: Boolean = false): T? {
    return getOrNull(property, emptyAsNull) as T?
}

fun ApplicationConfig.get(property: String): String {
    return property(property).getString()
}

fun ApplicationConfig.getOrNull(property: String, emptyAsNull: Boolean = false): String? {
    return propertyOrNull(property)?.let {
        val prop = it.getString()
        if (prop.isBlank() && emptyAsNull) { null } else { prop }
    }
}

fun readCertificate(value: String): String {
    return if (value.startsWith("-----BEGIN CERTIFICATE-----")) {
        value
    } else {
        File(value).readText(Charsets.US_ASCII)
    }
}

fun readPrivateKey(value: String): String {
    return if (value.startsWith("-----BEGIN") && value.endsWith("PRIVATE KEY-----")) {
        value
    } else {
        File(value).readText(Charsets.US_ASCII)
    }
}
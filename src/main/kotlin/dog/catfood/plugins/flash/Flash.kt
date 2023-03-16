package dog.catfood.plugins.flash

import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.response.respondRedirect
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.get
import io.ktor.server.sessions.set
import io.ktor.util.AttributeKey

interface TempData {
    val tempData: MutableMap<String, String>
}

class FlashConfiguration {
    lateinit var sessionName: String
}

fun FlashConfiguration.use(session: String) {
    sessionName = session
}

val Flash = createRouteScopedPlugin(
    name = "Flash",
    createConfiguration = ::FlashConfiguration
) {
    val sessionName = pluginConfig.sessionName
    onCall { call ->
        val session = call.sessions.get(sessionName) as? TempData
        val tempData = session?.tempData?.toMap()
        if (!tempData.isNullOrEmpty()) {
            session.tempData.clear()
            call.sessions.set(sessionName, session)
        }
        call.attributes.put(TempDataKey, tempData ?: emptyMap())
    }
}

val TempDataKey = AttributeKey<Map<String, String>>("TempDataKey")

val ApplicationCall.tempData get() = attributes[TempDataKey]

suspend inline fun <reified T: TempData>  ApplicationCall.respondRedirect(url: String, tempData: Map<String, String>) {
    val session = sessions.get(T::class)
    // TODO WARN no session found
    session?.let {
        it.tempData.putAll(tempData)
        sessions.set(it)
    }
    respondRedirect(url)
}

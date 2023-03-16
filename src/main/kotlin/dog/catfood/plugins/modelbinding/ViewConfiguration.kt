package dog.catfood.plugins.modelbinding

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.uri

interface TemplateProvider {
    fun getTemplateName(call: ApplicationCall): String
    fun content(template: String, context: Any?): Any
}

class NoOpTemplateProvider: TemplateProvider {
    override fun getTemplateName(call: ApplicationCall): String {
        TODO("Not yet implemented")
    }

    override fun content(template: String, context: Any?): Any {
        TODO("Not yet implemented")
    }
}

interface ContextProcessor {
    fun process(call: ApplicationCall): Map<String, Any?>
}

data class RequestContext(
    val uri: String
)

class RequestContextProcessor: ContextProcessor {
    override fun process(call: ApplicationCall): Map<String, Any?> {
        val requestContext = RequestContext(
            uri = call.request.uri
        )
        return mapOf("request" to requestContext)
    }
}

class ViewConfiguration {
    var templateProvider: TemplateProvider = NoOpTemplateProvider()
    var contextProcessors: List<ContextProcessor> = emptyList()
}
package dog.catfood.plugins.modelbinding.csrf

import dog.catfood.plugins.csrf.csrf
import dog.catfood.plugins.modelbinding.ContextProcessor
import io.ktor.server.application.ApplicationCall

class CsrfContextProcessor: ContextProcessor {
    override fun process(call: ApplicationCall): Map<String, Any?> {
        return mapOf("csrfToken" to call.csrf.getToken())
    }
}
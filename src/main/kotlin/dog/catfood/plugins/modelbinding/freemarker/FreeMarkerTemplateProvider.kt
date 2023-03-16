package dog.catfood.plugins.modelbinding.freemarker

import dog.catfood.plugins.modelbinding.TemplateProvider
import io.ktor.server.application.ApplicationCall
import io.ktor.server.freemarker.FreeMarkerContent

class FreeMarkerTemplateProvider(
    private val pathResolver: (ApplicationCall) -> String
): TemplateProvider {
    override fun getTemplateName(call: ApplicationCall): String {
        return "${pathResolver(call)}.ftl"
    }

    override fun content(template: String, context: Any?): Any {
        return FreeMarkerContent(template = template, model = context)
    }
}

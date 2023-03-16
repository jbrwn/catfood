package dog.catfood.plugins.modelbinding

import dog.catfood.utils.Converter
import dog.catfood.utils.ConverterFactory
import dog.catfood.utils.DEFAULT_CONVERTERS
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.util.AttributeKey
import kotlin.reflect.KType

fun ModelBindingConfiguration.views(block: ViewConfiguration.() -> Unit) {
    val config = ViewConfiguration().apply(block)
    viewConfiguration = config
}

class ModelBindingConfiguration {
    var validator: ModelValidator = NoOpModelValidator()
    var converters: Map<KType, Converter<*>> = emptyMap()
    var viewConfiguration = ViewConfiguration()
}

val ModelBinding = createRouteScopedPlugin(
    name = "ModelBinding",
    createConfiguration = ::ModelBindingConfiguration
) {
    val modelBinder = ModelBinder(
        validator = pluginConfig.validator,
        converterFactory = ConverterFactory(DEFAULT_CONVERTERS + pluginConfig.converters)
    )
    val viewConfig = pluginConfig.viewConfiguration
    onCall { call ->
        call.attributes.put(ModelBinderKey, modelBinder)
        call.attributes.put(ViewConfigKey, viewConfig)
    }
}

val ModelBinderKey = AttributeKey<ModelBinder>("ModelBinderKey")

val ViewConfigKey = AttributeKey<ViewConfiguration>("ViewConfigKey")

suspend inline fun <reified T : Any> ApplicationCall.receiveModel(): BindResult<T> {
    val modelBinder = attributes[ModelBinderKey]
    return modelBinder.bind(T::class, receiveParameters())
}

suspend fun ApplicationCall.respondView(bindResult: BindResult<*>, viewData: Map<String, Any?> = emptyMap())
        = respondView(template = null, model = null, bindResult = bindResult, viewData = viewData)

suspend fun ApplicationCall.respondView(viewData: Map<String, Any?> = emptyMap())
        = respondView(template = null, model = null, bindResult = null, viewData = viewData)

suspend fun <T> ApplicationCall.respondView(model: T, viewData: Map<String, Any?> = emptyMap())
        = respondView(template = null, model = model, bindResult = null, viewData = viewData)

suspend fun ApplicationCall.respondView(template: String, bindResult: BindResult<*>, viewData: Map<String, Any?> = emptyMap())
        = respondView(template = template, model = null, bindResult = bindResult, viewData = viewData)

suspend fun <T> ApplicationCall.respondView(
    template: String? = null,
    model: T? = null,
    bindResult: BindResult<*>? = null,
    viewData: Map<String, Any?> = emptyMap()
) {
    val viewConfig = attributes[ViewConfigKey]
    val templateProvider= viewConfig.templateProvider
    val contextProcessors = viewConfig.contextProcessors

    val context = contextProcessors.map { it.process(this) }
        .flatMap { it.entries }
        .associate { it.key to it.value }
        .plus(viewData)
        .plus(
            mapOf(
                "model" to model,
                "bindResult" to bindResult,
                "modelState" to ModelState(model, bindResult)
            )
        )

    respond(
        templateProvider.content(
            template = template ?: templateProvider.getTemplateName(this),
            context = context
        )
    )
}

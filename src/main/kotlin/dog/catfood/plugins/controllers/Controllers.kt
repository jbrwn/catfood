package dog.catfood.plugins.controllers

import dog.catfood.utils.Converter
import dog.catfood.utils.ConverterFactory
import dog.catfood.utils.DEFAULT_CONVERTERS
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.BaseRouteScopedPlugin
import io.ktor.server.application.call
import io.ktor.server.application.plugin
import io.ktor.server.routing.PathSegmentConstantRouteSelector
import io.ktor.server.routing.PathSegmentOptionalParameterRouteSelector
import io.ktor.server.routing.PathSegmentParameterRouteSelector
import io.ktor.server.routing.RootRouteSelector
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.util.AttributeKey
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaType

class ControllerConfiguration {
    var annotationProcessors: List<AnnotationProcessor<*>> = emptyList()
    var converters: Map<KType, Converter<*>> = emptyMap()
}

class Controllers(configuration: ControllerConfiguration) {
    val annotationProcessors = configuration.annotationProcessors
    val routeMap = mutableMapOf<ControllerActionPair, String>()
    val converterFactory = ConverterFactory(DEFAULT_CONVERTERS + configuration.converters)

    companion object Plugin: BaseRouteScopedPlugin<ControllerConfiguration, Controllers> {
        override val key = AttributeKey<Controllers>("Controllers")

        override fun install(pipeline: ApplicationCallPipeline, configure: ControllerConfiguration.() -> Unit): Controllers {
            val configuration = ControllerConfiguration().apply(configure)
            val plugin = Controllers(configuration)
            pipeline.intercept(ApplicationCallPipeline.Plugins) {
                call.attributes.put(ControllerRoutesHelper, RoutesHelper(plugin.routeMap))
            }
            return plugin
        }
    }
}

val ControllerRoutesHelper = AttributeKey<RoutesHelper>("ControllerRoutesHelper")
val ControllerName = AttributeKey<String>("ControllerName")
val ActionName = AttributeKey<String>("ActionName")

val ApplicationCall.routesHelper get() = attributes[ControllerRoutesHelper]
val ApplicationCall.controllerName get() = attributes[ControllerName]
val ApplicationCall.actionName get() = attributes[ActionName]

// Adapted from https://gist.github.com/SubSide/76c829a2fa7032372b6b168b273ac654
fun Route.controller(
    vararg controllers: Controller
) {
    val plugin = plugin(Controllers)
    val annotationProcessors = plugin.annotationProcessors.reversed()
    val routeMap = plugin.routeMap
    val converterFactory = plugin.converterFactory
    val routeProcessor = RouteProcessor(converterFactory)

    controllers.forEach { controller ->
        var controllerNode = this

        // process class annotations
        annotationProcessors.forEach { processor ->
            controllerNode = processor.processClass(controller::class, controllerNode)
        }

        getRoutePaths(controller).forEach { route ->
            var routeNode = controllerNode

            // process function annotations
            annotationProcessors.forEach { processor ->
                routeNode = processor.processFunction(route.function, routeNode)
            }

            // apply route handler
            routeNode = routeNode.route(
                route.path,
                route.method,
                routeProcessor.createRoute(controller, route.function)
            )

            // store in route map
            val controllerName = getControllerName(controller)
            val actionName = route.function.name
            val fullRoutePath = routeNode.fullPath(trimSlash = !route.path.endsWith("/"))
            routeMap[ControllerActionPair(controllerName, actionName)] = fullRoutePath
        }
    }
}

private fun Route.fullPath(trimSlash: Boolean): String {
    return if (trimSlash) {
        fullPath().trimEnd('/')
    } else {
        fullPath()
    }
}

private fun Route.fullPath(): String {
    val parentPath = parent?.fullPath()?.let { if (it.endsWith("/")) it else "$it/" } ?: "/"
    return when (selector) {
        is RootRouteSelector,
        is PathSegmentConstantRouteSelector,
        is PathSegmentParameterRouteSelector,
        is PathSegmentOptionalParameterRouteSelector -> parentPath + selector.toString()
        else -> parentPath
    }
}

class RouteProcessor(
    private val converterFactory: ConverterFactory
) {
    fun createRoute(controller: Controller, function: KFunction<*>): Route.() -> Unit = {
        handle {
            handleRoute(controller, function, call)
        }
    }

    private suspend fun handleRoute(
        controller: Controller,
        function: KFunction<*>,
        call: ApplicationCall
    ) {
        call.attributes.put(ControllerName, getControllerName(controller))
        call.attributes.put(ActionName, function.name)

        val parameters = function.parameters.associate {
            when (it.type.javaType) {
                controller.javaClass -> it to controller
                ApplicationCall::class.java -> it to call
                HttpMethod::class.java -> it to call.request.local.method
                else -> it to getParameterValue(it, call.parameters)
            }
        }

        function.callSuspendBy(parameters)
    }
    private fun getParameterValue(kParameter: KParameter, parameters: Parameters): Any? {
        val paramNameAnnotationValue = kParameter.findAnnotation<ParamName>()?.name
        val kParameterName = paramNameAnnotationValue ?: kParameter.name
        if (kParameterName != null) {
            val parameter = parameters[kParameterName]
            if (parameter != null) {
                return converterFactory.get(kParameter.type).convert(parameter)
            }
        }
        return null
    }

}

class RoutePath(
    val path: String,
    val method: HttpMethod,
    val function: KFunction<*>
)

private fun getRoutePaths(controller: Controller): List<RoutePath> {
    return controller::class.declaredMemberFunctions.map { method ->
        method.annotations.mapNotNull {
            when (it) {
                is GET -> RoutePath(it.route, HttpMethod.Get, method)
                is POST -> RoutePath(it.route, HttpMethod.Post, method)
                is PUT -> RoutePath(it.route, HttpMethod.Put, method)
                is PATCH -> RoutePath(it.route, HttpMethod.Patch, method)
                is DELETE -> RoutePath(it.route, HttpMethod.Delete, method)
                is HEAD -> RoutePath(it.route, HttpMethod.Head, method)
                is OPTIONS -> RoutePath(it.route, HttpMethod.Options, method)
                else -> null
            }
        }
    }.flatten()
}

private fun getControllerName(controller: Controller): String {
    val name = controller::class.simpleName ?: throw IllegalArgumentException("Controller class must be named")
    return if (name.endsWith("Controller")) {
        name.substringBeforeLast("Controller")
    } else {
        name
    }
}


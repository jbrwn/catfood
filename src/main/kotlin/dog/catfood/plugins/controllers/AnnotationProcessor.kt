package dog.catfood.plugins.controllers

import io.ktor.server.routing.Route
import io.ktor.util.reflect.instanceOf
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

abstract class AnnotationProcessor<T: Annotation>(
    val type: KClass<T>
) {
    fun processFunction(function: KFunction<*>, route: Route): Route = processKAnnotatedElement(function, route)
    fun processClass(kclass: KClass<*>, route: Route): Route = processKAnnotatedElement(kclass, route)
    private fun processKAnnotatedElement(element: KAnnotatedElement, route: Route): Route {
        @Suppress("UNCHECKED_CAST")
        val annotation = element.annotations.firstOrNull { it.instanceOf(type) } as T?
        return if (annotation != null) {
            process(annotation, route)
        } else {
            route
        }
    }
    abstract fun process(annotation: T, route: Route): Route
}
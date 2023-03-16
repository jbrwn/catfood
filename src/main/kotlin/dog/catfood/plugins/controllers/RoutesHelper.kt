package dog.catfood.plugins.controllers

import io.ktor.http.Parameters
import io.ktor.http.ParametersBuilder
import io.ktor.http.URLBuilder
import io.ktor.http.fullPath
import io.ktor.http.parametersOf
import io.ktor.util.filter

class RoutesHelper(
    private val routeMap: Map<ControllerActionPair, String>,
    private val urlBuilder: URLBuilder = URLBuilder()
) {

    fun href(
        controller: String,
        action: String
    ) = href(controller, action, Parameters.Empty)

    fun href(
        controller: String,
        action: String,
        parameterMap: Map<String, Any>
    ): String {
        val parameters = parameterMap.map {
            it.key to listOf(it.value.toString())
        }.toMap()
        return href(controller, action, parametersOf(parameters))
    }

    fun href(
        controller: String,
        action: String,
        singleParameters: Map<String, Any>,
        listParameters: Map<String, List<Any>>
    ): String {
        val coercedSingleParameters = singleParameters.map {
            it.key to listOf(it.value.toString())
        }
        val coercedListParameters = listParameters.map {
            it.key to it.value.map { value -> value.toString() }
        }
        val parameters = (coercedSingleParameters + coercedListParameters).toMap()
        return href(controller, action, parametersOf(parameters))
    }

    fun href(
        controller: String,
        action: String,
        parametersBuilder: ParametersBuilder.() -> Unit
    ) = href(controller, action, ParametersBuilder().apply(parametersBuilder).build())

    // Adapted from https://github.com/ktorio/ktor/blob/fb161cd7d268f2c0f62982d82d9264ca920173ad/ktor-shared/ktor-resources/common/src/io/ktor/resources/UrlBuilder.kt#L43
    fun href(
        controller: String,
        action: String,
        parameters: Parameters
    ): String {
        val pathTemplate = routeMap[ControllerActionPair(controller, action)]
            ?: throw IllegalArgumentException("No path template found for controller=$controller action=$action")

        val usedForPathParameterNames = mutableSetOf<String>()
        val pathParts = pathTemplate.split("/")

        val updatedParts = pathParts.flatMap {
            if (!it.startsWith('{') || !it.endsWith('}')) return@flatMap listOf(it)

            val part = it.substring(1, it.lastIndex)
            when {
                part.endsWith('?') -> {
                    val values = parameters.getAll(part.dropLast(1)) ?: return@flatMap emptyList()
                    if (values.size > 1) {
                        throw RouteUrlBuilderException(
                            "Expect zero or one parameter with name: ${part.dropLast(1)}, but found ${values.size}"
                        )
                    }
                    usedForPathParameterNames += part.dropLast(1)
                    values
                }
                part.endsWith("...") -> {
                    usedForPathParameterNames += part.dropLast(3)
                    parameters.getAll(part.dropLast(3)) ?: emptyList()
                }
                else -> {
                    val values = parameters.getAll(part)
                    if (values == null || values.size != 1) {
                        throw RouteUrlBuilderException(
                            "Expect exactly one parameter with name: $part, but found ${values?.size ?: 0}"
                        )
                    }
                    usedForPathParameterNames += part
                    values
                }
            }
        }

        urlBuilder.pathSegments = updatedParts

        val queryArgs = parameters.filter { key, _ -> !usedForPathParameterNames.contains(key) }
        urlBuilder.parameters.appendAll(queryArgs)
        return urlBuilder.build().fullPath
    }
}

class RouteUrlBuilderException(message: String) : Exception(message)

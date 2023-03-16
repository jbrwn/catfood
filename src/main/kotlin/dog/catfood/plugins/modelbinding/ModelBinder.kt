package dog.catfood.plugins.modelbinding

import dog.catfood.utils.ConversionException
import dog.catfood.utils.ConverterFactory
import io.ktor.http.Parameters
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class ModelBinder(
    private val converterFactory: ConverterFactory,
    private val validator: ModelValidator
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ModelBinder::class.java)
    }

    fun <T : Any> bind(
        kClass: KClass<T>,
        parameters: Parameters
    ): BindResult<T> {
        val errors = BindErrors()
        val model = deserialize(kClass, parameters, errors)
            ?: return InvalidBindResult(parameters, errors)
        validator.validate(model, errors)
        return if (errors.isEmpty()) {
            return ValidBindResult(parameters, errors, model)
        } else {
            InvalidBindResult(parameters, errors)
        }
    }

    private fun <T : Any> deserialize(
        kClass: KClass<T>,
        parameters: Parameters,
        errors: BindErrors
    ): T? {
        val constructor = kClass.primaryConstructor!!
        val modelParameters = constructor.parameters
        val ctorArgs = modelParameters.associateWith {
            val paramName = it.name
            val paramType = it.type
            if (paramName == null) {
                throw IllegalArgumentException("Class constructor parameter must be named. class=${kClass.simpleName}, param=$it")
            }
            val dataItem = parameters.getAll(paramName).orElse {
                logger.warn("Property $paramName is required for class ${kClass.simpleName} but was not found in form parameters")
                errors.addParamError(paramName, "missing value")
                return@associateWith null
            }
            try {
                val result = dataItem.map { item ->
                    converterFactory.get(paramType).convert(item)
                }
                if (result.size < 2)  result.firstOrNull() else result
            } catch (e: ConversionException) {
                logger.warn("Error converting parameter $paramName to type $paramType. value=$dataItem", e)
                errors.addParamError(paramName, "invalid value")
                null
            }
        }
        return try {
            constructor.callBy(ctorArgs)
        } catch (e: Throwable) {
            logger.warn("Error binding to ${kClass.simpleName} with for parameters $parameters", e)
            null
        }
    }
}

private inline fun <T> T?.orElse(ifNullBlock: () -> T): T = this ?: ifNullBlock()

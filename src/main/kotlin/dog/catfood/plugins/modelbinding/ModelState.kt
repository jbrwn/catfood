package dog.catfood.plugins.modelbinding

import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class ModelState<T>(
    val model: T?,
    val bindResult: BindResult<*>?,
) {
    fun hasValidationErrors(): Boolean = bindResult?.errors?.isEmpty() == false

    fun validationErrors(): List<String> = bindResult?.errors?.getAllErrors() ?: emptyList()

    fun globalValidationErrors(): List<String> = bindResult?.errors?.getGlobalErrors() ?: emptyList()

    fun fieldValidationErrors(): Map<String, List<String>> = bindResult?.errors?.getParamErrors() ?: emptyMap()

    fun validationErrorsFor(field: String): List<String> = bindResult?.errors?.getParamErrors()?.get(field) ?: emptyList()

    fun valueFor(field: String): String? = bindResult?.parameters?.get(field) ?: getModelProperty(field)

    @Suppress("UNCHECKED_CAST")
    private fun getModelProperty(field: String): String? {
        return model?.let { model ->
            model::class.memberProperties
                .find { it.name == field }
                ?.let { it as KProperty1<T, *> }
                ?.get(model)?.toString()
        }
    }

}

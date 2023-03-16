package dog.catfood.plugins.modelbinding

class BindErrors {
    private val paramErrors = mutableMapOf<String, List<String>>()
    private val globalErrors = mutableListOf<String>()
    fun addParamError(param: String, message: String) {
        val messages = paramErrors[param]
        if (messages == null) {
            paramErrors[param] = listOf(message)
        } else {
            paramErrors[param] = messages + listOf(message)
        }
    }
    fun addGlobalError(message: String) {
        globalErrors.add(message)
    }
    fun getParamErrors(): Map<String, List<String>> = paramErrors.toMap()
    fun getGlobalErrors(): List<String> = globalErrors.toList()
    fun getAllErrors(): List<String> {
        return paramErrors.flatMap {
            it.value.map { message -> "${it.key}: $message" }
        }.plus(globalErrors)
    }
    fun isEmpty(): Boolean {
        return paramErrors.isEmpty() && globalErrors.isEmpty()
    }
}
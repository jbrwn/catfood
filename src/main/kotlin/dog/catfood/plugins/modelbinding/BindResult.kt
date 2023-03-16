package dog.catfood.plugins.modelbinding

import io.ktor.http.Parameters

interface BindResult<T> {
    val parameters: Parameters
    val errors: BindErrors
}

class ValidBindResult<T>(
    override val parameters: Parameters,
    override val errors: BindErrors,
    val model: T
): BindResult<T>

class InvalidBindResult<T>(
    override val parameters: Parameters,
    override val errors: BindErrors,
): BindResult<T>
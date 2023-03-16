package dog.catfood.plugins.modelbinding

interface ModelValidator {
    fun <T> validate(
        model: T,
        errors: BindErrors
    )
}

class NoOpModelValidator: ModelValidator {
    override fun <T> validate(model: T, errors: BindErrors) {}
}
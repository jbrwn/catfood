package dog.catfood.plugins.modelbinding.hibernate

import dog.catfood.plugins.modelbinding.BindErrors
import dog.catfood.plugins.modelbinding.ModelValidator
import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory

class HibernateModelValidator(
    validationFactory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
): ModelValidator {
    private val validator: Validator = validationFactory.validator
    override fun <T> validate(model: T, errors: BindErrors) {
        val constraintErrors = validator.validate(model)
            .associate { it.propertyPath.toString() to it.message }
        if (constraintErrors.isNotEmpty()) {
            constraintErrors.forEach { errors.addParamError(it.key, it.value)}
        }
    }
}

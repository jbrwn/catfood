package dog.catfood.models.validators

import dog.catfood.models.CertificateStatus
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [CertificateStatusValidator::class])
annotation class CertificateStatusValue(
    val anyOf: Array<CertificateStatus>,
    val message: String = "must be any of {anyOf}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class CertificateStatusValidator : ConstraintValidator<CertificateStatusValue, CertificateStatus> {
    private lateinit var subset: List<CertificateStatus>
    override fun initialize(constraint: CertificateStatusValue) {
        subset = constraint.anyOf.toList()
    }

    override fun isValid(value: CertificateStatus?, context: ConstraintValidatorContext?): Boolean {
        return value == null || subset.contains(value)
    }
}
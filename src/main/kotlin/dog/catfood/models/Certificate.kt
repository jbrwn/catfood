package dog.catfood.models

import java.time.OffsetDateTime

data class Certificate(
    val id: Long,
    val certificateHash: String,
    val certificatePem: String,
    val subject: String,
    val issuer: String,
    val valid: OffsetDateTime,
    val expires: OffsetDateTime,
    val status: CertificateStatus,
    val deviceId: Long,
    val createdOn: OffsetDateTime,
    val modifiedOn: OffsetDateTime
)

enum class CertificateStatus {
    ACTIVE,
    INACTIVE,
    REVOKED,
    DELETED
}

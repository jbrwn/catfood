package dog.catfood.models

import java.time.OffsetDateTime

data class CertificateAuthority(
    val id: Long,
    val certificateHash: String,
    val certificatePem: String,
    val subject: String,
    val issuer: String,
    val valid: OffsetDateTime,
    val expires: OffsetDateTime,
    val createdOn: OffsetDateTime
)

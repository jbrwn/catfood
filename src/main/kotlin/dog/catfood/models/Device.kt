package dog.catfood.models

import java.time.OffsetDateTime

data class Device(
    val id: Long,
    val name: String,
    val userId: Long,
    val createdOn: OffsetDateTime,
    val modifiedOn: OffsetDateTime
)

package dog.catfood.models

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class Location(
    val id: Long,
    @JsonProperty("device_id") val deviceId: Long,
    val longitude: Double, // decimal degrees
    val latitude: Double, // decimal degrees
    val altitude: Double, // meters
    val speed: Double, // knots
    val angle: Double, // Degrees from true north
    @JsonProperty("magnetic_variation") val magneticVariation: Double, // Degrees from true north
    @JsonProperty("recorded_on") val recordedOn: OffsetDateTime, // fix time
    @JsonProperty("created_on") val createdOn: OffsetDateTime
)

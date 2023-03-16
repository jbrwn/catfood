package dog.catfood.dao

import dog.catfood.jooq.Tables.DEVICES
import dog.catfood.models.Location
import dog.catfood.jooq.Tables.LOCATIONS
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.kotlin.coroutines.transactionCoroutine
import java.time.OffsetDateTime

class LocationDao(private val context: DSLContext) {
    private companion object {
        private val LOCATION_COLUMNS = listOf(
            LOCATIONS.ID,
            LOCATIONS.DEVICE_ID,
            LOCATIONS.LONGITUDE,
            LOCATIONS.LATITUDE,
            LOCATIONS.ALTITUDE,
            LOCATIONS.SPEED,
            LOCATIONS.ANGLE,
            LOCATIONS.MAGNETICVARIATION,
            LOCATIONS.RECORDED_ON,
            LOCATIONS.CREATED_ON
        )
    }

    suspend fun createLocation(
        deviceId: Long,
        longitude: Double, // decimal degrees
        latitude: Double, // decimal degrees
        altitude: Double, // meters
        speed: Double, // knots
        angle: Double, // Degrees from true north
        magneticVariation: Double, // Degrees from true north
        recordedOn: OffsetDateTime, // fix time
    ): Location {
        return context.transactionCoroutine { t ->
            t.dsl().insertInto(LOCATIONS)
                .columns(
                    LOCATIONS.DEVICE_ID,
                    LOCATIONS.LONGITUDE,
                    LOCATIONS.LATITUDE,
                    LOCATIONS.ALTITUDE,
                    LOCATIONS.SPEED,
                    LOCATIONS.ANGLE,
                    LOCATIONS.MAGNETICVARIATION,
                    LOCATIONS.RECORDED_ON
                )
                .values(deviceId, longitude, latitude, altitude, speed, angle, magneticVariation, recordedOn)
                .returning(LOCATION_COLUMNS)
                .awaitSingle()
                .map { it.toLocation() }
        }
    }

    suspend fun getLocations(deviceId: Long, userId: Long, indexId: Long?, limit: Int): List<Location> {
        return context.transactionCoroutine { t ->
            val statement = t.dsl().select(LOCATION_COLUMNS)
                .from(LOCATIONS)
                .join(DEVICES)
                .on(LOCATIONS.DEVICE_ID.eq(DEVICES.ID))
                .where(LOCATIONS.DEVICE_ID.eq(deviceId))
                .and(DEVICES.USER_ID.eq(userId))
            indexId?.let { statement.and(LOCATIONS.ID.lessThan(indexId)) }
            statement
                .orderBy(LOCATIONS.RECORDED_ON.desc())
                .limit(limit)
                .asFlow()
                .map { it.toLocation() }
                .toList()
        }
    }
}

fun Record.toLocation() =
    Location(
        getValue(LOCATIONS.ID),
        getValue(LOCATIONS.DEVICE_ID),
        getValue(LOCATIONS.LONGITUDE),
        getValue(LOCATIONS.LATITUDE),
        getValue(LOCATIONS.ALTITUDE),
        getValue(LOCATIONS.SPEED),
        getValue(LOCATIONS.ANGLE),
        getValue(LOCATIONS.MAGNETICVARIATION),
        getValue(LOCATIONS.RECORDED_ON),
        getValue(LOCATIONS.CREATED_ON)
    )

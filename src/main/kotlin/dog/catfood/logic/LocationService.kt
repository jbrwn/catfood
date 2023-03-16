package dog.catfood.logic

import dog.catfood.controllers.CreateLocationRequest
import dog.catfood.dao.LocationDao
import dog.catfood.models.Cursor
import dog.catfood.models.Location

class LocationService(
    private val locationDao: LocationDao
) {
    suspend fun createLocation(deviceId: Long, createLocationRequest: CreateLocationRequest): Location {
        return locationDao.createLocation(
            deviceId,
            createLocationRequest.longitude,
            createLocationRequest.latitude,
            createLocationRequest.altitude,
            createLocationRequest.speed,
            createLocationRequest.angle,
            createLocationRequest.magneticVariation,
            createLocationRequest.timestamp
        )
    }

    suspend fun getLocations(deviceId: Long, userId: Long, cursor: Cursor<Long>): List<Location> {
        return locationDao.getLocations(deviceId, userId, cursor.id, cursor.limit)
    }
}
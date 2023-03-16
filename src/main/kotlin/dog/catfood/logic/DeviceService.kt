package dog.catfood.logic

import dog.catfood.dao.DeviceDao
import dog.catfood.models.Certificate
import dog.catfood.models.Device

class DeviceService(
    private val deviceDao: DeviceDao
) {
    suspend fun getDevices(userId: Long): List<Device> {
        return deviceDao.getDevices(userId)
    }

    suspend fun getDevice(deviceId: Long, userId: Long? = null): Device? {
        return deviceDao.getDevice(deviceId, userId)
    }

    suspend fun getDeviceWithCertificates(deviceId: Long, userId: Long): Pair<Device, List<Certificate>>? {
        return deviceDao.getDeviceWithCertificates(userId, deviceId)
    }

    suspend fun getDeviceWithCertificate(deviceId: Long, certificateId: Long, userId: Long): Pair<Device, Certificate>? {
        return deviceDao.getDeviceWithCertificate(userId, deviceId, certificateId)
    }

    suspend fun createDevice(name: String, userId: Long): Device {
        return deviceDao.createDevice(userId, name)
    }
}
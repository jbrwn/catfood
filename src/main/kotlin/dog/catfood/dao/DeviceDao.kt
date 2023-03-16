package dog.catfood.dao

import dog.catfood.jooq.Tables.CERTIFICATES
import dog.catfood.jooq.Tables.CERTIFICATE_AUTHORITY
import dog.catfood.jooq.Tables.DEVICES
import dog.catfood.models.Certificate
import dog.catfood.models.Device
import dog.catfood.utils.groupBy
import dog.catfood.utils.nullableSingle
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.kotlin.coroutines.transactionCoroutine

class DeviceDao(private val context: DSLContext) {
    private companion object {
        private val DEVICE_WITH_CERTIFICATE_COLUMNS = listOf(
            DEVICES.ID,
            DEVICES.NAME,
            DEVICES.USER_ID,
            DEVICES.CREATED_ON,
            DEVICES.MODIFIED_ON,
            CERTIFICATES.ID,
            CERTIFICATES.CERTIFICATE_HASH,
            CERTIFICATES.CERTIFICATE_PEM,
            CERTIFICATES.SUBJECT,
            CERTIFICATE_AUTHORITY.SUBJECT,
            CERTIFICATES.VALID,
            CERTIFICATES.EXPIRES,
            CERTIFICATES.STATUS,
            CERTIFICATES.DEVICE_ID,
            CERTIFICATES.CREATED_ON,
            CERTIFICATES.MODIFIED_ON
        )
    }

    suspend fun getDevices(userId: Long): List<Device> {
        return context.transactionCoroutine { t ->
            t.dsl().select(
                DEVICES.ID,
                DEVICES.NAME,
                DEVICES.USER_ID,
                DEVICES.CREATED_ON,
                DEVICES.MODIFIED_ON
            )
                .from(DEVICES)
                .where(DEVICES.USER_ID.eq(userId))
                .asFlow()
                .map { it.toDevice() }
                .toList()
        }
    }

    suspend fun getDevice(deviceId: Long, userId: Long? = null): Device? {
        return context.transactionCoroutine { t ->
            val statement = t.dsl().select(
                DEVICES.ID,
                DEVICES.NAME,
                DEVICES.USER_ID,
                DEVICES.CREATED_ON,
                DEVICES.MODIFIED_ON
            )
                .from(DEVICES)
                .where(DEVICES.ID.eq(deviceId))

            userId?.let { statement.and(DEVICES.USER_ID.eq(userId)) }
            statement
                .awaitFirstOrNull()
                ?.map { it.toDevice() }
        }
    }

    suspend fun getDeviceWithCertificates(userId: Long, deviceId: Long): Pair<Device, List<Certificate>>? {
        return context.transactionCoroutine { t ->
            t.dsl().select(DEVICE_WITH_CERTIFICATE_COLUMNS)
                .from(DEVICES)
                .leftJoin(CERTIFICATES)
                .on(CERTIFICATES.DEVICE_ID.eq(DEVICES.ID))
                .leftJoin(CERTIFICATE_AUTHORITY)
                .on(CERTIFICATES.CERTIFICATE_AUTHORITY_ID.eq(CERTIFICATE_AUTHORITY.ID))
                .where(DEVICES.USER_ID.eq(userId))
                .and(DEVICES.ID.eq(deviceId))
                .asFlow()
                //.groupBy({ it.toDevice() }, { it.toCertificate() })
                .groupBy({ it.toDevice() }, { it.toCertificateOrNull() })
                .toList()
                .nullableSingle()
        }
    }

    suspend fun getDeviceWithCertificate(userId: Long, deviceId: Long, certificateId: Long): Pair<Device, Certificate>? {
        return context.transactionCoroutine { t ->
            t.dsl().select(DEVICE_WITH_CERTIFICATE_COLUMNS)
                .from(DEVICES)
                .join(CERTIFICATES)
                .on(CERTIFICATES.DEVICE_ID.eq(DEVICES.ID))
                .leftJoin(CERTIFICATE_AUTHORITY)
                .on(CERTIFICATES.CERTIFICATE_AUTHORITY_ID.eq(CERTIFICATE_AUTHORITY.ID))
                .where(DEVICES.USER_ID.eq(userId))
                .and(DEVICES.ID.eq(deviceId))
                .and(CERTIFICATES.ID.eq(certificateId))
                .awaitFirstOrNull()
                ?.map { it.toDevice() to it.toCertificate() }
        }
    }

    suspend fun createDevice(userId: Long, name: String): Device {
        return context.transactionCoroutine { t ->
            t.dsl().insertInto(DEVICES)
                .columns(DEVICES.NAME, DEVICES.USER_ID)
                .values(name, userId)
                .returning(
                    DEVICES.ID,
                    DEVICES.NAME,
                    DEVICES.USER_ID,
                    DEVICES.CREATED_ON,
                    DEVICES.MODIFIED_ON
                )
                .awaitSingle()
                .map { it.toDevice() }
        }
    }
}

fun Record.toDevice() =
    Device(
        getValue(DEVICES.ID),
        getValue(DEVICES.NAME),
        getValue(DEVICES.USER_ID),
        getValue(DEVICES.CREATED_ON),
        getValue(DEVICES.MODIFIED_ON)
    )

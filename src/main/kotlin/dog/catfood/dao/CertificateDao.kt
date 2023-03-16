package dog.catfood.dao

import dog.catfood.jooq.Tables.CERTIFICATES
import dog.catfood.jooq.Tables.CERTIFICATE_AUTHORITY
import dog.catfood.jooq.Tables.DEVICES
import dog.catfood.models.Certificate
import dog.catfood.models.CertificateAuthority
import dog.catfood.models.CertificateStatus
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.kotlin.coroutines.transactionCoroutine
import java.time.OffsetDateTime

class CertificateDao(
    private val context: DSLContext
) {
    private companion object {
        private val CERTIFICATE_COLUMNS = listOf(
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

        private val CERTIFICATE_AUTHORITY_COLUMNS = listOf(
            CERTIFICATE_AUTHORITY.ID,
            CERTIFICATE_AUTHORITY.CERTIFICATE_HASH,
            CERTIFICATE_AUTHORITY.CERTIFICATE_PEM,
            CERTIFICATE_AUTHORITY.SUBJECT,
            CERTIFICATE_AUTHORITY.ISSUER,
            CERTIFICATE_AUTHORITY.VALID,
            CERTIFICATE_AUTHORITY.EXPIRES,
            CERTIFICATE_AUTHORITY.CREATED_ON
        )
    }

    suspend fun getCertificateById(id: Long,): Certificate? {
        return context.transactionCoroutine { t ->
            t.dsl().select(CERTIFICATE_COLUMNS)
                .from(CERTIFICATES)
                .join(CERTIFICATE_AUTHORITY)
                .on(CERTIFICATES.CERTIFICATE_AUTHORITY_ID.eq(CERTIFICATE_AUTHORITY.ID))
                .where(CERTIFICATES.ID.eq(id))
                .awaitFirstOrNull()
                ?.map { it.toCertificate() }
        }
    }

    suspend fun getCertificateByHash(certificateHash: String, status: CertificateStatus? = null): Certificate? {
        return context.transactionCoroutine { t ->
            val statement = t.dsl().select(CERTIFICATE_COLUMNS)
                .from(CERTIFICATES)
                .join(CERTIFICATE_AUTHORITY)
                .on(CERTIFICATES.CERTIFICATE_AUTHORITY_ID.eq(CERTIFICATE_AUTHORITY.ID))
                .where(CERTIFICATES.CERTIFICATE_HASH.eq(certificateHash))
            status?.let { statement.and(CERTIFICATES.STATUS.eq(status.name)) }

            statement
                .awaitFirstOrNull()
                ?.map { it.toCertificate() }
        }
    }

    suspend fun createCertificate(
        certificateHash: String,
        certificatePem: String,
        subject: String,
        valid: OffsetDateTime,
        expires: OffsetDateTime,
        status: CertificateStatus,
        deviceId: Long,
        certificateAuthorityId: Long,
    ): Long {
        return context.transactionCoroutine { t ->
            t.dsl().insertInto(CERTIFICATES)
                .columns(
                    CERTIFICATES.CERTIFICATE_HASH,
                    CERTIFICATES.CERTIFICATE_PEM,
                    CERTIFICATES.SUBJECT,
                    CERTIFICATES.CERTIFICATE_AUTHORITY_ID,
                    CERTIFICATES.VALID,
                    CERTIFICATES.EXPIRES,
                    CERTIFICATES.STATUS,
                    CERTIFICATES.DEVICE_ID,
                )
                .values(certificateHash, certificatePem, subject, certificateAuthorityId, valid, expires, status.name, deviceId)
                .returning(CERTIFICATES.ID)
                .awaitSingle()
                .getValue(CERTIFICATES.ID)
        }
    }

    suspend fun getCertificateAuthorityByHash(certificateHash: String): CertificateAuthority? {
        return context.transactionCoroutine { t ->
            t.dsl().select(CERTIFICATE_AUTHORITY_COLUMNS)
                .from(CERTIFICATE_AUTHORITY)
                .where(CERTIFICATE_AUTHORITY.CERTIFICATE_HASH.eq(certificateHash))
                .awaitFirstOrNull()
                ?.map { it.toCertificateAuthority() }
        }
    }

    suspend fun createCertificateAuthority(
        certificateHash: String,
        certificatePem: String,
        subject: String,
        issuer: String,
        valid: OffsetDateTime?,
        expires: OffsetDateTime?
    ): CertificateAuthority {
        return context.transactionCoroutine { t ->
            t.dsl().insertInto(CERTIFICATE_AUTHORITY)
                .columns(
                    CERTIFICATE_AUTHORITY.CERTIFICATE_HASH,
                    CERTIFICATE_AUTHORITY.CERTIFICATE_PEM,
                    CERTIFICATE_AUTHORITY.SUBJECT,
                    CERTIFICATE_AUTHORITY.ISSUER,
                    CERTIFICATE_AUTHORITY.VALID,
                    CERTIFICATE_AUTHORITY.EXPIRES,
                )
                .values(certificateHash, certificatePem, subject, issuer, valid, expires)
                .returning(CERTIFICATE_AUTHORITY_COLUMNS)
                .awaitSingle()
                .map { it.toCertificateAuthority() }
        }
    }
}

fun Record.toCertificate() =
    Certificate(
        getValue(CERTIFICATES.ID),
        getValue(CERTIFICATES.CERTIFICATE_HASH),
        getValue(CERTIFICATES.CERTIFICATE_PEM),
        getValue(CERTIFICATES.SUBJECT),
        getValue(CERTIFICATE_AUTHORITY.SUBJECT),
        getValue(CERTIFICATES.VALID),
        getValue(CERTIFICATES.EXPIRES),
        CertificateStatus.valueOf(getValue(CERTIFICATES.STATUS)),
        getValue(CERTIFICATES.DEVICE_ID),
        getValue(DEVICES.CREATED_ON),
        getValue(DEVICES.MODIFIED_ON)
    )

fun Record.toCertificateAuthority() =
    CertificateAuthority(
        getValue(CERTIFICATE_AUTHORITY.ID),
        getValue(CERTIFICATE_AUTHORITY.CERTIFICATE_HASH),
        getValue(CERTIFICATE_AUTHORITY.CERTIFICATE_PEM),
        getValue(CERTIFICATE_AUTHORITY.SUBJECT),
        getValue(CERTIFICATE_AUTHORITY.ISSUER),
        getValue(CERTIFICATE_AUTHORITY.VALID),
        getValue(CERTIFICATE_AUTHORITY.EXPIRES),
        getValue(CERTIFICATE_AUTHORITY.CREATED_ON)
    )

fun Record.toCertificateOrNull() = if (hasCertificateId()) { toCertificate() } else { null }

fun Record.hasCertificateId(): Boolean = getValue(CERTIFICATES.ID) != null
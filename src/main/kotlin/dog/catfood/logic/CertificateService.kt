package dog.catfood.logic

import dog.catfood.dao.CertificateDao
import dog.catfood.models.CertificateStatus
import dog.catfood.models.Certificate
import dog.catfood.models.KeyPair
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.Date
import javax.security.auth.x500.X500Principal

class CertificateService(
    private val certificateDao: CertificateDao,
    private val caKeyStore: CaKeyStore
) {
    companion object {
        private const val CLIENT_CERT_COMMON_NAME: String = "Catfood Client Certificate"
        private const val CLIENT_CERT_DURATION_DAYS = 3650L
    }

    suspend fun createCertificate(
        deviceId: Long,
        certificateStatus: CertificateStatus
    ): Pair<Certificate, KeyPair> {
        // create client cert
        val (clientX509Cert, keyPair) = caKeyStore.generateSignedX509Certificate(
            x500Principal = X500Principal("CN=$CLIENT_CERT_COMMON_NAME"),
            notBefore = Date.from(Instant.now()),
            notAfter = Date.from(Instant.now().plus(CLIENT_CERT_DURATION_DAYS, ChronoUnit.DAYS)),
        )

        // get or create ca record in db
        val caX509Cert = caKeyStore.getCaCertificate()
        val caX509CertHash = caX509Cert.getHash()
        val ca = certificateDao.getCertificateAuthorityByHash(caX509Cert.getHash())
            ?: certificateDao.createCertificateAuthority(
                certificateHash = caX509Cert.getHash(),
                certificatePem = caX509Cert.toPem(),
                subject = caX509Cert.subjectX500Principal.toString(),
                issuer = caX509Cert.issuerX500Principal.toString(),
                valid = caX509Cert.notBefore.toInstant().atOffset(ZoneOffset.UTC),
                expires = caX509Cert.notAfter.toInstant().atOffset(ZoneOffset.UTC)
            )

        // create client cert record in db
        val certificateId = certificateDao.createCertificate(
            certificateHash = clientX509Cert.getHash(),
            certificatePem = clientX509Cert.toPem(),
            subject = clientX509Cert.subjectX500Principal.toString(),
            valid = clientX509Cert.notBefore.toInstant().atOffset(ZoneOffset.UTC),
            expires = clientX509Cert.notAfter.toInstant().atOffset(ZoneOffset.UTC),
            status = certificateStatus,
            deviceId = deviceId,
            certificateAuthorityId = ca.id
        )
        val clientCertificate = certificateDao.getCertificateById(certificateId)!!
        return clientCertificate to KeyPair(keyPair.public.toPem(), keyPair.private.toPem())
    }
}

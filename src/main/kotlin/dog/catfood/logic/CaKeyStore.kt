package dog.catfood.logic

import io.ktor.util.toCharArray
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jcajce.util.MessageDigestUtils
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaMiscPEMGenerator
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo
import org.bouncycastle.util.encoders.Hex
import org.bouncycastle.util.io.pem.PemWriter
import java.io.StringReader
import java.io.StringWriter
import java.lang.IllegalArgumentException
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Security
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Date
import javax.security.auth.x500.X500Principal
import kotlin.random.Random

const val DEFAULT_CA_ALIAS = "APP_CA"

/**
 * @param certificate x509 certificate. e.g., openssl req -x509 -new -nodes -key root_ca_key.key -sha256 -days 3650 -out root_ca_cert.pem
 * @param key PKCS8 RSA private key. e.g, openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -des3 -out root_ca_key.key
 * @param password private key password.
 * @param alias optional java keystore alias for the keystore entry.
 */
fun buildCaKeyStore(
    certificate: String,
    key: String,
    password: String,
    alias: String = DEFAULT_CA_ALIAS
): CaKeyStore {
    val certificateFactory = CertificateFactory.getInstance("X.509")
    val x509Certificate = certificateFactory.generateCertificate(certificate.byteInputStream())

    val keyReader = StringReader(key)
    val pemObject = keyReader.use { r ->
        val pemParser = PEMParser(r)
        pemParser.readObject()
    }

    if (Security.getProvider("BC") == null) {
        Security.addProvider(BouncyCastleProvider())
    }
    val converter = JcaPEMKeyConverter()
    val privateKey = if (pemObject is PrivateKeyInfo) {
        val privateKeyInfo = PrivateKeyInfo.getInstance(pemObject)
        converter.getPrivateKey(privateKeyInfo)
    } else if (pemObject is PKCS8EncryptedPrivateKeyInfo) {
        val decryptor = JceOpenSSLPKCS8DecryptorProviderBuilder()
            .build(password.toCharArray());
        converter.getPrivateKey(pemObject.decryptPrivateKeyInfo(decryptor))
    } else {
        throw IllegalArgumentException("Private key type not supported")
    }
    val store = KeyStore.getInstance("JKS").apply {
        load(null, null)
        setKeyEntry(
            alias,
            privateKey,
            password.toCharArray(),
            listOf(x509Certificate).toTypedArray()
        )
    }

    return CaKeyStore(store, alias, password)
}

class CaKeyStore(
    private val keyStore: KeyStore,
    private val caAlias: String,
    private val caKeyPassword: String,
) {
    fun generateSignedX509Certificate(
        x500Principal: X500Principal,
        notBefore: Date,
        notAfter: Date,
        keyPairAlgorithm: String = "RSA",
        keySizeInBits: Int = 1024,
        signatureAlgorithm: String = "SHA256WithRSAEncryption"
    ): Pair<X509Certificate, KeyPair> {
        val caCert = keyStore.getCertificate(caAlias) as X509Certificate
        val caKeys = KeyPair(caCert.publicKey, keyStore.getKey(caAlias, caKeyPassword.toCharArray()) as PrivateKey)

        val keys = KeyPairGenerator.getInstance(keyPairAlgorithm).apply {
            initialize(keySizeInBits)
        }.genKeyPair()

        val contentSigner = JcaContentSignerBuilder(signatureAlgorithm).build(caKeys.private)
        val certBuilder = JcaX509v3CertificateBuilder(
            caCert,
            Random.nextLong().toBigInteger(),
            notBefore,
            notAfter,
            x500Principal,
            keys.public
        )
        // TODO cert extensions

        val cert = JcaX509CertificateConverter()
            .getCertificate(certBuilder.build(contentSigner))
        cert.verify(caKeys.public)
        return cert to keys
    }

    fun getCaCertificate(): X509Certificate = keyStore.getCertificate(caAlias) as X509Certificate
}

fun X509Certificate.toPem(): String {
    val stringWriter = StringWriter()
    val pemWriter = PemWriter(stringWriter)
    pemWriter.use {
        pemWriter.writeObject(JcaMiscPEMGenerator(this))
    }
    return stringWriter.toString()
}

fun PrivateKey.toPem(): String {
    val stringWriter = StringWriter()
    val pemWriter = PemWriter(stringWriter)
    pemWriter.use {
        pemWriter.writeObject(JcaPKCS8Generator(this, null))
    }
    return stringWriter.toString()
}

fun PublicKey.toPem(): String {
    val stringWriter = StringWriter()
    val pemWriter = PemWriter(stringWriter)
    pemWriter.use {
        pemWriter.writeObject(JcaMiscPEMGenerator(this))
    }
    return stringWriter.toString()
}

// hex encoded sha256 hash of the certificate DER
fun X509Certificate.getHash(): String {
    val hash = MessageDigest.getInstance("SHA-256")
        .digest(encoded)
    return Hex.toHexString(hash)
}
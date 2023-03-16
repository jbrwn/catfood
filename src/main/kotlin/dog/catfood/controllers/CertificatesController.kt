package dog.catfood.controllers

import dog.catfood.logic.CertificateService
import dog.catfood.logic.DeviceService
import dog.catfood.models.AppSession
import dog.catfood.models.Certificate
import dog.catfood.models.CertificateStatus
import dog.catfood.models.Device
import dog.catfood.models.KeyPair
import dog.catfood.models.getUserPrincipal
import dog.catfood.models.validators.CertificateStatusValue
import dog.catfood.plugins.controllers.Controller
import dog.catfood.plugins.controllers.GET
import dog.catfood.plugins.controllers.POST
import dog.catfood.plugins.controllers.authentication.Authenticate
import dog.catfood.plugins.controllers.routesHelper
import dog.catfood.plugins.flash.tempData
import dog.catfood.plugins.flash.respondRedirect
import dog.catfood.plugins.modelbinding.ValidBindResult
import dog.catfood.plugins.modelbinding.receiveModel
import dog.catfood.plugins.modelbinding.respondView
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.NotFoundException
import org.slf4j.LoggerFactory
import java.nio.charset.Charset
import java.util.Base64

@Authenticate("auth-session")
class CertificatesController(
    private val certificateService: CertificateService,
    private val deviceService: DeviceService
): Controller {
    @GET("/dashboard/devices/{deviceId}/certificates")
    suspend fun index(call: ApplicationCall, deviceId: Long) {
        val user = call.getUserPrincipal()
        val (device, certificates) = deviceService.getDeviceWithCertificates(deviceId, user.id)
            ?: throw NotFoundException("Device $deviceId not found")
        call.respondView(GetDeviceCertificatesResponse(device, certificates))
    }

    @GET("/dashboard/devices/{deviceId}/certificates/{certificateId}")
    suspend fun details(call: ApplicationCall, deviceId: Long, certificateId: Long) {
        val user = call.getUserPrincipal()
        val (device, certificate) = deviceService.getDeviceWithCertificate(deviceId, certificateId, user.id)
            ?: throw NotFoundException("Certificate $certificateId not found")
        val keyPair = getKeyPair(call, certificate.certificateHash)
        call.respondView(GetDeviceCertificateDetailsResponse(device, certificate), viewData = mapOf("keyPair" to keyPair))
    }

    @GET("/dashboard/devices/{deviceId}/certificates/new")
    suspend fun new(call: ApplicationCall, deviceId: Long) {
        val user = call.getUserPrincipal()
        val device = deviceService.getDevice(deviceId, user.id)
            ?: throw NotFoundException("Device $deviceId not found")
        call.respondView(viewData = mapOf("device" to device, "statuses" to NewCertificateStatuses))
    }

    @POST("/dashboard/devices/{deviceId}/certificates/new")
    suspend fun create(call: ApplicationCall, deviceId: Long) {
        val user = call.getUserPrincipal()
        val device = deviceService.getDevice(deviceId, user.id)
            ?: throw NotFoundException("Device $deviceId not found")

        val bindResult = call.receiveModel<NewCertificateRequest>()
        if (bindResult is ValidBindResult) {
            val status = bindResult.model.status
            val (certificate, keyPair) = certificateService.createCertificate(device.id, status)
            return call.respondRedirect<AppSession>(
                url = call.routesHelper.href(
                    "Certificates",
                    "details",
                    mapOf("deviceId" to device.id, "certificateId" to certificate.id)
                ),
                tempData = mapOf(
                    "certificateHash" to certificate.certificateHash,
                    "privateKey" to keyPair.privateKeyPem,
                    "publicKey" to keyPair.publicKeyPem
                )
            )
        }
        call.respondView("/certificate/new.ftl", bindResult, viewData = mapOf("device" to device, "statuses" to NewCertificateStatuses))
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CertificatesController::class.java)
        private fun getKeyPair(call: ApplicationCall, certificateHash: String): KeyPair? {
            val hash = call.tempData["certificateHash"] ?: return null
            if (hash != certificateHash) {
                logger.warn("TempData certificateHash $hash does match request certificateHash $certificateHash")
                return null
            }
            return KeyPair(call.tempData.getValue("publicKey"), call.tempData.getValue("privateKey"))
        }
    }
}

val NewCertificateStatuses =  listOf(CertificateStatus.INACTIVE, CertificateStatus.ACTIVE)

data class NewCertificateRequest(
    @CertificateStatusValue(anyOf = [CertificateStatus.ACTIVE, CertificateStatus.INACTIVE])
    val status: CertificateStatus
)

data class GetDeviceCertificatesResponse(
    val device: Device,
    val certificates: List<Certificate>
)

data class GetDeviceCertificateDetailsResponse(
    val device: Device,
    val certificate: Certificate
) {
    private val encoder = Base64.getEncoder()

    fun base64encode(value: String): String {
        return base64encode(value, Charsets.US_ASCII)
    }

    fun base64encode(value: String, charset: Charset): String {
        return encoder.encodeToString(value.toByteArray(charset))
    }
}

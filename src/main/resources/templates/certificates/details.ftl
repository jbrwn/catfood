<#-- @ftlvariable name="model" type="dog.catfood.controllers.GetDeviceCertificateDetailsResponse" -->
<#-- @ftlvariable name="keyPair" type="dog.catfood.models.KeyPair" -->
<#-- @ftlvariable name="routes" type="dog.catfood.plugins.controllers.RoutesHelper" -->
<#import "../_dashboardLayout.ftl" as layout />
<@layout.layout>
    <nav aria-label="breadcrumb">
        <ul>
            <li><a href="${routes.href("Devices", "index")}">Devices</a></li>
            <li><a href="${routes.href("Devices", "details", { 'deviceId': model.device.id })}">${model.device.name}</a></li>
            <li><a href="${routes.href("Certificates", "index", { 'deviceId': model.device.id })}">Certificates</a></li>
            <li>${model.certificate.certificateHash?truncate(16, '...')}</li>
        </ul>
    </nav>
    <#if keyPair??>
        <article>
            <header>
                <nav>
                    <ul>
                        <li><strong>Certificate And Keys</strong></li>
                    </ul>
                </nav>
            </header>
            <p>This is the only time you can download keys for this certificate</p>
            <div class="grid">
                <div>
                    <strong>Certificate</strong>
                    <p>
                        <a href="data:text/plain;base64,${model.base64encode(model.certificate.certificatePem)}" download="${model.certificate.certificateHash}-certificate.pem.crt">Download</a>
                    </p>
                </div>
                <div></div>
            </div>
            <div class="grid">
                <div>
                    <strong>Public Key</strong>
                    <p>
                        <a href="data:text/plain;base64,${model.base64encode(keyPair.publicKeyPem)}" download="${model.certificate.certificateHash}-public.pem.key">Download</a>
                    </p>
                </div>
                <div>
                    <strong>Private Key</strong>
                    <p>
                        <a href="data:text/plain;base64,${model.base64encode(keyPair.privateKeyPem)}" download="${model.certificate.certificateHash}-private.pem.key">Download</a>
                    </p>
                </div>
            </div>
        </article>
    </#if>
    <article>
        <header>
            <nav>
                <ul>
                    <li><strong>Certificate Details</strong></li>
                </ul>
            </nav>
        </header>
        <div class="grid">
            <div>
                <strong>Certificate_hash</strong>
                <p>${model.certificate.certificateHash}</p>
            </div>
            <div>
                <strong>Status</strong>
                <p>${model.certificate.status}</p>
            </div>
        </div>
        <div class="grid">
            <div>
                <strong>Subject</strong>
                <p>${model.certificate.subject}</p>
            </div>
            <div>
                <strong>Issuer</strong>
                <p>${model.certificate.issuer}</p>
            </div>
        </div>
        <div class="grid">
            <div>
                <strong>Valid</strong>
                <p>${model.certificate.valid.format()}</p>
            </div>
            <div>
                <strong>Expires</strong>
                <p>${model.certificate.expires.format()}</p>
            </div>
        </div>
        <div class="grid">
            <div>
                <strong>Modified On</strong>
                <p>${model.certificate.modifiedOn.format()}</p>
            </div>
            <div>
                <strong>Created On</strong>
                <p>${model.certificate.createdOn.format()}</p>
            </div>
        </div>
    </article>
</@layout.layout>

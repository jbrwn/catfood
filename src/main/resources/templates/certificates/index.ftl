<#-- @ftlvariable name="model" type="dog.catfood.controllers.GetDeviceCertificatesResponse" -->
<#-- @ftlvariable name="routes" type="dog.catfood.plugins.controllers.RoutesHelper" -->
<#import "../_dashboardLayout.ftl" as layout />
<@layout.layout>
    <nav aria-label="breadcrumb">
        <ul>
            <li><a href="${routes.href("Devices", "index")}">Devices</a></li>
            <li><a href="${routes.href("Devices", "details", { 'deviceId': model.device.id })}">${model.device.name}</a></li>
            <li>Certificates</li>
        </ul>
    </nav>
    <article xmlns="http://www.w3.org/1999/html">
        <header>
            <nav>
                <ul>
                    <li><strong>Certificates</strong></li>
                </ul>
                <ul>
                    <li><a href="${routes.href('Certificates', 'new', { 'deviceId': model.device.id })}">New Certificate</a></li>
                </ul>
            </nav>
        </header>
        <figure>
            <table class="fixed">
                <thead>
                <tr>
                    <th scope="col">Certificate Hash</th>
                    <th scope="col">Status</th>
                    <th scope="col">Created On</th>
                </tr>
                </thead>
                <tbody>
                <#list model.certificates as certificate>
                    <tr>
                        <th scope="row"><a href="${routes.href('Certificates', 'details', { 'deviceId': certificate.deviceId, 'certificateId': certificate.id })}">${certificate.certificateHash}</a></th>
                        <td>${certificate.status}</td>
                        <td>${certificate.createdOn.format()}</td>
                    </tr>
                </#list>
                </tbody>
            </table>
        </figure>
    </article>
</@layout.layout>

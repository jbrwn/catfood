<#-- @ftlvariable name="routes" type="dog.catfood.plugins.controllers.RoutesHelper" -->
<#-- @ftlvariable name="modelState" type="dog.catfood.plugins.modelbinding.ModelState" -->
<#-- @ftlvariable name="device" type="dog.catfood.models.Device" -->
<#-- @ftlvariable name="statuses" type="kotlin.collections.AbstractList<dog.catfood.models.CertificateStatus>" -->
<#import "../_dashboardLayout.ftl" as layout />
<@layout.layout>
    <nav aria-label="breadcrumb">
        <ul>
            <li><a href="${routes.href("Devices", "index")}">Devices</a></li>
            <li><a href="${routes.href("Devices", "details", { 'deviceId': device.id })}">${device.name}</a></li>
            <li><a href="${routes.href("Certificates", "index", { 'deviceId': device.id })}">Certificates</a></li>
            <li>New</li>
        </ul>
    </nav>
    <article>
        <header>
            <nav>
                <ul>
                    <li><strong>New Certificate</strong></li>
                </ul>
            </nav>
        </header>
        <ul>
            <#list modelState.validationErrors() as error>
                <li>${error}</li>
            </#list>
        </ul>
        <form action="${routes.href('Certificates', 'create', { 'deviceId': device.id })}" method="post">
            <input type="hidden" name="_csrf" value="${csrfToken}"/>
            <fieldset>
                <legend>Certificate Status</legend>
                <#list statuses as status>
                    <label for="${status}">
                        <input type="radio" id="${status}" name="status" value="${status}" checked>
                        ${status}
                    </label>
                </#list>
            </fieldset>
            <input type="submit"/>
        </form>
    </article>
</@layout.layout>

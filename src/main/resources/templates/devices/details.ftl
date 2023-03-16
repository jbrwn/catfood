<#-- @ftlvariable name="model" type="dog.catfood.controllers.GetDeviceResponse" -->
<#-- @ftlvariable name="routes" type="dog.catfood.plugins.controllers.RoutesHelper" -->
<#setting number_format="0.######">
<#import "../_dashboardLayout.ftl" as layout />
<#import "../_layout.ftl" as base />
<@layout.layout>
<nav aria-label="breadcrumb">
    <ul>
        <li><a href="${routes.href("Devices", "index")}">Devices</a></li>
        <li>${model.device.name}</li>
    </ul>
</nav>
    <#if model.location??>
        <article>
            <header>
                <nav>
                    <ul>
                        <li>
                            <strong>Device Location</strong><br/>
                            <small>Last Ping: ${model.location.recordedOn.format()}</small>
                        </li>
                    </ul>
                    <ul>
                        <li>
                            <a href="${routes.href("Devices", "live", { 'deviceId': model.device.id })}">Live</a>
                        </li>
                    </ul>
                </nav>
            </header>
            <img src="https://api.mapbox.com/styles/v1/mapbox/streets-v11/static/pin-s+555555(${model.location.longitude},${model.location.latitude})/${model.location.longitude},${model.location.latitude},15,0/880x600?access_token=${base.mapboxKey}" alt="Device Location">
        </article>
    </#if>
    <article>
        <header>
            <nav>
                <ul>
                    <li><strong>Device Details</strong></li>
                </ul>
            </nav>
        </header>
        <div class="grid">
            <div>
                <strong>Device Name</strong>
                <p>${model.device.name}</p>
            </div>
            <div>
                <strong>Created On</strong>
                <p>${model.device.createdOn.format()}</p>
            </div>
        </div>
        <div class="grid">
            <div>
                <strong>Modified On</strong>
                <p>${model.device.modifiedOn.format()}</p>
            </div>
            <div>
                <strong>Certificates</strong>
                <p><a href="${routes.href("Certificates", "index", { 'deviceId': model.device.id })}">Manage Certificates</a></p>
            </div>
        </div>
    </article>
</@layout.layout>

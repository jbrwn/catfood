<#-- @ftlvariable name="routes" type="dog.catfood.plugins.controllers.RoutesHelper" -->
<#import "../_dashboardLayout.ftl" as layout />
<@layout.layout>
<article>
    <header>
        <nav>
            <ul>
                <li><strong>Devices</strong></li>
            </ul>
            <ul>
                <li><a href="${routes.href('Devices', 'new')}">New Device</a></li>
            </ul>
        </nav>
    </header>
    <table class="fixed">
        <thead>
        <tr>
            <th scope="col">Name</th>
            <th scope="col">Created On</th>
        </tr>
        </thead>
        <tbody>
        <#list model.devices as device>
        <tr>
            <th scope="row"><a href="${routes.href('Devices', 'details', { 'deviceId': device.id })}">${device.name}<a/></th>
            <td>${device.createdOn.format()}</td>
        </tr>
        </#list>
        </tbody>
    </table>
</article>
</@layout.layout>

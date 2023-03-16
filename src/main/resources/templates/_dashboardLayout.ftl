<#-- @ftlvariable name="routes" type="dog.catfood.plugins.controllers.RoutesHelper" -->
<#import "_layout.ftl" as base />
<#macro layout>
<!DOCTYPE html>
<html lang="en">
<@base.head/>
<body>
<nav class="container-fluid sticky">
    <ul>
        <li>Catfood</li>
    </ul>
    <ul>
        <li>
            <a href="${routes.href('Account', 'logout')}">Logout</a>
        </li>
    </ul>
</nav>
<main class="container dashboard">
    <aside>
        <nav>
            <ul>
                <li><a href="${routes.href('Dashboard', 'index')}">Dashboard</a></li>
                <li><a href="${routes.href('Devices', 'index')}">Devices</a></li>
            </ul>
        </nav>
    </aside>
    <div role="document">
        <#nested>
    </div>
</main>
</body>
</html>
</#macro>
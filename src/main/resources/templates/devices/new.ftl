<#-- @ftlvariable name="routes" type="dog.catfood.plugins.controllers.RoutesHelper" -->
<#-- @ftlvariable name="modelState" type="dog.catfood.plugins.modelbinding.ModelState" -->
<#import "../_dashboardLayout.ftl" as layout />
<@layout.layout>
<nav aria-label="breadcrumb">
    <ul>
        <li><a href="${routes.href("Devices", "index")}">Devices</a></li>
        <li>New</li>
    </ul>
</nav>
<article>
    <header>
        <nav>
            <ul>
                <li><strong>New Device</strong></li>
            </ul>
        </nav>
    </header>
    <ul>
        <#list modelState.validationErrors() as error>
        <li>${error}</li>
    </#list>
    </ul>
    <form action="${routes.href('Devices', 'create')}" method="post">
        <input type="hidden" name="_csrf" value="${csrfToken}"/>
        <label for="name">
            Device Name
            <input type="text" id="name" name="name" value="${(modelState.valueFor('name'))!}"/>
        </label>
        <input type="submit"/>
    </form>
</article>
</@layout.layout>

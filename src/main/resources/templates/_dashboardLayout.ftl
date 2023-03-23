<#-- @ftlvariable name="routes" type="dog.catfood.plugins.controllers.RoutesHelper" -->
<#import "_layout.ftl" as base />
<#macro layout>
<!DOCTYPE html>
<html lang="en">
<@base.head/>
<body>
<nav class="container-fluid sticky">
    <ul>
        <li>
            <a href="${routes.href('Home', 'index')}">
                <svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="0 0 512 512"><path fill="currentColor" d="M253.4 2.9C249.2 1 244.7 0 240 0s-9.2 1-13.4 2.9L38.3 82.8C16.3 92.1-.1 113.8 0 140c.5 99.2 41.3 280.7 213.6 363.2c16.7 8 36.1 8 52.8 0C438.7 420.7 479.5 239.2 480 140c.1-26.2-16.3-47.9-38.3-57.2L253.4 2.9zM144 154.4c0-5.8 4.7-10.4 10.4-10.4h.2c3.4 0 6.5 1.6 8.5 4.3l40 53.3c3 4 7.8 6.4 12.8 6.4h48c5 0 9.8-2.4 12.8-6.4l40-53.3c2-2.7 5.2-4.3 8.5-4.3h.2c5.8 0 10.4 4.7 10.4 10.4V272c0 53-43 96-96 96s-96-43-96-96V154.4zM200 288a16 16 0 1 0 0-32a16 16 0 1 0 0 32zm96-16a16 16 0 1 0-32 0a16 16 0 1 0 32 0z"/></svg>
            </a>
        </li>
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
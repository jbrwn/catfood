<#import "/_layout.ftl" as layout />
<!DOCTYPE html>
<html lang="en">
<@layout.head/>
<body>
    <nav class="container-fluid">
        <ul>
            <li>Catfood</li>
        </ul>
        <ul>
            <#if !loggedIn>
                <li><a href="${routes.href('Account', 'login')}">Log in</a></li>
                <li><a href="${routes.href('Account', 'new')}">Sign up</a></li>
            <#else>
                <li><a href="${routes.href('Dashboard', 'index')}">Dashboard</a></li>
            </#if>
        </ul>
    </nav>
    <header class="container hero">
        <hgroup>
            <h1>Catfood.dog</h1>
            <h2>A website for tracking your cat</h2>
        </hgroup>
        <p>
            <a href="${routes.href('Account', 'new')}" role="button">Sign up</a>
        </p>
    </header>
</body>
</html>
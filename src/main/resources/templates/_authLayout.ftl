<#import "/_layout.ftl" as base />
<#macro layout>
<!DOCTYPE html>
<html lang="en">
<@base.head/>
<body>
<nav class="container-fluid">
    <ul>
        <li>Catfood</li>
    </ul>
</nav>
<#nested>
</body>
</html>
</#macro>
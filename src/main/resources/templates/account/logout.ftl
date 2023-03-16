<#import "/_authLayout.ftl" as layout />
<@layout.layout>
<main class="container auth">
<article>
    <h1>Log out</h1>
    <form name="logout" action="${routes.href('Account', 'doLogout')}" method="post">
        <input type="hidden" name="_csrf" value="${csrfToken}"/>
        <input type="submit"/>
    </form>
</article>
</main>
</@layout.layout>

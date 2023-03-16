<#import "/_authLayout.ftl" as layout />
<@layout.layout>
<main class="container auth">
    <article>
        <h1>Sign in</h1>
        <ul>
            <#list modelState.validationErrors() as error>
                <li>${error}</li>
            </#list>
        </ul>
        <form action="${routes.href('Account', 'doLogin', next???then({ 'next': next }, {}))}" method="post">
            <input type="hidden" name="_csrf" value="${csrfToken}"/>
            <label for="username">
                Username
                <input type="text" id="username" name="username" value="${(modelState.valueFor('username'))!}"/>
            </label>
            <label for="password">
                Password
                <input type="password" id="password" name="password"/>
            </label>
            <input type="submit"/>
        </form>
        <a href="${routes.href('Account', 'new')}">Need an Account?</a>
    </article>
</main>
</@layout.layout>

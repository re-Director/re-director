@import org.springframework.security.web.csrf.CsrfToken

@param CsrfToken csrfToken
@param boolean authenticated
@param boolean authEnabled

<!DOCTYPE html>
<html lang="en">
    @template.fragments.header()

    <body>
        @template.fragments.nav(csrfToken = csrfToken, authenticated = authenticated, authEnabled = authEnabled)
        <main class="container" style="padding-top: 1.5em;">
            <h2>Login</h2>
            <form method="post" action="/login">
                <label for="username">Username</label>
                <input id="username" name="username" required />

                <label for="password">Password</label>
                <input id="password" name="password" type="password" required />

                <input type="hidden" name="${csrfToken.getParameterName()}" value="${csrfToken.getToken()}" />

                <button>Login</button>
            </form>
        </main>
        @template.fragments.footer()
        @template.fragments.js()
    </body>
</html>
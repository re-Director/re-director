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
            <h2>Initial Setup</h2>
            <p>Create the first administrator account to secure this installation.</p>

            <form method="post" action="/setup">
                <label for="username">Username</label>
                <input id="username" name="username" required />

                <label for="password">Password</label>
                <input id="password" name="password" type="password" required />

                <label for="confirmPassword">Confirm Password</label>
                <input id="confirmPassword" name="confirmPassword" type="password" required />

                <input type="hidden" name="${csrfToken.getParameterName()}" value="${csrfToken.getToken()}" />

                <button>Create account</button>
            </form>
        </main>
        @template.fragments.footer()
        @template.fragments.js()
    </body>
</html>
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
            <div style="text-align: center;">
                <h1>
                    🙅‍♀️ 403 - Forbidden
                </h1>
                <h6>
                    You are not allowed to see the contents of this page.
                </h6>
            </div>
        </main>
        @template.fragments.footer()
        @template.fragments.js()
    </body>

</html>
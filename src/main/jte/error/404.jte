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
                    🕵️‍♂️ 404 - Not Found
                </h1>
                <h6>
                    The page you are looking for does not exist.
                </h6>
            </div>
        </main>
        @template.fragments.footer()
        @template.fragments.js()
    </body>

</html>
@import org.springframework.security.web.csrf.CsrfToken

@param CsrfToken csrfToken
@param boolean authenticated
@param boolean authEnabled

@param Integer status
@param String error
@param String exception
@param String message

<!DOCTYPE html>
<html lang="en">
    @template.fragments.header()

    <body>
        @template.fragments.nav(csrfToken = csrfToken, authenticated = authenticated, authEnabled = authEnabled)
        <main class="container" style="padding-top: 1.5em;">
            <div style="text-align: center;">
                <h1>
                    Oops!
                </h1>
                <h6>
                    An error occurred.
                </h6>
                <p>
                    <small>
                        In case the error persists, please open an issue on GitHub.<br />
                        Make sure to include the following data and steps to reproduce.
                    </small>
                </p>
            </div>
            <div id="error-details" style="text-align: left;">
                <small>
                    Status: <code id="status">${status}</code>
                    <br/>
                    Error: <code id="error">${error}</code>
                    @if(authenticated)
                        <br/>
                        Message: <code>${message}</code>
                    @else
                        <br/>
                        Log in to see further error details.
                    @endif
                </small>
            </div>
        </main>
        @template.fragments.footer()
        @template.fragments.js()
    </body>
</html>

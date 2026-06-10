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
            No redirect found!
        </h1>
        <h6>
            For the URl you specified, there is currently no active redirect configured.
        </h6>
    </div>
</main>
@template.fragments.footer()
@template.fragments.js()
</body>

</html>
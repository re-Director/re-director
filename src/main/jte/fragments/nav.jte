@import org.springframework.security.web.csrf.CsrfToken

@param boolean authenticated
@param boolean authEnabled
@param CsrfToken csrfToken

<header class="container">
    <nav>
        <ul>
            <li>
                <a href="/">
                    <img src="/img/re-director.svg" alt="Logo" class="logo"/>
                </a>
            </li>
            <li style="margin-right: 1em">
                <a href="/">
                    <h1>re:Director</h1>
                </a>
            </li>
            @if(!authEnabled || authenticated)
                <li>
                    <a href="/redirects">
                        Redirects
                    </a>
                </li>
            @endif
        </ul>
        <ul>
            @if(!authEnabled || authenticated)
                <li>
                    <a href="/test-redirect">
                        Test-Tool
                    </a>
                </li>
                <li>
                    <a href="/settings">
                        <i title="Settings" class="fa-solid fa-gear"></i>
                    </a>
                </li>
            @endif
            @if(authEnabled)
                <li>
                    @if(authenticated)
                        <form method="post" action="/logout">
                            <input type="hidden" name="${csrfToken.getParameterName()}" value="${csrfToken.getToken()}" />
                            <button type="submit">Logout</button>
                        </form>
                    @else
                        <a href="/login">
                            Login
                        </a>
                    @endif
                </li>
            @endif
        </ul>
    </nav>
    <hr>
</header>
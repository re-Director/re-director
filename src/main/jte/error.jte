@param Integer status
@param String error
@param String exception
@param String message

<!DOCTYPE html>
<html lang="en">
    @template.fragments.header()

    <body>
        @template.fragments.nav()
        <main class="container" style="padding-top: 1.5em;">
            <div style="text-align: center;">
                <h1>
                    Oops!
                </h1>
                <h6>
                    An error occured.
                </h6>
                <p>
                    <small>
                        In case the error persists, please open an issue on GitHub.<br />
                        Make sure to include the following data and steps to reproduce.
                    </small>
                </p>
            </div>
            <div style="text-align: left;">
                <small>
                    Status: <code>${status}</code>
                    <br/>
                    Error: <code>${error}</code>
                    <br/>
                    Exception: <code>${exception}</code>
                    <br/>
                    Message: <code>${message}</code>
                </small>
            </div>
        </main>
        @template.fragments.footer()
        @template.fragments.js()
    </body>
</html>

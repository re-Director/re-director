<!DOCTYPE html>
<html lang="en">

@template.fragments.header()

    <body>
        @template.fragments.nav()
        <main class="container" style="padding-top: 1.5em;">
            <div style="text-align: center;">
                <h1>
                    Settings
                </h1>
                <hr style="width: 90%; margin-left: auto; margin-right: auto; margin-top: 0.8em; margin-bottom: 0.8em">
                <div class="grid">
                    <div>
                        <h6>Clear Caches</h6>
                    </div>
                </div>
                <div class="grid">
                    <div>
                        <form method="POST" style="display: inline-block;" action="${"/settings/cache/redirects/clear"}">
                            <button type="submit">
                                <i class="fa-solid fa-broom"></i> Clear Redirect Cache
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </main>
        @template.fragments.footer()
        @template.fragments.js()
    </body>

</html>
@import de.jensknipper.re_director.test_redirects.TestRedirectResult
@import gg.jte.support.ForSupport
@import org.springframework.validation.BindingResult
@import java.util.List

@param String protocol
@param String url
@param TestRedirectResult result

@param BindingResult bindingResult

<!DOCTYPE html>
<html lang="en">
    @template.fragments.header()

    <body>
        @template.fragments.nav()
        <main class="container" style="padding-top: 1.5em;">
            <div style="text-align: center;">
                <h1>
                    Test Redirects
                </h1>
                <p>
                    <small>
                        Want to see if your configured redirect works? Just enter a URL into the tester below to find out!
                    </small>
                </p>
                <form action="/test-redirect"  method="get">
                    <fieldset role="group">
                        <input id="url-input-test-redirect" name="url" placeholder="example.com" value="${url}"/>
                        <button>
                            Test
                        </button>
                    </fieldset>
                    @template.fragments.fieldErrors(bindingResult = bindingResult)
                </form>

                @for(var entryLoop : ForSupport.of(result.steps()))
                    !{var step = entryLoop.get();}
                    !{var index = entryLoop.getIndex();}
                <article>
                    <div class="grid">
                        <div>
                            <b>${step.from()}</b>
                        </div>
                        <div>
                            @if(step.httpStatusCode() >= 200 && step.httpStatusCode() < 300)
                                <i class="fa-solid fa-circle-check pico-color-green-250"></i>
                            @elseif(step.httpStatusCode() >= 300 && step.httpStatusCode() <400)
                                <i class="fa-solid fa-circle-arrow-right pico-color-amber-250"></i>
                            @else
                                <i class="fa-solid fa-circle-xmark pico-color-red-250"></i>
                            @endif
                            ${step.httpStatusCode()}
                        </div>
                        <div>
                            ${step.to()}
                        </div>
                    </div>
                    <footer>
                        <details name="details-${index}">
                            <summary>
                                <small>
                                    <i class="fa-solid fa-circle-info"></i> Details
                                </small>
                            </summary>
                                @for(var attribute : step.attributes().entrySet())
                                    <hr style="width: 90%; margin-left: auto; margin-right: auto; margin-top: 0.4em; margin-bottom: 0.4em">
                                    <div class="grid">
                                        <div>
                                            <small>
                                                ${attribute.getKey()}:
                                            </small>
                                        </div>
                                        @for(var value : attribute.getValue())
                                            <div>
                                                <small>
                                                    ${value}
                                                </small>
                                            </div>
                                        @endfor
                                    </div>
                                @endfor
                        </details>
                    </footer>
                </article>
                @endfor
                @if(result.hasError())
                    <div>
                        <small class="pico-color-red-300">
                        @if(result.code().equals(TestRedirectResult.ExitCode.MAX_REDIRECTS))
                            Too many redirects.
                        @elseif(result.code().equals(TestRedirectResult.ExitCode.LOOP_DETECTED))
                            Loop detected.
                        @elseif(List.of(TestRedirectResult.ExitCode.SSRF_DETECTED,
                                        TestRedirectResult.ExitCode.URI_INVALID,
                                        TestRedirectResult.ExitCode.SSRF_DETECTED)
                                        .contains(result.code()))
                            URL invalid.
                        @else
                            An Error occurred.
                            <span style="display: none">
                                Returned code: ${result.code()}
                            </span>
                        @endif
                            
                        </small>
                    </div>
                @endif
            </div>
        </main>
        @template.fragments.footer()
        @template.fragments.js()
    </body>
</html>
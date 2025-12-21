@import de.jensknipper.re_director.db.entity.Status
@import de.jensknipper.re_director.web.controller.dto.CreateRedirectRequest
@import gg.jte.support.ForSupport

@import java.util.List
@import de.jensknipper.re_director.web.controller.dto.RedirectResponse
@import org.springframework.validation.BindingResult

@param String status
@param String search
@param Integer code

@param boolean isCreatePage = false
@param Integer editPageId

@param BindingResult bindingResult
@param CreateRedirectRequest createRedirectRequest
@param List<RedirectResponse> redirects

!{String urlParams = "";}
@if(status != null)
    !{urlParams += "?status=" + status;}
@endif
@if(search != null)
    @if(urlParams.isEmpty())
        !{urlParams += "?";}
    @else
        !{urlParams += "&";}
    @endif
    !{urlParams += "&search=" + search;}
@endif
@if(code != null)
    @if(urlParams.isEmpty())
        !{urlParams += "?";}
    @else
        !{urlParams += "&";}
    @endif
    !{urlParams += "&code=" + code;}
@endif


<!DOCTYPE html>
<html lang="en">
    @template.fragments.header()

    <body>
        @template.fragments.nav()
        <main class="container" style="padding-top: 1.5em;">
            <div style="text-align: center;">
                <h1>
                    Your Redirects
                </h1>
                <div class="grid">
                    <div>
                        <form action="/redirects"  method="get">
                            <fieldset role="group">
                                <select id="status-input-filter" name="status" aria-label="Status">
                                    <option value="" selected="${status == null}">All Status</option>
                                    <option value="ACTIVE" selected="${"ACTIVE".equals(status)}">Active</option>
                                    <option value="INACTIVE" selected="${"INACTIVE".equals(status)}">Inactive</option>
                                </select>
                                <input id="search-input-filter" name="search" placeholder="Filter" autocomplete="search" value="${search}"/>
                                <select id="status-code-input-filter" name="code" aria-label="Select a Redirect HTTP Status Code">
                                    <option value="" selected="${code == null}">Any HTTP Status Code</option>
                                    <option value="301" selected="${Integer.valueOf(301).equals(code)}">301: Moved Permanently</option>
                                    <option value="302" selected="${Integer.valueOf(302).equals(code)}">302: Found</option>
                                    <option value="307" selected="${Integer.valueOf(307).equals(code)}">307: Temporary Redirect</option>
                                    <option value="308" selected="${Integer.valueOf(308).equals(code)}">308: Permanent Redirect</option>
                                </select>
                                <button id="button-filter">
                                    <i class="fa-solid fa-magnifying-glass"></i>
                                </button>
                            </fieldset>
                        </form>
                    </div>
                    <div style="text-align: right;">
                        <button id="create-button" data-target="modal-create-redirect" onclick="toggleModal(event)">
                            <i class="fa-solid fa-plus"></i> Create
                        </button>
                    </div>
                </div>
                <div class="grid">
                        <div>
                            <b>Source</b>
                        </div>
                        <div>
                            <b>Target</b>
                        </div>
                        <div>
                            <b>HTTP Status Code</b>
                        </div>
                        <div>
                            <b>Status</b>
                        </div>
                        <div>
                            <b>Actions</b>
                        </div>
                </div>
                <hr style="height: 1px; background-color: black; border: none;">
                    @for(var entryLoop : ForSupport.of(redirects))
                        !{var redirect = entryLoop.get();}
                        !{var index = entryLoop.getIndex();}

                        <div class="grid" id="${"table-element-" + index}">
                            <div id="source">
                                ${redirect.source()}
                            </div>
                            <div id="target">
                                <a href="${redirect.target()}">${redirect.target()}</a>
                            </div>
                            <div id="httpStatusCode">
                                @if(redirect.httpStatusCode() == 301)
                                    <span title="Moved Permanently">301</span>
                                @elseif(redirect.httpStatusCode() == 302)
                                    <span title="Found">302</span>
                                @elseif(redirect.httpStatusCode() == 307)
                                    <span title="Temporary Redirect">307</span>
                                @elseif(redirect.httpStatusCode() == 308)
                                    <span title="Permanent Redirect">308</span>
                                @else
                                    Unknown Redirect
                                @endif
                            </div>
                            <div id="status">
                                @if(Status.ACTIVE.equals(redirect.status()))
                                    <i class="fa-solid fa-circle-check pico-color-green-200" title="Active"></i>
                                @elseif(Status.INACTIVE.equals(redirect.status()))
                                    <i class="fa-solid fa-square-xmark pico-color-red-400" title="Inactive"></i>
                                @else
                                    Unknown Status
                                @endif
                            </div>
                            <div>
                                <span id="edit-button" data-target="${"modal-update-redirect-" + redirect.id()}" onclick="toggleModal(event)">
                                    <i class="fa-solid fa-pen pico-color-slate-300" style="margin-right: 0.5em;" title="Edit"></i>
                                </span>
                                @if(Status.ACTIVE.equals(redirect.status()))
                                    <form method="POST" style="display: inline-block;"
                                          action="${"/redirects/" + redirect.id() + "/status/INACTIVE" + urlParams}">
                                        <button id="deactivate-button" type="submit" style="background: none; border: none; padding: 0; cursor: pointer;">
                                            <i class="fa-solid fa-pause pico-color-yellow-300" style="margin-right: 0.5em;" title="Pause"></i>
                                        </button>
                                    </form>
                                @elseif(Status.INACTIVE.equals(redirect.status()))
                                    <form method="POST" style="display: inline-block;"
                                          action="${"/redirects/" + redirect.id() + "/status/ACTIVE" + urlParams}">
                                        <button id="activate-button" type="submit" style="background: none; border: none; padding: 0; cursor: pointer;">
                                            <i class="fa-solid fa-play pico-color-jade-200" style="margin-right: 0.5em;" title="Start"></i>
                                        </button>
                                    </form>
                                @endif
                                <form method="POST" style="display: inline-block;"
                                      action="${"/redirects/" + redirect.id() + "/delete" + urlParams}">
                                    <button id="delete-button" type="submit" style="background: none; border: none; padding: 0; cursor: pointer;">
                                        <i class="fa-solid fa-trash pico-color-red-300" title="Delete"></i>
                                    </button>
                                </form>
                            </div>
                        </div>
                        @if(index + 1 == redirects.size())
                            <hr style="height: 1px; background-color: black; border: none;">
                        @else
                            <hr style="width: 90%; margin-left: auto; margin-right: auto; margin-top: 0.8em; margin-bottom: 0.8em">
                        @endif
                    @else
                        <div class="grid">
                            No Redirects found
                        </div>
                    @endfor
                    @if(!redirects.isEmpty())
                        <div class="grid">
                            <div style="text-align: left;">
                                Showing <b>${"1-" + redirects.size()}</b> of <b>${redirects.size()}</b>
                            </div>
                        </div>
                    @endif
                </div>
            <dialog id="modal-create-redirect"
                    open="${isCreatePage}"
                    data-url-addition="/create">
                <article>
                    <header>
                        <button aria-label="Close" rel="prev" data-target="modal-create-redirect" onclick="toggleModal(event)"></button>
                        <h3>Create Redirect</h3>
                    </header>
                    <form action="${"/redirects/create" + urlParams}" method="post">
                        <label for="source-input-create-modal">Source:</label>
                        <input id="source-input-create-modal" type="text" name="source" placeholder="Source" value="${createRedirectRequest.getSource()}">
                        <label for="target-input-create-modal">Target</label>
                        <input id="target-input-create-modal" type="text" name="target" placeholder="Target" value="${createRedirectRequest.getTarget()}" required minlength="1">
                        <label for="status-code-input-create-modal">Status Code:</label>
                        <select id="status-code-input-create-modal" name="httpStatusCode" aria-label="Select a Redirect HTTP Status Code" required>
                            <option disabled value="404">Select a Redirect HTTP Status Code</option>
                            <option selected value="301">301: Moved Permanently</option>
                            <option value="302">302: Found</option>
                            <option value="307">307: Temporary Redirect</option>
                            <option value="308">308: Permanent Redirect</option>
                        </select>

                        @template.fragments.fieldErrors(bindingResult = bindingResult)

                        <footer>
                            <button role="button" class="secondary" data-target="modal-create-redirect" onclick="toggleModal(event)">
                                Cancel
                            </button>
                            <button autofocus id="confirm-button-create-modal">
                                Confirm
                            </button>
                        </footer>
                    </form>
                </article>
            </dialog>

            @for(var redirect : redirects)
            <dialog id="${"modal-update-redirect-" + redirect.id()}"
                    open="${editPageId != null && editPageId == redirect.id()}"
                    data-url-addition="${"/" + redirect.id() + "/edit"}">
                <article>
                    <header>
                        <button aria-label="Close" rel="prev" data-target="${"modal-update-redirect-" + redirect.id()}" onclick="toggleModal(event)"></button>
                        <h3>Update Redirect</h3>
                    </header>
                    <form action="${"/redirects/" + redirect.id() + "/edit" + urlParams}" method="post">
                        <label for="source-input-edit-modal">Source:</label>
                        <input id="source-input-edit-modal" type="text" name="source" placeholder="Source" value="${redirect.source()}" required minlength="1">
                        <label for="target-input-edit-modal">Target</label>
                        <input id="target-input-edit-modal" type="text" name="target" placeholder="Target" value="${redirect.target()}" required minlength="1">
                        <label for="status-code-input-edit-modal">Status Code:</label>
                        <select id="status-code-input-edit-modal" name="httpStatusCode" aria-label="Select a Redirect HTTP Status Code" required>
                            <option value="301" selected="${redirect.httpStatusCode() == 301}">301: Moved Permanently</option>
                            <option value="302" selected="${redirect.httpStatusCode() == 302}">302: Found</option>
                            <option value="307" selected="${redirect.httpStatusCode() == 307}">307: Temporary Redirect</option>
                            <option value="308" selected="${redirect.httpStatusCode() == 308}">308: Permanent Redirect</option>
                        </select>

                        @template.fragments.fieldErrors(bindingResult = bindingResult)

                        <footer>
                            <button role="button" class="secondary" data-target="${"modal-update-redirect-" + redirect.id()}" onclick="toggleModal(event)">
                                Cancel
                            </button>
                            <button autofocus id="confirm-button-edit-modal">
                                Confirm
                            </button>
                        </footer>
                    </form>
                </article>
            </dialog>
            @endfor

            @template.fragments.footer()
            @template.fragments.js()
        </main>
    </body>
</html>
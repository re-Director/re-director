@import de.jensknipper.re_director.manage_redirects.PageContext
@import org.springframework.data.domain.Page
@import de.jensknipper.re_director.manage_redirects.dto.RedirectResponse

@param String status
@param String search
@param Integer code
@param String sort
@param String direction
@param PageContext pageContext

@param Page<RedirectResponse> redirects


@if(!redirects.isEmpty())
    <div class="grid text-center" style="margin-top:2em; align-items: center;">
        <div>
            Showing elements <b>${(redirects.getSize() * redirects.getNumber() + 1) + "-" + (redirects.getSize() * redirects.getNumber() + redirects.getNumberOfElements())}</b> of <b>${redirects.getTotalElements()}</b>
        </div>
        <div>
            <form action="/redirects" method="get">
                <select id="page-size-input-filter" name="size" aria-label="Page Size" onchange="this.form.requestSubmit()">
                    @if(redirects.getSize() != 10 && redirects.getSize() != 20 && redirects.getSize() != 50)
                        <option value="${redirects.getSize()}" selected="selected">${redirects.getSize()} per page</option>
                    @endif
                    <option value="10" selected="${redirects.getSize() == 10}">10 per page</option>
                    <option value="20" selected="${redirects.getSize() == 20}">20 per page</option>
                    <option value="50" selected="${redirects.getSize() == 50}">50 per page</option>
                </select>
                @if(status != null && !status.isBlank())
                    <input type="hidden" name="status" value="${status}">
                @endif
                @if(search != null && !search.isBlank())
                    <input type="hidden" name="search" value="${search}">
                @endif
                @if(code != null)
                    <input type="hidden" name="code" value="${code}">
                @endif
                @if(sort != null && !sort.isBlank())
                    <input type="hidden" name="sort" value="${sort}">
                @endif
                @if(direction != null && !direction.isBlank())
                    <input type="hidden" name="direction" value="${direction}">
                @endif
            </form>
        </div>
        <div>
            @if(redirects.hasPrevious())
                <a href="${pageContext.withPage(0).toParams()}"><i class="fa-solid fa-angles-left" title="first"></i></a>
                <a href="${pageContext.withPage(pageContext.page()-1).toParams()}"><i class="fa-solid fa-angle-left" title="previous"></i></a>
            @endif

            <span>Page <b>${redirects.getNumber() + 1}</b> of <b>${redirects.getTotalPages()}</b></span>

            @if(redirects.hasNext())
                <a href="${pageContext.withPage(pageContext.page()+1).toParams()}"><i class="fa-solid fa-angle-right" title="next"></i></a>
                <a href="${pageContext.withPage(redirects.getTotalPages() - 1).toParams()}"><i class="fa-solid fa-angles-right" title="last"></i></a>
            @endif
        </div>
    </div>
@endif
@import org.springframework.validation.BindingResult

@param BindingResult bindingResult

@if(bindingResult != null && bindingResult.hasErrors())
    <div>
        <small class="pico-color-red-300">
            Please fix the following errors:
        </small>
        <ul>
            @for(var err: bindingResult.getFieldErrors())
                <li>
                    <small class="pico-color-red-300">
                        <strong>${err.getField()}</strong>: ${err.getDefaultMessage()}
                    </small>
                </li>
            @endfor
        </ul>
    </div>
@endif
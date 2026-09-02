package de.jensknipper.redirector.test_redirects;

import de.jensknipper.redirector.common.validation.IsUrl;
import jakarta.annotation.Nullable;

public record TestRedirectRequest(@IsUrl @Nullable String url) {}

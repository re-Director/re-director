package de.jensknipper.re_director.test_redirects;

import de.jensknipper.re_director.common.validation.IsUrl;
import jakarta.annotation.Nullable;

public record TestRedirectRequest(@IsUrl @Nullable String url) {}

package de.jensknipper.redirector.common;

import de.jensknipper.redirector.common.db.RedirectHttpStatusCode;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RedirectStatusConverter
    implements Converter<String, @Nullable RedirectHttpStatusCode> {

  @Nullable
  @Override
  public RedirectHttpStatusCode convert(String source) {
    return RedirectHttpStatusCode.fromCode(source);
  }
}

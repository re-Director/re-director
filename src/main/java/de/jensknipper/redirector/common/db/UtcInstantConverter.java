package de.jensknipper.redirector.common.db;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.jooq.Converter;
import org.jspecify.annotations.Nullable;

/**
 * Converts between the naive {@link LocalDateTime} SQLite stores and {@link Instant}, treating the
 * stored value as UTC rather than jOOQ's default of the JVM's time zone.
 */
public class UtcInstantConverter implements Converter<LocalDateTime, Instant> {

  @Override
  public @Nullable Instant from(@Nullable LocalDateTime databaseObject) {
    return databaseObject == null ? null : databaseObject.toInstant(ZoneOffset.UTC);
  }

  @Override
  public @Nullable LocalDateTime to(@Nullable Instant userObject) {
    return userObject == null ? null : LocalDateTime.ofInstant(userObject, ZoneOffset.UTC);
  }

  @Override
  public Class<LocalDateTime> fromType() {
    return LocalDateTime.class;
  }

  @Override
  public Class<Instant> toType() {
    return Instant.class;
  }
}

package de.jensknipper.redirector.auth.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.cache.Cache;

public record RateLimitCache(Cache cache, Bandwidth limit) {

  boolean tryConsume(String key) {
    Bucket bucket = cache.get(hash(key), () -> Bucket.builder().addLimit(limit).build());
    return bucket.tryConsume(1);
  }

  void clear() {
    cache.clear();
  }

  private static String hash(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}

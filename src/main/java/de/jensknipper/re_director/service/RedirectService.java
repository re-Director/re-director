package de.jensknipper.re_director.service;

import de.jensknipper.re_director.db.RedirectRepository;
import de.jensknipper.re_director.db.entity.Redirect;
import de.jensknipper.re_director.db.entity.RedirectHttpStatusCode;
import de.jensknipper.re_director.db.entity.RedirectInformation;
import de.jensknipper.re_director.db.entity.Status;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class RedirectService {
  private final RedirectRepository redirectRepository;
  private final CacheManager cacheManager;

  public RedirectService(RedirectRepository redirectRepository, CacheManager cacheManager) {
    this.redirectRepository = redirectRepository;
    this.cacheManager = cacheManager;
  }

  public boolean redirectAlreadyExists(String source) {
    return redirectRepository.redirectAlreadyExists(source);
  }

  public boolean redirectAlreadyExists(String source, int id) {
    return redirectRepository.redirectAlreadyExists(source, id);
  }

  @Nullable
  public Redirect findById(int id) {
    return redirectRepository.findById(id);
  }

  @Nullable
  @Cacheable(cacheNames = {"redirects"})
  public RedirectInformation findRedirectInformationBySource(String source) {
    return redirectRepository.findRedirectInformationBySource(source);
  }

  public List<Redirect> findAllFiltered(
      @Nullable String search,
      @Nullable Status status,
      @Nullable RedirectHttpStatusCode httpStatusCodeFilter) {
    return redirectRepository.findAllFiltered(search, status, httpStatusCodeFilter);
  }

  public void update(
      int id,
      String source,
      String target,
      boolean pathForwarding,
      RedirectHttpStatusCode statusCode) {
    evictFromCacheWithId(id);
    redirectRepository.update(id, source, target, pathForwarding, statusCode);
  }

  public void updateStatus(int id, Status status) {
    if (Status.INACTIVE.equals(status)) {
      evictFromCacheWithId(id);
    }
    redirectRepository.updateStatus(id, status);
  }

  public void delete(int id) {
    evictFromCacheWithId(id);
    redirectRepository.delete(id);
  }

  public void create(
      String source, String target, boolean pathForwarding, RedirectHttpStatusCode statusCode) {
    redirectRepository.create(source, target, Status.ACTIVE, pathForwarding, statusCode);
  }

  private void evictFromCacheWithId(int id) {
    Redirect redirect = findById(id);
    if (redirect != null) {
      Cache cache = cacheManager.getCache("redirects");
      if (cache != null) {
        cache.evict(redirect.source());
      }
    }
  }

  @CacheEvict(
      cacheNames = {"redirects"},
      allEntries = true)
  public void clearCache() {
    // Eviction handled by annotation
  }
}

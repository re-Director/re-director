package de.jensknipper.re_director.manage_redirects;

import de.jensknipper.re_director.common.db.RedirectHttpStatusCode;
import de.jensknipper.re_director.common.db.Status;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ManageRedirectsService {
  private final ManageRedirectsRepository manageRedirectsRepository;
  private final CacheManager cacheManager;

  public ManageRedirectsService(
      ManageRedirectsRepository manageRedirectsRepository, CacheManager cacheManager) {
    this.manageRedirectsRepository = manageRedirectsRepository;
    this.cacheManager = cacheManager;
  }

  public boolean redirectAlreadyExists(String source) {
    return manageRedirectsRepository.redirectAlreadyExists(source);
  }

  public boolean redirectAlreadyExists(String source, int id) {
    return manageRedirectsRepository.redirectAlreadyExists(source, id);
  }

  @Nullable
  public Redirect findById(int id) {
    return manageRedirectsRepository.findById(id);
  }

  public Page<Redirect> findAllFiltered(
      @Nullable String search,
      @Nullable Status status,
      @Nullable RedirectHttpStatusCode httpStatusCodeFilter,
      Pageable pageable) {
    return manageRedirectsRepository.findAllFiltered(
        search, status, httpStatusCodeFilter, pageable);
  }

  public void update(
      int id,
      String source,
      String target,
      boolean pathForwarding,
      boolean queryForwarding,
      RedirectHttpStatusCode statusCode) {
    evictFromCacheWithId(id);
    manageRedirectsRepository.update(
        id,
        source.toLowerCase(),
        target.toLowerCase(),
        pathForwarding,
        queryForwarding,
        statusCode);
  }

  public void updateStatus(int id, Status status) {
    if (Status.INACTIVE.equals(status)) {
      evictFromCacheWithId(id);
    }
    manageRedirectsRepository.updateStatus(id, status);
  }

  public void delete(int id) {
    evictFromCacheWithId(id);
    manageRedirectsRepository.delete(id);
  }

  public void create(
      String source,
      String target,
      boolean pathForwarding,
      boolean queryForwarding,
      RedirectHttpStatusCode statusCode) {
    manageRedirectsRepository.create(
        source.toLowerCase(),
        target.toLowerCase(),
        Status.ACTIVE,
        pathForwarding,
        queryForwarding,
        statusCode);
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

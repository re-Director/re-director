package de.jensknipper.re_director.service;

import de.jensknipper.re_director.db.RedirectRepository;
import de.jensknipper.re_director.db.entity.Redirect;
import de.jensknipper.re_director.db.entity.RedirectHttpStatusCode;
import de.jensknipper.re_director.db.entity.RedirectInformation;
import de.jensknipper.re_director.db.entity.Status;
import jakarta.annotation.Nullable;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class RedirectService {
  private final RedirectRepository redirectRepository;

  public RedirectService(RedirectRepository redirectRepository) {
    this.redirectRepository = redirectRepository;
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

  public void update(int id, String source, String target, RedirectHttpStatusCode statusCode) {
    evictFromCacheWithId(id);
    redirectRepository.update(id, source, target, statusCode);
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

  public void create(String source, String target, RedirectHttpStatusCode statusCode) {
    redirectRepository.create(source, target, Status.ACTIVE, statusCode);
  }

  private void evictFromCacheWithId(int id) {
    Redirect redirect = findById(id);
    if (redirect != null) {
      evictFromCache(redirect.source());
    }
  }

  @CacheEvict(
      cacheNames = {"redirects"},
      key = "#source")
  public void evictFromCache(String source) {
    // Eviction handled by annotation
  }

  @CacheEvict(
      cacheNames = {"redirects"},
      allEntries = true)
  public void clearCache() {
    // Eviction handled by annotation
  }
}

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

  @Nullable
  @Cacheable(cacheNames = {"redirects"})
  public RedirectInformation findRedirectInformationBySource(String source) {
    return redirectRepository.findRedirectInformationBySource(source);
  }

  public List<Redirect> findAllFiltered(@Nullable String search, @Nullable Status status) {
    return redirectRepository.findAllFiltered(search, status);
  }

  @CacheEvict(
      cacheNames = {"redirects"},
      key = "#id")
  public void update(int id, String source, String target, RedirectHttpStatusCode statusCode) {
    redirectRepository.update(id, source, target, statusCode);
  }

  public void updateStatus(int id, Status status) {
    redirectRepository.updateStatus(id, status);
  }

  @CacheEvict(
      cacheNames = {"redirects"},
      key = "#id")
  public void delete(int id) {
    redirectRepository.delete(id);
  }

  public void create(String source, String target, RedirectHttpStatusCode statusCode) {
    redirectRepository.create(source, target, Status.ACTIVE, statusCode);
  }

  @CacheEvict(
      cacheNames = {"redirects"},
      allEntries = true)
  public void clearCache() {
    // nothing to do
  }
}

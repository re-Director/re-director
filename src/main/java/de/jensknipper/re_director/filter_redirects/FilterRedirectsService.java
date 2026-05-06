package de.jensknipper.re_director.filter_redirects;

import org.jspecify.annotations.Nullable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class FilterRedirectsService {

  private final FilterRedirectsRepository filterRedirectsRepository;

  public FilterRedirectsService(FilterRedirectsRepository filterRedirectsRepository) {
    this.filterRedirectsRepository = filterRedirectsRepository;
  }

  @Nullable
  @Cacheable(cacheNames = {"redirects"})
  public RedirectInformation findRedirectInformationBySource(String source) {
    return filterRedirectsRepository.findRedirectInformationBySource(source);
  }
}

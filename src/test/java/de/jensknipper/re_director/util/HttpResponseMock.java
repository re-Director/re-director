package de.jensknipper.re_director.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.net.ssl.SSLSession;

public class HttpResponseMock implements HttpResponse<Void> {
  private final int statusCode;
  private final HttpHeaders headers;

  public HttpResponseMock(int statusCode, Map<String, List<String>> headers) {
    this.statusCode = statusCode;
    this.headers = HttpHeaders.of(headers, (_, _) -> true);
  }

  @Override
  public int statusCode() {
    return statusCode;
  }

  @Override
  public HttpRequest request() {
    return null;
  }

  @Override
  public Optional<HttpResponse<Void>> previousResponse() {
    return Optional.empty();
  }

  @Override
  public HttpHeaders headers() {
    return headers;
  }

  @Override
  public Void body() {
    return null;
  }

  @Override
  public Optional<SSLSession> sslSession() {
    return Optional.empty();
  }

  @Override
  public URI uri() {
    return null;
  }

  @Override
  public HttpClient.Version version() {
    return null;
  }
}

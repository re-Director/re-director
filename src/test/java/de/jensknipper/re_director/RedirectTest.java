package de.jensknipper.re_director;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.jensknipper.re_director.database.tables.Redirects.REDIRECTS;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RedirectTest {

    public static final String requestUrl = "request.com";
    public static final String targetUrl = "http://target.com";

    @Autowired
    private DSLContext dsl;

    @LocalServerPort
    private int port;

    @BeforeEach
    void cleanup() {
        dsl
                .deleteFrom(REDIRECTS)
                .execute();
    }

    @Test
    void testRedirectNotPresent() throws IOException {
        // given
        OkHttpClient client = createHttpClientWithCustomDns(requestUrl)
                .followRedirects(false)
                .build();
        Request request = new Request.Builder()
                .url("http://" + requestUrl + ":" + port)
                .build();

        // when
        Response response = client.newCall(request).execute();

        // then
        assertThat(response.code()).isIn(200);
        assertThat(response.header("Location")).isEqualTo(null);

        response.close();
    }

    @Test
    void testRedirect() throws IOException {
        // given
        insertRedirect(requestUrl, targetUrl);
        OkHttpClient client = createHttpClientWithCustomDns(requestUrl)
                .followRedirects(false)
                .build();
        Request request = new Request.Builder()
                .url("http://" + requestUrl + ":" + port)
                .build();

        // when
        Response response = client.newCall(request).execute();

        // then
        assertThat(response.code()).isIn(300, 301, 302, 303, 307, 308);
        assertThat(response.header("Location")).isEqualTo(targetUrl);

        response.close();
    }


    private void insertRedirect(String source, String target) {
        dsl
                .insertInto(REDIRECTS)
                .columns(REDIRECTS.SOURCE, REDIRECTS.TARGET)
                .values(source, target)
                .execute();
    }

    private OkHttpClient.Builder createHttpClientWithCustomDns(String... requestUrls) {
        Map<String, String> customDnsMap =  Arrays.stream(requestUrls).collect(Collectors.toMap(s -> s, s -> "localhost"));

        Dns customDns = hostname -> {
            if (customDnsMap.containsKey(hostname)) {
                try {
                    return List.of(InetAddress.getByName(customDnsMap.get(hostname)));
                } catch (UnknownHostException e) {
                    throw new RuntimeException("Failed to resolve " + hostname, e);
                }
            }
            return Dns.SYSTEM.lookup(hostname);
        };

        return new OkHttpClient.Builder()
                .dns(customDns);
    }
}

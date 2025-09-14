package ai.summary.transactions.core.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.BasicAuthCache;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;

import ai.summary.transactions.core.config.OpenSearchConfig;

@Factory
@RequiredArgsConstructor
public class OpenSearchClientFactory {

    private final OpenSearchConfig openSearchConfig;

    @Bean
    @Singleton
    public OpenSearchClient openSearchClient() {
        // Configure Jackson with Java 8 time support
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Create JacksonJsonpMapper with configured ObjectMapper
        var jsonpMapper = new JacksonJsonpMapper(objectMapper);

        var httpHost = new HttpHost(openSearchConfig.getScheme(), openSearchConfig.getHost(),
                openSearchConfig.getPort());

        var transportBuilder = ApacheHttpClient5TransportBuilder.builder(httpHost)
                .setMapper(jsonpMapper);

        // Add authentication if username and password are configured
        var username = openSearchConfig.getUsername();
        var password = openSearchConfig.getPassword();

        // Configure authentication if credentials are provided
        if (username != null && password != null && !username.isEmpty() && !password.isEmpty()) {
            // Create credentials provider
            var credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    new AuthScope(httpHost),
                    new UsernamePasswordCredentials(username, password.toCharArray()));

            // Create auth cache
            var authCache = new BasicAuthCache();
            authCache.put(httpHost, new BasicScheme());

            // Create HTTP context
            var context = HttpClientContext.create();
            context.setAuthCache(authCache);
            context.setCredentialsProvider(credentialsProvider);

            transportBuilder.setHttpClientConfigCallback(
                    httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }

        var transport = transportBuilder.build();
        return new OpenSearchClient(transport);
    }
}

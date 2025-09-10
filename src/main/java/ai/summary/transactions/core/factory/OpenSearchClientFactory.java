package ai.summary.transactions.core.factory;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.HttpHost;
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
        var httpHost = new HttpHost(openSearchConfig.getScheme(), openSearchConfig.getHost(),
                openSearchConfig.getPort());
        var transport = ApacheHttpClient5TransportBuilder.builder(httpHost).build();

        return new OpenSearchClient(transport);
    }
}

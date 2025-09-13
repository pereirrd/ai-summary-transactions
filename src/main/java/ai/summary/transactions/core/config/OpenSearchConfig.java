package ai.summary.transactions.core.config;

import io.micronaut.context.annotation.Value;

import jakarta.inject.Singleton;
import lombok.Getter;

@Getter
@Singleton
public class OpenSearchConfig {

    @Value("${opensearch.host:localhost}")
    private String host;

    @Value("${opensearch.port:9200}")
    private int port;

    @Value("${opensearch.scheme:http}")
    private String scheme;

    @Value("${opensearch.index.transactions:transactions}")
    private String transactionsIndex;

    @Value("${opensearch.username:admin}")
    private String username;

    @Value("${opensearch.password:}")
    private String password;

}

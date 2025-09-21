package ai.summary.transactions.infrastructure;

import ai.summary.transactions.core.config.OpenSearchConfig;
import ai.summary.transactions.domain.transaction.model.Transaction;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class TransactionOpensearchClient {

    private final OpenSearchConfig openSearchConfig;

    public Optional<List<Transaction>> searchByDsl(String dslQuery) {
        try {
            log.info("Executing DSL search query: {}", dslQuery);

            var objectMapper = new ObjectMapper();
            var request = buildHttpRequest(dslQuery, objectMapper);

            // Executar requisição
            var httpClient = HttpClient.newHttpClient();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("OpenSearch HTTP error: {} - {}", response.statusCode(), response.body());
                throw new RuntimeException("OpenSearch request failed with status: " + response.statusCode());
            }

            // Parse da resposta
            var responseNode = objectMapper.readTree(response.body());
            var hits = responseNode.get("hits").get("hits");

            var totalHits = responseNode.get("hits").get("total").get("value").asLong();
            log.info("DSL search returned {} hits", totalHits);

            var transactions = new ArrayList<Transaction>();
            for (var hit : hits) {
                var source = hit.get("_source");
                var transaction = deserializeTransactionFromJson(source.toString());
                transactions.add(transaction);
            }

            return Optional.of(transactions);
        } catch (IOException | InterruptedException exception) {
            log.error("Error executing DSL search query: {}", dslQuery, exception);
            throw new RuntimeException("Failed to execute DSL search", exception);
        } catch (Exception exception) {
            log.error("Error parsing DSL query: {}", dslQuery, exception);
            throw new RuntimeException("Invalid DSL query format", exception);
        }
    }

    private HttpRequest buildHttpRequest(String dslQuery, ObjectMapper objectMapper) throws Exception {
        // Construir a query completa com paginação e ordenação
        var queryNode = objectMapper.readTree(dslQuery);
        var searchBody = objectMapper.createObjectNode();

        // Se a query tem wrapper "query", extrair o conteúdo
        if (queryNode.has("query")) {
            searchBody.set("query", queryNode.get("query"));
        } else {
            searchBody.set("query", queryNode);
        }

        // Construir URL do OpenSearch
        var searchUrl = String.format("%s://%s:%d/%s/_search",
                openSearchConfig.getScheme(),
                openSearchConfig.getHost(),
                openSearchConfig.getPort(),
                openSearchConfig.getTransactionsIndex());

        // Criar requisição HTTP
        var requestBody = searchBody.toString();
        var requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(searchUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));

        // Adicionar autenticação básica se configurada
        if (!openSearchConfig.getUsername().isEmpty() && !openSearchConfig.getPassword().isEmpty()) {
            var auth = java.util.Base64.getEncoder().encodeToString(
                    (openSearchConfig.getUsername() + ":" + openSearchConfig.getPassword()).getBytes());
            requestBuilder.header("Authorization", "Basic " + auth);
        }

        return requestBuilder.build();
    }

    private Transaction deserializeTransactionFromJson(String jsonString) {
        try {
            log.info("Deserializing transaction from JSON: {}", jsonString);

            var objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            var transaction = objectMapper.readValue(jsonString, Transaction.class);

            log.info("Successfully deserialized transaction with ID: {}", transaction.id());
            return transaction;
        } catch (Exception exception) {
            log.error("Error deserializing transaction from JSON: {}", jsonString, exception);
            throw new RuntimeException("Failed to deserialize transaction from JSON", exception);
        }
    }

}

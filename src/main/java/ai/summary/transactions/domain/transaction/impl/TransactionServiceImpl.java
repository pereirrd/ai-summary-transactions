package ai.summary.transactions.domain.transaction.impl;

import ai.summary.transactions.core.config.OpenSearchConfig;
import ai.summary.transactions.domain.transaction.TransactionService;
import ai.summary.transactions.domain.transaction.mapper.OpenSearchTransactionMapper;
import ai.summary.transactions.domain.transaction.model.Transaction;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final OpenSearchClient openSearchClient;
    private final OpenSearchConfig openSearchConfig;
    private final OpenSearchTransactionMapper openSearchTransactionMapper;

    @Override
    public Optional<List<Transaction>> getAllTransactions(int limit, int offset) {
        try {
            var searchRequest = SearchRequest.of(transactions -> transactions
                    .index(openSearchConfig.getTransactionsIndex())
                    .query(Query.of(query -> query.matchAll(m -> m)))
                    .from(offset)
                    .size(limit)
                    .sort(sort -> sort.field(
                            f -> f.field("date").order(SortOrder.Desc))));

            var response = openSearchClient.search(searchRequest, Object.class);

            return Optional.of(response.hits().hits().stream()
                    .map(hit -> openSearchTransactionMapper.fromOpenSearchObject(hit.source()))
                    .collect(Collectors.toList()));
        } catch (IOException exception) {
            log.error("Error retrieving all transactions", exception);
            throw new RuntimeException("Failed to retrieve transactions", exception);
        }
    }

    @Override
    public Optional<Transaction> getTransactionById(String id) {
        try {
            var getRequest = GetRequest.of(transactions -> transactions
                    .index(openSearchConfig.getTransactionsIndex())
                    .id(id));

            var response = openSearchClient.get(getRequest, Object.class);

            if (response.found()) {
                return Optional.of(openSearchTransactionMapper.fromOpenSearchObject(response.source()));
            } else {
                log.warn("Transaction with id {} not found", id);
                return Optional.empty();
            }
        } catch (IOException exception) {
            log.error("Error retrieving transaction with id: {}", id, exception);
            throw new RuntimeException("Failed to retrieve transaction", exception);
        }
    }

    @Override
    public Transaction createTransaction(Transaction transaction) {
        try {
            // Gerar ID se não fornecido
            var transactionId = transaction.id() != null ? transaction.id().toString()
                    : UUID.randomUUID().toString();

            var transactionWithId = new Transaction(
                    UUID.fromString(transactionId),
                    transaction.date(),
                    transaction.amount(),
                    transaction.description(),
                    transaction.merchant());

            var indexRequest = IndexRequest.of(i -> i
                    .index(openSearchConfig.getTransactionsIndex())
                    .id(transactionId)
                    .document(transactionWithId));

            var response = openSearchClient.index(indexRequest);

            if (response.result() == Result.Created || response.result() == Result.Updated) {
                log.info("Transaction created/updated with id: {}", transactionId);
                return transactionWithId;
            } else {
                throw new RuntimeException("Failed to create transaction");
            }
        } catch (IOException exception) {
            log.error("Error creating transaction", exception);
            throw new RuntimeException("Failed to create transaction", exception);
        }
    }

    @Override
    public Optional<Transaction> updateTransaction(String id, Transaction transaction) {
        try {
            // Verificar se a transação existe
            var existingTransaction = getTransactionById(id);
            if (existingTransaction.isEmpty()) {
                log.warn("Transaction with id {} not found for update", id);
                return Optional.empty();
            }

            // Criar nova transação com o ID existente
            var updatedTransaction = new Transaction(
                    UUID.fromString(id),
                    transaction.date() != null ? transaction.date() : existingTransaction.get().date(),
                    transaction.amount() != null ? transaction.amount() : existingTransaction.get().amount(),
                    transaction.description() != null ? transaction.description()
                            : existingTransaction.get().description(),
                    transaction.merchant() != null ? transaction.merchant() : existingTransaction.get().merchant());

            var indexRequest = IndexRequest.of(i -> i
                    .index(openSearchConfig.getTransactionsIndex())
                    .id(id)
                    .document(updatedTransaction));

            var response = openSearchClient.index(indexRequest);

            if (response.result() == Result.Updated || response.result() == Result.Created) {
                log.info("Transaction updated with id: {}", id);
                return Optional.of(updatedTransaction);
            } else {
                throw new RuntimeException("Failed to update transaction");
            }
        } catch (IOException exception) {
            log.error("Error updating transaction with id: {}", id, exception);
            throw new RuntimeException("Failed to update transaction", exception);
        }
    }

    @Override
    public void deleteTransaction(String id) {
        try {
            var deleteRequest = DeleteRequest.of(transaction -> transaction
                    .index(openSearchConfig.getTransactionsIndex())
                    .id(id));

            var response = openSearchClient.delete(deleteRequest);

            if (response.result() == Result.Deleted) {
                log.info("Transaction deleted with id: {}", id);
            } else if (response.result() == Result.NotFound) {
                log.warn("Transaction with id {} not found for deletion", id);
            } else {
                throw new RuntimeException("Failed to delete transaction");
            }
        } catch (IOException exception) {
            log.error("Error deleting transaction with id: {}", id, exception);
            throw new RuntimeException("Failed to delete transaction", exception);
        }
    }

    @Override
    public Optional<List<Transaction>> searchTransactionsByDsl(String dslQuery, int limit, int offset) {
        try {
            log.info("Executing DSL search query: {}", dslQuery);

            // Parse da query DSL JSON para Query usando Jackson
            var objectMapper = new ObjectMapper();
            var queryNode = objectMapper.readTree(dslQuery);

            // Se a query tem wrapper "query", extrair o conteúdo
            if (queryNode.has("query")) {
                queryNode = queryNode.get("query");
            }

            // Converter JsonNode para Query usando ObjectMapper
            var query = objectMapper.treeToValue(queryNode, Query.class);

            // Criar a requisição de busca
            var searchRequest = SearchRequest.of(search -> search
                    .index(openSearchConfig.getTransactionsIndex())
                    .query(query)
                    .from(offset)
                    .size(limit)
                    .sort(sort -> sort.field(
                            f -> f.field("date").order(SortOrder.Desc))));

            var response = openSearchClient.search(searchRequest, Object.class);

            var totalHits = response.hits().total() != null ? response.hits().total().value() : 0L;
            log.info("DSL search returned {} hits", totalHits);

            return Optional.of(response.hits().hits().stream()
                    .map(hit -> openSearchTransactionMapper.fromOpenSearchObject(hit.source()))
                    .collect(Collectors.toList()));
        } catch (IOException exception) {
            log.error("Error executing DSL search query: {}", dslQuery, exception);
            throw new RuntimeException("Failed to execute DSL search", exception);
        } catch (Exception exception) {
            log.error("Error parsing DSL query: {}", dslQuery, exception);
            throw new RuntimeException("Invalid DSL query format", exception);
        }
    }

}
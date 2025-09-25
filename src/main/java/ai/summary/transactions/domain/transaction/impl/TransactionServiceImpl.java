package ai.summary.transactions.domain.transaction.impl;

import ai.summary.transactions.core.config.OpenSearchConfig;
import ai.summary.transactions.domain.transaction.TransactionService;
import ai.summary.transactions.domain.transaction.mapper.OpenSearchTransactionMapper;
import ai.summary.transactions.domain.transaction.model.Transaction;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.*;
import java.io.IOException;
import java.time.LocalDate;
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
    public Optional<List<Transaction>> getAll(int limit, int offset) {
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
    public Optional<List<Transaction>> findByDateRange(LocalDate startDate, LocalDate endDate, int limit,
            int offset) {
        try {
            // Converter LocalDate para LocalDateTime para busca
            var startDateTime = startDate.atStartOfDay();
            var endDateTime = endDate.atTime(23, 59, 59, 999_999_999);

            var searchRequest = SearchRequest.of(transactions -> transactions
                    .index(openSearchConfig.getTransactionsIndex())
                    .query(Query.of(query -> query.range(range -> range
                            .field("date")
                            .gte(JsonData.of(startDateTime.toString()))
                            .lte(JsonData.of(endDateTime.toString())))))
                    .from(offset)
                    .size(limit)
                    .sort(sort -> sort.field(
                            f -> f.field("date").order(SortOrder.Desc))));

            var response = openSearchClient.search(searchRequest, Object.class);

            return Optional.of(response.hits().hits().stream()
                    .map(hit -> openSearchTransactionMapper.fromOpenSearchObject(hit.source()))
                    .collect(Collectors.toList()));
        } catch (IOException exception) {
            log.error("Error retrieving transactions by date range from {} to {}", startDate, endDate, exception);
            throw new RuntimeException("Failed to retrieve transactions by date range", exception);
        }
    }

    @Override
    public Optional<Transaction> getById(String id) {
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
    public Transaction create(Transaction transaction) {
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
    public Optional<Transaction> update(String id, Transaction transaction) {
        try {
            // Verificar se a transação existe
            var existingTransaction = getById(id);
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
    public void delete(String id) {
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

}
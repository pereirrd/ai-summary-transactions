package ai.summary.transactions.domain.transaction.impl;

import ai.summary.transactions.core.config.OpenSearchConfig;
import ai.summary.transactions.domain.transaction.TransactionService;
import ai.summary.transactions.domain.transaction.model.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.*;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final OpenSearchClient openSearchClient;
    private final OpenSearchConfig openSearchConfig;
    private final ObjectMapper objectMapper;

    @Override
    public List<Transaction> getAllTransactions(int limit, int offset) {
        try {
            var searchRequest = SearchRequest.of(s -> s
                    .index(openSearchConfig.getTransactionsIndex())
                    .query(Query.of(q -> q.matchAll(m -> m)))
                    .from(offset)
                    .size(limit)
                    .sort(sort -> sort.field(
                            f -> f.field("date").order(org.opensearch.client.opensearch._types.SortOrder.Desc))));

            var response = openSearchClient.search(searchRequest, Object.class);

            return response.hits().hits().stream()
                    .map(hit -> objectMapper.convertValue(hit.source(), Transaction.class))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Error retrieving all transactions", e);
            throw new RuntimeException("Failed to retrieve transactions", e);
        }
    }

    @Override
    public Transaction getTransactionById(String id) {
        try {
            var getRequest = GetRequest.of(g -> g
                    .index(openSearchConfig.getTransactionsIndex())
                    .id(id));

            var response = openSearchClient.get(getRequest, Object.class);

            if (response.found()) {
                return objectMapper.convertValue(response.source(), Transaction.class);
            } else {
                log.warn("Transaction with id {} not found", id);
                return null;
            }
        } catch (IOException e) {
            log.error("Error retrieving transaction with id: {}", id, e);
            throw new RuntimeException("Failed to retrieve transaction", e);
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
        } catch (IOException e) {
            log.error("Error creating transaction", e);
            throw new RuntimeException("Failed to create transaction", e);
        }
    }

    @Override
    public Transaction updateTransaction(String id, Transaction transaction) {
        try {
            // Verificar se a transação existe
            var existingTransaction = getTransactionById(id);
            if (existingTransaction == null) {
                log.warn("Transaction with id {} not found for update", id);
                return null;
            }

            // Criar nova transação com o ID existente
            var updatedTransaction = new Transaction(
                    UUID.fromString(id),
                    transaction.date() != null ? transaction.date() : existingTransaction.date(),
                    transaction.amount() != null ? transaction.amount() : existingTransaction.amount(),
                    transaction.description() != null ? transaction.description() : existingTransaction.description(),
                    transaction.merchant() != null ? transaction.merchant() : existingTransaction.merchant());

            var indexRequest = IndexRequest.of(i -> i
                    .index(openSearchConfig.getTransactionsIndex())
                    .id(id)
                    .document(updatedTransaction));

            var response = openSearchClient.index(indexRequest);

            if (response.result() == Result.Updated || response.result() == Result.Created) {
                log.info("Transaction updated with id: {}", id);
                return updatedTransaction;
            } else {
                throw new RuntimeException("Failed to update transaction");
            }
        } catch (IOException e) {
            log.error("Error updating transaction with id: {}", id, e);
            throw new RuntimeException("Failed to update transaction", e);
        }
    }

    @Override
    public void deleteTransaction(String id) {
        try {
            var deleteRequest = DeleteRequest.of(d -> d
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
        } catch (IOException e) {
            log.error("Error deleting transaction with id: {}", id, e);
            throw new RuntimeException("Failed to delete transaction", e);
        }
    }
}
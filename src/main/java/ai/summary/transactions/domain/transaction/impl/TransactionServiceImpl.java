package ai.summary.transactions.domain.transaction.impl;

import ai.summary.transactions.domain.transaction.TransactionService;
import ai.summary.transactions.domain.transaction.model.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch.core.*;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Singleton
public class TransactionServiceImpl implements TransactionService {

    @Value("${opensearch.index.transactions:transactions}")
    private String transactionsIndex;

    private final OpenSearchClient openSearchClient;
    private final ObjectMapper objectMapper;

    @Inject
    public TransactionServiceImpl(OpenSearchClient openSearchClient, ObjectMapper objectMapper) {
        this.openSearchClient = openSearchClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Transaction getTransactionById(String id) {
        try {
            GetRequest getRequest = GetRequest.of(g -> g
                    .index(transactionsIndex)
                    .id(id));

            GetResponse<Object> response = openSearchClient.get(getRequest, Object.class);

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
            String transactionId = transaction.id() != null ? transaction.id().toString()
                    : UUID.randomUUID().toString();

            Transaction transactionWithId = new Transaction(
                    UUID.fromString(transactionId),
                    transaction.date(),
                    transaction.amount(),
                    transaction.description(),
                    transaction.merchant());

            IndexRequest<Transaction> indexRequest = IndexRequest.of(i -> i
                    .index(transactionsIndex)
                    .id(transactionId)
                    .document(transactionWithId));

            IndexResponse response = openSearchClient.index(indexRequest);

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
            Transaction existingTransaction = getTransactionById(id);
            if (existingTransaction == null) {
                log.warn("Transaction with id {} not found for update", id);
                return null;
            }

            // Criar nova transação com o ID existente
            Transaction updatedTransaction = new Transaction(
                    UUID.fromString(id),
                    transaction.date() != null ? transaction.date() : existingTransaction.date(),
                    transaction.amount() != null ? transaction.amount() : existingTransaction.amount(),
                    transaction.description() != null ? transaction.description() : existingTransaction.description(),
                    transaction.merchant() != null ? transaction.merchant() : existingTransaction.merchant());

            IndexRequest<Transaction> indexRequest = IndexRequest.of(i -> i
                    .index(transactionsIndex)
                    .id(id)
                    .document(updatedTransaction));

            IndexResponse response = openSearchClient.index(indexRequest);

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
            DeleteRequest deleteRequest = DeleteRequest.of(d -> d
                    .index(transactionsIndex)
                    .id(id));

            DeleteResponse response = openSearchClient.delete(deleteRequest);

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
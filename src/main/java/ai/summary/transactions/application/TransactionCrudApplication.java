package ai.summary.transactions.application;

import ai.summary.transactions.domain.transaction.TransactionService;
import ai.summary.transactions.domain.transaction.mapper.TransactionMapper;
import ai.summary.transactions.model.CreateTransactionRequest;
import ai.summary.transactions.model.TransactionApiResponse;
import ai.summary.transactions.model.UpdateTransactionRequest;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class TransactionCrudApplication {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    public Optional<List<TransactionApiResponse>> getAllTransactions(int limit, int offset) {
        try {
            var domainTransactions = transactionService.getAllTransactions(limit, offset);

            if (domainTransactions.isEmpty()) {
                return Optional.empty();
            }

            var apiTransactions = transactionMapper.toApi(domainTransactions.get());
            return Optional.of(apiTransactions);
        } catch (Exception exception) {
            log.error("Error retrieving all transactions", exception);
            throw new RuntimeException("Failed to retrieve transactions", exception);
        }
    }

    public Optional<TransactionApiResponse> getTransactionById(String id) {
        try {
            var domainTransaction = transactionService.getTransactionById(id);

            if (domainTransaction.isEmpty()) {
                return Optional.empty();
            }

            var apiTransaction = transactionMapper.toApi(domainTransaction.get());
            return Optional.of(apiTransaction);
        } catch (Exception exception) {
            log.error("Error retrieving transaction with id: {}", id, exception);
            throw new RuntimeException("Failed to retrieve transaction", exception);
        }
    }

    public TransactionApiResponse createTransaction(CreateTransactionRequest createTransactionRequest) {
        try {
            var domainTransaction = transactionMapper.toDomain(createTransactionRequest);
            var createdTransaction = transactionService.createTransaction(domainTransaction);

            return transactionMapper.toApi(createdTransaction);
        } catch (Exception exception) {
            log.error("Error creating transaction", exception);
            throw new RuntimeException("Failed to create transaction", exception);
        }
    }

    public Optional<TransactionApiResponse> updateTransaction(String id,
            UpdateTransactionRequest updateTransactionRequest) {
        try {
            var domainTransaction = transactionMapper.toDomain(updateTransactionRequest);
            var updatedTransaction = transactionService.updateTransaction(id, domainTransaction);

            if (updatedTransaction.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(transactionMapper.toApi(updatedTransaction.get()));
        } catch (Exception exception) {
            log.error("Error updating transaction with id: {}", id, exception);
            throw new RuntimeException("Failed to update transaction", exception);
        }
    }

    public void deleteTransaction(String id) {
        try {
            transactionService.deleteTransaction(id);
        } catch (Exception e) {
            log.error("Error deleting transaction with id: {}", id, e);
            throw new RuntimeException("Failed to delete transaction", e);
        }
    }
}

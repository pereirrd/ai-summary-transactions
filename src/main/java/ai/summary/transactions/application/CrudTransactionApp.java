package ai.summary.transactions.application;

import ai.summary.transactions.domain.transaction.TransactionService;
import ai.summary.transactions.domain.transaction.mapper.TransactionMapper;
import ai.summary.transactions.model.CreateTransactionRequest;
import ai.summary.transactions.model.TransactionApiResponse;
import ai.summary.transactions.model.UpdateTransactionRequest;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class CrudTransactionApp {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    public Optional<List<TransactionApiResponse>> findByFilters(LocalDate startDate, LocalDate endDate, int limit,
            int offset) {
        try {
            var domainTransactions = transactionService.findByFilters(startDate, endDate, limit, offset);

            if (domainTransactions.isEmpty()) {
                return Optional.empty();
            }

            var apiTransactions = transactionMapper.toApi(domainTransactions.get());
            return Optional.of(apiTransactions);
        } catch (Exception exception) {
            log.error("Error retrieving transactions with filters", exception);
            throw new RuntimeException("Failed to retrieve transactions", exception);
        }
    }

    public Optional<TransactionApiResponse> getById(String id) {
        try {
            var domainTransaction = transactionService.getById(id);

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

    public TransactionApiResponse create(CreateTransactionRequest createTransactionRequest) {
        try {
            var domainTransaction = transactionMapper.toDomain(createTransactionRequest);
            var createdTransaction = transactionService.create(domainTransaction);

            return transactionMapper.toApi(createdTransaction);
        } catch (Exception exception) {
            log.error("Error creating transaction", exception);
            throw new RuntimeException("Failed to create transaction", exception);
        }
    }

    public Optional<TransactionApiResponse> update(String id,
            UpdateTransactionRequest updateTransactionRequest) {
        try {
            var domainTransaction = transactionMapper.toDomain(updateTransactionRequest);
            var updatedTransaction = transactionService.update(id, domainTransaction);

            if (updatedTransaction.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(transactionMapper.toApi(updatedTransaction.get()));
        } catch (Exception exception) {
            log.error("Error updating transaction with id: {}", id, exception);
            throw new RuntimeException("Failed to update transaction", exception);
        }
    }

    public void delete(String id) {
        try {
            transactionService.delete(id);
        } catch (Exception e) {
            log.error("Error deleting transaction with id: {}", id, e);
            throw new RuntimeException("Failed to delete transaction", e);
        }
    }
}

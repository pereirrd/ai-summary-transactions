package ai.summary.transactions.domain.ai.tools.impl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import ai.summary.transactions.domain.ai.tools.TransactionTool;
import ai.summary.transactions.domain.transaction.TransactionService;
import ai.summary.transactions.domain.transaction.model.Transaction;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class TransactionToolImpl implements TransactionTool {

    private final TransactionService transactionService;

    @Override
    public List<Transaction> getTransactions(LocalDate startDate, LocalDate endDate) {
        log.info("Getting transactions for startDate: {} and endDate: {}", startDate, endDate);

        var transactions = transactionService.findByFilters(startDate, endDate, 100, 0)
                .orElse(Collections.emptyList());

        log.info("Transactions found: {}", transactions.size());

        return transactions;
    }
}

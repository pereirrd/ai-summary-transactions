package ai.summary.transactions.domain.transaction;

import ai.summary.transactions.domain.transaction.model.Transaction;
import java.util.List;
import java.util.Optional;

public interface TransactionService {

    Optional<Transaction> getTransactionById(String id);

    Optional<List<Transaction>> getAllTransactions(int limit, int offset);

    Transaction createTransaction(Transaction transaction);

    Optional<Transaction> updateTransaction(String id, Transaction transaction);

    void deleteTransaction(String id);
}

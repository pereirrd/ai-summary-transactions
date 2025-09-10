package ai.summary.transactions.domain.transaction;

import ai.summary.transactions.domain.transaction.model.Transaction;
import java.util.List;

public interface TransactionService {

    Transaction getTransactionById(String id);

    List<Transaction> getAllTransactions(int limit, int offset);

    Transaction createTransaction(Transaction transaction);

    Transaction updateTransaction(String id, Transaction transaction);

    void deleteTransaction(String id);
}

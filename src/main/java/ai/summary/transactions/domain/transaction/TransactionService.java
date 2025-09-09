package ai.summary.transactions.domain.transaction;

import ai.summary.transactions.domain.transaction.model.Transaction;

public interface TransactionService {

    Transaction getTransactionById(String id);

    Transaction createTransaction(Transaction transaction);

    Transaction updateTransaction(String id, Transaction transaction);

    void deleteTransaction(String id);
}

package ai.summary.transactions.domain.transaction;

import ai.summary.transactions.domain.transaction.model.Transaction;
import java.util.List;
import java.util.Optional;

public interface TransactionService {

    Optional<Transaction> getById(String id);

    Optional<List<Transaction>> getAll(int limit, int offset);

    Transaction create(Transaction transaction);

    Optional<Transaction> update(String id, Transaction transaction);

    void delete(String id);

    Optional<List<Transaction>> searchByDsl(String dslQuery);
}

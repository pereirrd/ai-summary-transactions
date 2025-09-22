package ai.summary.transactions.application;

import java.util.List;

import ai.summary.transactions.domain.ai.query.AITransactionQueryService;
import ai.summary.transactions.domain.ai.summary.AITransactionSummaryService;
import ai.summary.transactions.domain.transaction.TransactionService;
import ai.summary.transactions.domain.transaction.model.Transaction;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class AISummaryTransactionApp {

    private final AITransactionQueryService aiTransactionService;
    private final AITransactionSummaryService aiTransactionSummaryService;
    private final TransactionService transactionService;

    public String processByQuery(String question) {
        try {
            log.info("Processing AI transaction question by query: {}", question);

            // Usar IA para processar a pergunta e gerar a query
            var query = aiTransactionService.createQuery(question);
            log.info("AI transaction question processed successfully");
            log.info("Query to search transactions: {}", query);

            var transactions = transactionService.searchByDsl(query);
            if (transactions.isEmpty()) {
                return logNotFoundTransactions();
            }

            return summarize(transactions.get());
        } catch (Exception exception) {
            return logError(question, exception);
        }
    }

    public String processByList(String question) {
        try {
            log.info("Processing AI transaction question by list: {}", question);

            var transactions = transactionService.getAll(100, 0);
            if (transactions.isEmpty()) {
                return logNotFoundTransactions();
            }

            return summarize(transactions.get());
        } catch (Exception exception) {
            return logError(question, exception);
        }
    }

    private String summarize(List<Transaction> transactions) {
        log.info("Summarizing transactions: {}", transactions);
        var summary = aiTransactionSummaryService.summarizeTransactions(transactions.toString());
        log.info("AI transaction summary processed successfully");
        log.info("AI transaction summary: {}", summary);
        return summary;
    }

    private String logError(String question, Exception exception) {
        log.error("Error processing AI transaction question: {}", question, exception);
        throw new RuntimeException("Failed to process AI transaction question", exception);
    }

    private String logNotFoundTransactions() {
        return "Não foi possível encontrar transações";
    }
}

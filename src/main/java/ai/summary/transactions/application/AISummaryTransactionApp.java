package ai.summary.transactions.application;

import ai.summary.transactions.domain.ai.query.AITransactionQueryService;
import ai.summary.transactions.domain.ai.summary.AITransactionSummaryService;
import ai.summary.transactions.domain.transaction.TransactionService;
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

    public String process(String question) {
        try {
            log.info("Processing AI transaction question: {}", question);

            // Usar o serviço de IA para processar a pergunta
            var query = aiTransactionService.createQuery(question);
            log.info("AI transaction question processed successfully");
            log.info("Query to search transactions: {}", query);

            var transactions = transactionService.searchByDsl(query);
            if (transactions.isEmpty()) {
                return "Não foi possível encontrar transações";
            }

            var summary = aiTransactionSummaryService.summarizeTransactions(transactions.get().toString());
            log.info("AI transaction summary processed successfully");
            log.info("AI transaction summary: {}", summary);

            return summary;
        } catch (Exception exception) {
            log.error("Error processing AI transaction question: {}", question, exception);
            throw new RuntimeException("Failed to process AI transaction question", exception);
        }
    }
}

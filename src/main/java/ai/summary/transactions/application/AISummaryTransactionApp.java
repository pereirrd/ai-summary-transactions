package ai.summary.transactions.application;

import ai.summary.transactions.domain.ai.summary.AITransactionSummaryService;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class AISummaryTransactionApp {

    private final AITransactionSummaryService aiTransactionSummaryService;

    public String processByList(String question) {
        try {
            log.info("Processing AI transaction question by list: {}", question);

            var summary = aiTransactionSummaryService.summarizeTransactions(question);
            log.info("AI transaction summary processed successfully");
            log.info("AI transaction summary: {}", summary);

            return summary;
        } catch (Exception exception) {
            return logError(question, exception);
        }
    }

    private String logError(String question, Exception exception) {
        log.error("Error processing AI transaction question: {}", question, exception);
        throw new RuntimeException("Failed to process AI transaction question", exception);
    }
}

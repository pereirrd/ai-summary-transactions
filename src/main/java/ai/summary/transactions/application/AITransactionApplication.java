package ai.summary.transactions.application;

import ai.summary.transactions.domain.ai.AITransactionService;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class AITransactionApplication {

    private final AITransactionService aiTransactionService;

    public String processAITransaction(String question) {
        try {
            log.info("Processing AI transaction question: {}", question);

            // Use the AI service to process the question
            String result = aiTransactionService.serachTransactions(question);

            log.info("AI transaction question processed successfully");
            return result;
        } catch (Exception exception) {
            log.error("Error processing AI transaction question: {}", question, exception);
            throw new RuntimeException("Failed to process AI transaction question", exception);
        }
    }
}

package ai.summary.transactions.application;

import ai.summary.transactions.domain.ai.AITransactionService;
import ai.summary.transactions.domain.transaction.TransactionService;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class AITransactionApplication {

    private final AITransactionService aiTransactionService;
    private final TransactionService transactionService;

    public String processAITransaction(String question) {
        try {
            log.info("Processing AI transaction question: {}", question);

            // Usar o serviço de IA para processar a pergunta
            var query = aiTransactionService.createQuery(question);

            var transactions = transactionService.searchTransactionsByDsl(query);
            if (transactions.isEmpty()) {
                return "Não foi possível encontrar transações";
            }

            log.info("AI transaction question processed successfully");

            return transactions.get().toString();
        } catch (Exception exception) {
            log.error("Error processing AI transaction question: {}", question, exception);
            throw new RuntimeException("Failed to process AI transaction question", exception);
        }
    }
}

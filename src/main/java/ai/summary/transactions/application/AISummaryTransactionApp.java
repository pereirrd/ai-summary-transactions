package ai.summary.transactions.application;

import ai.summary.transactions.domain.ai.summary.AITransactionSummaryService;

import java.time.LocalDate;

import ai.summary.transactions.domain.ai.insights.AITransactionInsightsService;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class AISummaryTransactionApp {

    private final AITransactionSummaryService aiTransactionSummaryService;
    private final AITransactionInsightsService aiTransactionInsightsService;

    public String processByList(String question) {
        try {
            log.info("Processing AI transaction question: {}", question);

            var summary = aiTransactionSummaryService.summarizeTransactions(question);
            log.info("AI transaction summary processed successfully");
            log.info("AI transaction summary: {}", summary);

            return summary;
        } catch (Exception exception) {
            return logError(question, exception);
        }
    }

    public String processByInsights(String scenario, LocalDate startDate, LocalDate endDate) {
        try {
            log.info("Processing AI transaction insights: {}", scenario);

            var insights = aiTransactionInsightsService.generateInsights(scenario, startDate, endDate);
            log.info("AI transaction insights processed successfully");
            log.info("AI transaction insights: {}", insights);

            return insights;
        } catch (Exception exception) {
            return logError(scenario, startDate, endDate, exception);
        }
    }

    private String logError(String question, Exception exception) {
        log.error("Error processing AI transaction question: {}", question, exception);
        throw new RuntimeException("Failed to process AI transaction question", exception);
    }

    private String logError(String scenario, LocalDate startDate, LocalDate endDate, Exception exception) {
        log.error("Error processing AI transaction insights: {}", scenario, startDate, endDate, exception);
        throw new RuntimeException("Failed to process AI transaction insights", exception);
    }
}

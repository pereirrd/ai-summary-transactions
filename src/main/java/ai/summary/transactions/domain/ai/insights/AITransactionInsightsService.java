package ai.summary.transactions.domain.ai.insights;

import java.time.LocalDate;

public interface AITransactionInsightsService {

    String generateInsights(String scenario, LocalDate startDate, LocalDate endDate);

}

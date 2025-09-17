package ai.summary.transactions.domain.ai.summary;

import dev.langchain4j.service.SystemMessage;
import io.micronaut.langchain4j.annotation.AiService;

@AiService
public interface AITransactionSummaryService {

    @SystemMessage(fromResource = "prompts/ai-transaction-summary.md")
    String summarizeTransactions(String transactions);
}

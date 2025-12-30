package ai.summary.transactions.domain.ai.summary;

import ai.summary.transactions.domain.ai.tools.impl.ReferenceDateToolImpl;
import ai.summary.transactions.domain.ai.tools.impl.TransactionToolImpl;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.micronaut.langchain4j.annotation.AiService;

@AiService(tools = { TransactionToolImpl.class, ReferenceDateToolImpl.class })
public interface AITransactionSummaryService {

    @SystemMessage(fromResource = "prompts/ai-transaction-summary.md")
    String summarizeTransactions(@UserMessage String customerQuestion);
}

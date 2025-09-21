package ai.summary.transactions.domain.ai.query;

import dev.langchain4j.service.SystemMessage;
import io.micronaut.langchain4j.annotation.AiService;

@AiService(tools = { ReferenceDateTool.class })
public interface AITransactionQueryService {

   @SystemMessage(fromResource = "prompts/ai-transaction-create-query.md")
   String createQuery(String userQuestion);
}

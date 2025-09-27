package ai.summary.transactions.domain.ai.insights;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface AITransactionInsights {

    @SystemMessage(fromResource = "prompts/ai-transaction-insights.md")
    @UserMessage("""
            Minha fatura está no cenário {{scenario}}, com data inicial {{startDate}} e data final {{endDate}}.
            Forneça um resumo das transações que ocorrem nesse período.
            """)
    String generateInsights(@V("scenario") String scenario,
            @V("startDate") String startDate,
            @V("endDate") String endDate);
}

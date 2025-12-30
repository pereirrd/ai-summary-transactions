package ai.summary.transactions.domain.ai.insights;

import java.time.LocalDate;
import ai.summary.transactions.core.config.AiConfig;
import ai.summary.transactions.domain.ai.insights.AITransactionInsightsService;
import ai.summary.transactions.domain.ai.tools.impl.ReferenceDateToolImpl;
import ai.summary.transactions.domain.ai.tools.impl.TransactionToolImpl;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import dev.langchain4j.service.AiServices;
import jakarta.inject.Singleton;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class AITransactionInsightsService {

    private final AiConfig aiConfig;
    private final ReferenceDateToolImpl referenceDateTool;
    private final TransactionToolImpl transactionTool;

    public String generateInsights(String scenario, LocalDate startDate, LocalDate endDate) {

        var chatModel = OpenAiChatModel.builder()
                .apiKey(aiConfig.getApiKey())
                .modelName(aiConfig.getModelName())
                .temperature(aiConfig.getTemperature())
                .build();

        var assistant = AiServices.builder(AITransactionInsights.class)
                .chatModel(chatModel)
                .tools(referenceDateTool, transactionTool)
                .build();

        var insights = assistant.generateInsights(scenario, startDate.toString(), endDate.toString());

        log.info("insights: {}", insights);

        return insights;
    }

}

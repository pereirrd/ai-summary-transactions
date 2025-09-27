package ai.summary.transactions.core.config;

import lombok.Getter;

import io.micronaut.context.annotation.Value;

import jakarta.inject.Singleton;

@Getter
@Singleton
public class AiConfig {

    @Value("${langchain4j.open-ai.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name}")
    private String modelName;

    @Value("${langchain4j.open-ai.chat-model.temperature}")
    private Double temperature;

}

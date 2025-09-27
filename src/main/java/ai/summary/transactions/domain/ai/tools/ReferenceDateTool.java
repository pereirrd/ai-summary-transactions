package ai.summary.transactions.domain.ai.tools;

import java.time.LocalDate;

import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ReferenceDateTool {

    @Tool("Essa é a data atual e deve ser usada como referencia para responder a pergunta do cliente.")
    public LocalDate referenceDate() {
        var today = LocalDate.now();

        log.info("Getting reference date: {}", today);

        return today;
    }
}

package ai.summary.transactions.domain.ai.summary;

import java.time.LocalDate;

import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ReferenceDateTool {

    @Tool("Essa é a data atual e deve ser usada como referencia para criar filtro por data das transações.")
    public LocalDate referenceDate() {
        log.info("Getting reference date");

        return LocalDate.now();
    }
}

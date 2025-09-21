package ai.summary.transactions.domain.ai.query;

import java.time.LocalDate;

import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;

@Singleton
public class ReferenceDateTool {

    @Tool("Essa é a data atual e deve ser usada como referencia para criar filtro por data das transações.")
    public LocalDate referenceDate() {
        return LocalDate.now();
    }
}

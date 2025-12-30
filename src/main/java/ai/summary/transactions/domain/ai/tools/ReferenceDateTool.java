package ai.summary.transactions.domain.ai.tools;

import java.time.LocalDate;
import dev.langchain4j.agent.tool.Tool;

public interface ReferenceDateTool {

    @Tool("""
            Essa é a data atual e deve ser usada como referência para responder a pergunta do cliente.
            Use esta tool quando precisar saber qual é a data de hoje para cálculos relativos de tempo,
            como "últimos 30 dias", "este mês", "mês passado", etc.
            Retorna a data atual no formato LocalDate.
            """)
    LocalDate referenceDate();
}

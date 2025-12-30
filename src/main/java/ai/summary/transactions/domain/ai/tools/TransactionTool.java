package ai.summary.transactions.domain.ai.tools;

import java.time.LocalDate;
import java.util.List;
import ai.summary.transactions.domain.transaction.model.Transaction;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

public interface TransactionTool {

    @Tool("""
            Essa é a lista de transações bancárias e deve ser usada para responder a pergunta do cliente.
            Use esta tool quando precisar buscar transações financeiras do período especificado.
            Retorna uma lista de transações contendo informações como valor, data, descrição e categoria.

            IMPORTANTE: Sempre será fornecido as datas de início e fim do período desejado pelo cliente.
            - O formato das datas será YYYY-MM-DD (ex: 2024-01-15)
            - A data inicial (startDate) deve ser anterior ou igual à data final (endDate)
            """)
    List<Transaction> getTransactions(
            @P("Data inicial do período para busca das transações. Formato: YYYY-MM-DD (ex: 2024-01-01).") LocalDate startDate,
            @P("Data final do período para busca das transações. Formato: YYYY-MM-DD (ex: 2024-01-31).") LocalDate endDate);
}

package ai.summary.transactions.domain.ai.summary;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import dev.langchain4j.agent.tool.Tool;
import io.micronaut.http.context.ServerRequestContext;
import ai.summary.transactions.domain.transaction.TransactionService;
import ai.summary.transactions.domain.transaction.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import jakarta.inject.Singleton;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class TransactionTool {

    private final TransactionService transactionService;

    @Tool("Essa é a lista de transações bancárias e deve ser usada para responder a pergunta do cliente. As datas serão passadas como parâmetros da função via prompt.")
    public List<Transaction> getTransactions() {
        var startDate = getStartDate();
        var endDate = getEndDate();
        log.info("Getting transactions for startDate: {} and endDate: {}", startDate, endDate);

        return transactionService.findByDateRange(startDate, endDate, 100, 0)
                .orElse(Collections.emptyList());
    }

    private LocalDate getStartDate() {
        var request = ServerRequestContext.currentRequest().get().getParameters().get("startDate");
        return LocalDate.parse(request);
    }

    private LocalDate getEndDate() {
        var request = ServerRequestContext.currentRequest().get().getParameters().get("endDate");
        return LocalDate.parse(request);
    }
}

package ai.summary.transactions.controller;

import ai.summary.transactions.model.ProcessAITransaction200Response;
import ai.summary.transactions.model.ProcessAITransactionRequest;
import ai.summary.transactions.application.AITransactionApplication;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AiTransactionsControllerImpl implements AiTransactionsApi {

    private final AITransactionApplication aiTransactionApplication;

    @Override
    public HttpResponse<@Valid ProcessAITransaction200Response> processAITransaction(
            @NotNull @Valid ProcessAITransactionRequest processAITransactionRequest) {
        try {
            String result = aiTransactionApplication
                    .processAITransaction(processAITransactionRequest.getQuestion());

            var response = new ProcessAITransaction200Response()
                    .result(result);

            return HttpResponse.ok(response);
        } catch (Exception exception) {
            log.error("Error processing AI transaction query: {}", processAITransactionRequest.getQuestion(),
                    exception);
            return HttpResponse.serverError();
        }
    }
}

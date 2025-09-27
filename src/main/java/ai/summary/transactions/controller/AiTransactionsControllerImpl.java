package ai.summary.transactions.controller;

import ai.summary.transactions.model.AIResultResponse;
import ai.summary.transactions.model.GetAITransactionInsightsScenarioParameter;
import ai.summary.transactions.model.ProcessAITransactionSummaryRequest;

import java.time.LocalDate;

import ai.summary.transactions.application.AISummaryTransactionApp;
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

    private final AISummaryTransactionApp aiTransactionApplication;

    @Override
    public HttpResponse<@Valid AIResultResponse> processAITransactionSummary(
            @NotNull LocalDate startDate, @NotNull LocalDate endDate,
            @NotNull @Valid ProcessAITransactionSummaryRequest processAITransactionSummaryRequest) {
        try {
            String result = aiTransactionApplication
                    .processByList(processAITransactionSummaryRequest.getQuestion());

            var response = new AIResultResponse()
                    .result(result);

            return HttpResponse.ok(response);
        } catch (Exception exception) {
            log.error("Error processing AI transaction with startDate {}: endDate {}: {}",
                    startDate, endDate, processAITransactionSummaryRequest.getQuestion(), exception);
            return HttpResponse.serverError();
        }
    }

    @Override
    public HttpResponse<@Valid AIResultResponse> getAITransactionInsights(
            @NotNull GetAITransactionInsightsScenarioParameter scenario, @NotNull LocalDate startDate,
            @NotNull LocalDate endDate) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAITransactionInsights'");
    }

}

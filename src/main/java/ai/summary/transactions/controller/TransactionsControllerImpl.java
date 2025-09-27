package ai.summary.transactions.controller;

import ai.summary.transactions.model.CreateTransactionRequest;
import ai.summary.transactions.model.GetAllTransactions200Response;
import ai.summary.transactions.model.TransactionApiResponse;
import ai.summary.transactions.model.UpdateTransactionRequest;
import ai.summary.transactions.application.CrudTransactionApp;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Slf4j
@Controller
@RequiredArgsConstructor
public class TransactionsControllerImpl implements TransactionsApi {

    private final CrudTransactionApp transactionsCrudApplication;

    @Override
    public HttpResponse<@Valid GetAllTransactions200Response> getAllTransactions(@NotNull Integer limit,
            @NotNull Integer offset,
            LocalDate startDate, LocalDate endDate) {
        try {
            var apiTransactions = transactionsCrudApplication.findByFilters(startDate, endDate, limit, offset);

            if (apiTransactions.isEmpty()) {
                return HttpResponse.notFound();
            }

            var response = new GetAllTransactions200Response()
                    .transactions(apiTransactions.get())
                    .total(apiTransactions.get().size())
                    .limit(limit)
                    .offset(offset);

            return HttpResponse.ok(response);
        } catch (Exception exception) {
            log.error("Error retrieving all transactions", exception);
            return HttpResponse.serverError();
        }
    }

    @Override
    public HttpResponse<@Valid TransactionApiResponse> createTransaction(
            @NotNull @Valid CreateTransactionRequest createTransactionRequest) {
        try {
            var apiTransaction = transactionsCrudApplication.create(createTransactionRequest);
            return HttpResponse.created(apiTransaction);
        } catch (Exception exception) {
            log.error("Error creating transaction", exception);
            return HttpResponse.badRequest();
        }
    }

    @Override
    public HttpResponse<@Valid TransactionApiResponse> getTransactionById(@NotNull String id) {
        try {
            var apiTransaction = transactionsCrudApplication.getById(id);

            if (apiTransaction.isEmpty()) {
                return HttpResponse.notFound();
            }

            return HttpResponse.ok(apiTransaction.get());
        } catch (Exception exception) {
            log.error("Error retrieving transaction with id: {}", id, exception);
            return HttpResponse.serverError();
        }
    }

    @Override
    public HttpResponse<@Valid TransactionApiResponse> updateTransaction(@NotNull String id,
            @NotNull @Valid UpdateTransactionRequest updateTransactionRequest) {
        try {
            var apiTransaction = transactionsCrudApplication.update(id, updateTransactionRequest);

            if (apiTransaction.isEmpty()) {
                return HttpResponse.notFound();
            }

            return HttpResponse.ok(apiTransaction.get());
        } catch (Exception exception) {
            log.error("Error updating transaction with id: {}", id, exception);
            return HttpResponse.serverError();
        }
    }

    @Override
    public HttpResponse<Void> deleteTransaction(@NotNull String id) {
        try {
            transactionsCrudApplication.delete(id);
            return HttpResponse.noContent();
        } catch (Exception exception) {
            log.error("Error deleting transaction with id: {}", id, exception);
            return HttpResponse.serverError();
        }
    }

}

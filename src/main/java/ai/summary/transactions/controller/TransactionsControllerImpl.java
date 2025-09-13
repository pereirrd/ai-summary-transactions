package ai.summary.transactions.controller;

import ai.summary.transactions.model.CreateTransactionRequest;
import ai.summary.transactions.model.GetAllTransactions200Response;
import ai.summary.transactions.model.TransactionApiResponse;
import ai.summary.transactions.model.UpdateTransactionRequest;
import ai.summary.transactions.domain.transaction.TransactionService;
import ai.summary.transactions.domain.transaction.mapper.TransactionMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class TransactionsControllerImpl implements TransactionsApi {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @Override
    public HttpResponse<@Valid GetAllTransactions200Response> getAllTransactions(Integer limit, Integer offset) {
        try {
            var domainTransactions = transactionService.getAllTransactions(limit, offset);

            var apiTransactions = transactionMapper.toApi(domainTransactions);

            var response = new GetAllTransactions200Response()
                    .transactions(apiTransactions)
                    .total(apiTransactions.size())
                    .limit(limit)
                    .offset(offset);

            return HttpResponse.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving all transactions", e);
            return HttpResponse.serverError();
        }
    }

    @Override
    public HttpResponse<@Valid TransactionApiResponse> createTransaction(
            @NotNull @Valid CreateTransactionRequest createTransactionRequest) {
        try {
            var domainTransaction = transactionMapper.toDomain(createTransactionRequest);
            var createdTransaction = transactionService.createTransaction(domainTransaction);

            var apiTransaction = transactionMapper.toApi(createdTransaction);
            return HttpResponse.created(apiTransaction);
        } catch (Exception e) {
            log.error("Error creating transaction", e);
            return HttpResponse.badRequest();
        }
    }

    @Override
    public HttpResponse<@Valid TransactionApiResponse> getTransactionById(@NotNull String id) {
        try {
            var domainTransaction = transactionService.getTransactionById(id);

            if (domainTransaction == null) {
                return HttpResponse.notFound();
            }

            var apiTransaction = transactionMapper.toApi(domainTransaction);
            return HttpResponse.ok(apiTransaction);
        } catch (Exception e) {
            log.error("Error retrieving transaction with id: {}", id, e);
            return HttpResponse.serverError();
        }
    }

    @Override
    public HttpResponse<@Valid TransactionApiResponse> updateTransaction(@NotNull String id,
            @NotNull @Valid UpdateTransactionRequest updateTransactionRequest) {
        try {
            var domainTransaction = transactionMapper.toDomain(updateTransactionRequest);
            var updatedTransaction = transactionService.updateTransaction(id, domainTransaction);

            if (updatedTransaction == null) {
                return HttpResponse.notFound();
            }

            var apiTransaction = transactionMapper.toApi(updatedTransaction);
            return HttpResponse.ok(apiTransaction);
        } catch (Exception e) {
            log.error("Error updating transaction with id: {}", id, e);
            return HttpResponse.serverError();
        }
    }

    @Override
    public HttpResponse<Void> deleteTransaction(@NotNull String id) {
        try {
            transactionService.deleteTransaction(id);
            return HttpResponse.noContent();
        } catch (Exception e) {
            log.error("Error deleting transaction with id: {}", id, e);
            return HttpResponse.serverError();
        }
    }

}

package ai.summary.transactions.controller;

import ai.summary.transactions.model.*;
import ai.summary.transactions.domain.transaction.TransactionService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class TransactionsControllerImpl implements DefaultApi {

    private final TransactionService transactionService;

    @Override
    public HttpResponse<@Valid GetAllTransactions200Response> getAllTransactions(Integer limit, Integer offset) {
        try {
            var domainTransactions = transactionService
                    .getAllTransactions(limit, offset);

            var apiTransactions = domainTransactions.stream()
                    .map(this::convertToApiTransaction)
                    .collect(Collectors.toList());

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
            var domainTransaction = convertToDomainTransaction(
                    createTransactionRequest);
            var createdTransaction = transactionService
                    .createTransaction(domainTransaction);

            var apiTransaction = convertToApiTransaction(createdTransaction);
            return HttpResponse.created(apiTransaction);
        } catch (Exception e) {
            log.error("Error creating transaction", e);
            return HttpResponse.badRequest();
        }
    }

    @Override
    public HttpResponse<@Valid TransactionApiResponse> getTransactionById(@NotNull String id) {
        try {
            var domainTransaction = transactionService
                    .getTransactionById(id);

            if (domainTransaction == null) {
                return HttpResponse.notFound();
            }

            var apiTransaction = convertToApiTransaction(domainTransaction);
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
            var domainTransaction = convertToDomainTransaction(
                    updateTransactionRequest);
            var updatedTransaction = transactionService
                    .updateTransaction(id, domainTransaction);

            if (updatedTransaction == null) {
                return HttpResponse.notFound();
            }

            var apiTransaction = convertToApiTransaction(updatedTransaction);
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

    private TransactionApiResponse convertToApiTransaction(
            ai.summary.transactions.domain.transaction.model.Transaction domainTransaction) {
        var apiTransaction = new TransactionApiResponse(
                domainTransaction.id().toString(),
                domainTransaction.date().atZone(java.time.ZoneId.systemDefault()),
                domainTransaction.amount(),
                domainTransaction.description(),
                null);

        if (domainTransaction.merchant() != null) {
            var apiMerchant = new ai.summary.transactions.model.Merchant(
                    domainTransaction.merchant().name(),
                    domainTransaction.merchant().category());
            apiTransaction.setMerchant(apiMerchant);
        }

        return apiTransaction;
    }

    private ai.summary.transactions.domain.transaction.model.Transaction convertToDomainTransaction(
            CreateTransactionRequest request) {
        var domainMerchant = new ai.summary.transactions.domain.transaction.model.Merchant(
                request.getMerchant().getName(),
                request.getMerchant().getCategory());

        return new ai.summary.transactions.domain.transaction.model.Transaction(
                null, // ID will be generated by the service
                request.getDate().toLocalDateTime(),
                request.getAmount(),
                request.getDescription(),
                domainMerchant);
    }

    private ai.summary.transactions.domain.transaction.model.Transaction convertToDomainTransaction(
            UpdateTransactionRequest request) {
        var domainMerchant = new ai.summary.transactions.domain.transaction.model.Merchant(
                request.getMerchant().getName(),
                request.getMerchant().getCategory());

        return new ai.summary.transactions.domain.transaction.model.Transaction(
                null, // ID will be set by the service
                request.getDate().toLocalDateTime(),
                request.getAmount(),
                request.getDescription(),
                domainMerchant);
    }

}

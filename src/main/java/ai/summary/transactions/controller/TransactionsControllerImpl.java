package ai.summary.transactions.controller;

import ai.summary.transactions.model.Transaction;
import ai.summary.transactions.domain.transaction.TransactionService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class TransactionsControllerImpl implements DefaultApi {

    private static final Logger logger = LoggerFactory.getLogger(TransactionsControllerImpl.class);

    private final TransactionService transactionService;

    @Inject
    public TransactionsControllerImpl(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public HttpResponse<@Valid Transaction> getTransactionById(@NotNull String id) {
        try {
            ai.summary.transactions.domain.transaction.model.Transaction domainTransaction = transactionService
                    .getTransactionById(id);

            if (domainTransaction == null) {
                return HttpResponse.notFound();
            }

            // Converter DomainTransaction para Transaction do modelo da API
            Transaction apiTransaction = new Transaction(
                    domainTransaction.id().toString(),
                    domainTransaction.date().atZone(java.time.ZoneId.systemDefault()),
                    domainTransaction.amount(),
                    domainTransaction.description(),
                    null);

            // Converter Merchant
            if (domainTransaction.merchant() != null) {
                ai.summary.transactions.model.Merchant apiMerchant = new ai.summary.transactions.model.Merchant(
                        domainTransaction.merchant().name(),
                        domainTransaction.merchant().category());
                apiTransaction.setMerchant(apiMerchant);
            }

            return HttpResponse.ok(apiTransaction);
        } catch (Exception e) {
            logger.error("Error retrieving transaction with id: {}", id, e);
            return HttpResponse.serverError();
        }
    }

}

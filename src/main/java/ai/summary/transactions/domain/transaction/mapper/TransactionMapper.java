package ai.summary.transactions.domain.transaction.mapper;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ai.summary.transactions.domain.transaction.model.Transaction;
import ai.summary.transactions.model.CreateTransactionRequest;
import ai.summary.transactions.model.TransactionApiResponse;
import ai.summary.transactions.model.UpdateTransactionRequest;

@Mapper(componentModel = "jsr330")
public interface TransactionMapper {

    Transaction toDomain(TransactionApiResponse transactionApiResponse);

    TransactionApiResponse toApi(Transaction transaction);

    List<Transaction> toDomain(List<TransactionApiResponse> transactionApiResponses);

    List<TransactionApiResponse> toApi(List<Transaction> transactions);

    // Métodos para converter de objetos de requisição para domínio
    @Mapping(target = "id", ignore = true)
    Transaction toDomain(CreateTransactionRequest createTransactionRequest);

    @Mapping(target = "id", ignore = true)
    Transaction toDomain(UpdateTransactionRequest updateTransactionRequest);

    // MapStruct precisa de ajuda para mapear LocalDateTime <-> ZonedDateTime
    default ZonedDateTime map(LocalDateTime value) {
        return value == null ? null : value.atZone(java.time.ZoneId.systemDefault());
    }

    default LocalDateTime map(ZonedDateTime value) {
        return value == null ? null : value.toLocalDateTime();
    }
}

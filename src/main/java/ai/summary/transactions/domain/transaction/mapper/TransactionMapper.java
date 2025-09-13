package ai.summary.transactions.domain.transaction.mapper;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

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

    // Methods to convert from request objects to domain
    @Mapping(target = "id", ignore = true)
    Transaction toDomain(CreateTransactionRequest createTransactionRequest);

    @Mapping(target = "id", ignore = true)
    Transaction toDomain(UpdateTransactionRequest updateTransactionRequest);

    // MapStruct needs help mapping LocalDateTime <-> ZonedDateTime
    default ZonedDateTime map(LocalDateTime value) {
        return value == null ? null : value.atZone(java.time.ZoneId.systemDefault());
    }

    default LocalDateTime map(ZonedDateTime value) {
        return value == null ? null : value.toLocalDateTime();
    }

    // Method to convert from OpenSearch Object to Transaction
    @SuppressWarnings("unchecked")
    default Transaction fromOpenSearchObject(Object source) {
        if (source == null) {
            return null;
        }

        if (source instanceof Map) {
            Map<String, Object> sourceMap = (Map<String, Object>) source;

            return new Transaction(
                    sourceMap.get("id") != null ? java.util.UUID.fromString(sourceMap.get("id").toString()) : null,
                    sourceMap.get("date") != null ? LocalDateTime.parse(sourceMap.get("date").toString()) : null,
                    sourceMap.get("amount") != null ? new java.math.BigDecimal(sourceMap.get("amount").toString())
                            : null,
                    (String) sourceMap.get("description"),
                    sourceMap.get("merchant") != null ? fromMerchantMap((Map<String, Object>) sourceMap.get("merchant"))
                            : null);
        }

        throw new IllegalArgumentException("Cannot convert object to Transaction: " + source.getClass());
    }

    // Helper method to convert merchant map to Merchant object
    default ai.summary.transactions.domain.transaction.model.Merchant fromMerchantMap(Map<String, Object> merchantMap) {
        if (merchantMap == null) {
            return null;
        }

        return new ai.summary.transactions.domain.transaction.model.Merchant(
                (String) merchantMap.get("name"),
                (String) merchantMap.get("category"));
    }

}

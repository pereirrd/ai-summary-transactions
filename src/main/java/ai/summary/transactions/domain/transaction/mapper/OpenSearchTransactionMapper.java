package ai.summary.transactions.domain.transaction.mapper;

import java.util.Map;
import java.time.LocalDateTime;

import ai.summary.transactions.domain.transaction.model.Merchant;
import ai.summary.transactions.domain.transaction.model.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "jsr330")
public interface OpenSearchTransactionMapper {

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
    default Merchant fromMerchantMap(Map<String, Object> merchantMap) {
        if (merchantMap == null) {
            return null;
        }

        return new Merchant(
                (String) merchantMap.get("name"),
                (String) merchantMap.get("category"));
    }
}

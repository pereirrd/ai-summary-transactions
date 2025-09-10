package ai.summary.transactions.domain.transaction.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Transaction(
        UUID id,
        LocalDateTime date,
        BigDecimal amount,
        String description,
        Merchant merchant) {

}

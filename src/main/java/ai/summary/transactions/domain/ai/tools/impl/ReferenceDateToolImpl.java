package ai.summary.transactions.domain.ai.tools.impl;

import java.time.LocalDate;

import ai.summary.transactions.domain.ai.tools.ReferenceDateTool;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ReferenceDateToolImpl implements ReferenceDateTool {

    @Override
    public LocalDate referenceDate() {
        var today = LocalDate.now();

        log.info("Getting reference date: {}", today);

        return today;
    }
}

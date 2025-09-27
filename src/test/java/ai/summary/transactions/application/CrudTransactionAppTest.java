package ai.summary.transactions.application;

import ai.summary.transactions.domain.transaction.TransactionService;
import ai.summary.transactions.domain.transaction.mapper.TransactionMapper;
import ai.summary.transactions.domain.transaction.model.Merchant;
import ai.summary.transactions.domain.transaction.model.Transaction;
import ai.summary.transactions.model.CreateTransactionRequest;
import ai.summary.transactions.model.MerchantApiResponse;
import ai.summary.transactions.model.TransactionApiResponse;
import ai.summary.transactions.model.UpdateTransactionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CrudTransactionApp Tests")
class CrudTransactionAppTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private CrudTransactionApp crudTransactionApp;

    private final String TRANSACTION_ID = "123e4567-e89b-12d3-a456-426614174000";
    private final LocalDate START_DATE = LocalDate.of(2024, 1, 1);
    private final LocalDate END_DATE = LocalDate.of(2024, 1, 31);
    private final int LIMIT = 10;
    private final int OFFSET = 0;

    private Transaction domainTransaction;
    private TransactionApiResponse apiTransaction;
    private CreateTransactionRequest createRequest;
    private UpdateTransactionRequest updateRequest;
    private Merchant domainMerchant;
    private MerchantApiResponse apiMerchant;

    @BeforeEach
    void setUp() {
        var uuid = UUID.fromString(TRANSACTION_ID);
        var dateTime = LocalDateTime.of(2024, 1, 15, 14, 30);
        var zonedDateTime = ZonedDateTime.of(dateTime, java.time.ZoneOffset.UTC);
        var amount = new BigDecimal("150.50");

        domainMerchant = new Merchant("Supermercado ABC", "Alimentação");
        apiMerchant = new MerchantApiResponse("Supermercado ABC", "Alimentação");

        domainTransaction = new Transaction(uuid, dateTime, amount, "Compra no supermercado", domainMerchant);
        apiTransaction = new TransactionApiResponse(TRANSACTION_ID, zonedDateTime, amount, "Compra no supermercado",
                apiMerchant);

        createRequest = new CreateTransactionRequest(zonedDateTime, amount, "Compra no supermercado", apiMerchant);
        updateRequest = new UpdateTransactionRequest(zonedDateTime, amount, "Compra atualizada", apiMerchant);
    }

    @Test
    @DisplayName("Deve buscar transações por filtros com sucesso")
    void shouldFindTransactionsByFiltersSuccessfully() {
        // Given
        var domainTransactions = List.of(domainTransaction);
        var apiTransactions = List.of(apiTransaction);

        when(transactionService.findByFilters(START_DATE, END_DATE, LIMIT, OFFSET))
                .thenReturn(Optional.of(domainTransactions));
        when(transactionMapper.toApi(domainTransactions))
                .thenReturn(apiTransactions);

        // When
        var result = crudTransactionApp.findByFilters(START_DATE, END_DATE, LIMIT, OFFSET);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(1);
        assertThat(result.get().get(0)).isEqualTo(apiTransaction);

        verify(transactionService).findByFilters(START_DATE, END_DATE, LIMIT, OFFSET);
        verify(transactionMapper).toApi(domainTransactions);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando não há transações")
    void shouldReturnEmptyOptionalWhenNoTransactionsFound() {
        // Given
        when(transactionService.findByFilters(START_DATE, END_DATE, LIMIT, OFFSET))
                .thenReturn(Optional.empty());

        // When
        var result = crudTransactionApp.findByFilters(START_DATE, END_DATE, LIMIT, OFFSET);

        // Then
        assertThat(result).isEmpty();
        verify(transactionService).findByFilters(START_DATE, END_DATE, LIMIT, OFFSET);
    }

    @Test
    @DisplayName("Deve buscar transação por ID com sucesso")
    void shouldGetTransactionByIdSuccessfully() {
        // Given
        when(transactionService.getById(TRANSACTION_ID))
                .thenReturn(Optional.of(domainTransaction));
        when(transactionMapper.toApi(domainTransaction))
                .thenReturn(apiTransaction);

        // When
        var result = crudTransactionApp.getById(TRANSACTION_ID);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(apiTransaction);

        verify(transactionService).getById(TRANSACTION_ID);
        verify(transactionMapper).toApi(domainTransaction);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando transação não existe")
    void shouldReturnEmptyOptionalWhenTransactionNotFound() {
        // Given
        when(transactionService.getById(TRANSACTION_ID))
                .thenReturn(Optional.empty());

        // When
        var result = crudTransactionApp.getById(TRANSACTION_ID);

        // Then
        assertThat(result).isEmpty();
        verify(transactionService).getById(TRANSACTION_ID);
    }

    @Test
    @DisplayName("Deve criar transação com sucesso")
    void shouldCreateTransactionSuccessfully() {
        // Given
        when(transactionMapper.toDomain(createRequest))
                .thenReturn(domainTransaction);
        when(transactionService.create(domainTransaction))
                .thenReturn(domainTransaction);
        when(transactionMapper.toApi(domainTransaction))
                .thenReturn(apiTransaction);

        // When
        var result = crudTransactionApp.create(createRequest);

        // Then
        assertThat(result).isEqualTo(apiTransaction);

        verify(transactionMapper).toDomain(createRequest);
        verify(transactionService).create(domainTransaction);
        verify(transactionMapper).toApi(domainTransaction);
    }

    @Test
    @DisplayName("Deve atualizar transação com sucesso")
    void shouldUpdateTransactionSuccessfully() {
        // Given
        var updatedDomainTransaction = new Transaction(
                UUID.fromString(TRANSACTION_ID),
                LocalDateTime.of(2024, 1, 16, 15, 0),
                new BigDecimal("200.00"),
                "Compra atualizada",
                domainMerchant);

        var updatedApiTransaction = new TransactionApiResponse(
                TRANSACTION_ID,
                ZonedDateTime.of(2024, 1, 16, 15, 0, 0, 0, java.time.ZoneOffset.UTC),
                new BigDecimal("200.00"),
                "Compra atualizada",
                apiMerchant);

        when(transactionMapper.toDomain(updateRequest))
                .thenReturn(domainTransaction);
        when(transactionService.update(TRANSACTION_ID, domainTransaction))
                .thenReturn(Optional.of(updatedDomainTransaction));
        when(transactionMapper.toApi(updatedDomainTransaction))
                .thenReturn(updatedApiTransaction);

        // When
        var result = crudTransactionApp.update(TRANSACTION_ID, updateRequest);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(updatedApiTransaction);

        verify(transactionMapper).toDomain(updateRequest);
        verify(transactionService).update(TRANSACTION_ID, domainTransaction);
        verify(transactionMapper).toApi(updatedDomainTransaction);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio ao tentar atualizar transação inexistente")
    void shouldReturnEmptyOptionalWhenUpdatingNonExistentTransaction() {
        // Given
        when(transactionMapper.toDomain(updateRequest))
                .thenReturn(domainTransaction);
        when(transactionService.update(TRANSACTION_ID, domainTransaction))
                .thenReturn(Optional.empty());

        // When
        var result = crudTransactionApp.update(TRANSACTION_ID, updateRequest);

        // Then
        assertThat(result).isEmpty();

        verify(transactionMapper).toDomain(updateRequest);
        verify(transactionService).update(TRANSACTION_ID, domainTransaction);
    }

    @Test
    @DisplayName("Deve deletar transação com sucesso")
    void shouldDeleteTransactionSuccessfully() {
        // Given - não precisa configurar mock pois método é void

        // When
        crudTransactionApp.delete(TRANSACTION_ID);

        // Then
        verify(transactionService).delete(TRANSACTION_ID);
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando busca por filtros falha")
    void shouldThrowRuntimeExceptionWhenFindByFiltersFails() {
        // Given
        var exception = new RuntimeException("Erro de conexão com OpenSearch");
        when(transactionService.findByFilters(START_DATE, END_DATE, LIMIT, OFFSET))
                .thenThrow(exception);

        // When & Then
        assertThatThrownBy(() -> crudTransactionApp.findByFilters(START_DATE, END_DATE, LIMIT, OFFSET))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to retrieve transactions")
                .hasCause(exception);

        verify(transactionService).findByFilters(START_DATE, END_DATE, LIMIT, OFFSET);
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando busca por ID falha")
    void shouldThrowRuntimeExceptionWhenGetByIdFails() {
        // Given
        var exception = new RuntimeException("Erro de conexão com OpenSearch");
        when(transactionService.getById(TRANSACTION_ID))
                .thenThrow(exception);

        // When & Then
        assertThatThrownBy(() -> crudTransactionApp.getById(TRANSACTION_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to retrieve transaction")
                .hasCause(exception);

        verify(transactionService).getById(TRANSACTION_ID);
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando criação falha")
    void shouldThrowRuntimeExceptionWhenCreateFails() {
        // Given
        var exception = new RuntimeException("Erro ao salvar no OpenSearch");
        when(transactionMapper.toDomain(createRequest))
                .thenReturn(domainTransaction);
        when(transactionService.create(domainTransaction))
                .thenThrow(exception);

        // When & Then
        assertThatThrownBy(() -> crudTransactionApp.create(createRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to create transaction")
                .hasCause(exception);

        verify(transactionMapper).toDomain(createRequest);
        verify(transactionService).create(domainTransaction);
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando atualização falha")
    void shouldThrowRuntimeExceptionWhenUpdateFails() {
        // Given
        var exception = new RuntimeException("Erro ao atualizar no OpenSearch");
        when(transactionMapper.toDomain(updateRequest))
                .thenReturn(domainTransaction);
        when(transactionService.update(TRANSACTION_ID, domainTransaction))
                .thenThrow(exception);

        // When & Then
        assertThatThrownBy(() -> crudTransactionApp.update(TRANSACTION_ID, updateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to update transaction")
                .hasCause(exception);

        verify(transactionMapper).toDomain(updateRequest);
        verify(transactionService).update(TRANSACTION_ID, domainTransaction);
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando deleção falha")
    void shouldThrowRuntimeExceptionWhenDeleteFails() {
        // Given
        var exception = new RuntimeException("Erro ao deletar do OpenSearch");
        doThrow(exception).when(transactionService).delete(TRANSACTION_ID);

        // When & Then
        assertThatThrownBy(() -> crudTransactionApp.delete(TRANSACTION_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to delete transaction")
                .hasCause(exception);

        verify(transactionService).delete(TRANSACTION_ID);
    }

    @Test
    @DisplayName("Deve buscar transações com filtros nulos")
    void shouldFindTransactionsWithNullFilters() {
        // Given
        var domainTransactions = List.of(domainTransaction);
        var apiTransactions = List.of(apiTransaction);

        when(transactionService.findByFilters(null, null, LIMIT, OFFSET))
                .thenReturn(Optional.of(domainTransactions));
        when(transactionMapper.toApi(domainTransactions))
                .thenReturn(apiTransactions);

        // When
        var result = crudTransactionApp.findByFilters(null, null, LIMIT, OFFSET);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(1);

        verify(transactionService).findByFilters(null, null, LIMIT, OFFSET);
        verify(transactionMapper).toApi(domainTransactions);
    }

    @Test
    @DisplayName("Deve buscar transações com múltiplos resultados")
    void shouldFindMultipleTransactions() {
        // Given
        var secondTransaction = new Transaction(
                UUID.randomUUID(),
                LocalDateTime.of(2024, 1, 16, 10, 0),
                new BigDecimal("75.25"),
                "Segunda compra",
                domainMerchant);
        var domainTransactions = List.of(domainTransaction, secondTransaction);
        var secondApiTransaction = new TransactionApiResponse(
                secondTransaction.id().toString(),
                ZonedDateTime.of(2024, 1, 16, 10, 0, 0, 0, java.time.ZoneOffset.UTC),
                new BigDecimal("75.25"),
                "Segunda compra",
                apiMerchant);
        var apiTransactions = List.of(apiTransaction, secondApiTransaction);

        when(transactionService.findByFilters(START_DATE, END_DATE, LIMIT, OFFSET))
                .thenReturn(Optional.of(domainTransactions));
        when(transactionMapper.toApi(domainTransactions))
                .thenReturn(apiTransactions);

        // When
        var result = crudTransactionApp.findByFilters(START_DATE, END_DATE, LIMIT, OFFSET);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(2);
        assertThat(result.get()).containsExactly(apiTransaction, secondApiTransaction);

        verify(transactionService).findByFilters(START_DATE, END_DATE, LIMIT, OFFSET);
        verify(transactionMapper).toApi(domainTransactions);
    }
}

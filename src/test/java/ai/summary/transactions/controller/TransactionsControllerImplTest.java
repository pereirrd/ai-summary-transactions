package ai.summary.transactions.controller;

import ai.summary.transactions.application.CrudTransactionApp;
import ai.summary.transactions.model.CreateTransactionRequest;
import ai.summary.transactions.model.MerchantApiResponse;
import ai.summary.transactions.model.TransactionApiResponse;
import ai.summary.transactions.model.UpdateTransactionRequest;
import io.micronaut.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionsControllerImpl Tests")
class TransactionsControllerImplTest {

    @Mock
    private CrudTransactionApp transactionsCrudApplication;

    @InjectMocks
    private TransactionsControllerImpl transactionsController;

    private final String TRANSACTION_ID = "123e4567-e89b-12d3-a456-426614174000";
    private final LocalDate START_DATE = LocalDate.of(2024, 1, 1);
    private final LocalDate END_DATE = LocalDate.of(2024, 1, 31);
    private final Integer LIMIT = 10;
    private final Integer OFFSET = 0;

    private TransactionApiResponse transactionResponse;
    private CreateTransactionRequest createRequest;
    private UpdateTransactionRequest updateRequest;
    private MerchantApiResponse merchantResponse;

    @BeforeEach
    void setUp() {
        var zonedDateTime = ZonedDateTime.of(2024, 1, 15, 14, 30, 0, 0, java.time.ZoneOffset.UTC);
        var amount = new BigDecimal("150.50");

        merchantResponse = new MerchantApiResponse("Supermercado ABC", "Alimentação");
        transactionResponse = new TransactionApiResponse(
                TRANSACTION_ID, zonedDateTime, amount, "Compra no supermercado", merchantResponse);

        createRequest = new CreateTransactionRequest(zonedDateTime, amount, "Compra no supermercado", merchantResponse);
        updateRequest = new UpdateTransactionRequest(zonedDateTime, amount, "Compra atualizada", merchantResponse);
    }

    @Test
    @DisplayName("Deve buscar todas as transações com sucesso")
    void shouldGetAllTransactionsSuccessfully() {
        // Given
        var transactions = List.of(transactionResponse);
        when(transactionsCrudApplication.findByFilters(START_DATE, END_DATE, LIMIT, OFFSET))
                .thenReturn(Optional.of(transactions));

        // When
        var response = transactionsController.getAllTransactions(LIMIT, OFFSET, START_DATE, END_DATE);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus().compareTo(HttpStatus.OK)).isZero();
        assertThat(response.body()).isNotNull();

        var responseBody = response.body();
        assertThat(responseBody.getTransactions()).hasSize(1);
        assertThat(responseBody.getTransactions().get(0)).isEqualTo(transactionResponse);
        assertThat(responseBody.getTotal()).isEqualTo(1);
        assertThat(responseBody.getLimit()).isEqualTo(LIMIT);
        assertThat(responseBody.getOffset()).isEqualTo(OFFSET);

        verify(transactionsCrudApplication).findByFilters(START_DATE, END_DATE, LIMIT, OFFSET);
    }

    @Test
    @DisplayName("Deve usar valores padrão quando limit e offset são nulos")
    void shouldUseDefaultValuesWhenLimitAndOffsetAreNull() {
        // Given
        var transactions = List.of(transactionResponse);
        when(transactionsCrudApplication.findByFilters(START_DATE, END_DATE, 20, 0))
                .thenReturn(Optional.of(transactions));

        // When
        var response = transactionsController.getAllTransactions(null, null, START_DATE, END_DATE);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus().compareTo(HttpStatus.OK)).isZero();
        assertThat(response.body()).isNotNull();

        var responseBody = response.body();
        assertThat(responseBody.getLimit()).isEqualTo(20);
        assertThat(responseBody.getOffset()).isEqualTo(0);

        verify(transactionsCrudApplication).findByFilters(START_DATE, END_DATE, 20, 0);
    }

    @Test
    @DisplayName("Deve retornar 404 quando não há transações")
    void shouldReturnNotFoundWhenNoTransactionsFound() {
        // Given
        when(transactionsCrudApplication.findByFilters(START_DATE, END_DATE, LIMIT, OFFSET))
                .thenReturn(Optional.empty());

        // When
        var response = transactionsController.getAllTransactions(LIMIT, OFFSET, START_DATE, END_DATE);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus().compareTo(HttpStatus.NOT_FOUND)).isZero();

        verify(transactionsCrudApplication).findByFilters(START_DATE, END_DATE, LIMIT, OFFSET);
    }

    @Test
    @DisplayName("Deve retornar erro 500 quando busca falha")
    void shouldReturnServerErrorWhenFindFails() {
        // Given
        when(transactionsCrudApplication.findByFilters(START_DATE, END_DATE, LIMIT, OFFSET))
                .thenThrow(new RuntimeException("Erro de conexão"));

        // When
        var response = transactionsController.getAllTransactions(LIMIT, OFFSET, START_DATE, END_DATE);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus().compareTo(HttpStatus.INTERNAL_SERVER_ERROR)).isZero();

        verify(transactionsCrudApplication).findByFilters(START_DATE, END_DATE, LIMIT, OFFSET);
    }

    @Test
    @DisplayName("Deve criar transação com sucesso")
    void shouldCreateTransactionSuccessfully() {
        // Given
        when(transactionsCrudApplication.create(createRequest))
                .thenReturn(transactionResponse);

        // When
        var response = transactionsController.createTransaction(createRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus().compareTo(HttpStatus.CREATED)).isZero();
        assertThat(response.body()).isEqualTo(transactionResponse);

        verify(transactionsCrudApplication).create(createRequest);
    }

    @Test
    @DisplayName("Deve retornar erro 400 quando criação falha")
    void shouldReturnBadRequestWhenCreateFails() {
        // Given
        when(transactionsCrudApplication.create(createRequest))
                .thenThrow(new RuntimeException("Dados inválidos"));

        // When
        var response = transactionsController.createTransaction(createRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus().compareTo(HttpStatus.BAD_REQUEST)).isZero();

        verify(transactionsCrudApplication).create(createRequest);
    }

    @Test
    @DisplayName("Deve buscar transação por ID com sucesso")
    void shouldGetTransactionByIdSuccessfully() {
        // Given
        when(transactionsCrudApplication.getById(TRANSACTION_ID))
                .thenReturn(Optional.of(transactionResponse));

        // When
        var response = transactionsController.getTransactionById(TRANSACTION_ID);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus().compareTo(HttpStatus.OK)).isZero();
        assertThat(response.body()).isEqualTo(transactionResponse);

        verify(transactionsCrudApplication).getById(TRANSACTION_ID);
    }

    @Test
    @DisplayName("Deve retornar 404 quando transação não existe")
    void shouldReturnNotFoundWhenTransactionDoesNotExist() {
        // Given
        when(transactionsCrudApplication.getById(TRANSACTION_ID))
                .thenReturn(Optional.empty());

        // When
        var response = transactionsController.getTransactionById(TRANSACTION_ID);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus().compareTo(HttpStatus.NOT_FOUND)).isZero();

        verify(transactionsCrudApplication).getById(TRANSACTION_ID);
    }

    @Test
    @DisplayName("Deve retornar erro 500 quando busca por ID falha")
    void shouldReturnServerErrorWhenGetByIdFails() {
        // Given
        when(transactionsCrudApplication.getById(TRANSACTION_ID))
                .thenThrow(new RuntimeException("Erro de conexão"));

        // When
        var response = transactionsController.getTransactionById(TRANSACTION_ID);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus().compareTo(HttpStatus.INTERNAL_SERVER_ERROR)).isZero();

        verify(transactionsCrudApplication).getById(TRANSACTION_ID);
    }

    @Test
    @DisplayName("Deve atualizar transação com sucesso")
    void shouldUpdateTransactionSuccessfully() {
        // Given
        var updatedResponse = new TransactionApiResponse(
                TRANSACTION_ID,
                ZonedDateTime.of(2024, 1, 16, 15, 0, 0, 0, java.time.ZoneOffset.UTC),
                new BigDecimal("200.00"),
                "Compra atualizada",
                merchantResponse);

        when(transactionsCrudApplication.update(TRANSACTION_ID, updateRequest))
                .thenReturn(Optional.of(updatedResponse));

        // When
        var response = transactionsController.updateTransaction(TRANSACTION_ID, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus().compareTo(HttpStatus.OK)).isZero();
        assertThat(response.body()).isEqualTo(updatedResponse);

        verify(transactionsCrudApplication).update(TRANSACTION_ID, updateRequest);
    }

    @Test
    @DisplayName("Deve retornar 404 quando transação para atualização não existe")
    void shouldReturnNotFoundWhenTransactionToUpdateDoesNotExist() {
        // Given
        when(transactionsCrudApplication.update(TRANSACTION_ID, updateRequest))
                .thenReturn(Optional.empty());

        // When
        var response = transactionsController.updateTransaction(TRANSACTION_ID, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus().compareTo(HttpStatus.NOT_FOUND)).isZero();

        verify(transactionsCrudApplication).update(TRANSACTION_ID, updateRequest);
    }

    @Test
    @DisplayName("Deve retornar erro 500 quando atualização falha")
    void shouldReturnServerErrorWhenUpdateFails() {
        // Given
        when(transactionsCrudApplication.update(TRANSACTION_ID, updateRequest))
                .thenThrow(new RuntimeException("Erro de conexão"));

        // When
        var response = transactionsController.updateTransaction(TRANSACTION_ID, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus().compareTo(HttpStatus.INTERNAL_SERVER_ERROR)).isZero();

        verify(transactionsCrudApplication).update(TRANSACTION_ID, updateRequest);
    }

    @Test
    @DisplayName("Deve deletar transação com sucesso")
    void shouldDeleteTransactionSuccessfully() {
        // Given - não precisa configurar mock pois método é void

        // When
        var response = transactionsController.deleteTransaction(TRANSACTION_ID);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus().compareTo(HttpStatus.NO_CONTENT)).isZero();

        verify(transactionsCrudApplication).delete(TRANSACTION_ID);
    }

    @Test
    @DisplayName("Deve retornar erro 500 quando deleção falha")
    void shouldReturnServerErrorWhenDeleteFails() {
        // Given
        doThrow(new RuntimeException("Erro de conexão")).when(transactionsCrudApplication).delete(TRANSACTION_ID);

        // When
        var response = transactionsController.deleteTransaction(TRANSACTION_ID);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus().compareTo(HttpStatus.INTERNAL_SERVER_ERROR)).isZero();

        verify(transactionsCrudApplication).delete(TRANSACTION_ID);
    }
}

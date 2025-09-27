package ai.summary.transactions.domain.transaction.impl;

import ai.summary.transactions.core.config.OpenSearchConfig;
import ai.summary.transactions.domain.transaction.mapper.OpenSearchTransactionMapper;
import ai.summary.transactions.domain.transaction.model.Merchant;
import ai.summary.transactions.domain.transaction.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch.core.*;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionServiceImpl Tests")
class TransactionServiceImplTest {

    @Mock
    private OpenSearchClient openSearchClient;

    @Mock
    private OpenSearchConfig openSearchConfig;

    @Mock
    private OpenSearchTransactionMapper openSearchTransactionMapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private final String TRANSACTION_ID = "123e4567-e89b-12d3-a456-426614174000";
    private final String INDEX_NAME = "transactions";
    private final LocalDate START_DATE = LocalDate.of(2024, 1, 1);
    private final LocalDate END_DATE = LocalDate.of(2024, 1, 31);

    private Transaction transaction;
    private Merchant merchant;
    private Map<String, Object> openSearchObject;

    @BeforeEach
    void setUp() {
        var uuid = UUID.fromString(TRANSACTION_ID);
        var dateTime = LocalDateTime.of(2024, 1, 15, 14, 30);
        var amount = new BigDecimal("150.50");

        merchant = new Merchant("Supermercado ABC", "Alimentação");
        transaction = new Transaction(uuid, dateTime, amount, "Compra no supermercado", merchant);

        openSearchObject = new HashMap<>();
        openSearchObject.put("id", TRANSACTION_ID);
        openSearchObject.put("date", dateTime.toString());
        openSearchObject.put("amount", amount.toString());
        openSearchObject.put("description", "Compra no supermercado");

        Map<String, Object> merchantMap = new HashMap<>();
        merchantMap.put("name", "Supermercado ABC");
        merchantMap.put("category", "Alimentação");
        openSearchObject.put("merchant", merchantMap);

        when(openSearchConfig.getTransactionsIndex()).thenReturn(INDEX_NAME);
    }

    @Test
    @DisplayName("Deve buscar transações por filtros com sucesso - com datas")
    void shouldFindTransactionsByFiltersWithDatesSuccessfully() throws IOException {
        // Given
        var searchResponse = mock(SearchResponse.class);
        var hitsMetadata = mock(HitsMetadata.class);
        var hit = mock(Hit.class);

        when(openSearchClient.search(any(SearchRequest.class), eq(Object.class)))
                .thenReturn(searchResponse);
        when(searchResponse.hits()).thenReturn(hitsMetadata);
        when(hitsMetadata.hits()).thenReturn(List.of(hit));
        when(hit.source()).thenReturn(openSearchObject);
        when(openSearchTransactionMapper.fromOpenSearchObject(openSearchObject))
                .thenReturn(transaction);

        // When
        var result = transactionService.findByFilters(START_DATE, END_DATE, 10, 0);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(1);
        assertThat(result.get().get(0)).isEqualTo(transaction);

        verify(openSearchClient).search(any(SearchRequest.class), eq(Object.class));
        verify(openSearchTransactionMapper).fromOpenSearchObject(openSearchObject);
    }

    @Test
    @DisplayName("Deve buscar todas as transações quando não há filtros de data")
    void shouldFindAllTransactionsWhenNoDateFilters() throws IOException {
        // Given
        var searchResponse = mock(SearchResponse.class);
        var hitsMetadata = mock(HitsMetadata.class);
        var hit = mock(Hit.class);

        when(openSearchClient.search(any(SearchRequest.class), eq(Object.class)))
                .thenReturn(searchResponse);
        when(searchResponse.hits()).thenReturn(hitsMetadata);
        when(hitsMetadata.hits()).thenReturn(List.of(hit));
        when(hit.source()).thenReturn(openSearchObject);
        when(openSearchTransactionMapper.fromOpenSearchObject(openSearchObject))
                .thenReturn(transaction);

        // When
        var result = transactionService.findByFilters(null, null, 10, 0);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(1);

        verify(openSearchClient).search(any(SearchRequest.class), eq(Object.class));
        verify(openSearchTransactionMapper).fromOpenSearchObject(openSearchObject);
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando busca por filtros falha")
    void shouldThrowRuntimeExceptionWhenFindByFiltersFails() throws IOException {
        // Given
        when(openSearchClient.search(any(SearchRequest.class), eq(Object.class)))
                .thenThrow(new IOException("Erro de conexão com OpenSearch"));

        // When & Then
        assertThatThrownBy(() -> transactionService.findByFilters(START_DATE, END_DATE, 10, 0))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to retrieve transactions")
                .hasCauseInstanceOf(IOException.class);

        verify(openSearchClient).search(any(SearchRequest.class), eq(Object.class));
    }

    @Test
    @DisplayName("Deve buscar transação por ID com sucesso")
    void shouldGetTransactionByIdSuccessfully() throws IOException {
        // Given
        var getResponse = mock(GetResponse.class);
        when(openSearchClient.get(any(GetRequest.class), eq(Object.class)))
                .thenReturn(getResponse);
        when(getResponse.found()).thenReturn(true);
        when(getResponse.source()).thenReturn(openSearchObject);
        when(openSearchTransactionMapper.fromOpenSearchObject(openSearchObject))
                .thenReturn(transaction);

        // When
        var result = transactionService.getById(TRANSACTION_ID);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(transaction);

        verify(openSearchClient).get(any(GetRequest.class), eq(Object.class));
        verify(openSearchTransactionMapper).fromOpenSearchObject(openSearchObject);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando transação não existe")
    void shouldReturnEmptyOptionalWhenTransactionNotFound() throws IOException {
        // Given
        var getResponse = mock(GetResponse.class);
        when(openSearchClient.get(any(GetRequest.class), eq(Object.class)))
                .thenReturn(getResponse);
        when(getResponse.found()).thenReturn(false);

        // When
        var result = transactionService.getById(TRANSACTION_ID);

        // Then
        assertThat(result).isEmpty();

        verify(openSearchClient).get(any(GetRequest.class), eq(Object.class));
        verify(openSearchTransactionMapper, never()).fromOpenSearchObject(any());
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando busca por ID falha")
    void shouldThrowRuntimeExceptionWhenGetByIdFails() throws IOException {
        // Given
        when(openSearchClient.get(any(GetRequest.class), eq(Object.class)))
                .thenThrow(new IOException("Erro de conexão com OpenSearch"));

        // When & Then
        assertThatThrownBy(() -> transactionService.getById(TRANSACTION_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to retrieve transaction")
                .hasCauseInstanceOf(IOException.class);

        verify(openSearchClient).get(any(GetRequest.class), eq(Object.class));
    }

    @Test
    @DisplayName("Deve criar transação com sucesso")
    void shouldCreateTransactionSuccessfully() throws IOException {
        // Given
        var indexResponse = mock(IndexResponse.class);
        when(openSearchClient.index(any(IndexRequest.class)))
                .thenReturn(indexResponse);
        when(indexResponse.result()).thenReturn(Result.Created);

        // When
        var result = transactionService.create(transaction);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(transaction.id());
        assertThat(result.date()).isEqualTo(transaction.date());
        assertThat(result.amount()).isEqualTo(transaction.amount());
        assertThat(result.description()).isEqualTo(transaction.description());
        assertThat(result.merchant()).isEqualTo(transaction.merchant());

        verify(openSearchClient).index(any(IndexRequest.class));
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando criação falha")
    void shouldThrowRuntimeExceptionWhenCreateFails() throws IOException {
        // Given
        when(openSearchClient.index(any(IndexRequest.class)))
                .thenThrow(new IOException("Erro ao salvar no OpenSearch"));

        // When & Then
        assertThatThrownBy(() -> transactionService.create(transaction))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to create transaction")
                .hasCauseInstanceOf(IOException.class);

        verify(openSearchClient).index(any(IndexRequest.class));
    }

    @Test
    @DisplayName("Deve atualizar transação com sucesso")
    void shouldUpdateTransactionSuccessfully() throws IOException {
        // Given
        var getResponse = mock(GetResponse.class);
        var indexResponse = mock(IndexResponse.class);

        when(openSearchClient.get(any(GetRequest.class), eq(Object.class)))
                .thenReturn(getResponse);
        when(getResponse.found()).thenReturn(true);
        when(getResponse.source()).thenReturn(openSearchObject);
        when(openSearchTransactionMapper.fromOpenSearchObject(openSearchObject))
                .thenReturn(transaction);
        when(openSearchClient.index(any(IndexRequest.class)))
                .thenReturn(indexResponse);
        when(indexResponse.result()).thenReturn(Result.Updated);

        var updatedTransaction = new Transaction(
                transaction.id(),
                transaction.date(),
                new BigDecimal("200.00"),
                "Descrição atualizada",
                transaction.merchant());

        // When
        var result = transactionService.update(TRANSACTION_ID, updatedTransaction);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(transaction.id());
        assertThat(result.get().amount()).isEqualTo(new BigDecimal("200.00"));
        assertThat(result.get().description()).isEqualTo("Descrição atualizada");

        verify(openSearchClient).get(any(GetRequest.class), eq(Object.class));
        verify(openSearchClient).index(any(IndexRequest.class));
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando transação para atualização não existe")
    void shouldReturnEmptyOptionalWhenTransactionToUpdateNotFound() throws IOException {
        // Given
        var getResponse = mock(GetResponse.class);
        when(openSearchClient.get(any(GetRequest.class), eq(Object.class)))
                .thenReturn(getResponse);
        when(getResponse.found()).thenReturn(false);

        // When
        var result = transactionService.update(TRANSACTION_ID, transaction);

        // Then
        assertThat(result).isEmpty();

        verify(openSearchClient).get(any(GetRequest.class), eq(Object.class));
        verify(openSearchClient, never()).index(any(IndexRequest.class));
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando atualização falha")
    void shouldThrowRuntimeExceptionWhenUpdateFails() throws IOException {
        // Given
        var getResponse = mock(GetResponse.class);
        when(openSearchClient.get(any(GetRequest.class), eq(Object.class)))
                .thenReturn(getResponse);
        when(getResponse.found()).thenReturn(true);
        when(getResponse.source()).thenReturn(openSearchObject);
        when(openSearchTransactionMapper.fromOpenSearchObject(openSearchObject))
                .thenReturn(transaction);
        when(openSearchClient.index(any(IndexRequest.class)))
                .thenThrow(new IOException("Erro ao atualizar no OpenSearch"));

        // When & Then
        assertThatThrownBy(() -> transactionService.update(TRANSACTION_ID, transaction))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to update transaction")
                .hasCauseInstanceOf(IOException.class);

        verify(openSearchClient).get(any(GetRequest.class), eq(Object.class));
        verify(openSearchClient).index(any(IndexRequest.class));
    }

    @Test
    @DisplayName("Deve deletar transação com sucesso")
    void shouldDeleteTransactionSuccessfully() throws IOException {
        // Given
        var deleteResponse = mock(DeleteResponse.class);
        when(openSearchClient.delete(any(DeleteRequest.class)))
                .thenReturn(deleteResponse);
        when(deleteResponse.result()).thenReturn(Result.Deleted);

        // When
        transactionService.delete(TRANSACTION_ID);

        // Then
        verify(openSearchClient).delete(any(DeleteRequest.class));
    }

    @Test
    @DisplayName("Deve lidar com transação não encontrada na deleção")
    void shouldHandleTransactionNotFoundInDeletion() throws IOException {
        // Given
        var deleteResponse = mock(DeleteResponse.class);
        when(openSearchClient.delete(any(DeleteRequest.class)))
                .thenReturn(deleteResponse);
        when(deleteResponse.result()).thenReturn(Result.NotFound);

        // When
        transactionService.delete(TRANSACTION_ID);

        // Then
        verify(openSearchClient).delete(any(DeleteRequest.class));
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando deleção falha")
    void shouldThrowRuntimeExceptionWhenDeleteFails() throws IOException {
        // Given
        when(openSearchClient.delete(any(DeleteRequest.class)))
                .thenThrow(new IOException("Erro ao deletar do OpenSearch"));

        // When & Then
        assertThatThrownBy(() -> transactionService.delete(TRANSACTION_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to delete transaction")
                .hasCauseInstanceOf(IOException.class);

        verify(openSearchClient).delete(any(DeleteRequest.class));
    }
}

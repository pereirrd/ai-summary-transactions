package ai.summary.transactions.controller;

import ai.summary.transactions.application.AISummaryTransactionApp;
import ai.summary.transactions.model.GetAITransactionInsightsScenarioParameter;
import ai.summary.transactions.model.ProcessAITransactionSummaryRequest;
import io.micronaut.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiTransactionsControllerImpl Tests")
class AiTransactionsControllerImplTest {

    @Mock
    private AISummaryTransactionApp aiTransactionApplication;

    @InjectMocks
    private AiTransactionsControllerImpl aiTransactionsController;

    private final LocalDate START_DATE = LocalDate.of(2024, 1, 1);
    private final LocalDate END_DATE = LocalDate.of(2024, 1, 31);
    private final String QUESTION = "Quais foram minhas despesas de alimentação este mês?";
    private final String SCENARIO_VALUE = "Fechada";
    private final String EXPECTED_SUMMARY = "Suas despesas de alimentação em janeiro de 2024 totalizaram R$ 850,00.";
    private final String EXPECTED_INSIGHTS = "Baseado na análise, você gastou 25% a mais em alimentação comparado ao mês anterior.";

    private ProcessAITransactionSummaryRequest processRequest;
    private GetAITransactionInsightsScenarioParameter scenarioParameter;

    @BeforeEach
    void setUp() {
        processRequest = new ProcessAITransactionSummaryRequest(QUESTION);

        scenarioParameter = GetAITransactionInsightsScenarioParameter.fromValue(SCENARIO_VALUE);
    }

    @Test
    @DisplayName("Deve processar resumo de transação com sucesso")
    void shouldProcessAITransactionSummarySuccessfully() {
        // Given
        when(aiTransactionApplication.processByList(QUESTION))
                .thenReturn(EXPECTED_SUMMARY);

        // When
        var response = aiTransactionsController.processAITransactionSummary(
                START_DATE, END_DATE, processRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus().compareTo(HttpStatus.OK)).isZero();
        assertThat(response.body()).isNotNull();
        assertThat(response.body().getResult()).isEqualTo(EXPECTED_SUMMARY);

        verify(aiTransactionApplication).processByList(QUESTION);
    }

    @Test
    @DisplayName("Deve retornar erro 500 quando processamento de resumo falha")
    void shouldReturnServerErrorWhenSummaryProcessingFails() {
        // Given
        when(aiTransactionApplication.processByList(QUESTION))
                .thenThrow(new RuntimeException("Erro na API da OpenAI"));

        // When
        var response = aiTransactionsController.processAITransactionSummary(
                START_DATE, END_DATE, processRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus().compareTo(HttpStatus.INTERNAL_SERVER_ERROR)).isZero();

        verify(aiTransactionApplication).processByList(QUESTION);
    }

    @Test
    @DisplayName("Deve obter insights de transação com sucesso")
    void shouldGetAITransactionInsightsSuccessfully() {
        // Given
        when(aiTransactionApplication.processByInsights(SCENARIO_VALUE, START_DATE, END_DATE))
                .thenReturn(EXPECTED_INSIGHTS);

        // When
        var response = aiTransactionsController.getAITransactionInsights(
                scenarioParameter, START_DATE, END_DATE);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus().compareTo(HttpStatus.OK)).isZero();
        assertThat(response.body()).isNotNull();
        assertThat(response.body().getResult()).isEqualTo(EXPECTED_INSIGHTS);

        verify(aiTransactionApplication).processByInsights(SCENARIO_VALUE, START_DATE, END_DATE);
    }

    @Test
    @DisplayName("Deve retornar erro 500 quando processamento de insights falha")
    void shouldReturnServerErrorWhenInsightsProcessingFails() {
        // Given
        when(aiTransactionApplication.processByInsights(SCENARIO_VALUE, START_DATE, END_DATE))
                .thenThrow(new RuntimeException("Erro de conexão com banco de dados"));

        // When
        var response = aiTransactionsController.getAITransactionInsights(
                scenarioParameter, START_DATE, END_DATE);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus().compareTo(HttpStatus.INTERNAL_SERVER_ERROR)).isZero();

        verify(aiTransactionApplication).processByInsights(SCENARIO_VALUE, START_DATE, END_DATE);
    }
}

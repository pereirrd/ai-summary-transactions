package ai.summary.transactions.application;

import ai.summary.transactions.domain.ai.insights.AITransactionInsightsService;
import ai.summary.transactions.domain.ai.summary.AITransactionSummaryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AISummaryTransactionApp Tests")
class AISummaryTransactionAppTest {

    @Mock
    private AITransactionSummaryService aiTransactionSummaryService;

    @Mock
    private AITransactionInsightsService aiTransactionInsightsService;

    @InjectMocks
    private AISummaryTransactionApp aiSummaryTransactionApp;

    private final String VALID_QUESTION = "Quais foram minhas despesas de alimentação este mês?";
    private final String VALID_SCENARIO = "Análise de gastos mensais";
    private final LocalDate START_DATE = LocalDate.of(2024, 1, 1);
    private final LocalDate END_DATE = LocalDate.of(2024, 1, 31);
    private final String EXPECTED_SUMMARY = "Suas despesas de alimentação em janeiro de 2024 totalizaram R$ 850,00.";
    private final String EXPECTED_INSIGHTS = "Baseado na análise, você gastou 25% a mais em alimentação comparado ao mês anterior.";

    @Test
    @DisplayName("Deve processar pergunta com sucesso e retornar resumo")
    void shouldProcessQuestionSuccessfullyAndReturnSummary() {
        // Given
        when(aiTransactionSummaryService.summarizeTransactions(VALID_QUESTION))
                .thenReturn(EXPECTED_SUMMARY);

        // When
        var result = aiSummaryTransactionApp.processByList(VALID_QUESTION);

        // Then
        assertThat(result).isEqualTo(EXPECTED_SUMMARY);
        verify(aiTransactionSummaryService).summarizeTransactions(VALID_QUESTION);
    }

    @Test
    @DisplayName("Deve processar insights com sucesso e retornar análise")
    void shouldProcessInsightsSuccessfullyAndReturnAnalysis() {
        // Given
        when(aiTransactionInsightsService.generateInsights(VALID_SCENARIO, START_DATE, END_DATE))
                .thenReturn(EXPECTED_INSIGHTS);

        // When
        var result = aiSummaryTransactionApp.processByInsights(VALID_SCENARIO, START_DATE, END_DATE);

        // Then
        assertThat(result).isEqualTo(EXPECTED_INSIGHTS);
        verify(aiTransactionInsightsService).generateInsights(VALID_SCENARIO, START_DATE, END_DATE);
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando serviço de resumo falha")
    void shouldThrowRuntimeExceptionWhenSummaryServiceFails() {
        // Given
        var exception = new RuntimeException("Erro na API da OpenAI");
        when(aiTransactionSummaryService.summarizeTransactions(VALID_QUESTION))
                .thenThrow(exception);

        // When & Then
        assertThatThrownBy(() -> aiSummaryTransactionApp.processByList(VALID_QUESTION))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to process AI transaction question")
                .hasCause(exception);

        verify(aiTransactionSummaryService).summarizeTransactions(VALID_QUESTION);
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando serviço de insights falha")
    void shouldThrowRuntimeExceptionWhenInsightsServiceFails() {
        // Given
        var exception = new RuntimeException("Erro de conexão com banco de dados");
        when(aiTransactionInsightsService.generateInsights(VALID_SCENARIO, START_DATE, END_DATE))
                .thenThrow(exception);

        // When & Then
        assertThatThrownBy(
                () -> aiSummaryTransactionApp.processByInsights(VALID_SCENARIO, START_DATE, END_DATE))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to process AI transaction insights")
                .hasCause(exception);

        verify(aiTransactionInsightsService).generateInsights(VALID_SCENARIO, START_DATE, END_DATE);
    }
}

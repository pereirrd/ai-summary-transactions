# Melhorias nas Tools do Langchain4j

## Resumo da Conversa

Esta conversa documenta as melhorias realizadas nas classes de tools do projeto, separando interfaces e implementações, e melhorando a interface `AITransactionInsights` para uso obrigatório das tools.

## 1. Separação de Interface e Implementação das Tools

### Objetivo
Separar as anotações do langchain4j (com prompts e orientações para a LLM) nas interfaces, mantendo a lógica de negócio nas classes concretas de implementação.

### Estrutura Criada

#### TransactionTool
- **Interface**: `src/main/java/ai/summary/transactions/domain/ai/tools/TransactionTool.java`
  - Contém anotação `@Tool` com prompts detalhados
  - Define método `getTransactions(LocalDate startDate, LocalDate endDate)`
  - Usa `@P` para documentar parâmetros

- **Implementação**: `src/main/java/ai/summary/transactions/domain/ai/tools/impl/TransactionToolImpl.java`
  - Implementa a interface `TransactionTool`
  - Contém a lógica de negócio para buscar transações
  - Usa `TransactionService` para buscar dados

#### ReferenceDateTool
- **Interface**: `src/main/java/ai/summary/transactions/domain/ai/tools/ReferenceDateTool.java`
  - Contém anotação `@Tool` com descrição da funcionalidade
  - Define método `referenceDate()` que retorna a data atual

- **Implementação**: `src/main/java/ai/summary/transactions/domain/ai/tools/impl/ReferenceDateToolImpl.java`
  - Implementa a interface `ReferenceDateTool`
  - Retorna `LocalDate.now()`

### Benefícios
1. Separação clara entre contratos (interfaces) e implementações
2. Facilita manutenção e testes
3. Prompts e documentação concentrados nas interfaces
4. Lógica de negócio isolada nas implementações

## 2. Adição de Parâmetros na TransactionTool

### Mudança Realizada
Adicionados parâmetros `startDate` e `endDate` no método `getTransactions` da interface `TransactionTool`.

### Antes
```java
@Tool("...")
List<Transaction> getTransactions();
```

### Depois
```java
@Tool("""
    Essa é a lista de transações bancárias e deve ser usada para responder a pergunta do cliente.
    Use esta tool quando precisar buscar transações financeiras do período especificado.
    Retorna uma lista de transações contendo informações como valor, data, descrição e categoria.

    IMPORTANTE: Sempre será fornecido as datas de início e fim do período desejado pelo cliente.
    - O formato das datas será YYYY-MM-DD (ex: 2024-01-15)
    - A data inicial (startDate) deve ser anterior ou igual à data final (endDate)
    """)
List<Transaction> getTransactions(
    @P("Data inicial do período para busca das transações. Formato: YYYY-MM-DD (ex: 2024-01-01).") 
    LocalDate startDate,
    @P("Data final do período para busca das transações. Formato: YYYY-MM-DD (ex: 2024-01-31).") 
    LocalDate endDate
);
```

### Impacto
- A LLM agora recebe os parâmetros diretamente ao invés de buscar do contexto HTTP
- Implementação simplificada (removida dependência de `ServerRequestContext`)
- Melhor testabilidade

## 3. Melhoria da Interface AITransactionInsights

### Objetivo
Tornar obrigatório o uso da tool `getTransactions` e orientar claramente a LLM sobre como usar as tools.

### Mudanças no Prompt SystemMessage
Adicionada seção "REGRA CRÍTICA - USO OBRIGATÓRIO DE TOOLS" no arquivo `prompts/ai-transaction-insights.md`:

```markdown
## ⚠️ REGRA CRÍTICA - USO OBRIGATÓRIO DE TOOLS

**VOCÊ DEVE OBRIGATORIAMENTE usar a tool `getTransactions` ANTES de gerar qualquer resumo ou insight.**

### Processo Obrigatório:
1. **PRIMEIRO**: Sempre chame a tool `getTransactions` com os parâmetros `startDate` e `endDate` fornecidos na mensagem do usuário
2. **SEGUNDO**: Aguarde o retorno das transações
3. **TERCEIRO**: Apenas então analise e gere o resumo baseado nas transações obtidas

**NUNCA** tente gerar insights sem primeiro buscar as transações usando a tool `getTransactions`.
```

### Mudanças na Interface
- `@SystemMessage`: Carrega o prompt completo do arquivo markdown (regras gerais)
- `@UserMessage`: Apenas a solicitação específica com parâmetros (sem duplicação)

### Versão Final
```java
@SystemMessage(fromResource = "prompts/ai-transaction-insights.md")
@UserMessage("""
    Minha fatura está no cenário {{scenario}}, com data inicial {{startDate}} e data final {{endDate}}.
    Forneça um resumo das transações que ocorrem nesse período.
    """)
String generateInsights(@V("scenario") String scenario,
        @V("startDate") String startDate,
        @V("endDate") String endDate);
```

## 4. Decisões sobre Anotações

### Pergunta: É necessário ter as mesmas orientações nas duas anotações?

**Resposta**: Não é necessário duplicar.

### Diferença entre Anotações

- **`@SystemMessage`**: 
  - Opcional mas altamente recomendado
  - Define regras gerais e comportamento permanente do sistema
  - Carrega contexto que sempre estará presente
  - Ideal para instruções sobre quando e como usar tools

- **`@UserMessage`**: 
  - **OBRIGATÓRIA**
  - Define a mensagem específica do usuário para cada chamada
  - Contém parâmetros específicos daquela interação
  - Não deve repetir regras gerais já definidas no SystemMessage

### Boa Prática
- Regras gerais → `@SystemMessage` (arquivo markdown)
- Solicitação específica → `@UserMessage` (com parâmetros)
- Evitar duplicação entre as duas

## 5. Arquivos Modificados

### Criados
- `src/main/java/ai/summary/transactions/domain/ai/tools/TransactionTool.java` (interface)
- `src/main/java/ai/summary/transactions/domain/ai/tools/impl/TransactionToolImpl.java` (implementação)
- `src/main/java/ai/summary/transactions/domain/ai/tools/ReferenceDateTool.java` (interface)
- `src/main/java/ai/summary/transactions/domain/ai/tools/impl/ReferenceDateToolImpl.java` (implementação)

### Modificados
- `src/main/java/ai/summary/transactions/domain/ai/insights/AITransactionInsights.java`
- `src/main/resources/prompts/ai-transaction-insights.md`
- `src/main/java/ai/summary/transactions/domain/ai/summary/AITransactionSummaryService.java`
- `src/main/java/ai/summary/transactions/domain/ai/insights/AITransactionInsightsService.java`

### Removidos
- Métodos privados `getStartDate()` e `getEndDate()` de `TransactionToolImpl`
- Dependência de `ServerRequestContext` em `TransactionToolImpl`

## 6. Estrutura Final do Projeto

```
domain/ai/tools/
├── TransactionTool.java          (interface com @Tool e prompts)
├── ReferenceDateTool.java        (interface com @Tool e prompts)
└── impl/
    ├── TransactionToolImpl.java  (implementação com lógica)
    └── ReferenceDateToolImpl.java (implementação com lógica)
```

## 7. Lições Aprendidas

1. **Separação de Responsabilidades**: Interfaces para contratos e documentação, implementações para lógica
2. **Evitar Duplicação**: Regras gerais no `@SystemMessage`, solicitações específicas no `@UserMessage`
3. **Documentação Clara**: Usar `@P` para documentar parâmetros das tools
4. **Orientação Explícita**: Instruções claras sobre uso obrigatório de tools no prompt do sistema

## 8. Próximos Passos Sugeridos

1. Adicionar validações nos parâmetros das tools
2. Criar testes unitários para as implementações
3. Documentar casos de uso específicos
4. Considerar adicionar mais tools conforme necessário


# Análise do uso de Query DSL no OpenSearch

## Pergunta
**Usuário:** "esse projeto está usando Query Domain-Specific Language (DSL) para o opensearch?"

## Resposta

**Sim, este projeto está usando Query Domain-Specific Language (DSL) para o OpenSearch**, mas de forma limitada no momento.

## Evidências do uso do Query DSL:

### 1. **Dependência do OpenSearch Java Client**
O projeto usa a biblioteca oficial do OpenSearch:
```xml
<dependency>
    <groupId>org.opensearch.client</groupId>
    <artifactId>opensearch-java</artifactId>
    <version>3.1.0</version>
</dependency>
```

### 2. **Uso do Query DSL no código atual**
No arquivo `TransactionServiceImpl.java`, há um exemplo de uso do Query DSL:

```java
var searchRequest = SearchRequest.of(s -> s
    .index(openSearchConfig.getTransactionsIndex())
    .query(Query.of(q -> q.matchAll(m -> m)))  // ← Query DSL aqui
    .from(offset)
    .size(limit)
    .sort(sort -> sort.field(
        f -> f.field("date").order(SortOrder.Desc))));
```

### 3. **Imports específicos do Query DSL**
O código importa classes específicas do Query DSL:
```java
import org.opensearch.client.opensearch._types.query_dsl.Query;
```

## Estado atual do uso:

**Uso básico implementado:**
- ✅ `matchAll` query para buscar todas as transações
- ✅ Operações CRUD básicas (Create, Read, Update, Delete)
- ✅ Ordenação por campo (`sort`)

**Potencial para uso mais avançado:**
O projeto está preparado para usar queries mais complexas do Query DSL, como:
- `match` queries para busca textual
- `bool` queries para combinar condições
- `range` queries para filtros de data/valor
- `term` queries para busca exata
- `wildcard` queries para busca com padrões

## Conclusão:
O projeto **está configurado e usando** o Query DSL do OpenSearch, mas atualmente implementa apenas funcionalidades básicas. A infraestrutura está pronta para implementar queries mais sofisticadas conforme a necessidade do negócio.

## Arquivos analisados:
- `/src/main/java/ai/summary/transactions/domain/transaction/impl/TransactionServiceImpl.java`
- `/src/main/java/ai/summary/transactions/core/config/OpenSearchConfig.java`
- `/src/main/java/ai/summary/transactions/core/factory/OpenSearchClientFactory.java`
- `/pom.xml`
- `/docker-compose.yml`

## Data da análise:
Janeiro 2025

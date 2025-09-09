# Configuração do OpenSearch para AI Summary Transactions

## Visão Geral

A classe `TransactionServiceImpl` foi implementada para usar o cliente OpenSearch Java para executar operações CRUD (Create, Read, Update, Delete) em transações. O sistema utiliza o módulo `opensearch-java` declarado como dependência no Maven.

## Dependências

As seguintes dependências foram adicionadas ao `pom.xml`:

```xml
<!-- OpenSearch dependencies -->
<dependency>
  <groupId>org.opensearch.client</groupId>
  <artifactId>opensearch-java</artifactId>
  <version>3.1.0</version>
</dependency>
<dependency>
  <groupId>org.apache.httpcomponents.client5</groupId>
  <artifactId>httpclient5</artifactId>
  <version>5.3.1</version>
</dependency>
```

## Configuração

### 1. Configuração do Cliente OpenSearch

O cliente OpenSearch é configurado através da classe `OpenSearchConfig` que utiliza o `ApacheHttpClient5TransportBuilder`:

```java
@Factory
public class OpenSearchConfig {
    @Bean
    @Singleton
    public OpenSearchClient openSearchClient() {
        HttpHost httpHost = new HttpHost(scheme, host, port);
        OpenSearchTransport transport = ApacheHttpClient5TransportBuilder.builder(httpHost).build();
        return new OpenSearchClient(transport);
    }
}
```

### 2. Configuração da Aplicação

As configurações do OpenSearch são definidas no `application.yml`:

```yaml
opensearch:
  host: ${OPENSEARCH_HOST:localhost}
  port: ${OPENSEARCH_PORT:9200}
  scheme: ${OPENSEARCH_SCHEME:http}
  username: ${OPENSEARCH_USERNAME:}
  password: ${OPENSEARCH_PASSWORD:}
  index:
    transactions: ${OPENSEARCH_TRANSACTIONS_INDEX:transactions}
```

### 3. Variáveis de Ambiente

Configure as seguintes variáveis de ambiente:

```bash
export OPENSEARCH_HOST=localhost
export OPENSEARCH_PORT=9200
export OPENSEARCH_SCHEME=http
export OPENSEARCH_USERNAME=admin  # opcional
export OPENSEARCH_PASSWORD=admin  # opcional
export OPENSEARCH_TRANSACTIONS_INDEX=transactions
```

## Implementação dos Métodos CRUD

### 1. getTransactionById(String id)

```java
@Override
public Transaction getTransactionById(String id) {
    try {
        GetRequest getRequest = GetRequest.of(g -> g
                .index(transactionsIndex)
                .id(id)
        );

        GetResponse<Object> response = openSearchClient.get(getRequest, Object.class);

        if (response.found()) {
            return objectMapper.convertValue(response.source(), Transaction.class);
        } else {
            logger.warn("Transaction with id {} not found", id);
            return null;
        }
    } catch (IOException e) {
        logger.error("Error retrieving transaction with id: {}", id, e);
        throw new RuntimeException("Failed to retrieve transaction", e);
    }
}
```

### 2. createTransaction(Transaction transaction)

```java
@Override
public Transaction createTransaction(Transaction transaction) {
    try {
        String transactionId = transaction.id() != null ? 
                transaction.id().toString() : UUID.randomUUID().toString();

        Transaction transactionWithId = new Transaction(
                UUID.fromString(transactionId),
                transaction.date(),
                transaction.amount(),
                transaction.description(),
                transaction.merchant()
        );

        IndexRequest<Transaction> indexRequest = IndexRequest.of(i -> i
                .index(transactionsIndex)
                .id(transactionId)
                .document(transactionWithId)
        );

        IndexResponse response = openSearchClient.index(indexRequest);

        if (response.result() == Result.Created || response.result() == Result.Updated) {
            logger.info("Transaction created/updated with id: {}", transactionId);
            return transactionWithId;
        } else {
            throw new RuntimeException("Failed to create transaction");
        }
    } catch (IOException e) {
        logger.error("Error creating transaction", e);
        throw new RuntimeException("Failed to create transaction", e);
    }
}
```

### 3. updateTransaction(String id, Transaction transaction)

```java
@Override
public Transaction updateTransaction(String id, Transaction transaction) {
    try {
        Transaction existingTransaction = getTransactionById(id);
        if (existingTransaction == null) {
            logger.warn("Transaction with id {} not found for update", id);
            return null;
        }

        Transaction updatedTransaction = new Transaction(
                UUID.fromString(id),
                transaction.date() != null ? transaction.date() : existingTransaction.date(),
                transaction.amount() != null ? transaction.amount() : existingTransaction.amount(),
                transaction.description() != null ? transaction.description() : existingTransaction.description(),
                transaction.merchant() != null ? transaction.merchant() : existingTransaction.merchant()
        );

        IndexRequest<Transaction> indexRequest = IndexRequest.of(i -> i
                .index(transactionsIndex)
                .id(id)
                .document(updatedTransaction)
        );

        IndexResponse response = openSearchClient.index(indexRequest);

        if (response.result() == Result.Updated || response.result() == Result.Created) {
            logger.info("Transaction updated with id: {}", id);
            return updatedTransaction;
        } else {
            throw new RuntimeException("Failed to update transaction");
        }
    } catch (IOException e) {
        logger.error("Error updating transaction with id: {}", id, e);
        throw new RuntimeException("Failed to update transaction", e);
    }
}
```

### 4. deleteTransaction(String id)

```java
@Override
public void deleteTransaction(String id) {
    try {
        DeleteRequest deleteRequest = DeleteRequest.of(d -> d
                .index(transactionsIndex)
                .id(id)
        );

        DeleteResponse response = openSearchClient.delete(deleteRequest);

        if (response.result() == Result.Deleted) {
            logger.info("Transaction deleted with id: {}", id);
        } else if (response.result() == Result.NotFound) {
            logger.warn("Transaction with id {} not found for deletion", id);
        } else {
            throw new RuntimeException("Failed to delete transaction");
        }
    } catch (IOException e) {
        logger.error("Error deleting transaction with id: {}", id, e);
        throw new RuntimeException("Failed to delete transaction", e);
    }
}
```

## Características Importantes

1. **Uso do Cliente OpenSearch**: O sistema utiliza o cliente Java oficial do OpenSearch, não chamadas HTTP diretas.

2. **Injeção de Dependência**: O `OpenSearchClient` é injetado via Micronaut DI.

3. **Configuração Flexível**: As configurações podem ser definidas via variáveis de ambiente.

4. **Tratamento de Erros**: Todos os métodos incluem tratamento adequado de erros com logging.

5. **Logging**: Utiliza SLF4J para logging estruturado.

6. **Serialização**: Utiliza Jackson ObjectMapper para conversão de objetos.

## Executando o Sistema

1. Certifique-se de que o OpenSearch está rodando
2. Configure as variáveis de ambiente necessárias
3. Execute a aplicação Micronaut
4. O índice `transactions` será criado automaticamente quando a primeira transação for inserida

## Exemplo de Uso

```bash
# Criar uma transação
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "date": "2024-01-15T10:30:00Z",
    "amount": 100.50,
    "description": "Purchase at store",
    "merchant": {
      "name": "Example Store",
      "category": "Retail"
    }
  }'

# Buscar uma transação
curl http://localhost:8080/transactions/123e4567-e89b-12d3-a456-426614174000
```

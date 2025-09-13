# Chat: Correção de Problemas de Conexão com OpenSearch

**Data:** 13 de Setembro de 2025  
**Problema:** Erros de conexão com OpenSearch em aplicação Java/Micronaut

## Problemas Identificados e Soluções

### 1. Erro: `ClientH2UpgradeHandler` Constructor

**Erro Original:**
```
void org.apache.hc.core5.http2.impl.nio.ClientH2UpgradeHandler.<init>(org.apache.hc.core5.http2.impl.nio.ClientH2StreamMultiplexerFactory)
```

**Causa:** Incompatibilidade de versões entre dependências HTTP
- OpenSearch Java Client (3.1.0) usava `httpcore5:5.3.4` e `httpcore5-h2:5.3.4`
- Projeto declarava explicitamente `httpclient5:5.3.1`

**Solução:**
- Removida dependência explícita do `httpclient5` do `pom.xml`
- Deixado o OpenSearch gerenciar suas próprias dependências HTTP
- Compilação bem-sucedida após correção

### 2. Erro: `[string_error] Not Found` ao criar índice

**Erro Original:**
```
Caused by: org.opensearch.client.opensearch._types.OpenSearchException: Request failed: [string_error] Not Found
```

**Causa:** 
- Configuração de porta incorreta (5601 ao invés de 9200)
- Índice `transactions` não existia no OpenSearch

**Solução:**
- Corrigida porta no `application.yml` de 5601 para 9200
- Implementada criação automática de índice no `TransactionServiceImpl`
- Adicionado método `ensureIndexExists()` que verifica e cria o índice se necessário

### 3. Erro: `Connection closed by peer`

**Erro Original:**
```
Caused by: org.apache.hc.core5.http.ConnectionClosedException: Connection closed by peer
```

**Causa:** OpenSearch configurado com autenticação, mas cliente Java não configurado para usar credenciais

**Solução:**
- Configurado `OpenSearchClientFactory` com suporte a autenticação
- Adicionadas propriedades `username` e `password` no `OpenSearchConfig`
- Implementada configuração flexível via variáveis de ambiente

### 4. Configuração HTTP vs HTTPS

**Decisão:** Migração para HTTP para simplificar desenvolvimento

**Mudanças no `docker-compose.yml`:**
```yaml
# Adicionado em ambos os nós OpenSearch:
- plugins.security.disabled=true  # Desabilita segurança para HTTP

# OpenSearch Dashboards configurado para HTTP:
OPENSEARCH_HOSTS: '["http://opensearch-node1:9200","http://opensearch-node2:9200"]'
```

**Mudanças no `application.yml`:**
```yaml
opensearch:
  host: ${OPENSEARCH_HOST:localhost}
  port: ${OPENSEARCH_PORT:9200}
  scheme: ${OPENSEARCH_SCHEME:http}
  username: ${OPENSEARCH_USERNAME:}  # Vazio = sem autenticação
  password: ${OPENSEARCH_PASSWORD:}  # Vazio = sem autenticação
  index:
    transactions: ${OPENSEARCH_TRANSACTIONS_INDEX:transactions}
```

## Arquivos Modificados

### 1. `pom.xml`
- Removida dependência explícita do `httpclient5`

### 2. `src/main/java/ai/summary/transactions/core/factory/OpenSearchClientFactory.java`
- Adicionado suporte a autenticação
- Configuração flexível para HTTP/HTTPS
- Uso de `BasicCredentialsProvider` quando credenciais estão disponíveis

### 3. `src/main/java/ai/summary/transactions/core/config/OpenSearchConfig.java`
- Adicionadas propriedades `username` e `password`
- Configuração via `@Value` annotations

### 4. `src/main/resources/application.yml`
- Corrigida porta de 5601 para 9200
- Configuração simplificada para HTTP
- Variáveis de ambiente para flexibilidade

### 5. `docker-compose.yml`
- Adicionado `plugins.security.disabled=true` em ambos os nós
- Configurado OpenSearch Dashboards para HTTP
- Mantida configuração de cluster com 2 nós

## Código Implementado

### Criação Automática de Índice (removido posteriormente)
```java
private void ensureIndexExists() {
    try {
        String indexName = openSearchConfig.getTransactionsIndex();
        ExistsRequest existsRequest = ExistsRequest.of(e -> e.index(indexName));
        
        boolean indexExists = openSearchClient.indices().exists(existsRequest).value();
        
        if (!indexExists) {
            log.info("Index '{}' does not exist. Creating it...", indexName);
            createIndex(indexName);
        }
    } catch (IOException e) {
        log.error("Error checking/creating index", e);
        throw new RuntimeException("Failed to ensure index exists", e);
    }
}
```

### Configuração de Autenticação no Cliente
```java
// Add authentication if username and password are configured
var username = openSearchConfig.getUsername();
var password = openSearchConfig.getPassword();

if (username != null && password != null && !username.isEmpty() && !password.isEmpty()) {
    // Create credentials provider
    var credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(
        new AuthScope(httpHost),
        new UsernamePasswordCredentials(username, password.toCharArray())
    );
    
    transportBuilder.setHttpClientConfigCallback(
        httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
    );
}
```

## Comandos Úteis

### Para iniciar OpenSearch:
```bash
export OPENSEARCH_INITIAL_ADMIN_PASSWORD=admin123
sudo docker-compose up -d opensearch-node1
```

### Para testar conexão:
```bash
# Teste simples (sem autenticação)
curl http://localhost:9200/_cluster/health

# Com autenticação (se habilitada)
curl -u admin:admin123 http://localhost:9200/_cluster/health
```

### Para compilar projeto:
```bash
mvn clean compile
```

## Resultado Final

✅ **Todos os erros de conexão resolvidos**  
✅ **Configuração HTTP funcionando**  
✅ **Sem necessidade de autenticação para desenvolvimento**  
✅ **Aplicação compilando com sucesso**  
✅ **OpenSearch configurado para desenvolvimento local**

## Vantagens da Configuração HTTP

1. **Simplicidade**: Sem necessidade de certificados SSL
2. **Desenvolvimento**: Mais fácil para debug e testes
3. **Performance**: Menos overhead de criptografia
4. **Compatibilidade**: Funciona com ferramentas simples como `curl`

## Notas Importantes

- A configuração HTTP é adequada para desenvolvimento local
- Para produção, considere usar HTTPS com autenticação adequada
- O OpenSearch Dashboards está configurado para acessar via HTTP
- A aplicação Java funciona tanto com quanto sem autenticação

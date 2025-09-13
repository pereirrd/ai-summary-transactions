# Configuração OpenSearch + Kibana - Chat de Suporte

**Data:** $(date)  
**Problema:** Não conseguia acessar o Kibana do OpenSearch local após adicionar `plugins.security.disabled=true`

## Problema Inicial

O usuário estava enfrentando problemas para acessar o Kibana após adicionar a configuração `plugins.security.disabled=true` no docker-compose.yml do OpenSearch.

## Análise do Problema

O problema identificado foi um **conflito de configuração**:

1. **Conflito de configuração**: Quando a segurança do OpenSearch é desabilitada com `plugins.security.disabled=true`, o OpenSearch Dashboards (Kibana) ainda espera que a segurança esteja habilitada por padrão.

2. **Configuração incompleta**: O OpenSearch Dashboards precisa de configurações específicas para trabalhar com segurança desabilitada.

## Soluções Propostas

### Opção 1: HTTP sem autenticação (Escolhida)
- **OpenSearch**: `plugins.security.disabled=true` (sem autenticação)
- **OpenSearch Dashboards**: `DISABLE_SECURITY_DASHBOARDS_PLUGIN: "true"` (compatível com segurança desabilitada)
- **Acesso**: HTTP direto sem credenciais

### Opção 2: HTTP com autenticação simples
- **OpenSearch**: Segurança habilitada (padrão)
- **Kibana**: Usa credenciais `admin` / `${OPENSEARCH_INITIAL_ADMIN_PASSWORD}`
- **Acesso**: HTTP com login básico

## Configuração Final Implementada

```yaml
services:
  opensearch-node1:
    image: opensearchproject/opensearch:3
    container_name: opensearch-node1
    environment:
      - cluster.name=opensearch-cluster
      - node.name=opensearch-node1
      - discovery.seed_hosts=opensearch-node1,opensearch-node2
      - cluster.initial_cluster_manager_nodes=opensearch-node1,opensearch-node2
      - bootstrap.memory_lock=true
      - OPENSEARCH_JAVA_OPTS=-Xms512m -Xmx512m
      - OPENSEARCH_INITIAL_ADMIN_PASSWORD=${OPENSEARCH_INITIAL_ADMIN_PASSWORD}
      - plugins.security.disabled=true  # Disable security for HTTP access
    # ... resto da configuração

  opensearch-node2:
    image: opensearchproject/opensearch:3
    container_name: opensearch-node2
    environment:
      - cluster.name=opensearch-cluster
      - node.name=opensearch-node2
      - discovery.seed_hosts=opensearch-node1,opensearch-node2
      - cluster.initial_cluster_manager_nodes=opensearch-node1,opensearch-node2
      - bootstrap.memory_lock=true
      - OPENSEARCH_JAVA_OPTS=-Xms512m -Xmx512m
      - OPENSEARCH_INITIAL_ADMIN_PASSWORD=${OPENSEARCH_INITIAL_ADMIN_PASSWORD}
      - plugins.security.disabled=true  # Disable security for HTTP access
    # ... resto da configuração

  opensearch-dashboards:
    image: opensearchproject/opensearch-dashboards:3
    container_name: opensearch-dashboards
    ports:
      - 5601:5601
    expose:
      - '5601'
    environment:
      OPENSEARCH_HOSTS: '["http://opensearch-node1:9200","http://opensearch-node2:9200"]'
      DISABLE_SECURITY_DASHBOARDS_PLUGIN: "true"
    networks:
      - opensearch-net
```

## Correções Aplicadas

1. **Adicionada configuração no OpenSearch Dashboards**: `DISABLE_SECURITY_DASHBOARDS_PLUGIN: "true"`
2. **Correção de tipo**: A variável foi definida como string (`"true"`) em vez de booleano (`true`)

## Comandos para Aplicar as Mudanças

```bash
# Parar os containers atuais
docker-compose down

# Iniciar novamente
docker-compose up -d

# Aguardar alguns minutos para os serviços iniciarem completamente
```

## Verificação

**URLs de acesso:**
- **OpenSearch**: `http://localhost:9200` (sem autenticação)
- **Kibana**: `http://localhost:5601` (sem autenticação)

**Teste do OpenSearch:**
```bash
curl http://localhost:9200
```

## Resumo

✅ **Problema resolvido**: O conflito entre OpenSearch com segurança desabilitada e OpenSearch Dashboards foi resolvido adicionando a configuração `DISABLE_SECURITY_DASHBOARDS_PLUGIN: "true"` no container do Dashboards.

✅ **Configuração funcionando**: Ambos os serviços agora funcionam sem necessidade de autenticação.

✅ **Correção de tipo**: A variável de ambiente foi corrigida para usar string em vez de booleano.

## Lições Aprendidas

1. **Compatibilidade de versões**: OpenSearch 3.x requer configurações específicas para trabalhar com segurança desabilitada
2. **Configuração de variáveis**: Variáveis de ambiente no Docker Compose devem usar strings quando apropriado
3. **Documentação**: Sempre verificar a documentação oficial para variáveis de ambiente específicas de cada versão

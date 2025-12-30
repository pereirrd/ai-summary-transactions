# AI Summary Transactions

Projeto desenvolvido para aprendizado e exploração do **LangChain4J** utilizando um cenário fictício de transações de cartões de crédito. O projeto demonstra como integrar inteligência artificial (OpenAI) com armazenamento e busca em OpenSearch para gerar resumos e insights de transações financeiras.

## Tecnologias

- **Java 21** - Linguagem de programação
- **Maven** - Gerenciador de dependências e build
- **Micronaut 4.9.1** - Framework Java moderno e reativo
- **LangChain4J** - Biblioteca para integração com modelos de linguagem
- **OpenAI GPT-4o-mini** - Modelo de IA para geração de resumos e insights
- **OpenSearch 3** - Motor de busca e análise de dados
- **OpenSearch Dashboards** - Interface de visualização e análise
- **MapStruct** - Mapeamento de objetos
- **Lombok** - Redução de boilerplate

## Estrutura do Projeto

O projeto segue uma arquitetura em camadas com os seguintes pacotes principais:

- `ai.summary.transactions.application` - Casos de uso da aplicação
- `ai.summary.transactions.controller` - Controllers REST (gerados via OpenAPI)
- `ai.summary.transactions.core` - Configurações e factories
- `ai.summary.transactions.domain` - Entidades de domínio e serviços de IA
- `ai.summary.transactions.model` - Modelos de dados (gerados via OpenAPI)

## OpenSearch

O projeto utiliza **OpenSearch** rodando em containers Docker para armazenar e buscar transações. A configuração inclui um cluster com dois nós e o OpenSearch Dashboards para visualização.

### Inicialização dos Containers

Para iniciar os containers do OpenSearch:

```bash
docker-compose up -d
```

Isso iniciará:
- **OpenSearch Node 1** na porta `9200`
- **OpenSearch Node 2** (nó secundário)
- **OpenSearch Dashboards** na porta `5601`

### Variáveis de Ambiente Necessárias

Antes de iniciar os containers, defina a variável de ambiente:

```bash
export OPENSEARCH_INITIAL_ADMIN_PASSWORD=seu_password_aqui
```

### Acessando o OpenSearch Dashboards

Após iniciar os containers, acesse o OpenSearch Dashboards em:
- **URL**: http://localhost:5601

O OpenSearch está configurado sem segurança para facilitar o desenvolvimento local.

### Geração de Massa de Dados para Testes

O projeto inclui um prompt detalhado para geração de massa de dados de teste no arquivo `chats/criando_massa_teste.md`. Este prompt pode ser executado com uma IA (como ChatGPT, Claude, etc.) para gerar um arquivo CSV com transações fictícias de cartão de crédito.

O arquivo CSV gerado deve ser importado via **API própria do projeto** utilizando a automação do Postman. A collection do Postman já possui configurado um **Collection Runner** na pasta "CRUD Transactions > Runner" que permite importar automaticamente os dados do CSV através do endpoint `POST /transactions`.

O prompt especifica:

- **1000 transações** distribuídas ao longo de 2025
- **Categorias realistas** de estabelecimentos brasileiros
- **Distribuição de valores** seguindo padrões do mercado
- **Formato CSV** padronizado para importação

**Como importar os dados:**

1. Gere o arquivo CSV usando o prompt em `chats/criando_massa_teste.md`
2. Abra a collection do Postman no aplicativo
3. Execute o **Collection Runner** na pasta "CRUD Transactions > Runner"
4. Configure o arquivo CSV como fonte de dados no Runner
5. O Postman irá executar automaticamente as requisições para criar as transações via API

Isso populará o índice `transactions` no OpenSearch com dados de teste através da própria API do projeto.

## LangChain4J

O projeto utiliza o **LangChain4J** para integrar com a API da OpenAI e gerar resumos e insights inteligentes sobre transações de cartão de crédito. O cenário fictício permite explorar:

- Geração de resumos de transações usando IA
- Análise de padrões e insights de gastos
- Integração de prompts estruturados com modelos de linguagem

A configuração do LangChain4J está em `application.yml` e utiliza o modelo `gpt-4o-mini` da OpenAI.

## Como Rodar Localmente

### Pré-requisitos

- Java 21 instalado
- Maven instalado
- Docker e Docker Compose instalados
- Chave de API da OpenAI

### Passos para Execução

1. **Clone o repositório** (se ainda não tiver feito):
   ```bash
   git clone <url-do-repositorio>
   cd ai-summary-transactions
   ```

2. **Configure a chave de API da OpenAI**:
   
   Crie um arquivo `.env` na raiz do projeto ou exporte a variável de ambiente:
   ```bash
   export OPENAI_API_KEY=sua_chave_api_openai_aqui
   ```
   
   Você pode obter uma chave de API em: https://platform.openai.com/api-keys

3. **Configure a senha do OpenSearch**:
   ```bash
   export OPENSEARCH_INITIAL_ADMIN_PASSWORD=admin123
   ```

4. **Inicie os containers do OpenSearch**:
   ```bash
   docker-compose up -d
   ```

5. **Aguarde os containers estarem prontos** (pode levar alguns segundos):
   ```bash
   docker-compose ps
   ```

6. **Compile e execute a aplicação**:
   ```bash
   ./mvnw clean install
   ./mvnw mn:run
   ```

   Ou usando Maven diretamente:
   ```bash
   mvn clean install
   mvn mn:run
   ```

7. **Acesse a aplicação**:
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui
   - OpenSearch Dashboards: http://localhost:5601

## Collection do Postman

O projeto inclui uma collection do Postman salva em:
```
src/main/resources/collections/AI Summary Transactions.postman_collection.json
```

Importe esta collection no Postman para testar os endpoints da API facilmente.

## Desenvolvimento

Este projeto foi desenvolvido utilizando o **Cursor IDE** como auxílio durante o desenvolvimento. O diretório `chats/` contém o histórico dos principais chats durante as implementações, documentando as decisões técnicas e o processo de desenvolvimento.

## Documentação Adicional

- [Micronaut 4.9.1 User Guide](https://docs.micronaut.io/4.9.1/guide/index.html)
- [Micronaut API Reference](https://docs.micronaut.io/4.9.1/api/index.html)
- [LangChain4J Documentation](https://github.com/langchain4j/langchain4j)

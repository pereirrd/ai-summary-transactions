[Português](README.pt-br.md) | [Español](README.es.md)

# AI Summary Transactions

Project developed for learning and exploring **LangChain4J** using a fictional credit card transaction scenario. The project demonstrates how to integrate artificial intelligence (OpenAI) with storage and search in OpenSearch to generate summaries and insights of financial transactions.

## Technologies

- **Java 21** - Programming language
- **Maven** - Dependency manager and build tool
- **Micronaut 4.9.1** - Modern and reactive Java framework
- **LangChain4J** - Library for integration with language models
- **OpenAI GPT-4o-mini** - AI model for generating summaries and insights
- **OpenSearch 3** - Search engine and data analysis
- **OpenSearch Dashboards** - Visualization and analysis interface
- **MapStruct** - Object mapping
- **Lombok** - Boilerplate reduction

## Project Structure

The project follows a layered architecture with the following main packages:

- `ai.summary.transactions.application` - Application use cases
- `ai.summary.transactions.controller` - REST controllers (generated via OpenAPI)
- `ai.summary.transactions.core` - Configurations and factories
- `ai.summary.transactions.domain` - Domain entities and AI services
- `ai.summary.transactions.model` - Data models (generated via OpenAPI)

## OpenSearch

The project uses **OpenSearch** running in Docker containers to store and search transactions. The configuration includes a cluster with two nodes and OpenSearch Dashboards for visualization.

### Container Initialization

To start the OpenSearch containers:

```bash
docker-compose up -d
```

This will start:
- **OpenSearch Node 1** on port `9200`
- **OpenSearch Node 2** (secondary node)
- **OpenSearch Dashboards** on port `5601`

### Required Environment Variables

Before starting the containers, set the environment variable:

```bash
export OPENSEARCH_INITIAL_ADMIN_PASSWORD=your_password_here
```

### Accessing OpenSearch Dashboards

After starting the containers, access OpenSearch Dashboards at:
- **URL**: http://localhost:5601

OpenSearch is configured without security to facilitate local development.

### Test Data Generation

The project includes a detailed prompt for generating test data in the file `chats/criando_massa_teste.md`. This prompt can be executed with an AI (such as ChatGPT, Claude, etc.) to generate a CSV file with fictional credit card transactions.

The generated CSV file should be imported via the **project's own API** using Postman automation. The Postman collection already has a **Collection Runner** configured in the "CRUD Transactions > Runner" folder that allows automatic import of CSV data through the `POST /transactions` endpoint.

The prompt specifies:

- **1000 transactions** distributed throughout 2025
- **Realistic categories** of Brazilian establishments
- **Value distribution** following market patterns
- **Standardized CSV format** for import

**How to import the data:**

1. Generate the CSV file using the prompt in `chats/criando_massa_teste.md`
2. Open the Postman collection in the application
3. Run the **Collection Runner** in the "CRUD Transactions > Runner" folder
4. Configure the CSV file as the data source in the Runner
5. Postman will automatically execute the requests to create transactions via API

This will populate the `transactions` index in OpenSearch with test data through the project's own API.

## LangChain4J

The project uses **LangChain4J** to integrate with the OpenAI API and generate intelligent summaries and insights about credit card transactions. The fictional scenario allows exploring:

- Transaction summary generation using AI
- Pattern analysis and spending insights
- Integration of structured prompts with language models

LangChain4J configuration is in `application.yml` and uses the `gpt-4o-mini` model from OpenAI.

## How to Run Locally

### Prerequisites

- Java 21 installed
- Maven installed
- Docker and Docker Compose installed
- OpenAI API key

### Execution Steps

1. **Clone the repository** (if you haven't already):
   ```bash
   git clone <repository-url>
   cd ai-summary-transactions
   ```

2. **Configure the OpenAI API key**:
   
   Create a `.env` file in the project root or export the environment variable:
   ```bash
   export OPENAI_API_KEY=your_openai_api_key_here
   ```
   
   You can obtain an API key at: https://platform.openai.com/api-keys

3. **Configure the OpenSearch password**:
   ```bash
   export OPENSEARCH_INITIAL_ADMIN_PASSWORD=admin123
   ```

4. **Start the OpenSearch containers**:
   ```bash
   docker-compose up -d
   ```

5. **Wait for the containers to be ready** (may take a few seconds):
   ```bash
   docker-compose ps
   ```

6. **Compile and run the application**:
   ```bash
   ./mvnw clean install
   ./mvnw mn:run
   ```

   Or using Maven directly:
   ```bash
   mvn clean install
   mvn mn:run
   ```

7. **Access the application**:
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui
   - OpenSearch Dashboards: http://localhost:5601

## Postman Collection

The project includes a Postman collection saved at:
```
src/main/resources/collections/AI Summary Transactions.postman_collection.json
```

Import this collection into Postman to easily test the API endpoints.

## Development

This project was developed using **Cursor IDE** as an aid during development. The `chats/` directory contains the history of the main chats during implementations, documenting technical decisions and the development process.

## Additional Documentation

- [Micronaut 4.9.1 User Guide](https://docs.micronaut.io/4.9.1/guide/index.html)
- [Micronaut API Reference](https://docs.micronaut.io/4.9.1/api/index.html)
- [LangChain4J Documentation](https://github.com/langchain4j/langchain4j)

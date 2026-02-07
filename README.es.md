[English](README.md) | [Português](README.pt-br.md)

# AI Summary Transactions

Proyecto desarrollado para aprendizaje y exploración de **LangChain4J** utilizando un escenario ficticio de transacciones de tarjetas de crédito. El proyecto demuestra cómo integrar inteligencia artificial (OpenAI) con almacenamiento y búsqueda en OpenSearch para generar resúmenes e insights de transacciones financieras.

## Tecnologías

- **Java 21** - Lenguaje de programación
- **Maven** - Gestor de dependencias y build
- **Micronaut 4.9.1** - Framework Java moderno y reactivo
- **LangChain4J** - Biblioteca para integración con modelos de lenguaje
- **OpenAI GPT-4o-mini** - Modelo de IA para generación de resúmenes e insights
- **OpenSearch 3** - Motor de búsqueda y análisis de datos
- **OpenSearch Dashboards** - Interfaz de visualización y análisis
- **MapStruct** - Mapeo de objetos
- **Lombok** - Reducción de boilerplate

## Estructura del Proyecto

El proyecto sigue una arquitectura en capas con los siguientes paquetes principales:

- `ai.summary.transactions.application` - Casos de uso de la aplicación
- `ai.summary.transactions.controller` - Controladores REST (generados vía OpenAPI)
- `ai.summary.transactions.core` - Configuraciones y factories
- `ai.summary.transactions.domain` - Entidades de dominio y servicios de IA
- `ai.summary.transactions.model` - Modelos de datos (generados vía OpenAPI)

## OpenSearch

El proyecto utiliza **OpenSearch** ejecutándose en contenedores Docker para almacenar y buscar transacciones. La configuración incluye un cluster con dos nodos y OpenSearch Dashboards para visualización.

### Inicialización de los Contenedores

Para iniciar los contenedores de OpenSearch:

```bash
docker-compose up -d
```

Esto iniciará:
- **OpenSearch Node 1** en el puerto `9200`
- **OpenSearch Node 2** (nodo secundario)
- **OpenSearch Dashboards** en el puerto `5601`

### Variables de Entorno Necesarias

Antes de iniciar los contenedores, defina la variable de entorno:

```bash
export OPENSEARCH_INITIAL_ADMIN_PASSWORD=su_contraseña_aqui
```

### Accediendo a OpenSearch Dashboards

Después de iniciar los contenedores, acceda a OpenSearch Dashboards en:
- **URL**: http://localhost:5601

OpenSearch está configurado sin seguridad para facilitar el desarrollo local.

### Generación de Datos de Prueba

El proyecto incluye un prompt detallado para generar datos de prueba en el archivo `chats/criando_massa_teste.md`. Este prompt puede ser ejecutado con una IA (como ChatGPT, Claude, etc.) para generar un archivo CSV con transacciones ficticias de tarjeta de crédito.

El archivo CSV generado debe ser importado vía la **API propia del proyecto** utilizando la automatización de Postman. La colección de Postman ya tiene configurado un **Collection Runner** en la carpeta "CRUD Transactions > Runner" que permite importar automáticamente los datos del CSV a través del endpoint `POST /transactions`.

El prompt especifica:

- **1000 transacciones** distribuidas a lo largo de 2025
- **Categorías realistas** de establecimientos brasileños
- **Distribución de valores** siguiendo patrones del mercado
- **Formato CSV** estandarizado para importación

**Cómo importar los datos:**

1. Genere el archivo CSV usando el prompt en `chats/criando_massa_teste.md`
2. Abra la colección de Postman en la aplicación
3. Ejecute el **Collection Runner** en la carpeta "CRUD Transactions > Runner"
4. Configure el archivo CSV como fuente de datos en el Runner
5. Postman ejecutará automáticamente las solicitudes para crear las transacciones vía API

Esto poblará el índice `transactions` en OpenSearch con datos de prueba a través de la propia API del proyecto.

## LangChain4J

El proyecto utiliza **LangChain4J** para integrar con la API de OpenAI y generar resúmenes e insights inteligentes sobre transacciones de tarjeta de crédito. El escenario ficticio permite explorar:

- Generación de resúmenes de transacciones usando IA
- Análisis de patrones e insights de gastos
- Integración de prompts estructurados con modelos de lenguaje

La configuración de LangChain4J está en `application.yml` y utiliza el modelo `gpt-4o-mini` de OpenAI.

## Cómo Ejecutar Localmente

### Prerrequisitos

- Java 21 instalado
- Maven instalado
- Docker y Docker Compose instalados
- Clave de API de OpenAI

### Pasos para Ejecución

1. **Clone el repositorio** (si aún no lo ha hecho):
   ```bash
   git clone <url-del-repositorio>
   cd ai-summary-transactions
   ```

2. **Configure la clave de API de OpenAI**:
   
   Cree un archivo `.env` en la raíz del proyecto o exporte la variable de entorno:
   ```bash
   export OPENAI_API_KEY=su_clave_api_openai_aqui
   ```
   
   Puede obtener una clave de API en: https://platform.openai.com/api-keys

3. **Configure la contraseña de OpenSearch**:
   ```bash
   export OPENSEARCH_INITIAL_ADMIN_PASSWORD=admin123
   ```

4. **Inicie los contenedores de OpenSearch**:
   ```bash
   docker-compose up -d
   ```

5. **Espere a que los contenedores estén listos** (puede tardar unos segundos):
   ```bash
   docker-compose ps
   ```

6. **Compile y ejecute la aplicación**:
   ```bash
   ./mvnw clean install
   ./mvnw mn:run
   ```

   O usando Maven directamente:
   ```bash
   mvn clean install
   mvn mn:run
   ```

7. **Acceda a la aplicación**:
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui
   - OpenSearch Dashboards: http://localhost:5601

## Colección de Postman

El proyecto incluye una colección de Postman guardada en:
```
src/main/resources/collections/AI Summary Transactions.postman_collection.json
```

Importe esta colección en Postman para probar fácilmente los endpoints de la API.

## Desarrollo

Este proyecto fue desarrollado utilizando **Cursor IDE** como ayuda durante el desarrollo. El directorio `chats/` contiene el historial de los principales chats durante las implementaciones, documentando las decisiones técnicas y el proceso de desarrollo.

## Documentación Adicional

- [Micronaut 4.9.1 User Guide](https://docs.micronaut.io/4.9.1/guide/index.html)
- [Micronaut API Reference](https://docs.micronaut.io/4.9.1/api/index.html)
- [LangChain4J Documentation](https://github.com/langchain4j/langchain4j)

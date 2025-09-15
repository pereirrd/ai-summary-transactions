package ai.summary.transactions.domain.ai;

import dev.langchain4j.service.SystemMessage;
import io.micronaut.langchain4j.annotation.AiService;

@AiService(tools = { ReferenceDateTool.class })
public interface AITransactionService {

   @SystemMessage("""
         - Como um desenvolvedor de software, você é responsável por criar uma query de busca de transações bancárias.
         - Você deve usar DSL (Domain Specific Language) que fará busca em um Opensearch.

         - A estrutura de dados a ser buscada é a seguinte (exemplo):
         {
             "date": "2025-01-08T20:22:32Z",
             "amount": 29.99,
             "description": "Purchase at grocery store",
             "merchant": {
                 "name": "Walmart",
                 "category": "Groceries"
             }
         }

         - EXEMPLOS DETIPOS DE PERGUNTAS(podem haver combinações dessas ou outras):

         1. PERGUNTAS POR DATA:
            - Quais foram as transações de hoje?
            - Quais foram as transações de ontem?
            - Quais foram as transações de último ano?
            Use o campo "date" para buscar as transações.

         2. PERGUNTAS POR VALOR MONETÁRIO:
            - Quais foram as transações maiores que R$ 100,00?
            - Quais foram as transações menores que R$ 200,00?
            - Quais foram as transações entre R$ 100,00 e R$ 200,00?
            Use o campo "amount" para buscar as transações.

         3. PERGUNTAS POR DESCRIÇÃO:
            - Quais foram as transações com a descrição "Compra de alimentos"?
            Use o campo "description" para buscar as transações.

         4. PERGUNTAS POR NOME DA LOJA:
            - Quais foram as transações com o nome da loja "Magalu"?
            Use o campo "merchant.name" para buscar as transações.

         5. PERGUNTAS POR CATEGORIA:
            - Quais foram as transações com a categoria "Alimentos"?
            Use o campo "merchant.category" para buscar as transações.

         6. PERGUNTAS COMBINADAS:
            - Quais foram as transações de R$ 100,00 de "Compra de alimentos" no "Walmart" em 2025-01-08?
            Use os campos "amount", "description", "merchant.name", "merchant.category" e "date" conforme necessário.

         - Entenda "compras no cartão" como as transações que foram feitas com o cartão de crédito e que estamos buscando no Opensearch.
         - Essas são apenas alguns exemplos de como o usuário pode fazer perguntas.
         - Você deve retornar somente a query em formato JSON com sintaxe DSL sem nenhum tipo de formatação e sem comentários.
         - A query deve ser somente texto puro.
         """)
   String createQuery(String userQuestion);

   /*
    * - Crie duas queries:
    * 1. Primeira query para buscar as transações que atendam todos os critérios
    * possíveis do usuário.
    * 2. Segunda query para buscar as transações que seja menos específica que a
    * primeira query e que seja mais genérica.
    * - Essas duas queries devem ser retornadas em um array de strings sem nenhum
    * tipo de formatação, somente texto puro.
    */
}

# Geração de Massa de Dados para Teste - Transações de Cartão de Crédito

## Objetivo
Gerar uma massa de dados realista para testes do projeto, simulando transações de cartão de crédito com características típicas do mercado brasileiro.

## Modelo de Dados

O arquivo CSV deve seguir exatamente este formato:

```csv
date,amount,description,merchant_name,merchant_category
2025-04-09T00:00:00Z,70.8,Almoço no restaurante,Super Mercado BH,Supermercado
```

## Especificações Técnicas

### Quantidade e Localização
- **Quantidade**: 1000 transações
- **Formato**: Arquivo CSV
- **Localização**: `/test/resources/transactions_test_data.csv`
- **Encoding**: UTF-8

### Período Temporal
- **Data inicial**: 1º de janeiro de 2025 (2025-01-01T00:00:00Z)
- **Data final**: 31 de dezembro de 2025 (2025-12-31T23:59:59Z)
- **Limite mensal**: Máximo 100 transações por mês
- **Limite diário**: Máximo 10 transações por dia
- **Distribuição temporal**: Variação natural ao longo do ano (considerar sazonalidade)

### Distribuição de Valores

| Faixa de Valor | Percentual | Exemplos de Transações |
|---|---|---|
| < R$ 150,00 | 70% | Almoço, café, transporte público, pequenas compras |
| R$ 150,00 - R$ 500,00 | 20% | Supermercado, farmácia, combustível, roupas |
| R$ 500,00 - R$ 5.000,00 | 9% | Eletrônicos, móveis, consultas médicas |
| > R$ 5.000,00 | 1% | Viagens, eletrodomésticos, cursos |

### Categorias de Transações

#### 1. Alimentação e Consumo (40%)
- **Supermercados**: Compra de alimentos, produtos de limpeza
- **Restaurantes**: Almoço, jantar, lanches
- **Padarias**: Pão, café da manhã
- **Açougues**: Carnes, frango
- **Hortifrúti**: Verduras, frutas

#### 2. Serviços (25%)
- **Educação**: Mensalidade escolar, cursos
- **Saúde**: Consultas médicas, exames, farmácia
- **Transporte**: Combustível, estacionamento, Uber/Taxi
- **Manutenção**: Carro, casa, eletrodomésticos
- **Serviços domésticos**: Faxina, jardinagem

#### 3. Eletrônicos e Tecnologia (15%)
- **E-commerce**: Amazon, Mercado Livre, Magazine Luiza
- **Eletrônicos**: Celular, notebook, tablet, fones
- **Acessórios**: Cabos, carregadores, pilhas
- **Software**: Assinaturas, jogos

#### 4. Vestuário e Acessórios (10%)
- **Roupas**: Lojas de departamento, marcas
- **Calçados**: Sapatos, tênis
- **Acessórios**: Bolsas, relógios, joias

#### 5. Lazer e Entretenimento (7%)
- **Cinema**: Ingressos, pipoca
- **Streaming**: Netflix, Spotify, Amazon Prime
- **Viagens**: Hotéis, passagens, turismo
- **Esportes**: Academia, equipamentos

#### 6. Outros (3%)
- **Financiamentos**: Parcelas de financiamento
- **Investimentos**: Aplicações, seguros
- **Doações**: ONGs, instituições

### Nomes de Estabelecimentos

#### Reais (70%)
- **Supermercados**: Extra, Pão de Açúcar, Carrefour, Walmart
- **Farmácias**: Pague Menos, Drogasil, Farmácia Araújo
- **E-commerce**: Amazon, Mercado Livre, Magazine Luiza, Casas Bahia
- **Restaurantes**: McDonald's, Subway, Burger King, Outback
- **Postos**: Shell, Petrobras, Ipiranga
- **Bancos**: Itaú, Bradesco, Santander, Nubank

#### Fictícios (30%)
- **Supermercados**: Super Mercado BH, Hipermercado Central
- **Farmácias**: Farmácia São José, Drogaria Popular
- **Restaurantes**: Restaurante do João, Cantina da Maria
- **Serviços**: Auto Center Silva, Escola ABC

## Validações de Qualidade

### Consistência de Dados
- Valores devem ser compatíveis com a descrição
- Categorias devem fazer sentido com o estabelecimento
- Datas devem ser cronologicamente válidas
- Não deve haver transações duplicadas

### Realismo
- Horários de transação compatíveis com tipo de estabelecimento
- Valores em reais (R$) com até 2 casas decimais
- Descrições em português brasileiro natural
- Nomes de estabelecimentos realistas

## Entregáveis

### 1. Arquivo Principal
- **Nome**: `transactions_test_data.csv`
- **Localização**: `/test/resources/`
- **Formato**: CSV com header, encoding UTF-8

### 2. Relatório de Análise
- **Nome**: `test_data_analysis.md`
- **Localização**: `/test/resources/`
- **Conteúdo**:
  - Gráfico de distribuição temporal (linha do tempo)
  - Gráfico de distribuição de valores (histograma)
  - Gráfico de categorias de estabelecimentos (pizza)
  - Estatísticas descritivas (média, mediana, desvio padrão)
  - Validação das regras de negócio

### 3. Arquivo de Metadados
- **Nome**: `test_data_metadata.json`
- **Localização**: `/test/resources/`
- **Conteúdo**:
  - Data de geração
  - Parâmetros utilizados
  - Contadores por categoria
  - Estatísticas de validação

## Observações Adicionais

- **Moeda**: Todos os valores em Real brasileiro (R$)
- **Timezone**: UTC (formato ISO 8601)
- **Separador CSV**: Vírgula (,)
- **Aspas**: Não utilizar aspas desnecessárias
- **Espaços**: Evitar espaços extras nos campos
- **Caracteres especiais**: UTF-8 para acentos e cedilhas
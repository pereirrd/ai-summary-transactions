Me ajude a gerar uma massa de dados para teste do projeto. O modelo de dados são tranações de cartão de crédito que segue o modelo:

```` json
{
  "date": "2025-01-08T20:22:32Z",
  "amount": 29.99,
  "description": "Purchase at grocery store",
  "merchant": {
    "name": "Walmart",
    "category": "Groceries"
  }
}
````

Regras para a geração: 
- Gere 1000 variações desse modelo de dados em um array.
- Salve em um arquivo json no diretório /test/resources.
- Gere os dados em portugês brasileiro.
- Faça variações de data e hora, o campo "date", de modo que:
    - tenha no máximo 100 tranações por mês;
    - tenha no máximo 10 transações por dia;
- A data incial será primeiro janeiro de 2025 e a data final será 31 de dezembro de 2025.
- As variações de valor, o campo "amount", devem ser compativeis com a descrição. Por exemplo:
    - a compra de uma casa é um valor muito alto como 500000.00.
    - a compra de um chocolate é um valor baixo como 2.00.
- A descrição da transação pode ser itens gerais de compra, por exemplo:
    - itens de consumo, como: materiais de limpeza e higiene, alimentos, remérdios, produtos para pets;
    - serviços, como: mensalidade da escola, manutenção do carro, consulta médica, diária da faxina, reforma da casa;
    - produtos eletrônicos: tablet, celular, pilhas, notebook, fone de ouvido;
    - bens: casa, carro, moto;
- O nome e categoria dos estabelicimentos podem ser ficticios ou reais, como:
    - Amazon, Mercado Livre, Famácia Araujo, Super mercado BH, etc;

No final da geração crie um resumo em um arquivo markdown com gráficos da distribuição de datas, valores e quantidade de categorias de estabelecimentos.


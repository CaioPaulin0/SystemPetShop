# Busca de produtos pelo nome no banco de métricas

Para consultar o banco principal, use [a busca em main](MAIN_SEARCH.md). As duas buscas compartilham as regras de validação, normalização, paginação e mapeamento em `NameSearch`, com repositórios e transações separados.

`GET /product/metrics/search?name=racao&page=0`

- `name`: obrigatório, aceita o nome completo ou qualquer trecho, sem diferenciar maiúsculas e minúsculas. Espaços no início e no fim são removidos.
- `page`: opcional, começa em `0`. Cada página contém até 6 produtos; o tamanho é fixo.
- A consulta pesquisa os produtos do banco de métricas, de todas as marcas e distribuidores, ordenados por nome e ID. Usa `ProductMetricsRepository`, vinculado ao `metricsEntityManagerFactory`, e uma transação de leitura com `metricsTransactionManager`.
- A resposta `200 OK` é uma lista JSON de `ProductResponse`, com os mesmos campos de `/product/all`, incluindo o nome completo do produto.
- Sem correspondências ou com uma página além dos resultados, retorna `[]`.
- Nome ausente ou em branco e página negativa ou inválida retornam `400 Bad Request`.
- `%` e `_` são pesquisados como caracteres literais.

Exemplos no PowerShell, com a API local em execução:

```powershell
Invoke-RestMethod 'http://localhost:8080/product/metrics/search?name=racao&page=0'
Invoke-RestMethod 'http://localhost:8080/product/metrics/search?name=racao&page=1'
```

Fluxo: `ProductController.searchProductMetrics` → `ProductMetricsSearchUseCase` → `ProductRepository.searchMetricsByName` → `JpaProductEntityRepository` → `ProductMetricsRepository`. O banco aplica o filtro e a paginação; as entidades são convertidas em domínio, `ProductOutput` e `ProductResponse`.

Validação automatizada: `mvn -Dtest=ProductMetricsSearchTest test` executa o fluxo HTTP até a consulta real em dois bancos H2 com as configurações Spring de persistência, cobrindo isolamento entre o banco principal e o de métricas, busca parcial e completa, caixa, paginação, dados da resposta, ausência de resultados, caracteres especiais e parâmetros inválidos.

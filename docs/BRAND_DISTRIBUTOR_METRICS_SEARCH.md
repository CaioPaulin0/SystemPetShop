# Busca de marcas e distribuidores no banco de métricas

Para consultar o banco principal, use [as buscas em main](MAIN_SEARCH.md). As buscas dos dois bancos compartilham as regras de validação, normalização, paginação e mapeamento em `NameSearch`, com repositórios e transações separados.

```http
GET /brand/metrics/search?name=pet&page=0
GET /distributor/metrics/search?name=pet&page=0
```

As duas rotas seguem o mesmo contrato da busca de produtos em métricas:

- `name` é obrigatório: aceita o nome completo ou qualquer trecho, sem diferenciar maiúsculas e minúsculas. Espaços externos são removidos. `%` e `_` são tratados como texto literal.
- `page` é opcional e começa em `0`. Cada página contém até 6 itens; o tamanho é fixo.
- Os resultados são ordenados por nome e ID. A busca de marcas abrange todos os distribuidores, sem exigir `idDistributor`.
- A resposta `200 OK` é uma lista de `BrandReponse` ou `DistributorReponse`, com os mesmos campos das respectivas listagens existentes.
- Sem correspondências ou com uma página além dos resultados, retorna `[]`.
- Nome ausente ou em branco e página negativa ou inválida retornam `400 Bad Request`.

Os fluxos usam `BrandMetricsSearchUseCase` e `DistributorMetricsSearchUseCase`, passando por `searchMetricsByName` nos contratos de domínio e adaptadores JPA. As consultas são executadas por `BrandMetricsRepository` e `DistributorMetricsRepository`, vinculados ao `metricsEntityManagerFactory`, com `metricsTransactionManager` em modo somente leitura.

Exemplos no PowerShell:

```powershell
Invoke-RestMethod 'http://localhost:8080/brand/metrics/search?name=pet&page=0'
Invoke-RestMethod 'http://localhost:8080/distributor/metrics/search?name=pet&page=1'
```

Validação: `mvn -Dtest=BrandDistributorMetricsSearchTest test`. Os testes percorrem HTTP, casos de uso e persistência em dois bancos H2, usando as configurações Spring para verificar isolamento entre principal e métricas, busca parcial e completa, paginação, dados da resposta e validações.

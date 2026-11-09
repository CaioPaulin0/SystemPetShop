# Busca por nome no banco principal

Os três endpoints usam `mainTransactionManager` em modo somente leitura e os repositórios vinculados ao `mainEntityManagerFactory`:

| Rota | Repositório JPA | Resposta |
| --- | --- | --- |
| `GET /product/search?name=racao&page=0` | `ProductEntityRepository` | Lista de `ProductResponse` |
| `GET /brand/search?name=pet&page=0` | `BrandEntityRepository` | Lista de `BrandReponse` |
| `GET /distributor/search?name=pet&page=0` | `DistributorRepositoryEntity` | Lista de `DistributorReponse` |

- `name` é obrigatório. Aceita o nome completo ou qualquer trecho, sem diferenciar maiúsculas e minúsculas. Espaços externos são removidos; `%` e `_` são caracteres literais.
- `page` é opcional, começa em `0` e tem tamanho fixo de 6 itens.
- A ordenação usa nome e ID. A busca abrange todas as marcas e distribuidores, sem exigir seus identificadores.
- A resposta `200 OK` contém os campos completos dos itens encontrados. Sem correspondências ou em uma página além dos resultados, retorna `[]`.
- Nome ausente ou em branco e página negativa ou inválida retornam `400 Bad Request`.

## Reutilização e acesso aos bancos

`NameSearch` concentra validação, normalização do nome, paginação e conversão dos resultados. Os casos de uso `ProductSearchUseCase`, `BrandSearchUseCase` e `DistributorSearchUseCase` usam esse mesmo código que os casos de uso de métricas.

Os métodos `searchByName` dos adaptadores consultam o banco principal; `searchMetricsByName` consulta o banco de métricas. A consulta parcial é derivada pelo Spring Data com `ContainingIgnoreCase`, e o filtro e a paginação são aplicados pelo banco.

As rotas `/product/metrics/search`, `/brand/metrics/search` e `/distributor/metrics/search` continuam usando `metricsTransactionManager` e seus respectivos repositórios de métricas.

## Exemplos

```powershell
Invoke-RestMethod 'http://localhost:8080/product/search?name=racao&page=0'
Invoke-RestMethod 'http://localhost:8080/brand/search?name=pet&page=0'
Invoke-RestMethod 'http://localhost:8080/distributor/search?name=pet&page=1'
```

## Testes

`mvn -Dtest=MainSearchTest,ProductMetricsSearchTest,BrandDistributorMetricsSearchTest test`

Os testes usam dois bancos H2 com as configurações Spring de persistência. Verificam isolamento entre principal e métricas, busca parcial e completa, ordenação, paginação, campos da resposta, caracteres especiais e parâmetros inválidos.

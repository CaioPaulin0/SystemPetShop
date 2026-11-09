# Busca por nome no módulo expired

`GET /expired/search?name=racao&page=0`

- `name` é obrigatório e aceita o nome completo ou qualquer trecho do nome do produto, sem diferenciar maiúsculas e minúsculas. Espaços externos são removidos; `%`, `_` e `!` são tratados como texto literal.
- `page` é opcional, começa em `0` e tem tamanho fixo de 6 itens.
- A consulta segue o critério de `/expired/findAll`: retorna os produtos com a maior `versionProduct` de cada `lotProduct`, ordenados por nome e ID. A versão mais recente é determinada antes de filtrar pelo nome.
- A resposta `200 OK` é uma lista de `ExpiredResponse`, com os mesmos campos da listagem existente, incluindo nome da marca, nome do distribuidor, lote, estoque e datas.
- Produtos sem marca ou distribuidor cadastrado também são retornados; os respectivos nomes ficam nulos.
- Sem correspondências ou em uma página além dos resultados, retorna `[]`.
- Nome ausente ou em branco e página negativa ou inválida retornam `400 Bad Request`.

## Fluxo e persistência

`ExpiredController.searchExpired` → `ExpiredSearchUseCase` → `ExpiredRepository.searchByName` → `JpaExpiredEntityRepository` → `ExpiredEntityRepository`.

O caso de uso reutiliza `NameSearch` para validação, normalização, paginação e mapeamento. A consulta usa `expiredEntityManagerFactory` e `expiredTransactionManager` em modo somente leitura, com filtro e paginação no banco.

## Exemplo

```powershell
Invoke-RestMethod 'http://localhost:8080/expired/search?name=racao&page=0'
Invoke-RestMethod 'http://localhost:8080/expired/search?name=racao&page=1'
```

## Testes

`mvn -Dtest=ExpiredSearchTest test`

Os testes percorrem HTTP, caso de uso e persistência, usando três bancos H2 com as configurações Spring de main, metrics e expired. Cobrem isolamento dos bancos, busca parcial e completa, paginação, parâmetros inválidos, caracteres especiais, seleção da versão mais recente por lote e relacionamentos presentes ou ausentes.

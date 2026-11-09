# Filtro de verificação no módulo expired

`GET /expired/filter?dataDueProductVerify=true&page=0`

- `dataDueProductVerify` é obrigatório: `true` retorna registros com o campo verdadeiro; `false` retorna registros com o campo falso.
- `page` começa em zero e tem valor padrão `0`. Cada página contém até 8 itens, ordenados por `nameProduct` e `idProduct`.
- A consulta usa somente o banco expired. Seleciona a maior `versionProduct` de cada `lotProduct` antes de aplicar o filtro, como na listagem existente.
- O filtro consulta o boolean armazenado; não recalcula o vencimento pela data.
- O retorno HTTP 200 é uma lista JSON, sem envelope de paginação. Sem resultados ou após a última página, retorna `[]`.
- Boolean ausente ou inválido e página negativa ou inválida retornam HTTP 400.
- Produtos sem marca ou distribuidor cadastrado continuam no resultado, com os respectivos nomes nulos.

O contrato `ExpiredRepository.filterByDataDueProductVerify(boolean, Pageable)` retorna `List<Expired>`. `JpaExpiredEntityRepository` implementa o contrato usando a consulta paginada de `ExpiredEntityRepository`. O `ExpiredFilterUseCase` valida a página e converte o domínio para o DTO usado pelo controller.

O campo `dataDueProductVerify` em `ExpiredOutput` e `ExpiredResponse` foi corrigido de `LocalDate` para `boolean`, inclusive nas respostas de `/expired/search` e `/expired/findAll`. A data de vencimento permanece em `dataDueProduct`.

```powershell
Invoke-RestMethod 'http://localhost:8080/expired/filter?dataDueProductVerify=true&page=0'
Invoke-RestMethod 'http://localhost:8080/expired/filter?dataDueProductVerify=false&page=1'
```

Os testes HTTP com persistência H2 estão em `ExpiredSearchTest` e cobrem ambos os valores, isolamento dos bancos, paginação, versão mais recente por lote e parâmetros inválidos.

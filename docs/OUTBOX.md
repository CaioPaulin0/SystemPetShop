# Outbox: publicacao duravel de eventos

Os casos de uso que ja produziam eventos agora registram o JSON na tabela `outbox_event`
na mesma transacao da alteracao de negocio. Os produtores nao acessam Kafka durante
o request. O publicador agendado envia os registros confirmados e marca `published_at`
somente depois da confirmacao do broker.

## Preparacao do banco

Requer MySQL 8+ e tabelas InnoDB, pois o publicador usa `FOR UPDATE SKIP LOCKED`.
Antes de iniciar esta versao, execute `src/main/resources/db/manual/2026-11-04-outbox.sql`
separadamente nos bancos configurados em:

- `app.datasource.main`: atualmente `systempetshop`;
- `app.datasource.productexpired`: atualmente `systemExpired`.

Selecione o banco antes de executar o script. Ele nao altera registros de negocio,
nao cria eventos retroativos e nao e executado automaticamente pela aplicacao.
Nao e necessario criar uma outbox no banco de metricas.

Sem essa tabela, operacoes que geram eventos falham e sao revertidas; o publicador
registra erros de acesso. Desativar o publicador nao desativa a gravacao da outbox.

## Fluxos cobertos

- Produto: criacao, edicao, exclusao e nova versao, incluindo os eventos de metricas.
- Marca: criacao, edicao e exclusao, incluindo os eventos de metricas.
- Distribuidor: criacao e exclusao, incluindo os eventos de metricas.
- Verificacao de vencimento: alteracao e evento no banco de validade.

A edicao de distribuidor continua sem evento, como no comportamento anterior
(problema independente identificado na auditoria).

Os topicos, chaves e payloads atuais foram preservados. Cada envio recebe um header
`outbox-event-id`, que permanece igual em todas as tentativas do mesmo registro.
O payload e enviado como JSON UTF-8 sem dupla serializacao, compativel com os
consumidores atuais, que nao dependem dos headers de tipo do serializer anterior.

## Configuracao opcional

As propriedades podem ser fornecidas pela configuracao externa do Spring:

| Propriedade | Padrao | Finalidade |
| --- | --- | --- |
| `app.outbox.publisher-enabled` | `true` | Habilita o agendamento; `false` acumula eventos duravelmente. |
| `app.outbox.poll-delay-ms` | `1000` | Intervalo apos terminar cada rodada e atraso inicial. |
| `app.outbox.batch-size` | `20` | Maximo de eventos tentados por banco em cada rodada. |
| `app.outbox.send-timeout-ms` | `10000` | Limite de espera pelo envio; a busca de metadados pode consumir mais um intervalo igual. |
| `app.outbox.retry-delay-ms` | `1000` | Atraso inicial depois de uma falha. |
| `app.outbox.max-retry-delay-ms` | `60000` | Limite do atraso exponencial entre tentativas. |

O produtor usa as configuracoes de conexao e seguranca de `spring.kafka`, com
serializer de texto, confirmacao `acks=all` e idempotencia habilitada. O timeout de
requisicao/entrega e o limite de bloqueio seguem o timeout configurado acima.

Cada tentativa bloqueia somente a linha selecionada ate o envio terminar. Outros
publicadores pulam linhas bloqueadas. Nao ha limite de tentativas nem descarte
automatico de eventos com erro. Uma indisponibilidade do banco nao encerra o
agendamento e nao impede que o outro banco seja tentado.

## Garantias e limites

- A gravacao do evento exige uma transacao ativa do banco correspondente.
- Rollback do negocio reverte seus eventos; erro de serializacao ou insercao da outbox
  reverte a alteracao de negocio.
- Eventos confirmados no banco sobrevivem a reinicializacoes.
- Falhas de envio permanecem pendentes, com `attempts`, `next_attempt_at` e `last_error`.
- A entrega e **pelo menos uma vez**. Se Kafka confirmar e a aplicacao cair antes de
  confirmar `published_at`, o evento sera reenviado. O header permite identificar a
  repeticao; nao foi implementada uma tabela de deduplicacao nos consumidores.
- A idempotencia do produtor Kafka nao elimina essa janela entre Kafka e MySQL.
- O ponto 6 da auditoria continua separado: topicos distintos, retries e publicadores
  concorrentes nao garantem ordem global por entidade. Esta mudanca nao implementa
  sequenciamento, tombstones ou protecao contra eventos antigos nos consumidores.
- Dados que ja estavam divergentes antes da mudanca precisam de reconciliacao propria.

## Acompanhamento e retencao

Execute em cada banco de origem:

```sql
SELECT COUNT(*) AS pendentes, MIN(created_at) AS pendente_mais_antigo
FROM outbox_event WHERE published_at IS NULL;

SELECT event_id, topic, attempts, next_attempt_at, last_error
FROM outbox_event WHERE published_at IS NULL
ORDER BY next_attempt_at, id LIMIT 100;
```

As datas da outbox sao UTC. `last_error` registra a classe da excecao, sem payload
ou credenciais. Monitore eventos antigos e tentativas crescentes. Registros publicados
sao mantidos; defina uma politica de retencao e remova apenas linhas com
`published_at IS NOT NULL` apos o periodo escolhido. Nunca limpe pendentes para
resolver erro de publicacao.

## Verificacao

```powershell
mvn -o '-Dtest=OutboxStoreTest,OutboxUseCaseTransactionTest,OutboxPublisherTest,KafkaOutboxSenderTest,BrandTest,ProductMetricsMessagingTest' test
```

Os testes usam H2 em modo MySQL, executam o proprio script SQL e incluem
transacoes JPA reais com JDBC, rollback de dados/eventos, isolamento dos bancos,
retentativas, reinicio do publicador, locks concorrentes, confirmacao assincrona,
serializacao compativel e falha de commit apos confirmacao do envio.

MySQL e Kafka reais ainda precisam de verificacao no ambiente de implantacao.
O teste de sucesso da consulta nativa `existsProduct` nao e portavel para H2
(o SQL `EXISTS` retorna Boolean, enquanto a interface existente espera Long);
o versionamento tem cobertura unitaria de eventos e integracao do rollback.

A suite ampla ainda apresenta as falhas anteriores da auditoria nos mapeamentos de
ProductOutput e no filtro de produtos desativados. Elas nao foram mascaradas.

# Transferência Bancária API

API REST em Java para transferências entre contas, com foco em **controle de concorrência real** — não é mais um CRUD: é um sistema pequeno em escopo, mas profundo em engenharia, pensado para resolver o mesmo problema que qualquer banco de verdade enfrenta: garantir que duas transferências simultâneas não corrompam o saldo de uma conta (o clássico problema de *double spending*).

## Stack

- **Java 21** + **Spring Boot 3**
- **Spring Data JPA** / Hibernate
- **PostgreSQL 18.4** (via Docker)
- **Bean Validation** (Jakarta Validation)
- **JUnit 5** + `ExecutorService`/`CountDownLatch` para teste de concorrência automatizado
- **JMeter** para teste de carga manual

## O problema central: double spending

Duas requisições de transferência disparadas ao mesmo tempo, sobre a mesma conta de origem, podem ler o mesmo saldo antes de qualquer uma escrever — e as duas debitam, mesmo que o saldo só coubesse uma vez. Esse projeto existe para provar, sob concorrência real (não só na teoria), que isso não acontece aqui.

### A solução: lock pessimista

`ContaRepository` expõe uma busca que trava a linha da conta no banco (`SELECT ... FOR UPDATE`, via `@Lock(LockModeType.PESSIMISTIC_WRITE)`), dentro de uma transação (`@Transactional`). Enquanto uma transferência está processando uma conta, qualquer outra transferência concorrente sobre a mesma conta fica bloqueada no Postgres até a primeira terminar — garantindo que a segunda sempre lê o saldo já atualizado, nunca o valor obsoleto.

### Prevenção de deadlock: ordenação de lock

Uma transferência sempre envolve duas contas (origem e destino). Se cada transferência travasse "origem primeiro, depois destino", duas transferências em direções opostas (A→B e B→A, simultâneas) poderiam travar uma esperando a outra para sempre. A solução: as duas contas são sempre travadas na mesma ordem determinística (comparando os UUIDs), independente de qual é origem e qual é destino — eliminando a espera circular que causa deadlock.

## Um bug real encontrado e resolvido: deadlock entre `REQUIRES_NEW` e foreign key

Esse foi o problema mais difícil do projeto, e vale documentar porque é o tipo de bug que só aparece sob carga real.

Toda tentativa de transferência — sucesso ou falha — gera um registro em `transacoes`. Como uma falha de saldo insuficiente precisa reverter o débito/crédito (rollback) mas **preservar** o registro da tentativa, o registro de falha é gravado numa transação separada (`@Transactional(propagation = REQUIRES_NEW)`), com sua própria conexão ao banco, para que sobreviva ao rollback da transação principal.

O problema: a entidade `Transacao` tinha uma *foreign key* apontando para `Conta`. Ao inserir um registro de falha, o Postgres precisa adquirir um lock `FOR KEY SHARE` na linha da conta referenciada, para garantir integridade referencial — mas essa mesma linha já estava travada com `FOR UPDATE` pela própria transação externa (mesma thread Java, sessão diferente no banco). Resultado: a thread trava esperando a si mesma, um deadlock que o Postgres não detecta (porque não existe ciclo visível *dentro* do banco — a dependência está na thread Java, do lado de fora).

Sob 50 requisições concorrentes na mesma conta, isso esgotava o pool de conexões (cada thread presa segurando conexão indefinidamente) e as requisições falhavam com `500` após o timeout padrão do HikariCP (30s), em vez do `409` esperado.

**Solução**: a foreign key foi removida (`@JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))`) nos dois campos de `Transacao`. `Transacao` funciona como um log de auditoria, intencionalmente desacoplado da integridade referencial da tabela `contas` — um padrão real em tabelas de log, que evita exatamente esse tipo de contenção de lock entre a tabela "viva" e o registro histórico.

## Endpoints

| Método | Rota | Descrição | Status de sucesso |
|---|---|---|---|
| `POST` | `/contas` | Cria uma conta com saldo inicial | `201 Created` |
| `POST` | `/transacoes` | Executa uma transferência entre duas contas | `200 OK` |

### Erros tratados

| Exceção | Status | Situação |
|---|---|---|
| `MethodArgumentNotValidException` | `400` | Campo inválido no corpo da requisição |
| `ContaNaoEncontradaException` | `404` | UUID de conta não existe |
| `ContaPropriaException` | `409` | Origem e destino são a mesma conta |
| `SaldoInsuficienteException` | `409` | Saldo da origem menor que o valor da transferência |

## Como rodar

```bash
docker compose up -d
```

Isso sobe um Postgres 18.4 já configurado para bater com o `application.yml` do projeto (usuário, senha, banco e porta). Depois, roda a aplicação normalmente pela IDE ou `mvn spring-boot:run` — o schema é criado automaticamente na primeira subida.

## Testes

### Automatizado — `TransacaoServiceConcorrenciaTest`

Dispara 50 chamadas concorrentes de `transferir(...)` contra a mesma conta de origem, usando `ExecutorService` + `CountDownLatch` para forçar que todas as threads iniciem no mesmo instante (a mesma ideia do `Synchronizing Timer` do JMeter, só que em Java puro, dentro de um `@SpringBootTest` contra o banco real). Verifica que exatamente 1 transferência é bem-sucedida e o saldo final bate exatamente com o esperado.

```bash
mvn test
```

### Carga — JMeter

50 threads sincronizadas disparando a mesma transferência simultaneamente contra a mesma conta. Resultado validado: 1 sucesso (`200`), 49 rejeitadas por saldo insuficiente (`409`), saldo final consistente — sem inconsistência mesmo sob concorrência real.

## Decisões de arquitetura, resumidas

- **`BigDecimal`** para valores monetários — `double`/`float` não representam frações decimais com exatidão.
- **`@ManyToOne` unidirecional** em `Transacao` (sem coleção em `Conta`) — evita N+1 e o risco de relação bidirecional mal configurada; histórico de transações por conta é resolvido via query própria no repository, não navegação de coleção.
- **`ddl-auto: update`** é uma simplificação deliberada para a fase de aprendizado — em produção, o padrão seria uma ferramenta de migração versionada (Flyway/Liquibase).

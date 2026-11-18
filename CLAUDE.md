# Ajudante Familiar

App para auxiliar nos cuidados de idosos. Projeto de estudo.

## Perfis de acesso

### Cuidador
- Cadastrar medicamentos e informações do idoso
- Registrar atividades a serem feitas com o idoso
- Marcar lembretes no calendário do app para futuras consultas
- Registrar a alimentação do idoso

### Familiar
- Somente visualização de tudo que o cuidador registrar: medicamentos, alimentação, futuras consultas e atividades diárias

## Arquitetura

Microsserviços (3 APIs) comunicando-se via RabbitMQ:

1. **API de funções**: funções HTTP do cuidador e do paciente
2. **API de autenticação**: validação de acessos e permissões via JWT
3. **API de RAG**: RAG básico para um chat que busca informações do paciente e gera relatórios conforme o cuidador pedir

Migrations de cada API via Flyway.

## Convenções

- Código, comentários, nomes de classes, métodos, variáveis e tabelas sempre em **português**.
- Exceção: os nomes dos packages listados abaixo permanecem em inglês.

## Organização das APIs (mesmos packages em todas)

| Package | Responsabilidade |
|---|---|
| `models` | Entidades e relacionamentos de tabelas |
| `repositories` | Acesso ao banco de dados, consultas e alterações |
| `services` | Regras de negócio |
| `dto` | Objetos que definem o que entra e o que sai da API |
| `controller` | Recebe as requisições HTTP |

## Regras de camadas

- `controller` conversa apenas com `services` e usa `dto` na entrada e saída.
- `services` concentra as regras de negócio e acessa o banco somente via `repositories`.
- Entidades (`models`) nunca são expostas diretamente nas respostas da API.

## Banco e migrations

- Nunca edite uma migration já mergeada: crie uma nova.
- O schema é só do Flyway.

## Padrão de ambiente (vale para todas as APIs)

### Docker Compose

- Toda API tem um `compose.yaml` **idêntico** ao da API de funções, com `name: api-funcoes`. O mesmo nome de projeto faz todas usarem os mesmos containers (um Oracle e um RabbitMQ) em vez de subir cópias que disputariam as portas.
- Ao alterar o `compose.yaml` de uma API, replique a mudança nas outras.
- Dependência `spring-boot-docker-compose` (`runtime`, `optional`): o `./mvnw spring-boot:run` sobe o compose e configura banco e RabbitMQ a partir dele. **Não** declare `spring.datasource` nem `spring.rabbitmq` no `application.yaml`.
- O Spring só para os containers que ele mesmo subiu. Se outra API já estava rodando, os containers são reaproveitados.
- Ordem de subida: **API de funções primeiro**. As migrations dela criam as tabelas base (cargo, perfil, cuidador, idoso, parente...) das quais as outras dependem.

### Portas

| Serviço | Porta |
|---|---|
| Oracle Free (serviço `FREEPDB1`) | 1521 |
| RabbitMQ (AMQP) | 5672 |
| API de funções | 8080 |
| API de autenticação | 8081 |
| API de RAG | 8082 |

A porta da API fica fixa em `server.port` no `application.yaml`. As portas do Oracle e do RabbitMQ vêm só do `compose.yaml`.

### Credenciais de desenvolvimento

| Serviço | Usuário / senha |
|---|---|
| Oracle (schema da aplicação `AJUDANTE`) | `ajudante` / `secret` |
| Oracle (admin) | `system` / `secret` |
| RabbitMQ | `myuser` / `secret` |

### Variáveis de ambiente

Em desenvolvimento nenhuma é obrigatória. Fora do Docker Compose (servidor, `java -jar`), defina:

| Variável | Descrição |
|---|---|
| `JWT_SEGREDO` | Segredo HS256 dos tokens (mínimo 32 caracteres, **igual em todas as APIs**) |
| `SPRING_DATASOURCE_URL` | Ex.: `jdbc:oracle:thin:@//host:1521/FREEPDB1` |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | Usuário e senha do Oracle |
| `SPRING_RABBITMQ_HOST` / `SPRING_RABBITMQ_PORT` | Endereço e porta do RabbitMQ |
| `SPRING_RABBITMQ_USERNAME` / `SPRING_RABBITMQ_PASSWORD` | Usuário e senha do RabbitMQ |
| `SERVER_PORT` | Opcional: troca a porta da API |

Regras para novas variáveis:

- Para o que o Spring já cobre, use os nomes padrão dele (`SPRING_...`, `SERVER_...`). Não invente nomes próprios.
- Configuração própria da aplicação: chave em português no YAML e variável em MAIÚSCULAS com `_`, com valor padrão de dev, ex.: `jwt.segredo: "${JWT_SEGREDO:valor-de-dev}"`.
- O valor padrão serve só para desenvolvimento. Em produção, a variável deve ser sempre definida.

### Flyway

Todas as APIs usam o mesmo schema `AJUDANTE`. Cada uma tem prefixo e tabela de histórico próprios:

| API | Prefixo | Tabela de histórico |
|---|---|---|
| Funções | `M_AJ_V` | `flyway_schema_history` |
| Autenticação | `M_AUT_V` | `flyway_historico_autenticacao` |
| RAG | `M_RAG_V` | `flyway_historico_rag` |

APIs que não são a de funções usam `baseline-on-migrate: true` e `baseline-version: 0`, porque o schema já existe quando elas sobem.

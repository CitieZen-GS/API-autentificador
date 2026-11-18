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

## Organização da API de funções

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

-- =====================================================================
-- Ajudante Familiar - API de autenticação
-- script-bd.sql: objetos de banco (Oracle) desta API.
-- Depende das tabelas da API de funções (rode API-Funcoes/scripts/script-bd.sql antes).
-- Gerado a partir das migrations do Flyway em src/main/resources/db/migration,
-- na ordem em que são aplicadas. Na aplicação, o Flyway executa esses mesmos
-- arquivos sozinho; este script serve para criar o schema manualmente.
-- =====================================================================

-- ---------------------------------------------------------------------
-- M_AUT_V01__criar_tabela_token_atualizacao.sql
-- ---------------------------------------------------------------------
-- Depende das tabelas T_Ajudante_familiar_Cuidador e T_Ajudante_familiar_Parente,
-- criadas pelas migrations da API de funções (M_AJ_V03 e M_AJ_V05).
CREATE TABLE T_Ajudante_familiar_Token_Atualizacao (
    id_token INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ds_token_hash VARCHAR(64) NOT NULL,
    id_cuidador INTEGER,
    id_parente INTEGER,
    dt_criacao TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    dt_expiracao TIMESTAMP NOT NULL,
    dt_revogacao TIMESTAMP,
    CONSTRAINT uk_token_hash UNIQUE (ds_token_hash),
    CONSTRAINT fk_token_cuidador FOREIGN KEY (id_cuidador)
        REFERENCES T_Ajudante_familiar_Cuidador (id_cuidador),
    CONSTRAINT fk_token_parente FOREIGN KEY (id_parente)
        REFERENCES T_Ajudante_familiar_Parente (id_parente),
    -- O token pertence a exatamente um dono: cuidador ou parente
    CONSTRAINT ck_token_dono CHECK (
        (id_cuidador IS NOT NULL AND id_parente IS NULL)
        OR (id_cuidador IS NULL AND id_parente IS NOT NULL)
    )
);

-- O Oracle não cria índice automático para FK
CREATE INDEX ix_token_cuidador ON T_Ajudante_familiar_Token_Atualizacao (id_cuidador);
CREATE INDEX ix_token_parente ON T_Ajudante_familiar_Token_Atualizacao (id_parente);

-- ---------------------------------------------------------------------
-- M_AUT_V02__inserir_cargos_e_perfis_padrao.sql
-- ---------------------------------------------------------------------
-- Cargos e perfis usados no cadastro: Cuidador (escrita) e Familiar (somente leitura).
-- Os inserts só acontecem se o registro ainda não existir (ex.: criado à mão pelo README da API de funções).

INSERT INTO T_Ajudante_familiar_Cargo (nm_cargo, fg_permissao)
SELECT 'Cuidador', 'S' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM T_Ajudante_familiar_Cargo WHERE nm_cargo = 'Cuidador');

INSERT INTO T_Ajudante_familiar_Cargo (nm_cargo, fg_permissao)
SELECT 'Familiar', 'N' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM T_Ajudante_familiar_Cargo WHERE nm_cargo = 'Familiar');

INSERT INTO T_Ajudante_familiar_Perfil (nm_perfil, id_cargo)
SELECT 'Cuidador', MIN(c.id_cargo) FROM T_Ajudante_familiar_Cargo c
WHERE c.nm_cargo = 'Cuidador'
  AND NOT EXISTS (SELECT 1 FROM T_Ajudante_familiar_Perfil WHERE nm_perfil = 'Cuidador')
HAVING MIN(c.id_cargo) IS NOT NULL;

INSERT INTO T_Ajudante_familiar_Perfil (nm_perfil, id_cargo)
SELECT 'Familiar', MIN(c.id_cargo) FROM T_Ajudante_familiar_Cargo c
WHERE c.nm_cargo = 'Familiar'
  AND NOT EXISTS (SELECT 1 FROM T_Ajudante_familiar_Perfil WHERE nm_perfil = 'Familiar')
HAVING MIN(c.id_cargo) IS NOT NULL;

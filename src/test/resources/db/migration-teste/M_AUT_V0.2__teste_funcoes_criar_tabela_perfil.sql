-- SÓ PARA TESTES: cópia de M_AJ_V02__criar_tabela_perfil.sql da API de funções.
-- No banco real essa tabela é criada pela API de funções. Aqui ela existe para o Oracle
-- vazio do Testcontainers ter as tabelas de que as migrations M_AUT_V01 e V02 dependem.
-- Se a migration original mudar (numa nova M_AJ_V), atualize esta cópia.
CREATE TABLE T_Ajudante_familiar_Perfil (
    id_perfil INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nm_perfil VARCHAR(20) NOT NULL,
    id_cargo INTEGER NOT NULL,
    CONSTRAINT fk_perfil_cargo FOREIGN KEY (id_cargo)
        REFERENCES T_Ajudante_familiar_Cargo (id_cargo)
);

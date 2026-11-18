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

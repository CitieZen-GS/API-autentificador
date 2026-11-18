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

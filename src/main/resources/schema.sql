-- Criação do esquema SCHEMA_USER (se necessário)
BEGIN
EXECUTE IMMEDIATE 'CREATE SCHEMA AUTHORIZATION SCHEMA_USER';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -1918 THEN -- Ignora erro se o esquema já existe
      RAISE;
END IF;
END;
/

-- Criação do esquema SCHEMA_CAR (se necessário)
BEGIN
EXECUTE IMMEDIATE 'CREATE SCHEMA AUTHORIZATION SCHEMA_CAR';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -1918 THEN -- Ignora erro se o esquema já existe
      RAISE;
END IF;
END;
/

-- Criação da tabela USUARIO no esquema SCHEMA_USER
DECLARE
v_count NUMBER;
BEGIN
  -- Verifica se a tabela USUARIO existe no SCHEMA_USER
SELECT COUNT(*) INTO v_count
FROM all_tables
WHERE table_name = 'USUARIO' AND owner = 'SCHEMA_USER';

-- Cria a tabela apenas se ela não existir
IF v_count = 0 THEN
    EXECUTE IMMEDIATE '
      CREATE TABLE SCHEMA_USER.USUARIO (
        id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        nome VARCHAR2(100) NOT NULL,
        email VARCHAR2(100) NOT NULL
      )
    ';
END IF;
END;
/

-- Criação da tabela CARRO no esquema SCHEMA_CAR
DECLARE
v_count NUMBER;
BEGIN
  -- Verifica se a tabela CARRO existe no SCHEMA_CAR
SELECT COUNT(*) INTO v_count
FROM all_tables
WHERE table_name = 'CARRO' AND owner = 'SCHEMA_CAR';

-- Cria a tabela apenas se ela não existir
IF v_count = 0 THEN
    EXECUTE IMMEDIATE '
      CREATE TABLE SCHEMA_CAR.CARRO (
        id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        modelo VARCHAR2(100) NOT NULL,
        ano NUMBER NOT NULL
      )
    ';
END IF;
END;
/

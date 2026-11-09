-- MySQL: executar antes de iniciar a aplicacao com lotProduct como String.
-- Aplicar em cada banco configurado em app.datasource.main e
-- app.datasource.productexpired que contenha a tabela product_entity.
-- A configuracao JPA usa validate; este script nao e executado automaticamente.
-- Os lotes numericos existentes sao convertidos para texto.
ALTER TABLE product_entity
    MODIFY COLUMN lot_product VARCHAR(255) NOT NULL;

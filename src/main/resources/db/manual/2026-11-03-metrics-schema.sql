-- Execute na mesma instancia MySQL usada pela aplicacao (127.0.0.1:3308).
-- Requer as tabelas do banco principal ja criadas e atualizadas.
-- Cria somente estruturas ausentes; nao copia registros nem altera tabelas existentes.
-- CREATE TABLE LIKE nao copia foreign keys; os mapeamentos atuais usam IDs simples.
CREATE DATABASE IF NOT EXISTS systemMetrics;

CREATE TABLE IF NOT EXISTS systemMetrics.distributor_entity
    LIKE systempetshop.distributor_entity;
CREATE TABLE IF NOT EXISTS systemMetrics.brand_entity
    LIKE systempetshop.brand_entity;
CREATE TABLE IF NOT EXISTS systemMetrics.product_entity
    LIKE systempetshop.product_entity;

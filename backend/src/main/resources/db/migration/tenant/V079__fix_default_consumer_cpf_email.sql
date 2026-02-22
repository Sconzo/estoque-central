-- V079__fix_default_consumer_cpf_email.sql
-- Fix default consumer rows that were inserted with plain-text CPF/email
-- by V037 or V078. These values cannot be decrypted by CryptoService,
-- so they must be set to NULL.

UPDATE customers
SET cpf   = NULL,
    email = NULL
WHERE is_default_consumer = true
  AND (cpf IS NOT NULL OR email IS NOT NULL);

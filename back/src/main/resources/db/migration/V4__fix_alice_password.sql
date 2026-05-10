-- Establece contraseña válida para alice@example.com
-- Contraseña en texto plano: admin123
UPDATE users
SET password_hash = '$2b$10$d/IHm23e0eoFaauiM.00UurKkFKMzlf997hVnT0ELfodH7lCQGgfm'
WHERE email = 'alice@example.com';

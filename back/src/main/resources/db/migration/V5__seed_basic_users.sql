-- Seed 10 basic users with CUSTOMER role (no admin, no business owner)
-- Plain-text password for all: user123
-- BCrypt hash (cost 10): $2a$10$zUQVSuaMBb9BibaM58bGCeHOFQ.qhK6HV0tdCw3ccAElQn25318ru
SET NAMES utf8mb4; SET time_zone = '+00:00';

INSERT INTO users (id, email, password_hash, full_name, phone, is_business_owner, created_at, updated_at) VALUES
  ( 6, 'user01@example.com', '$2a$10$zUQVSuaMBb9BibaM58bGCeHOFQ.qhK6HV0tdCw3ccAElQn25318ru', 'Laura García',     '+34611000001', 0, '2025-02-01 09:00:00', '2025-02-01 09:00:00'),
  ( 7, 'user02@example.com', '$2a$10$zUQVSuaMBb9BibaM58bGCeHOFQ.qhK6HV0tdCw3ccAElQn25318ru', 'Marcos López',     '+34611000002', 0, '2025-02-01 09:05:00', '2025-02-01 09:05:00'),
  ( 8, 'user03@example.com', '$2a$10$zUQVSuaMBb9BibaM58bGCeHOFQ.qhK6HV0tdCw3ccAElQn25318ru', 'Sofía Martínez',   '+34611000003', 0, '2025-02-01 09:10:00', '2025-02-01 09:10:00'),
  ( 9, 'user04@example.com', '$2a$10$zUQVSuaMBb9BibaM58bGCeHOFQ.qhK6HV0tdCw3ccAElQn25318ru', 'Pablo Sánchez',    '+34611000004', 0, '2025-02-01 09:15:00', '2025-02-01 09:15:00'),
  (10, 'user05@example.com', '$2a$10$zUQVSuaMBb9BibaM58bGCeHOFQ.qhK6HV0tdCw3ccAElQn25318ru', 'Elena Fernández',  '+34611000005', 0, '2025-02-01 09:20:00', '2025-02-01 09:20:00'),
  (11, 'user06@example.com', '$2a$10$zUQVSuaMBb9BibaM58bGCeHOFQ.qhK6HV0tdCw3ccAElQn25318ru', 'Javier Ruiz',      '+34611000006', 0, '2025-02-01 09:25:00', '2025-02-01 09:25:00'),
  (12, 'user07@example.com', '$2a$10$zUQVSuaMBb9BibaM58bGCeHOFQ.qhK6HV0tdCw3ccAElQn25318ru', 'Lucía Moreno',     '+34611000007', 0, '2025-02-01 09:30:00', '2025-02-01 09:30:00'),
  (13, 'user08@example.com', '$2a$10$zUQVSuaMBb9BibaM58bGCeHOFQ.qhK6HV0tdCw3ccAElQn25318ru', 'Adrián Jiménez',   '+34611000008', 0, '2025-02-01 09:35:00', '2025-02-01 09:35:00'),
  (14, 'user09@example.com', '$2a$10$zUQVSuaMBb9BibaM58bGCeHOFQ.qhK6HV0tdCw3ccAElQn25318ru', 'Carmen Torres',    '+34611000009', 0, '2025-02-01 09:40:00', '2025-02-01 09:40:00'),
  (15, 'user10@example.com', '$2a$10$zUQVSuaMBb9BibaM58bGCeHOFQ.qhK6HV0tdCw3ccAElQn25318ru', 'Daniel Ramírez',   '+34611000010', 0, '2025-02-01 09:45:00', '2025-02-01 09:45:00');

-- Assign CUSTOMER role (id=3) to all 10 users
INSERT INTO user_roles (user_id, role_id) VALUES
  ( 6, 3),
  ( 7, 3),
  ( 8, 3),
  ( 9, 3),
  (10, 3),
  (11, 3),
  (12, 3),
  (13, 3),
  (14, 3),
  (15, 3);

-- Default address for each user
INSERT INTO user_addresses (id, user_id, label, line1, line2, city, state, postal_code, country_code, location, is_default, created_at, updated_at) VALUES
  ( 6,  6, 'Casa', 'Calle Mayor 6',    NULL, 'Madrid',    NULL, '28006', 'ES', NULL, 1, '2025-02-01 10:00:00', '2025-02-01 10:00:00'),
  ( 7,  7, 'Casa', 'Calle Mayor 7',    NULL, 'Barcelona', NULL, '08007', 'ES', NULL, 1, '2025-02-01 10:05:00', '2025-02-01 10:05:00'),
  ( 8,  8, 'Casa', 'Avenida Sol 8',    NULL, 'Sevilla',   NULL, '41008', 'ES', NULL, 1, '2025-02-01 10:10:00', '2025-02-01 10:10:00'),
  ( 9,  9, 'Casa', 'Calle Luna 9',     NULL, 'Valencia',  NULL, '46009', 'ES', NULL, 1, '2025-02-01 10:15:00', '2025-02-01 10:15:00'),
  (10, 10, 'Casa', 'Paseo Norte 10',   NULL, 'Zaragoza',  NULL, '50010', 'ES', NULL, 1, '2025-02-01 10:20:00', '2025-02-01 10:20:00'),
  (11, 11, 'Casa', 'Calle Pinos 11',   NULL, 'Málaga',    NULL, '29011', 'ES', NULL, 1, '2025-02-01 10:25:00', '2025-02-01 10:25:00'),
  (12, 12, 'Casa', 'Avenida Mar 12',   NULL, 'Murcia',    NULL, '30012', 'ES', NULL, 1, '2025-02-01 10:30:00', '2025-02-01 10:30:00'),
  (13, 13, 'Casa', 'Calle Rosas 13',   NULL, 'Palma',     NULL, '07013', 'ES', NULL, 1, '2025-02-01 10:35:00', '2025-02-01 10:35:00'),
  (14, 14, 'Casa', 'Gran Vía 14',      NULL, 'Bilbao',    NULL, '48014', 'ES', NULL, 1, '2025-02-01 10:40:00', '2025-02-01 10:40:00'),
  (15, 15, 'Casa', 'Paseo Alameda 15', NULL, 'Alicante',  NULL, '03015', 'ES', NULL, 1, '2025-02-01 10:45:00', '2025-02-01 10:45:00');

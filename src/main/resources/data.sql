-- Inicialización de Roles
INSERT INTO roles (name) 
SELECT 'ROLE_USER' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_USER');

INSERT INTO roles (name) 
SELECT 'ROLE_MODERATOR' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_MODERATOR');

INSERT INTO roles (name) 
SELECT 'ROLE_ADMIN' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_ADMIN');

-- Inicialización de Categorías
INSERT INTO categories (name) 
SELECT 'Desayunos' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Desayunos');

INSERT INTO categories (name) 
SELECT 'Almuerzos' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Almuerzos');

INSERT INTO categories (name) 
SELECT 'Cenas' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Cenas');

INSERT INTO categories (name) 
SELECT 'Postres' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Postres');

INSERT INTO categories (name) 
SELECT 'Ensaladas' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Ensaladas');

INSERT INTO categories (name) 
SELECT 'Vegetariano' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Vegetariano');

-- Inicialización de Ingredientes
INSERT INTO ingredients (name, unit_measure, active) 
SELECT 'Sal', 'GRAMOS', true WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE name = 'Sal');

INSERT INTO ingredients (name, unit_measure, active) 
SELECT 'Pimienta', 'GRAMOS', true WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE name = 'Pimienta');

INSERT INTO ingredients (name, unit_measure, active) 
SELECT 'Aceite de oliva', 'MILILITROS', true WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE name = 'Aceite de oliva');

INSERT INTO ingredients (name, unit_measure, active) 
SELECT 'Ajo', 'UNIDADES', true WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE name = 'Ajo');

INSERT INTO ingredients (name, unit_measure, active) 
SELECT 'Cebolla', 'UNIDADES', true WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE name = 'Cebolla');

INSERT INTO ingredients (name, unit_measure, active) 
SELECT 'Tomate', 'UNIDADES', true WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE name = 'Tomate');

INSERT INTO ingredients (name, unit_measure, active) 
SELECT 'Huevos', 'UNIDADES', true WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE name = 'Huevos');

INSERT INTO ingredients (name, unit_measure, active) 
SELECT 'Leche', 'MILILITROS', true WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE name = 'Leche');

INSERT INTO ingredients (name, unit_measure, active) 
SELECT 'Harina', 'GRAMOS', true WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE name = 'Harina');

INSERT INTO ingredients (name, unit_measure, active) 
SELECT 'Azúcar', 'GRAMOS', true WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE name = 'Azúcar');

INSERT INTO ingredients (name, unit_measure, active) 
SELECT 'Mantequilla', 'GRAMOS', true WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE name = 'Mantequilla');

INSERT INTO ingredients (name, unit_measure, active) 
SELECT 'Arroz', 'GRAMOS', true WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE name = 'Arroz');

INSERT INTO ingredients (name, unit_measure, active) 
SELECT 'Pasta', 'GRAMOS', true WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE name = 'Pasta');

INSERT INTO ingredients (name, unit_measure, active) 
SELECT 'Pollo', 'GRAMOS', true WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE name = 'Pollo');

INSERT INTO ingredients (name, unit_measure, active) 
SELECT 'Carne de res', 'GRAMOS', true WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE name = 'Carne de res');

INSERT INTO ingredients (name, unit_measure, active) 
SELECT 'Pescado', 'GRAMOS', true WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE name = 'Pescado');

INSERT INTO ingredients (name, unit_measure, active) 
SELECT 'Queso', 'GRAMOS', true WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE name = 'Queso');

INSERT INTO ingredients (name, unit_measure, active) 
SELECT 'Zanahoria', 'UNIDADES', true WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE name = 'Zanahoria');

INSERT INTO ingredients (name, unit_measure, active) 
SELECT 'Papa', 'UNIDADES', true WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE name = 'Papa');

INSERT INTO ingredients (name, unit_measure, active) 
SELECT 'Limón', 'UNIDADES', true WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE name = 'Limón');

-- Inicialización de Usuarios
-- Usuario Admin (password: admin1234)
INSERT INTO users (username, email, password)
SELECT 'admin', 'admin@example.com', '$2y$10$WbgpQFQ1li/K3kR/NidTDupUbMhr4dA/KgOOFoqrYKYMBkSi.h0vq'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

-- Usuario Normal (password: user1234)
INSERT INTO users (username, email, password)
SELECT 'user', 'user@example.com', '$2y$10$WbgpQFQ1li/K3kR/NidTDupUbMhr4dA/KgOOFoqrYKYMBkSi.h0vq'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user');

-- Asignación de Roles
-- Admin tiene ROLE_ADMIN y ROLE_USER
INSERT INTO user_roles (user_id, role_id)
SELECT 
    (SELECT id FROM users WHERE username = 'admin'),
    (SELECT id FROM roles WHERE name = 'ROLE_ADMIN')
WHERE NOT EXISTS (
    SELECT 1 FROM user_roles 
    WHERE user_id = (SELECT id FROM users WHERE username = 'admin')
    AND role_id = (SELECT id FROM roles WHERE name = 'ROLE_ADMIN')
);

INSERT INTO user_roles (user_id, role_id)
SELECT 
    (SELECT id FROM users WHERE username = 'admin'),
    (SELECT id FROM roles WHERE name = 'ROLE_USER')
WHERE NOT EXISTS (
    SELECT 1 FROM user_roles 
    WHERE user_id = (SELECT id FROM users WHERE username = 'admin')
    AND role_id = (SELECT id FROM roles WHERE name = 'ROLE_USER')
);

-- Usuario normal tiene ROLE_USER
INSERT INTO user_roles (user_id, role_id)
SELECT 
    (SELECT id FROM users WHERE username = 'user'),
    (SELECT id FROM roles WHERE name = 'ROLE_USER')
WHERE NOT EXISTS (
    SELECT 1 FROM user_roles 
    WHERE user_id = (SELECT id FROM users WHERE username = 'user')
    AND role_id = (SELECT id FROM roles WHERE name = 'ROLE_USER')
);

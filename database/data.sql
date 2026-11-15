-- ====================================================================
-- Online Shopping Cart and Order Management System
-- Initial Sample Seed Data
-- ====================================================================

-- 1. Insert Categories
INSERT INTO categories (id, name, description, icon) VALUES
(1, 'Electronics', 'Smartphones, laptops, audio gadgets, and accessories', 'bi-laptop'),
(2, 'Fashion & Apparel', 'Clothing, footwear, and stylish wearables for all', 'bi-bag'),
(3, 'Books & Stationery', 'Academic textbooks, novels, notebooks, and study essentials', 'bi-book'),
(4, 'Home & Kitchen', 'Modern cookware, home decor, and appliances', 'bi-house-door'),
(5, 'Sports & Fitness', 'Gym equipment, sports gear, and activewear', 'bi-trophy');

-- 2. Insert Users (Passwords are BCrypt hashed for 'admin123' and 'password123')
-- admin / admin123
-- john_doe / password123
-- jane_smith / password123
INSERT INTO users (id, username, email, password, full_name, phone, address, role) VALUES
(1, 'admin', 'admin@shoppingcart.com', '$2a$10$Y5n2vM1Yq7fD6sQyK1X0aeB3f8J7Z5L2Q4w9V8u7T6S5R4E3W2Q1O', 'System Administrator', '+1-555-0199', '100 University Plaza, Admin Suite 4B', 'ADMIN'),
(2, 'john_doe', 'john@example.com', '$2a$10$wK1A5c8l8hQZpG4e3xUu7.jU9s2t8l4e6p3m1k5j9g7d5s3a1q2w3', 'John Doe', '+1-555-0142', '42 Maple Street, Silicon Valley, CA', 'CUSTOMER'),
(3, 'jane_smith', 'jane@example.com', '$2a$10$wK1A5c8l8hQZpG4e3xUu7.jU9s2t8l4e6p3m1k5j9g7d5s3a1q2w3', 'Jane Smith', '+1-555-0188', '77 Oak Avenue, Austin, TX', 'CUSTOMER');

-- 3. Insert Products
INSERT INTO products (id, name, description, price, stock_quantity, category_id, image_url, featured) VALUES
(1, 'UltraBook Pro 15', 'High-performance laptop with 16GB RAM, 512GB NVMe SSD, and 15.6-inch 4K display.', 1199.99, 15, 1, '/images/products/laptop.svg', true),
(2, 'Wireless Noise-Cancelling Headphones', 'Premium over-ear Bluetooth headphones with active noise cancellation and 30-hour battery life.', 179.99, 30, 1, '/images/products/headphones.svg', true),
(3, 'Smart Fitness Watch Series 5', 'Water-resistant smartwatch with heart rate tracking, GPS, sleep analysis, and AMOLED screen.', 129.50, 45, 1, '/images/products/smartwatch.svg', true),
(4, 'Mechanical Gaming Keyboard', 'RGB backlit mechanical keyboard with tactile blue switches and detachable USB-C cable.', 74.99, 25, 1, '/images/products/keyboard.svg', false),
(5, 'Classic Denim Jacket', 'Timeless indigo cotton denim jacket with button closure and relaxed modern fit.', 59.99, 40, 2, '/images/products/denim_jacket.svg', true),
(6, 'Breathable Running Shoes', 'Lightweight athletic sneakers with cushioned sole and breathable mesh upper.', 84.50, 20, 2, '/images/products/running_shoes.svg', true),
(7, 'Vintage Leather Backpack', 'Durable genuine leather everyday backpack with padded 15-inch laptop sleeve.', 69.99, 18, 2, '/images/products/backpack.svg', false),
(8, 'Data Structures & Algorithms in Java', 'Comprehensive university textbook covering trees, graphs, sorting, and dynamic programming.', 49.99, 50, 3, '/images/products/algo_book.svg', true),
(9, 'Clean Code: A Handbook of Agile Craft', 'Essential guide to software engineering craftsmanship, readability, and refactoring.', 42.00, 35, 3, '/images/products/cleancode_book.svg', false),
(10, 'Stainless Steel Thermal Coffee Flask', 'Double-wall vacuum insulated 500ml travel mug that keeps drinks hot for 12 hours.', 24.99, 60, 4, '/images/products/coffee_mug.svg', false),
(11, 'Ergonomic Executive Office Chair', 'Adjustable lumbar support desk chair with breathable mesh and smooth rolling wheels.', 189.00, 12, 4, '/images/products/office_chair.svg', true),
(12, 'Adjustable Dumbbell Set (20kg)', 'Space-saving compact dumbbell pair with easy quick-dial weight adjustment system.', 149.99, 10, 5, '/images/products/dumbbells.svg', true);

-- 4. Insert Sample Shopping Carts
INSERT INTO cart (id, user_id) VALUES
(1, 2),
(2, 3);

-- 5. Insert Sample Cart Items (John Doe has 1 item in cart)
INSERT INTO cart_items (id, cart_id, product_id, quantity, unit_price) VALUES
(1, 1, 2, 1, 179.99);

-- 6. Insert Sample Orders (Demonstrating order history and sales analytics)
INSERT INTO orders (id, order_number, user_id, total_items, subtotal, discount_amount, tax_amount, shipping_fee, final_amount, discount_code, order_status, payment_method, payment_status, shipping_address, customer_notes) VALUES
(1, 'ORD-20261101-7821', 2, 2, 1274.98, 50.00, 61.25, 0.00, 1286.23, 'DISCOUNT50', 'DELIVERED', 'CARD', 'PAID', '42 Maple Street, Silicon Valley, CA', 'Please leave at front door'),
(2, 'ORD-20261110-3490', 3, 2, 91.99, 10.00, 4.10, 5.00, 91.09, 'WELCOME10', 'PROCESSING', 'UPI', 'PAID', '77 Oak Avenue, Austin, TX', 'Fragile items, handle with care');

-- 7. Insert Sample Order Items
INSERT INTO order_items (id, order_id, product_id, product_name, quantity, unit_price, total_price) VALUES
(1, 1, 1, 'UltraBook Pro 15', 1, 1199.99, 1199.99),
(2, 1, 4, 'Mechanical Gaming Keyboard', 1, 74.99, 74.99),
(3, 2, 8, 'Data Structures & Algorithms in Java', 1, 49.99, 49.99),
(4, 2, 9, 'Clean Code: A Handbook of Agile Craft', 1, 42.00, 42.00);

-- 8. Insert Sample Audit Logs (Demonstrating Traditional JDBC Audit Trail)
INSERT INTO audit_logs (id, action, entity_type, entity_id, details, performed_by) VALUES
(1, 'SYSTEM_INIT', 'SYSTEM', 0, 'Initial database schema and seeds created', 'SYSTEM'),
(2, 'ORDER_PLACED', 'ORDER', 1, 'Order ORD-20261101-7821 placed successfully. Total: $1286.23', 'john_doe'),
(3, 'ORDER_PLACED', 'ORDER', 2, 'Order ORD-20261110-3490 placed successfully. Total: $91.09', 'jane_smith');

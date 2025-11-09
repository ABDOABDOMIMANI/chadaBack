-- ============================================
-- Chada Perfumes - Sample Data for Railway
-- ============================================
-- This SQL file creates sample categories and products
-- Run this in Railway MySQL database to populate initial data
-- ============================================

-- Clear existing data (optional - uncomment if you want to reset)
-- DELETE FROM reviews;
-- DELETE FROM order_items;
-- DELETE FROM orders;
-- DELETE FROM products;
-- DELETE FROM categories;

-- ============================================
-- CATEGORIES
-- ============================================
INSERT INTO categories (name, description, image_url, created_at, updated_at) VALUES
('عطور نسائية', 'مجموعة فاخرة من العطور النسائية الأصيلة', NULL, NOW(), NOW()),
('عطور رجالية', 'عطور رجالية مميزة بروائح قوية وأنيقة', NULL, NOW(), NOW()),
('بخور', 'بخور عالي الجودة من أفضل المصادر', NULL, NOW(), NOW()),
('عطور عائلية', 'عطور مناسبة للرجال والنساء', NULL, NOW(), NOW()),
('عطور فاخرة', 'مجموعة خاصة من العطور الفاخرة المميزة', NULL, NOW(), NOW());

-- ============================================
-- PRODUCTS
-- ============================================

-- عطور نسائية
INSERT INTO products (name, description, price, stock, category_id, image_url, image_urls, image_details, fragrance, volume, active, discount_percentage, original_price, promotion_start_date, promotion_end_date, created_at, updated_at) VALUES
(
    'عطر الورد الدمشقي',
    'عطر نسائي فاخر برائحة الورد الدمشقي الأصيل، يجمع بين الأناقة والجمال',
    450.00,
    25,
    1,
    '/api/images/rose-perfume.jpg',
    '["/api/images/rose-perfume-1.jpg", "/api/images/rose-perfume-2.jpg", "/api/images/rose-perfume-3.jpg", "/api/images/rose-perfume-4.jpg"]',
    '[{"url":"/api/images/rose-perfume-1.jpg","price":450.00,"description":"زجاجة 50 مل","quantity":15},{"url":"/api/images/rose-perfume-2.jpg","price":750.00,"description":"زجاجة 100 مل","quantity":10}]',
    'ورد دمشقي',
    50,
    TRUE,
    20,
    562.50,
    '2025-01-15',
    '2025-02-15',
    NOW(),
    NOW()
),
(
    'عطر الياسمين الملكي',
    'عطر نسائي مميز برائحة الياسمين الملكي الفاخرة، مثالي للنساء الأنيقات',
    380.00,
    30,
    1,
    '/api/images/jasmine-perfume.jpg',
    '["/api/images/jasmine-perfume-1.jpg", "/api/images/jasmine-perfume-2.jpg"]',
    '[{"url":"/api/images/jasmine-perfume-1.jpg","price":380.00,"description":"زجاجة 50 مل","quantity":20},{"url":"/api/images/jasmine-perfume-2.jpg","price":650.00,"description":"زجاجة 100 مل","quantity":10}]',
    'ياسمين',
    50,
    TRUE,
    NULL,
    NULL,
    NULL,
    NULL,
    NOW(),
    NOW()
),
(
    'عطر الفانيليا الساحرة',
    'عطر نسائي حلو برائحة الفانيليا الدافئة والجذابة',
    320.00,
    35,
    1,
    '/api/images/vanilla-perfume.jpg',
    '["/api/images/vanilla-perfume-1.jpg", "/api/images/vanilla-perfume-2.jpg", "/api/images/vanilla-perfume-3.jpg"]',
    '[{"url":"/api/images/vanilla-perfume-1.jpg","price":320.00,"description":"زجاجة 50 مل","quantity":25},{"url":"/api/images/vanilla-perfume-2.jpg","price":550.00,"description":"زجاجة 100 مل","quantity":10}]',
    'فانيليا',
    50,
    TRUE,
    15,
    376.47,
    '2025-01-20',
    '2025-02-20',
    NOW(),
    NOW()
);

-- عطور رجالية
INSERT INTO products (name, description, price, stock, category_id, image_url, image_urls, image_details, fragrance, volume, active, discount_percentage, original_price, promotion_start_date, promotion_end_date, created_at, updated_at) VALUES
(
    'عطر العود الأصيل',
    'عطر رجالي قوي برائحة العود الأصيل الفاخر، للرجال الأقوياء',
    550.00,
    20,
    2,
    '/api/images/oud-perfume.jpg',
    '["/api/images/oud-perfume-1.jpg", "/api/images/oud-perfume-2.jpg", "/api/images/oud-perfume-3.jpg", "/api/images/oud-perfume-4.jpg"]',
    '[{"url":"/api/images/oud-perfume-1.jpg","price":550.00,"description":"زجاجة 50 مل","quantity":12},{"url":"/api/images/oud-perfume-2.jpg","price":950.00,"description":"زجاجة 100 مل","quantity":8}]',
    'عود',
    50,
    TRUE,
    25,
    733.33,
    '2025-01-10',
    '2025-02-10',
    NOW(),
    NOW()
),
(
    'عطر المسك الأسود',
    'عطر رجالي مميز برائحة المسك الأسود القوية والأنيقة',
    480.00,
    28,
    2,
    '/api/images/musk-perfume.jpg',
    '["/api/images/musk-perfume-1.jpg", "/api/images/musk-perfume-2.jpg"]',
    '[{"url":"/api/images/musk-perfume-1.jpg","price":480.00,"description":"زجاجة 50 مل","quantity":18},{"url":"/api/images/musk-perfume-2.jpg","price":820.00,"description":"زجاجة 100 مل","quantity":10}]',
    'مسك',
    50,
    TRUE,
    NULL,
    NULL,
    NULL,
    NULL,
    NOW(),
    NOW()
),
(
    'عطر الصندل الفاخر',
    'عطر رجالي أنيق برائحة الصندل الطبيعية الفاخرة',
    420.00,
    32,
    2,
    '/api/images/sandalwood-perfume.jpg',
    '["/api/images/sandalwood-perfume-1.jpg", "/api/images/sandalwood-perfume-2.jpg", "/api/images/sandalwood-perfume-3.jpg"]',
    '[{"url":"/api/images/sandalwood-perfume-1.jpg","price":420.00,"description":"زجاجة 50 مل","quantity":22},{"url":"/api/images/sandalwood-perfume-2.jpg","price":720.00,"description":"زجاجة 100 مل","quantity":10}]',
    'صندل',
    50,
    TRUE,
    10,
    466.67,
    '2025-01-25',
    '2025-02-25',
    NOW(),
    NOW()
);

-- بخور
INSERT INTO products (name, description, price, stock, category_id, image_url, image_urls, image_details, fragrance, volume, active, discount_percentage, original_price, promotion_start_date, promotion_end_date, created_at, updated_at) VALUES
(
    'بخور العود الأصلي',
    'بخور عود أصلي عالي الجودة من أفضل المصادر',
    280.00,
    40,
    3,
    '/api/images/oud-incense.jpg',
    '["/api/images/oud-incense-1.jpg", "/api/images/oud-incense-2.jpg"]',
    '[{"url":"/api/images/oud-incense-1.jpg","price":280.00,"description":"علبة 50 غرام","quantity":30},{"url":"/api/images/oud-incense-2.jpg","price":500.00,"description":"علبة 100 غرام","quantity":10}]',
    'عود',
    50,
    TRUE,
    20,
    350.00,
    '2025-01-15',
    '2025-02-15',
    NOW(),
    NOW()
),
(
    'بخور المسك الفاخر',
    'بخور مسك فاخر برائحة قوية ومميزة',
    220.00,
    45,
    3,
    '/api/images/musk-incense.jpg',
    '["/api/images/musk-incense-1.jpg"]',
    '[{"url":"/api/images/musk-incense-1.jpg","price":220.00,"description":"علبة 50 غرام","quantity":45}]',
    'مسك',
    50,
    TRUE,
    NULL,
    NULL,
    NULL,
    NULL,
    NOW(),
    NOW()
);

-- عطور عائلية
INSERT INTO products (name, description, price, stock, category_id, image_url, image_urls, image_details, fragrance, volume, active, discount_percentage, original_price, promotion_start_date, promotion_end_date, created_at, updated_at) VALUES
(
    'عطر الزعفران الملكي',
    'عطر عائلي فاخر برائحة الزعفران الملكي المميزة',
    520.00,
    22,
    4,
    '/api/images/saffron-perfume.jpg',
    '["/api/images/saffron-perfume-1.jpg", "/api/images/saffron-perfume-2.jpg", "/api/images/saffron-perfume-3.jpg"]',
    '[{"url":"/api/images/saffron-perfume-1.jpg","price":520.00,"description":"زجاجة 50 مل","quantity":15},{"url":"/api/images/saffron-perfume-2.jpg","price":880.00,"description":"زجاجة 100 مل","quantity":7}]',
    'زعفران',
    50,
    TRUE,
    30,
    742.86,
    '2025-01-05',
    '2025-02-05',
    NOW(),
    NOW()
),
(
    'عطر الورد والياسمين',
    'عطر عائلي جميل يجمع بين رائحة الورد والياسمين',
    360.00,
    38,
    4,
    '/api/images/rose-jasmine-perfume.jpg',
    '["/api/images/rose-jasmine-perfume-1.jpg", "/api/images/rose-jasmine-perfume-2.jpg"]',
    '[{"url":"/api/images/rose-jasmine-perfume-1.jpg","price":360.00,"description":"زجاجة 50 مل","quantity":28},{"url":"/api/images/rose-jasmine-perfume-2.jpg","price":620.00,"description":"زجاجة 100 مل","quantity":10}]',
    'ورد وياسمين',
    50,
    TRUE,
    NULL,
    NULL,
    NULL,
    NULL,
    NOW(),
    NOW()
);

-- عطور فاخرة
INSERT INTO products (name, description, price, stock, category_id, image_url, image_urls, image_details, fragrance, volume, active, discount_percentage, original_price, promotion_start_date, promotion_end_date, created_at, updated_at) VALUES
(
    'عطر الذهب الأسود',
    'عطر فاخر حصري برائحة العود والمسك والزعفران',
    850.00,
    15,
    5,
    '/api/images/black-gold-perfume.jpg',
    '["/api/images/black-gold-perfume-1.jpg", "/api/images/black-gold-perfume-2.jpg", "/api/images/black-gold-perfume-3.jpg", "/api/images/black-gold-perfume-4.jpg"]',
    '[{"url":"/api/images/black-gold-perfume-1.jpg","price":850.00,"description":"زجاجة 50 مل","quantity":10},{"url":"/api/images/black-gold-perfume-2.jpg","price":1500.00,"description":"زجاجة 100 مل","quantity":5}]',
    'عود ومسك وزعفران',
    50,
    TRUE,
    35,
    1307.69,
    '2025-01-01',
    '2025-01-31',
    NOW(),
    NOW()
),
(
    'عطر الماس الأبيض',
    'عطر فاخر حصري برائحة الياسمين والورد والمسك',
    720.00,
    18,
    5,
    '/api/images/white-diamond-perfume.jpg',
    '["/api/images/white-diamond-perfume-1.jpg", "/api/images/white-diamond-perfume-2.jpg", "/api/images/white-diamond-perfume-3.jpg"]',
    '[{"url":"/api/images/white-diamond-perfume-1.jpg","price":720.00,"description":"زجاجة 50 مل","quantity":12},{"url":"/api/images/white-diamond-perfume-2.jpg","price":1250.00,"description":"زجاجة 100 مل","quantity":6}]',
    'ياسمين وورد ومسك',
    50,
    TRUE,
    25,
    960.00,
    '2025-01-12',
    '2025-02-12',
    NOW(),
    NOW()
);

-- ============================================
-- Verification Queries (optional)
-- ============================================
-- SELECT COUNT(*) as total_categories FROM categories;
-- SELECT COUNT(*) as total_products FROM products;
-- SELECT c.name as category, COUNT(p.id) as product_count 
-- FROM categories c 
-- LEFT JOIN products p ON c.id = p.category_id 
-- GROUP BY c.id, c.name;


-- =========================================================================================
-- DATABASE DEFINITION SCRIPT: ADVANCED AGRICULTURE CROP HUB
-- Target RDBMS: MySQL 5.7+ / MySQL 8.0+ / MariaDB 10.4+ (XAMPP Environment)
-- Architecture Level: Fully Normalized up to Third Normal Form (3NF)
-- Project: BSCS 3rd-Semester Database Management Systems (DBMS) Project
-- =========================================================================================

-- 1. DATABASE CREATION AND INITIALIZATION
-- -----------------------------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS `crop_hub_db` 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `crop_hub_db`;

-- Temporarily disable foreign key checks for safe idempotent recreation
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `order_items`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `marketplace_products`;
DROP TABLE IF EXISTS `crop_diseases`;
DROP TABLE IF EXISTS `fertilizers`;
DROP TABLE IF EXISTS `crop_production`;
DROP TABLE IF EXISTS `fields`;
DROP TABLE IF EXISTS `crops`;
DROP TABLE IF EXISTS `farmers`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `weather_records`;
SET FOREIGN_KEY_CHECKS = 1;


-- =========================================================================================
-- 2. TABLE DEFINITIONS (With 3NF Integrity Constraints & Foreign Keys)
-- =========================================================================================

-- -----------------------------------------------------------------------------------------
-- TABLE 1: `users`
-- Purpose: System authentication, role access control, and account profile management.
-- 3NF Justification: All non-key attributes are dependent solely on the primary key `id`.
-- -----------------------------------------------------------------------------------------
CREATE TABLE `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `full_name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(120) NOT NULL,
    `phone` VARCHAR(25) NOT NULL,
    `password_hash` VARCHAR(255) NOT NULL COMMENT 'Bcrypt cryptographic hash',
    `user_type` ENUM('Farmer', 'Crop Dealer', 'Agriculture Expert', 'Student') NOT NULL DEFAULT 'Farmer',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `uq_users_email` UNIQUE (`email`),
    CONSTRAINT `chk_users_phone` CHECK (CHAR_LENGTH(`phone`) >= 7)
) ENGINE=InnoDB COMMENT='System user accounts';


-- -----------------------------------------------------------------------------------------
-- TABLE 2: `farmers`
-- Purpose: Agricultural profile for growers, farm land dimensions, and agronomic experience.
-- 3NF Justification: Separated from `users` so that non-farmer accounts (students/dealers)
-- do not possess NULL landholding attributes, avoiding insertion and update anomalies.
-- -----------------------------------------------------------------------------------------
CREATE TABLE `farmers` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NULL COMMENT 'Associated user login account, if registered',
    `name` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(25) NOT NULL,
    `email` VARCHAR(120) NOT NULL,
    `address` VARCHAR(255) NOT NULL,
    `farm_size_acres` DECIMAL(8, 2) NOT NULL DEFAULT 0.00,
    `experience_years` INT NOT NULL DEFAULT 1,
    `registered_date` DATE NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_farmers_user_id` (`user_id`),
    INDEX `idx_farmers_name` (`name`),
    CONSTRAINT `fk_farmers_user` 
        FOREIGN KEY (`user_id`) 
        REFERENCES `users` (`id`) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE,
    CONSTRAINT `chk_farmer_acres` CHECK (`farm_size_acres` >= 0)
) ENGINE=InnoDB COMMENT='Registered farmer profiles and landholdings';


-- -----------------------------------------------------------------------------------------
-- TABLE 3: `crops`
-- Purpose: Comprehensive crop taxonomy, agronomic attributes, seasons, and soil requirements.
-- 3NF Justification: Independent entity where attributes depend directly on `id`.
-- -----------------------------------------------------------------------------------------
CREATE TABLE `crops` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `crop_name` VARCHAR(100) NOT NULL,
    `category` VARCHAR(50) NOT NULL COMMENT 'Cereals, Legumes, Cash Crops, Vegetables, Fruits, Oilseeds',
    `scientific_name` VARCHAR(120) NOT NULL,
    `season` VARCHAR(50) NOT NULL COMMENT 'Kharif, Rabi, Zaid, Year-round',
    `soil_type` VARCHAR(100) NOT NULL,
    `water_requirement` VARCHAR(50) NOT NULL COMMENT 'Low, Medium, High',
    `expected_yield_per_acre` VARCHAR(50) NOT NULL COMMENT 'Estimated harvest yield range',
    `description` TEXT NOT NULL,
    `image_url` VARCHAR(255) NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_crops_category` (`category`),
    INDEX `idx_crops_name` (`crop_name`)
) ENGINE=InnoDB COMMENT='Botanical crop catalog and cultivation benchmarks';


-- -----------------------------------------------------------------------------------------
-- TABLE 4: `fields`
-- Purpose: Specific parcels of agricultural land owned/managed by farmers.
-- 3NF Justification: `farmer_id` and `current_crop_id` are normalized foreign keys.
-- -----------------------------------------------------------------------------------------
CREATE TABLE `fields` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `field_name` VARCHAR(100) NOT NULL,
    `farmer_id` INT NOT NULL,
    `location` VARCHAR(150) NOT NULL,
    `area_acres` DECIMAL(8, 2) NOT NULL,
    `soil_type` VARCHAR(100) NOT NULL,
    `irrigation_type` VARCHAR(80) NOT NULL COMMENT 'Canal, Tubewell, Drip, Sprinkler, Rainfed',
    `current_crop_id` INT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_fields_farmer` (`farmer_id`),
    INDEX `idx_fields_crop` (`current_crop_id`),
    CONSTRAINT `fk_fields_farmer` 
        FOREIGN KEY (`farmer_id`) 
        REFERENCES `farmers` (`id`) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT `fk_fields_crop` 
        FOREIGN KEY (`current_crop_id`) 
        REFERENCES `crops` (`id`) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE,
    CONSTRAINT `chk_field_area` CHECK (`area_acres` > 0)
) ENGINE=InnoDB COMMENT='Farmer land parcels and active cultivation state';


-- -----------------------------------------------------------------------------------------
-- TABLE 5: `crop_production`
-- Purpose: Records planting and harvest batches, quantities produced, and financials.
-- 3NF Justification: Links Farmer, Field, and Crop without storing redundant farmer/crop
-- data, preserving integrity when prices, names, or addresses change.
-- -----------------------------------------------------------------------------------------
CREATE TABLE `crop_production` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `farmer_id` INT NOT NULL,
    `field_id` INT NOT NULL,
    `crop_id` INT NOT NULL,
    `planting_date` DATE NOT NULL,
    `harvest_date` DATE NOT NULL,
    `quantity_produced_kg` DECIMAL(10, 2) NOT NULL,
    `production_cost` DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    `expected_revenue` DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    `notes` TEXT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_prod_farmer` (`farmer_id`),
    INDEX `idx_prod_crop` (`crop_id`),
    INDEX `idx_prod_field` (`field_id`),
    CONSTRAINT `fk_production_farmer` 
        FOREIGN KEY (`farmer_id`) 
        REFERENCES `farmers` (`id`) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT `fk_production_field` 
        FOREIGN KEY (`field_id`) 
        REFERENCES `fields` (`id`) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT `fk_production_crop` 
        FOREIGN KEY (`crop_id`) 
        REFERENCES `crops` (`id`) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT `chk_prod_quantity` CHECK (`quantity_produced_kg` >= 0),
    CONSTRAINT `chk_prod_cost` CHECK (`production_cost` >= 0)
) ENGINE=InnoDB COMMENT='Harvest logging, output yields, and profitability';


-- -----------------------------------------------------------------------------------------
-- TABLE 6: `fertilizers`
-- Purpose: Soil nutritional amendments, chemical classifications, and dosages.
-- 3NF Justification: Standalone entity with dosage and suitability guidelines.
-- -----------------------------------------------------------------------------------------
CREATE TABLE `fertilizers` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `type` VARCHAR(60) NOT NULL COMMENT 'Nitrogenous, Phosphatic, Potassic, Organic, Bio-Fertilizer',
    `suitable_crops` VARCHAR(255) NOT NULL,
    `recommended_quantity` VARCHAR(100) NOT NULL COMMENT 'Recommended kg or liters per acre',
    `price_per_unit` DECIMAL(10, 2) NOT NULL,
    `description` TEXT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_fertilizer_type` (`type`),
    CONSTRAINT `chk_fertilizer_price` CHECK (`price_per_unit` >= 0)
) ENGINE=InnoDB COMMENT='Nutrient inputs, formulations, and application rates';


-- -----------------------------------------------------------------------------------------
-- TABLE 7: `crop_diseases`
-- Purpose: Plant pathology educational guide (symptoms, prevention, and control).
-- 3NF Justification: Decoupled educational reference entity.
-- -----------------------------------------------------------------------------------------
CREATE TABLE `crop_diseases` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `disease_name` VARCHAR(120) NOT NULL,
    `affected_crops` VARCHAR(255) NOT NULL,
    `symptoms` TEXT NOT NULL,
    `prevention` TEXT NOT NULL,
    `treatment_method` TEXT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_disease_name` (`disease_name`)
) ENGINE=InnoDB COMMENT='Plant pathology diagnoses, symptoms, and control measures';


-- -----------------------------------------------------------------------------------------
-- TABLE 8: `marketplace_products`
-- Purpose: Catalog of agri-inputs (Seeds, Fertilizers, Machinery, Sprayers, Pesticides).
-- 3NF Justification: Product attributes are functionally dependent strictly on `id`.
-- -----------------------------------------------------------------------------------------
CREATE TABLE `marketplace_products` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(120) NOT NULL,
    `category` VARCHAR(60) NOT NULL COMMENT 'Seeds, Fertilizers, Farming Tools, Irrigation Equipment, Pesticides, Agricultural Machinery',
    `price` DECIMAL(10, 2) NOT NULL,
    `seller_name` VARCHAR(100) NOT NULL,
    `seller_contact` VARCHAR(50) NOT NULL,
    `description` TEXT NOT NULL,
    `image_url` VARCHAR(255) NULL,
    `stock_quantity` INT NOT NULL DEFAULT 10,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_market_cat` (`category`),
    INDEX `idx_market_price` (`price`),
    CONSTRAINT `chk_market_price` CHECK (`price` >= 0),
    CONSTRAINT `chk_market_stock` CHECK (`stock_quantity` >= 0)
) ENGINE=InnoDB COMMENT='Agricultural marketplace catalog and merchant inventory';


-- -----------------------------------------------------------------------------------------
-- TABLE 9: `orders`
-- Purpose: Header records for purchases placed on the marketplace.
-- 3NF Justification: Represents the 1 side of the 1:M relationship with `order_items`.
-- -----------------------------------------------------------------------------------------
CREATE TABLE `orders` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NULL COMMENT 'Registered customer account (optional for guest)',
    `customer_name` VARCHAR(100) NOT NULL,
    `customer_phone` VARCHAR(25) NOT NULL,
    `delivery_address` VARCHAR(255) NOT NULL,
    `total_amount` DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    `status` ENUM('Pending', 'Confirmed', 'Dispatched', 'Delivered', 'Cancelled') NOT NULL DEFAULT 'Confirmed',
    `order_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_orders_user` (`user_id`),
    INDEX `idx_orders_status` (`status`),
    CONSTRAINT `fk_orders_user` 
        FOREIGN KEY (`user_id`) 
        REFERENCES `users` (`id`) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE,
    CONSTRAINT `chk_order_total` CHECK (`total_amount` >= 0)
) ENGINE=InnoDB COMMENT='Customer sales order headers';


-- -----------------------------------------------------------------------------------------
-- TABLE 10: `order_items`
-- Purpose: Normalized line items for orders (resolves M:N between `orders` and `products`).
-- 3NF Justification: Eliminates repeating groups (1NF requirement) and stores fixed
-- `unit_price` at time of transaction to prevent historic price tampering.
-- -----------------------------------------------------------------------------------------
CREATE TABLE `order_items` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `order_id` INT NOT NULL,
    `product_id` INT NOT NULL,
    `product_name` VARCHAR(120) NOT NULL,
    `quantity` INT NOT NULL,
    `unit_price` DECIMAL(10, 2) NOT NULL,
    `subtotal` DECIMAL(12, 2) NOT NULL,
    INDEX `idx_order_items_order` (`order_id`),
    INDEX `idx_order_items_product` (`product_id`),
    CONSTRAINT `fk_order_items_order` 
        FOREIGN KEY (`order_id`) 
        REFERENCES `orders` (`id`) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT `fk_order_items_product` 
        FOREIGN KEY (`product_id`) 
        REFERENCES `marketplace_products` (`id`) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    CONSTRAINT `chk_order_item_qty` CHECK (`quantity` > 0)
) ENGINE=InnoDB COMMENT='Line items for sales orders';


-- -----------------------------------------------------------------------------------------
-- TABLE 11: `weather_records`
-- Purpose: Environmental sensor logging (temperature, humidity, rain chance, wind).
-- -----------------------------------------------------------------------------------------
CREATE TABLE `weather_records` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `location_name` VARCHAR(100) NOT NULL,
    `temperature_c` DECIMAL(5, 2) NOT NULL,
    `humidity_pct` INT NOT NULL,
    `rain_probability_pct` INT NOT NULL,
    `wind_speed_kph` DECIMAL(5, 2) NOT NULL,
    `condition_text` VARCHAR(80) NOT NULL,
    `recorded_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_weather_location` (`location_name`)
) ENGINE=InnoDB COMMENT='Meteorological farm telemetry readings';


-- =========================================================================================
-- 3. ANALYTICAL DATABASE VIEWS (For University DBMS Project Reports)
-- =========================================================================================

-- View: Crop-Wise Yield & Revenue Totals
CREATE OR REPLACE VIEW `view_crop_production_summary` AS
SELECT 
    c.id AS `crop_id`,
    c.crop_name,
    c.category,
    COALESCE(COUNT(cp.id), 0) AS `total_harvests`,
    COALESCE(SUM(cp.quantity_produced_kg), 0) AS `total_quantity_kg`,
    COALESCE(SUM(cp.production_cost), 0) AS `total_cost_pkr`,
    COALESCE(SUM(cp.expected_revenue), 0) AS `total_revenue_pkr`,
    COALESCE(SUM(cp.expected_revenue - cp.production_cost), 0) AS `net_profit_pkr`
FROM `crops` c
LEFT JOIN `crop_production` cp ON c.id = cp.crop_id
GROUP BY c.id, c.crop_name, c.category;

-- View: Farmer Landholdings and Performance
CREATE OR REPLACE VIEW `view_farmer_performance` AS
SELECT 
    f.id AS `farmer_id`,
    f.name AS `farmer_name`,
    f.phone,
    f.farm_size_acres,
    COUNT(DISTINCT fd.id) AS `total_fields`,
    COALESCE(SUM(cp.quantity_produced_kg), 0) AS `lifetime_production_kg`,
    COALESCE(SUM(cp.expected_revenue), 0) AS `gross_revenue_pkr`
FROM `farmers` f
LEFT JOIN `fields` fd ON f.id = fd.farmer_id
LEFT JOIN `crop_production` cp ON f.id = cp.farmer_id
GROUP BY f.id, f.name, f.phone, f.farm_size_acres;


-- =========================================================================================
-- 4. SAMPLE SEED DATA INSERTIONS
-- =========================================================================================

-- Seed Users (Bcrypt hash corresponds to 'password123')
INSERT INTO `users` (`id`, `full_name`, `email`, `phone`, `password_hash`, `user_type`) VALUES
(1, 'Haji Muhammad Akram', 'akram.farmer@crophub.com', '+92 300 1234567', '$2y$10$wJk6qC94B.XpE1nFwKj/ve0U2N9mE4qHj1H2Y.K3B7n7D.Z5yGjKG', 'Farmer'),
(2, 'AgriCorp Agro Supplies', 'dealer@agricorp.com', '+92 312 9876543', '$2y$10$wJk6qC94B.XpE1nFwKj/ve0U2N9mE4qHj1H2Y.K3B7n7D.Z5yGjKG', 'Crop Dealer'),
(3, 'Dr. Ayesha Tariq', 'ayesha.expert@crophub.com', '+92 333 4567890', '$2y$10$wJk6qC94B.XpE1nFwKj/ve0U2N9mE4qHj1H2Y.K3B7n7D.Z5yGjKG', 'Agriculture Expert'),
(4, 'Arslan Zafar', 'arslan.student@crophub.com', '+92 304 5556677', '$2y$10$wJk6qC94B.XpE1nFwKj/ve0U2N9mE4qHj1H2Y.K3B7n7D.Z5yGjKG', 'Student');

-- Seed Farmers
INSERT INTO `farmers` (`id`, `user_id`, `name`, `phone`, `email`, `address`, `farm_size_acres`, `experience_years`, `registered_date`) VALUES
(1, 1, 'Haji Muhammad Akram', '+92 300 1234567', 'akram.farmer@crophub.com', 'Chak 42-JB, Faisalabad, Punjab', 25.50, 18, '2023-01-15'),
(2, NULL, 'Chaudhry Bashir Ahmed', '+92 301 7654321', 'bashir.ahmed@gmail.com', 'Mouza Kot Radha Kishan, Kasur', 40.00, 24, '2023-03-20'),
(3, NULL, 'Malik Tariq Mehmood', '+92 302 3344556', 'tariq.malik@outlook.com', 'Tehsil Depalpur, Okara', 15.00, 10, '2023-06-11'),
(4, NULL, 'Rao Farhan Ali', '+92 305 8899001', 'farhan.rao@yahoo.com', 'Basti Malook, Multan', 30.00, 15, '2023-08-05');

-- Seed Crops
INSERT INTO `crops` (`id`, `crop_name`, `category`, `scientific_name`, `season`, `soil_type`, `water_requirement`, `expected_yield_per_acre`, `description`, `image_url`) VALUES
(1, 'Wheat (Gandum)', 'Cereals', 'Triticum aestivum', 'Rabi (Winter)', 'Loamy & Clay Loam', 'Medium', '1800 - 2400 kg', 'Staple grain crop sown in November and harvested in April. Crucial for food security.', 'wheat.jpg'),
(2, 'Basmati Rice (Dhan)', 'Cereals', 'Oryza sativa', 'Kharif (Summer)', 'Heavy Clay & Clay Loam', 'High', '1400 - 1900 kg', 'Premium aromatic long grain rice cultivated in fertile river valley tracts.', 'rice.jpg'),
(3, 'Cotton (Kapas)', 'Cash Crops', 'Gossypium hirsutum', 'Kharif (Summer)', 'Deep Sandy Loam to Clay', 'Medium', '1000 - 1500 kg', 'Industrial textile cash crop requiring careful pest scouting and sunny maturation weather.', 'cotton.jpg'),
(4, 'Sugarcane (Ganna)', 'Cash Crops', 'Saccharum officinarum', 'Year-round', 'Deep fertile Loam', 'High', '28000 - 38000 kg', 'Perennial sugar crop with high biomass production and industrial byproduct utility.', 'sugarcane.jpg'),
(5, 'Maize (Makai)', 'Cereals', 'Zea mays', 'Spring / Kharif', 'Well-drained Loam', 'Medium', '2800 - 3500 kg', 'Versatile hybrid cereal grain for human consumption, livestock silage, and poultry feeds.', 'maize.jpg'),
(6, 'Chickpea (Chana)', 'Legumes', 'Cicer arietinum', 'Rabi (Winter)', 'Sandy Loam to Loamy', 'Low', '700 - 1000 kg', 'Drought-hardy legume with root nodules fixing atmospheric nitrogen in dryland zones.', 'chickpea.jpg');

-- Seed Fields
INSERT INTO `fields` (`id`, `field_name`, `farmer_id`, `location`, `area_acres`, `soil_type`, `irrigation_type`, `current_crop_id`) VALUES
(1, 'Green Horizon Field A', 1, 'Sector 4 North Canal Side', 10.00, 'Loamy Soil', 'Canal & Tubewell', 1),
(2, 'Green Horizon Field B', 1, 'Sector 4 East Tube-well Plot', 8.50, 'Clay Loam', 'Tubewell', 5),
(3, 'Riverbed Delta Acre 1', 2, 'River Ravi Basin Block', 22.00, 'Heavy Clay', 'Canal Irrigation', 2),
(4, 'Sunny South Plot', 3, 'Depalpur Bypass Sector 2', 15.00, 'Sandy Loam', 'Drip Irrigation', 3),
(5, 'Multan Cotton Farm C', 4, 'Shujabad Road Block 5', 18.00, 'Silt Loam', 'Tubewell & Canal', 3);

-- Seed Crop Production
INSERT INTO `crop_production` (`id`, `farmer_id`, `field_id`, `crop_id`, `planting_date`, `harvest_date`, `quantity_produced_kg`, `production_cost`, `expected_revenue`, `notes`) VALUES
(1, 1, 1, 1, '2023-11-15', '2024-04-20', 21500.00, 320000.00, 580000.00, 'Superb kernel weight with balanced DAP and urea top-dressing.'),
(2, 2, 3, 2, '2024-06-10', '2024-10-25', 38000.00, 750000.00, 1420000.00, 'Certified Super Basmati harvested with export-grade moisture index.'),
(3, 3, 4, 3, '2024-05-02', '2024-11-12', 19500.00, 480000.00, 890000.00, 'Bollworm scouting performed weekly; neem extract bio-spray applied.'),
(4, 1, 2, 5, '2024-02-18', '2024-06-05', 26000.00, 310000.00, 560000.00, 'Spring hybrid maize yield with robust ear formation.');

-- Seed Fertilizers
INSERT INTO `fertilizers` (`id`, `name`, `type`, `suitable_crops`, `recommended_quantity`, `price_per_unit`, `description`) VALUES
(1, 'Urea (46% Nitrogen)', 'Nitrogenous', 'Wheat, Rice, Maize, Sugarcane', '50 kg / acre at tillering', 3800.00, 'High-potency nitrogen fertilizer promoting vegetative foliage growth and tillering.'),
(2, 'Di-Ammonium Phosphate (DAP)', 'Phosphatic', 'Wheat, Cotton, Oilseeds, Pulses', '50 kg / acre at sowing', 11500.00, 'Essential starter fertilizer providing 46% phosphorus and 18% nitrogen for root architecture.'),
(3, 'SOP (Sulphate of Potash)', 'Potassic', 'Potato, Sugarcane, Cotton, Fruits', '25 kg / acre during flowering', 13200.00, 'Chlorine-free potassium providing osmotic stress tolerance and grain plumpness.'),
(4, 'Bio-Enriched Compost', 'Organic', 'All Vegetables, Maize, Wheat', '500 kg / acre pre-plowing', 4200.00, 'Organic matter enhancing soil water holding capacity and microbial aeration.');

-- Seed Crop Diseases
INSERT INTO `crop_diseases` (`id`, `disease_name`, `affected_crops`, `symptoms`, `prevention`, `treatment_method`) VALUES
(1, 'Yellow Rust (Stripe Rust)', 'Wheat', 'Yellow powdery stripes appear along the veins of wheat leaves, reducing photosynthetic area rapidly.', 'Plant certified rust-resistant seed varieties like Akbar-19 or Ghazi-19. Avoid excessive late nitrogen.', 'Foliar spray with systemic triazole fungicides (such as Propiconazole or Tebuconazole) upon first symptom detection.'),
(2, 'Bacterial Leaf Blight (BLB)', 'Rice', 'Water-soaked lesions on leaf margins that turn yellowish-white with wavy margins; leaves dry out rapidly.', 'Maintain balanced potash fertilization, avoid over-flooding nursery beds, and use disease-free seed.', 'Spray Copper Oxychloride mixed with validamycin; drain field water temporarily to reduce moisture stress.'),
(3, 'Cotton Leaf Curl Virus (CLCuV)', 'Cotton', 'Upward or downward leaf curling, vein thickening, and leaf-like enation outgrowths on leaf undersides.', 'Control whitefly (Bemisia tabaci) vector vigorously; cultivate CLCuV-tolerant certified varieties.', 'No curative chemical for virus; manage vector whitefly with insect growth regulators (Pyriproxyfen / Dinotefuran).'),
(4, 'Early Blight', 'Potato, Tomato', 'Concentric dark brown rings forming target-board patterns on older leaves surrounded by yellow chlorotic halo.', 'Crop rotation with non-solanaceous crops, avoid overhead sprinkler irrigation, destroy crop residues.', 'Apply protective Mancozeb spray preventatively, or Difenoconazole/Azoxystrobin once spots emerge.');

-- Seed Marketplace Products
INSERT INTO `marketplace_products` (`id`, `name`, `category`, `price`, `seller_name`, `seller_contact`, `description`, `image_url`, `stock_quantity`) VALUES
(1, 'Faisalabad-2008 Certified Wheat Seeds (50kg)', 'Seeds', 4800.00, 'Punjab Seed Corporation', '+92 300 4455667', 'High-germination certified wheat seed bag with 98% purity guarantee.', 'seed_wheat.png', 45),
(2, 'Super Kernel Basmati Seed (40kg)', 'Seeds', 6200.00, 'Kisan Seed Center', '+92 312 8899112', 'Pure line long grain aroma paddy seed with strong seedling vigor.', 'seed_rice.png', 30),
(3, 'Fauji DAP Fertilizer (50kg Bag)', 'Fertilizers', 11500.00, 'AgriCorp Agro Supplies', '+92 312 9876543', 'High grade phosphorus fertilizer essential for root development and crop yield.', 'dap_bag.png', 60),
(4, 'Heavy-Duty Knapsack Battery Sprayer (16L)', 'Farming Tools', 5800.00, 'Zamindar Tool Mart', '+92 333 1122334', 'Rechargeable 12V 8Ah battery sprayer with stainless steel lance and 4 adjustable nozzles.', 'sprayer.png', 25),
(5, 'Automatic Drip Irrigation Kit (1 Acre)', 'Irrigation Equipment', 48500.00, 'EcoFlow Agro Water Tech', '+92 301 5566778', 'Complete micro-drip lateral pipe system with pressure compensating drippers and venturi injector.', 'drip_kit.png', 12),
(6, 'Neem Seed Oil Botanical Insecticide (1L)', 'Pesticides', 1650.00, 'GreenBio Plant Protection', '+92 321 7788990', 'Pure cold-pressed azadirachtin extract for broad-spectrum sucking pest prevention.', 'neem_oil.png', 50),
(7, 'Rotavator / Rotary Tiller (48 Blades)', 'Agricultural Machinery', 285000.00, 'Pak Tractor Implements', '+92 300 9988776', 'Heavy-duty tractor PTO driven rotary cultivator for rapid seedbed preparation.', 'rotavator.png', 5);

-- Seed Orders and Order Line Items
INSERT INTO `orders` (`id`, `user_id`, `customer_name`, `customer_phone`, `delivery_address`, `total_amount`, `status`) VALUES
(1, 1, 'Haji Muhammad Akram', '+92 300 1234567', 'Chak 42-JB, Faisalabad, Punjab', 23000.00, 'Confirmed');

INSERT INTO `order_items` (`order_id`, `product_id`, `product_name`, `quantity`, `unit_price`, `subtotal`) VALUES
(1, 3, 'Fauji DAP Fertilizer (50kg Bag)', 2, 11500.00, 23000.00);

-- Seed Weather Records
INSERT INTO `weather_records` (`location_name`, `temperature_c`, `humidity_pct`, `rain_probability_pct`, `wind_speed_kph`, `condition_text`) VALUES
('Central Farm Zone', 28.5, 52, 10, 12.4, 'Clear Sunny'),
('Northern Valley Plains', 23.0, 68, 45, 18.0, 'Partly Cloudy'),
('Southern Canal Belt', 31.2, 44, 5, 9.8, 'Warm & Dry');

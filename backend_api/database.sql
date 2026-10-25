-- =====================================================================
-- Advanced Agriculture Crop Hub - Database Schema (MySQL 5.7+ / 8.0+)
-- Suitable for BSCS 3rd-Semester DBMS Project (Normalized up to 3NF)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `crop_hub_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `crop_hub_db`;

-- 1. USERS TABLE
CREATE TABLE IF NOT EXISTS `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `full_name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(120) NOT NULL UNIQUE,
    `phone` VARCHAR(20) NOT NULL,
    `password_hash` VARCHAR(255) NOT NULL,
    `user_type` ENUM('Farmer', 'Crop Dealer', 'Agriculture Expert', 'Student') NOT NULL DEFAULT 'Farmer',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. FARMERS TABLE (Normalized separate entity with farm specifications)
CREATE TABLE IF NOT EXISTS `farmers` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NULL,
    `name` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(20) NOT NULL,
    `email` VARCHAR(120) NOT NULL,
    `address` VARCHAR(255) NOT NULL,
    `farm_size_acres` DECIMAL(8, 2) NOT NULL DEFAULT 0.00,
    `experience_years` INT NOT NULL DEFAULT 1,
    `registered_date` DATE NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_farmers_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 3. CROPS TABLE
CREATE TABLE IF NOT EXISTS `crops` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `crop_name` VARCHAR(100) NOT NULL,
    `category` VARCHAR(50) NOT NULL, -- e.g. Cereals, Legumes, Cash Crops, Vegetables, Fruits
    `scientific_name` VARCHAR(120) NOT NULL,
    `season` VARCHAR(50) NOT NULL,   -- e.g. Kharif, Rabi, Zaid, Year-round
    `soil_type` VARCHAR(100) NOT NULL,
    `water_requirement` VARCHAR(50) NOT NULL, -- Low, Medium, High
    `expected_yield_per_acre` VARCHAR(50) NOT NULL, -- e.g. 3500-4500 kg
    `description` TEXT NOT NULL,
    `image_url` VARCHAR(255) NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 4. FIELDS TABLE (Fields belong to Farmers and track active cultivation)
CREATE TABLE IF NOT EXISTS `fields` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `field_name` VARCHAR(100) NOT NULL,
    `farmer_id` INT NOT NULL,
    `location` VARCHAR(150) NOT NULL,
    `area_acres` DECIMAL(8, 2) NOT NULL,
    `soil_type` VARCHAR(100) NOT NULL,
    `irrigation_type` VARCHAR(80) NOT NULL, -- Drip, Canal, Tubewell, Sprinkler, Rainfed
    `current_crop_id` INT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_fields_farmer` FOREIGN KEY (`farmer_id`) REFERENCES `farmers` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_fields_crop` FOREIGN KEY (`current_crop_id`) REFERENCES `crops` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 5. CROP PRODUCTION TABLE (Harvest & Yield recording for reports)
CREATE TABLE IF NOT EXISTS `crop_production` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `farmer_id` INT NOT NULL,
    `field_id` INT NOT NULL,
    `crop_id` INT NOT NULL,
    `planting_date` DATE NOT NULL,
    `harvest_date` DATE NOT NULL,
    `quantity_produced_kg` DECIMAL(10, 2) NOT NULL,
    `production_cost` DECIMAL(12, 2) NOT NULL,
    `expected_revenue` DECIMAL(12, 2) NOT NULL,
    `notes` TEXT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_production_farmer` FOREIGN KEY (`farmer_id`) REFERENCES `farmers` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_production_field` FOREIGN KEY (`field_id`) REFERENCES `fields` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_production_crop` FOREIGN KEY (`crop_id`) REFERENCES `crops` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. FERTILIZERS TABLE
CREATE TABLE IF NOT EXISTS `fertilizers` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `type` VARCHAR(60) NOT NULL, -- Nitrogenous, Phosphatic, Potassic, Organic, Bio-Fertilizer
    `suitable_crops` VARCHAR(255) NOT NULL,
    `recommended_quantity` VARCHAR(100) NOT NULL, -- e.g. 50 kg/acre
    `price_per_unit` DECIMAL(10, 2) NOT NULL,
    `description` TEXT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 7. CROP DISEASES TABLE (Educational agricultural information)
CREATE TABLE IF NOT EXISTS `crop_diseases` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `disease_name` VARCHAR(120) NOT NULL,
    `affected_crops` VARCHAR(255) NOT NULL,
    `symptoms` TEXT NOT NULL,
    `prevention` TEXT NOT NULL,
    `treatment_method` TEXT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 8. MARKETPLACE PRODUCTS TABLE
CREATE TABLE IF NOT EXISTS `marketplace_products` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(120) NOT NULL,
    `category` VARCHAR(60) NOT NULL, -- Seeds, Fertilizers, Farming Tools, Irrigation Equipment, Pesticides, Agricultural Machinery
    `price` DECIMAL(10, 2) NOT NULL,
    `seller_name` VARCHAR(100) NOT NULL,
    `seller_contact` VARCHAR(50) NOT NULL,
    `description` TEXT NOT NULL,
    `image_url` VARCHAR(255) NULL,
    `stock_quantity` INT NOT NULL DEFAULT 10,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 9. ORDERS TABLE
CREATE TABLE IF NOT EXISTS `orders` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NULL,
    `customer_name` VARCHAR(100) NOT NULL,
    `customer_phone` VARCHAR(20) NOT NULL,
    `delivery_address` VARCHAR(255) NOT NULL,
    `total_amount` DECIMAL(12, 2) NOT NULL,
    `status` ENUM('Pending', 'Confirmed', 'Dispatched', 'Delivered', 'Cancelled') NOT NULL DEFAULT 'Confirmed',
    `order_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_orders_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 10. ORDER ITEMS TABLE (Normalized order line items)
CREATE TABLE IF NOT EXISTS `order_items` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `order_id` INT NOT NULL,
    `product_id` INT NOT NULL,
    `product_name` VARCHAR(120) NOT NULL,
    `quantity` INT NOT NULL,
    `unit_price` DECIMAL(10, 2) NOT NULL,
    `subtotal` DECIMAL(12, 2) NOT NULL,
    CONSTRAINT `fk_order_items_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_order_items_product` FOREIGN KEY (`product_id`) REFERENCES `marketplace_products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 11. WEATHER RECORDS TABLE
CREATE TABLE IF NOT EXISTS `weather_records` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `location_name` VARCHAR(100) NOT NULL,
    `temperature_c` DECIMAL(5, 2) NOT NULL,
    `humidity_pct` INT NOT NULL,
    `rain_probability_pct` INT NOT NULL,
    `wind_speed_kph` DECIMAL(5, 2) NOT NULL,
    `condition_text` VARCHAR(80) NOT NULL,
    `recorded_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =====================================================================
-- SAMPLE DATABASE RECORDS (Real agricultural data for demo/evaluation)
-- =====================================================================

-- Default Users (Password is 'password123' hashed using bcrypt)
INSERT INTO `users` (`id`, `full_name`, `email`, `phone`, `password_hash`, `user_type`) VALUES
(1, 'Haji Muhammad Akram', 'akram.farmer@crophub.com', '+92 300 1234567', '$2y$10$wJk6qC94B.XpE1nFwKj/ve0U2N9mE4qHj1H2Y.K3B7n7D.Z5yGjKG', 'Farmer'),
(2, 'AgriCorp Agro Supplies', 'dealer@agricorp.com', '+92 312 9876543', '$2y$10$wJk6qC94B.XpE1nFwKj/ve0U2N9mE4qHj1H2Y.K3B7n7D.Z5yGjKG', 'Crop Dealer'),
(3, 'Dr. Ayesha Tariq', 'ayesha.expert@crophub.com', '+92 333 4567890', '$2y$10$wJk6qC94B.XpE1nFwKj/ve0U2N9mE4qHj1H2Y.K3B7n7D.Z5yGjKG', 'Agriculture Expert'),
(4, 'Arslan Zafar', 'arslan.student@crophub.com', '+92 304 5556677', '$2y$10$wJk6qC94B.XpE1nFwKj/ve0U2N9mE4qHj1H2Y.K3B7n7D.Z5yGjKG', 'Student');

-- Farmers
INSERT INTO `farmers` (`id`, `user_id`, `name`, `phone`, `email`, `address`, `farm_size_acres`, `experience_years`, `registered_date`) VALUES
(1, 1, 'Haji Muhammad Akram', '+92 300 1234567', 'akram.farmer@crophub.com', 'Chak 42-JB, Faisalabad, Punjab', 25.50, 18, '2023-01-15'),
(2, NULL, 'Chaudhry Bashir Ahmed', '+92 301 7654321', 'bashir.ahmed@gmail.com', 'Mouza Kot Radha Kishan, Kasur', 40.00, 24, '2023-03-20'),
(3, NULL, 'Malik Tariq Mehmood', '+92 302 3344556', 'tariq.malik@outlook.com', 'Tehsil Depalpur, Okara', 15.00, 10, '2023-06-11'),
(4, NULL, 'Rao Farhan Ali', '+92 305 8899001', 'farhan.rao@yahoo.com', 'Basti Malook, Multan', 30.00, 15, '2023-08-05');

-- Crops
INSERT INTO `crops` (`id`, `crop_name`, `category`, `scientific_name`, `season`, `soil_type`, `water_requirement`, `expected_yield_per_acre`, `description`, `image_url`) VALUES
(1, 'Wheat (Gandum)', 'Cereals', 'Triticum aestivum', 'Rabi (Winter)', 'Loamy & Clay Loam', 'Medium (4-5 Irrigations)', '1800 - 2400 kg', 'Major staple food crop. Sown in November-December and harvested in April. Requires cool weather during early growth and warm dry weather during ripening.', 'wheat.jpg'),
(2, 'Basmati Rice (Dhan)', 'Cereals', 'Oryza sativa', 'Kharif (Summer)', 'Heavy Clay & Clay Loam', 'High (Continuous standing water)', '1400 - 1900 kg', 'World-renowned aromatic long-grain rice variety. Best cultivated in fertile river valley soils under hot humid summers.', 'rice.jpg'),
(3, 'Cotton (Kapas)', 'Cash Crops', 'Gossypium hirsutum', 'Kharif (Summer)', 'Deep Sandy Loam to Clay', 'Medium (5-6 Irrigations)', '1000 - 1500 kg', 'Silver fiber crop of tremendous industrial value. Requires warm days, abundant sunshine, and pest-monitored irrigation.', 'cotton.jpg'),
(4, 'Sugarcane (Ganna)', 'Cash Crops', 'Saccharum officinarum', 'Year-round', 'Deep fertile Loam', 'High (12-16 Irrigations)', '28000 - 38000 kg', 'Long-duration cash crop crucial for sugar and jaggery production. Requires heavy feeding and abundant irrigation.', 'sugarcane.jpg'),
(5, 'Maize (Makai)', 'Cereals', 'Zea mays', 'Spring / Kharif', 'Well-drained Loam', 'Medium (6-8 Irrigations)', '2800 - 3500 kg', 'High-yielding cereal grain utilized as food, animal feed, and industrial starch. Sown in February or July.', 'maize.jpg'),
(6, 'Chickpea (Chana)', 'Legumes', 'Cicer arietinum', 'Rabi (Winter)', 'Sandy Loam to Loamy', 'Low (1-2 Irrigations or Rainfed)', '700 - 1000 kg', 'Protein-rich legume that enriches soil through atmospheric nitrogen fixation. Drought-tolerant winter crop.', 'chickpea.jpg');

-- Fields
INSERT INTO `fields` (`id`, `field_name`, `farmer_id`, `location`, `area_acres`, `soil_type`, `irrigation_type`, `current_crop_id`) VALUES
(1, 'Green Horizon Field A', 1, 'Sector 4 North Canal Side', 10.00, 'Loamy Soil', 'Canal & Tubewell', 1),
(2, 'Green Horizon Field B', 1, 'Sector 4 East Tube-well Plot', 8.50, 'Clay Loam', 'Tubewell', 5),
(3, 'Riverbed Delta Acre 1', 2, 'River Ravi Basin Block', 22.00, 'Heavy Clay', 'Canal Irrigation', 2),
(4, 'Sunny South Plot', 3, 'Depalpur Bypass Sector 2', 15.00, 'Sandy Loam', 'Drip Irrigation', 3),
(5, 'Multan Cotton Farm C', 4, 'Shujabad Road Block 5', 18.00, 'Silt Loam', 'Tubewell & Canal', 3);

-- Crop Production (Harvest records)
INSERT INTO `crop_production` (`id`, `farmer_id`, `field_id`, `crop_id`, `planting_date`, `harvest_date`, `quantity_produced_kg`, `production_cost`, `expected_revenue`, `notes`) VALUES
(1, 1, 1, 1, '2023-11-15', '2024-04-20', 21500.00, 320000.00, 580000.00, 'Excellent yield achieved with balanced DAP and urea application; minimum lodging occurred.'),
(2, 2, 3, 2, '2024-06-10', '2024-10-25', 38000.00, 750000.00, 1420000.00, 'Super Basmati variety with prime aroma. Export quality moisture managed perfectly.'),
(3, 3, 4, 3, '2024-05-02', '2024-11-12', 19500.00, 480000.00, 890000.00, 'Bollworm pest scouted early; biological predator insects and neem spray used successfully.'),
(4, 1, 2, 5, '2024-02-18', '2024-06-05', 26000.00, 310000.00, 560000.00, 'Spring hybrid maize with high kernel test weight.');

-- Fertilizers
INSERT INTO `fertilizers` (`id`, `name`, `type`, `suitable_crops`, `recommended_quantity`, `price_per_unit`, `description`) VALUES
(1, 'Urea (46% Nitrogen)', 'Nitrogenous', 'Wheat, Rice, Maize, Sugarcane', '50 kg / acre at tillering', 3800.00, 'High-potency nitrogen fertilizer promoting vegetative leaf growth and vigorous tillering.'),
(2, 'Di-Ammonium Phosphate (DAP)', 'Phosphatic', 'Wheat, Cotton, Oilseeds, Pulses', '50 kg / acre at sowing', 11500.00, 'Essential starter fertilizer rich in Phosphorus (46%) and Nitrogen (18%) for deep root establishment.'),
(3, 'SOP (Sulphate of Potash)', 'Potassic', 'Potato, Sugarcane, Cotton, Fruits', '25 kg / acre during flowering', 13200.00, 'Chlorine-free potassium providing drought tolerance, disease resistance, and premium fruit/grain grain weight.'),
(4, 'Bio-Enriched Compost', 'Organic', 'All Vegetables, Maize, Wheat', '500 kg / acre pre-plowing', 4200.00, 'Microbially active organic matter enhancing soil water holding capacity and soil carbon.');

-- Crop Diseases
INSERT INTO `crop_diseases` (`id`, `disease_name`, `affected_crops`, `symptoms`, `prevention`, `treatment_method`) VALUES
(1, 'Yellow Rust (Stripe Rust)', 'Wheat', 'Yellow powdery stripes appear along the veins of wheat leaves, reducing photosynthetic area rapidly.', 'Plant certified rust-resistant seed varieties like Akbar-19 or Ghazi-19. Avoid excessive late nitrogen.', 'Foliar spray with systemic triazole fungicides (such as Propiconazole or Tebuconazole) upon first symptom detection.'),
(2, 'Bacterial Leaf Blight (BLB)', 'Rice', 'Water-soaked lesions on leaf margins that turn yellowish-white with wavy margins; leaves dry out rapidly.', 'Maintain balanced potash fertilization, avoid over-flooding nursery beds, and use disease-free seed.', 'Spray Copper Oxychloride mixed with validamycin; drain field water temporarily to reduce moisture stress.'),
(3, 'Cotton Leaf Curl Virus (CLCuV)', 'Cotton', 'Upward or downward leaf curling, vein thickening, and leaf-like enation outgrowths on leaf undersides.', 'Control whitefly (Bemisia tabaci) vector vigorously; cultivate CLCuV-tolerant certified varieties.', 'No curative chemical for virus; manage vector whitefly with insect growth regulators (Pyriproxyfen / Dinotefuran).'),
(4, 'Early Blight', 'Potato, Tomato', 'Concentric dark brown rings forming target-board patterns on older leaves surrounded by yellow chlorotic halo.', 'Crop rotation with non-solanaceous crops, avoid overhead sprinkler irrigation, destroy crop residues.', 'Apply protective Mancozeb spray preventatively, or Difenoconazole/Azoxystrobin once spots emerge.');

-- Marketplace Products
INSERT INTO `marketplace_products` (`id`, `name`, `category`, `price`, `seller_name`, `seller_contact`, `description`, `image_url`, `stock_quantity`) VALUES
(1, 'Faisalabad-2008 Certified Wheat Seeds (50kg)', 'Seeds', 4800.00, 'Punjab Seed Corporation', '+92 300 4455667', 'High-germination disease-certified wheat seed bag with 98% purity guarantee.', 'seed_wheat.png', 45),
(2, 'Super Kernel Basmati Seed (40kg)', 'Seeds', 6200.00, 'Kisan Seed Center', '+92 312 8899112', 'Pure line long grain aroma paddy seed with strong seedling vigor.', 'seed_rice.png', 30),
(3, 'Fauji DAP Fertilizer (50kg Bag)', 'Fertilizers', 11500.00, 'AgriCorp Agro Supplies', '+92 312 9876543', 'High grade phosphorus fertilizer essential for root development and crop yield.', 'dap_bag.png', 60),
(4, 'Heavy-Duty Knapsack Battery Sprayer (16L)', 'Farming Tools', 5800.00, 'Zamindar Tool Mart', '+92 333 1122334', 'Rechargeable 12V 8Ah battery sprayer with stainless steel lance and 4 adjustable nozzles.', 'sprayer.png', 25),
(5, 'Automatic Drip Irrigation Kit (1 Acre)', 'Irrigation Equipment', 48500.00, 'EcoFlow Agro Water Tech', '+92 301 5566778', 'Complete micro-drip lateral pipe system with pressure compensating drippers and venturi fertilizer injector.', 'drip_kit.png', 12),
(6, 'Neem Seed Oil Botanical Insecticide (1L)', 'Pesticides', 1650.00, 'GreenBio Plant Protection', '+92 321 7788990', 'Pure cold-pressed azadirachtin extract for broad-spectrum sucking pest prevention.', 'neem_oil.png', 50),
(7, 'Rotavator / Rotary Tiller (48 Blades)', 'Agricultural Machinery', 285000.00, 'Pak Tractor Implements', '+92 300 9988776', 'Heavy-duty tractor PTO driven rotary cultivator for rapid seedbed preparation.', 'rotavator.png', 5);

-- Weather Records
INSERT INTO `weather_records` (`location_name`, `temperature_c`, `humidity_pct`, `rain_probability_pct`, `wind_speed_kph`, `condition_text`) VALUES
('Central Farm Zone', 28.5, 52, 10, 12.4, 'Clear Sunny'),
('Northern Valley Plains', 23.0, 68, 45, 18.0, 'Partly Cloudy'),
('Southern Canal Belt', 31.2, 44, 5, 9.8, 'Warm & Dry');

-- Orders & Order Items (Initial demo orders)
INSERT INTO `orders` (`id`, `user_id`, `customer_name`, `customer_phone`, `delivery_address`, `total_amount`, `status`) VALUES
(1, 1, 'Haji Muhammad Akram', '+92 300 1234567', 'Chak 42-JB, Faisalabad, Punjab', 23000.00, 'Confirmed');

INSERT INTO `order_items` (`order_id`, `product_id`, `product_name`, `quantity`, `unit_price`, `subtotal`) VALUES
(1, 3, 'Fauji DAP Fertilizer (50kg Bag)', 2, 11500.00, 23000.00);

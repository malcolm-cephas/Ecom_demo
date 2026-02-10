-- ==========================================
-- 1. Table Creation
-- ==========================================
CREATE TABLE IF NOT EXISTS PRODUCT (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    AVAILABLE BOOLEAN,
    BRAND VARCHAR(100),
    CATEGORY VARCHAR(100),
    DESCRIPTION TEXT,
    IMAGE_DATA LONGBLOB,
    IMAGE_NAME VARCHAR(255),
    IMAGE_TYPE VARCHAR(50),
    NAME VARCHAR(150),
    PRICE DECIMAL(10,2),
    RELEASE_DATE DATE,
    STOCK_QUANTITY INT
) ENGINE=InnoDB;

-- ==========================================
-- 2. Data Cleanup
-- ==========================================
SET SQL_SAFE_UPDATES = 0;
DELETE FROM PRODUCT;
ALTER TABLE PRODUCT AUTO_INCREMENT = 1;
SET SQL_SAFE_UPDATES = 1;

-- ==========================================
-- 3. Product Insertion
-- ==========================================
INSERT INTO PRODUCT (AVAILABLE, BRAND, CATEGORY, DESCRIPTION, IMAGE_DATA, IMAGE_NAME, IMAGE_TYPE, NAME, PRICE, RELEASE_DATE, STOCK_QUANTITY) VALUES
(TRUE, 'Samsung', 'Electronics', '5G smartphone with AMOLED display', NULL, 'galaxy_s23.jpg', 'image/jpeg', 'Galaxy S23', 74999, '2023-02-01', 35),
(TRUE, 'Apple', 'Electronics', 'A15 Bionic smartphone', NULL, 'iphone_14.jpg', 'image/jpeg', 'iPhone 14', 79999, '2022-09-16', 28),
(TRUE, 'Xiaomi', 'Electronics', '120Hz display budget smartphone', NULL, 'redmi_note_13.jpg', 'image/jpeg', 'Redmi Note 13', 17999, '2024-01-10', 60),
(TRUE, 'OnePlus', 'Electronics', 'Flagship AMOLED smartphone', NULL, 'oneplus_11.jpg', 'image/jpeg', 'OnePlus 11', 56999, '2023-01-07', 25),
(TRUE, 'Realme', 'Electronics', '108MP camera 5G phone', NULL, 'realme_11_pro.jpg', 'image/jpeg', 'Realme 11 Pro', 24999, '2024-02-15', 50),
(TRUE, 'Motorola', 'Electronics', 'Stock Android 5G phone', NULL, 'moto_g54.jpg', 'image/jpeg', 'Moto G54', 15999, '2023-08-10', 55),
(TRUE, 'Vivo', 'Electronics', 'Slim AMOLED smartphone', NULL, 'vivo_v29.jpg', 'image/jpeg', 'Vivo V29', 32999, '2023-09-05', 30),
(TRUE, 'Oppo', 'Electronics', 'Fast charging smartphone', NULL, 'oppo_reno_8.jpg', 'image/jpeg', 'Oppo Reno 8', 28999, '2023-06-20', 25),
(TRUE, 'Samsung', 'Electronics', '55-inch 4K Smart TV', NULL, 'samsung_qled_tv.jpg', 'image/jpeg', 'Samsung QLED TV', 65000, '2023-09-25', 10),
(TRUE, 'LG', 'Electronics', 'Premium OLED television', NULL, 'lg_oled_tv.jpg', 'image/jpeg', 'LG OLED TV', 135000, '2023-10-01', 5),
(TRUE, 'Sony', 'Electronics', 'Noise cancelling headphones', NULL, 'sony_wh_1000xm5.jpg', 'image/jpeg', 'Sony WH-1000XM5', 29990, '2022-05-13', 12),
(TRUE, 'JBL', 'Electronics', 'Portable Bluetooth speaker', NULL, 'jbl_flip_6.jpg', 'image/jpeg', 'JBL Flip 6', 11999, '2023-08-18', 40),
(TRUE, 'Bose', 'Electronics', 'Premium wireless headphones', NULL, 'bose_qc45.jpg', 'image/jpeg', 'Bose QC45', 32999, '2022-12-01', 15),
(TRUE, 'Boat', 'Electronics', 'True wireless earbuds', NULL, 'boat_airdopes_441.jpg', 'image/jpeg', 'Boat Airdopes 441', 2499, '2023-05-22', 80),
(TRUE, 'Nothing', 'Electronics', 'Transparent earbuds', NULL, 'nothing_ear_1.jpg', 'image/jpeg', 'Nothing Ear 1', 6999, '2023-04-18', 35),
(TRUE, 'Anker', 'Electronics', 'Fast charging power bank', NULL, 'anker_powercore.jpg', 'image/jpeg', 'Anker PowerCore', 3999, '2023-02-05', 60),
(TRUE, 'Belkin', 'Electronics', 'Wireless charging pad', NULL, 'belkin_boostcharge.jpg', 'image/jpeg', 'Belkin BoostCharge', 2999, '2023-03-14', 45),
(TRUE, 'Amazon', 'Electronics', 'Smart speaker with Alexa', NULL, 'echo_dot_5.jpg', 'image/jpeg', 'Echo Dot 5', 5499, '2023-10-01', 25),
(TRUE, 'Xiaomi', 'Electronics', 'WiFi enabled smart bulb', NULL, 'mi_smart_bulb.jpg', 'image/jpeg', 'Mi Smart Bulb', 799, '2023-05-14', 100),
(TRUE, 'TP-Link', 'Electronics', 'Dual band WiFi router', NULL, 'tp_link_archer_c6.jpg', 'image/jpeg', 'TP-Link Archer C6', 2999, '2023-02-28', 30),
(TRUE, 'HP', 'Computers', 'Intel i5 laptop', NULL, 'hp_pavilion_15.jpg', 'image/jpeg', 'HP Pavilion 15', 62000, '2023-03-05', 15),
(TRUE, 'Dell', 'Computers', 'Lightweight laptop', NULL, 'dell_inspiron_14.jpg', 'image/jpeg', 'Dell Inspiron 14', 54500, '2023-07-20', 18),
(TRUE, 'Lenovo', 'Computers', 'Business laptop', NULL, 'lenovo_thinkpad_e14.jpg', 'image/jpeg', 'Lenovo ThinkPad E14', 58999, '2023-06-12', 20),
(TRUE, 'Asus', 'Computers', 'Gaming laptop', NULL, 'asus_tuf_f15.jpg', 'image/jpeg', 'Asus TUF F15', 89999, '2023-05-20', 12),
(TRUE, 'Acer', 'Computers', 'Student laptop', NULL, 'acer_aspire_5.jpg', 'image/jpeg', 'Acer Aspire 5', 47999, '2023-05-18', 20),
(TRUE, 'MSI', 'Computers', 'High performance gaming laptop', NULL, 'msi_katana_gf66.jpg', 'image/jpeg', 'MSI Katana GF66', 99999, '2023-07-01', 10),
(TRUE, 'Apple', 'Computers', 'Apple M2 laptop', NULL, 'macbook_air_m2.jpg', 'image/jpeg', 'MacBook Air M2', 114999, '2023-06-15', 12),
(TRUE, 'HP', 'Computers', 'Desktop PC', NULL, 'hp_all_in_one.jpg', 'image/jpeg', 'HP All-in-One', 68999, '2023-04-25', 8),
(TRUE, 'Dell', 'Computers', 'Professional workstation', NULL, 'dell_precision_3570.jpg', 'image/jpeg', 'Dell Precision 3570', 124999, '2023-03-10', 6),
(TRUE, 'Apple', 'Computers', '10.9-inch tablet', NULL, 'ipad_10th_gen.jpg', 'image/jpeg', 'iPad 10th Gen', 44999, '2023-09-22', 18),
(TRUE, 'Samsung', 'Computers', 'AMOLED tablet', NULL, 'galaxy_tab_s9.jpg', 'image/jpeg', 'Galaxy Tab S9', 62999, '2023-08-11', 12),
(TRUE, 'Seagate', 'Computers', 'External hard drive', NULL, 'seagate_1tb_hdd.jpg', 'image/jpeg', 'Seagate 1TB HDD', 4599, '2022-09-05', 26),
(TRUE, 'Logitech', 'Computers', 'Wireless gaming mouse', NULL, 'logitech_g502.jpg', 'image/jpeg', 'Logitech G502', 4999, '2023-07-10', 35),
(TRUE, 'Razer', 'Computers', 'Mechanical gaming keyboard', NULL, 'razer_blackwidow.jpg', 'image/jpeg', 'Razer BlackWidow', 12999, '2023-08-02', 20),
(TRUE, 'Canon', 'Computers', 'DSLR camera', NULL, 'canon_eos_1500d.jpg', 'image/jpeg', 'Canon EOS 1500D', 37999, '2022-11-10', 14),
(TRUE, 'Apple', 'Wearables', 'Smartwatch', NULL, 'apple_watch_se.jpg', 'image/jpeg', 'Apple Watch SE', 29900, '2023-09-15', 22),
(TRUE, 'Xiaomi', 'Wearables', 'Fitness tracker', NULL, 'mi_band_8.jpg', 'image/jpeg', 'Mi Band 8', 3999, '2024-01-30', 90),
(TRUE, 'Noise', 'Wearables', 'Bluetooth calling watch', NULL, 'noise_colorfit_pro.jpg', 'image/jpeg', 'Noise ColorFit Pro', 4999, '2024-01-25', 55),
(TRUE, 'Fire-Boltt', 'Wearables', 'AMOLED smartwatch', NULL, 'fire_boltt_visionary.jpg', 'image/jpeg', 'Fire-Boltt Visionary', 3999, '2023-12-10', 60),
(TRUE, 'Titan', 'Wearables', 'Hybrid smartwatch', NULL, 'titan_smart_pro.jpg', 'image/jpeg', 'Titan Smart Pro', 12999, '2024-02-20', 16),
(TRUE, 'Casio', 'Wearables', 'Digital sports watch', NULL, 'casio_g_shock.jpg', 'image/jpeg', 'Casio G-Shock', 7999, '2023-02-14', 28),
(TRUE, 'Fossil', 'Wearables', 'Analog wrist watch', NULL, 'fossil_grant.jpg', 'image/jpeg', 'Fossil Grant', 10999, '2023-06-30', 22),
(TRUE, 'Ray-Ban', 'Wearables', 'UV sunglasses', NULL, 'ray_ban_aviator.jpg', 'image/jpeg', 'Ray-Ban Aviator', 8999, '2022-09-12', 18),
(TRUE, 'Skullcandy', 'Wearables', 'Bass headphones', NULL, 'skullcandy_crusher.jpg', 'image/jpeg', 'Skullcandy Crusher', 17999, '2022-11-30', 20),
(TRUE, 'Sony', 'Wearables', 'Gaming console', NULL, 'sony_ps5.jpg', 'image/jpeg', 'Sony PS5', 49999, '2022-11-20', 5),
(TRUE, 'Microsoft', 'Wearables', 'Gaming console', NULL, 'xbox_series_x.jpg', 'image/jpeg', 'Xbox Series X', 52999, '2022-10-15', 4),
(TRUE, 'GoPro', 'Wearables', 'Action camera', NULL, 'gopro_hero_11.jpg', 'image/jpeg', 'GoPro Hero 11', 42999, '2023-10-01', 18),
(TRUE, 'DJI', 'Wearables', '4K drone', NULL, 'dji_mini_3.jpg', 'image/jpeg', 'DJI Mini 3', 55999, '2023-08-20', 9),
(TRUE, 'Insta360', 'Wearables', '360 camera', NULL, 'insta360_x3.jpg', 'image/jpeg', 'Insta360 X3', 44999, '2023-11-15', 12),
(TRUE, 'Canon', 'Wearables', 'Mirrorless camera', NULL, 'canon_eos_r50.jpg', 'image/jpeg', 'Canon EOS R50', 58999, '2023-09-12', 14),
(TRUE, 'Nike', 'Footwear', 'Sports shoes', NULL, 'nike_air_max.jpg', 'image/jpeg', 'Nike Air Max', 8999, '2023-08-01', 40),
(TRUE, 'Adidas', 'Footwear', 'Running shoes', NULL, 'adidas_ultraboost.jpg', 'image/jpeg', 'Adidas Ultraboost', 12999, '2023-04-18', 30),
(TRUE, 'Puma', 'Footwear', 'Running shoes', NULL, 'puma_run_rider.jpg', 'image/jpeg', 'Puma Run Rider', 5999, '2023-09-01', 35),
(TRUE, 'Levis', 'Clothing', 'Slim fit shirt', NULL, 'levis_511_shirt.jpg', 'image/jpeg', 'Levis 511 Shirt', 2199, '2023-10-10', 50),
(TRUE, 'Zara', 'Clothing', 'Women casual dress', NULL, 'zara_floral_dress.jpg', 'image/jpeg', 'Zara Floral Dress', 3499, '2024-01-05', 30),
(TRUE, 'H&M', 'Clothing', 'Cotton hoodie', NULL, 'h&m_hoodie.jpg', 'image/jpeg', 'H&M Hoodie', 2499, '2023-11-18', 40),
(TRUE, 'Ikea', 'Furniture', 'Study table', NULL, 'ikea_linnmon.jpg', 'image/jpeg', 'Ikea Linnmon', 6999, '2023-05-05', 12),
(TRUE, 'Godrej', 'Furniture', 'Steel wardrobe', NULL, 'godrej_interio.jpg', 'image/jpeg', 'Godrej Interio', 18999, '2023-04-22', 7),
(TRUE, 'Nilkamal', 'Furniture', 'Plastic chair set', NULL, 'nilkamal_chair.jpg', 'image/jpeg', 'Nilkamal Chair', 2499, '2022-12-30', 50),
(TRUE, 'Wakefit', 'Furniture', 'Orthopedic mattress', NULL, 'wakefit_mattress.jpg', 'image/jpeg', 'Wakefit Mattress', 15999, '2023-06-01', 14),
(TRUE, 'Samsung', 'Home Appliances', 'Double door fridge', NULL, 'samsung_refrigerator.jpg', 'image/jpeg', 'Samsung Refrigerator', 27999, '2023-04-10', 8),
(TRUE, 'LG', 'Home Appliances', 'Front load washer', NULL, 'lg_washing_machine.jpg', 'image/jpeg', 'LG Washing Machine', 31999, '2023-03-25', 6),
(TRUE, 'Philips', 'Home Appliances', 'Induction cooktop', NULL, 'philips_induction.jpg', 'image/jpeg', 'Philips Induction', 3299, '2022-07-19', 60),
(TRUE, 'Bosch', 'Home Appliances', 'Eco dishwasher', NULL, 'bosch_dishwasher.jpg', 'image/jpeg', 'Bosch Dishwasher', 34999, '2023-06-30', 6),
(TRUE, 'Voltas', 'Home Appliances', '1.5T inverter AC', NULL, 'voltas_ac.jpg', 'image/jpeg', 'Voltas AC', 38999, '2023-02-25', 5),
(TRUE, 'Mamaearth', 'Personal Care', 'Natural face wash', NULL, 'mamaearth_face_wash.jpg', 'image/jpeg', 'Mamaearth Face Wash', 399, '2023-01-10', 120),
(TRUE, 'Nivea', 'Personal Care', 'Moisturizing lotion', NULL, 'nivea_body_lotion.jpg', 'image/jpeg', 'Nivea Body Lotion', 299, '2022-12-05', 150),
(TRUE, 'WOW', 'Personal Care', 'Hair care oil', NULL, 'wow_onion_oil.jpg', 'image/jpeg', 'WOW Onion Oil', 499, '2023-04-01', 90),
(TRUE, 'Minimalist', 'Personal Care', 'Vitamin C serum', NULL, 'minimalist_serum.jpg', 'image/jpeg', 'Minimalist Serum', 699, '2023-08-14', 60),
(TRUE, 'Faber-Castell', 'Stationery', 'Sketch pen set', NULL, 'faber_castell_sketch_pens.jpg', 'image/jpeg', 'Faber-Castell Sketch Pens', 249, '2022-09-20', 180);

-- ==========================================
-- 4. Verification Query
-- ==========================================
SELECT COUNT(*) AS total_products FROM PRODUCT;

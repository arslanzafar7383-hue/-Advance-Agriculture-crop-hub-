# Advanced Agriculture Crop Hub (BSCS 3rd-Semester DBMS Project)

A modern, full-stack Android & MySQL agricultural management platform designed for farmers, agricultural students, crop dealers, and agriculture businesses.

---

## 1. Project Overview & Architecture
The system employs an **MVC / MVVM architecture**:
- **Android Front-End**: Native Android application written in **Kotlin** with **Jetpack Compose (Material Design 3)**, Retrofit2 for REST networking, and Coroutines + Flow for asynchronous state management.
- **REST API Backend**: Lightweight, high-performance **PHP 7.4+ / 8.x REST API** using **PDO (PHP Data Objects)** with prepared SQL statements and bcrypt password hashing.
- **Database Engine**: **MySQL 5.7+ / 8.0+** with 11 normalized tables structured up to Third Normal Form (3NF), foreign keys, and CASCADE constraints.
- **Local Dev Environment**: **XAMPP** (Apache & MySQL).

---

## 2. Database Relationship & 3NF Schema Structure

The database (`crop_hub_db`) is normalized up to **3NF** to eliminate data redundancy and anomalies:

```
[users] 1 ──── 0..1 [farmers] 1 ──── ∞ [fields] 1 ──── ∞ [crop_production]
   │                                       │                       │
   │                                       ∞                       ∞
   │                                   [crops] 1 ──────────────────┘
   │
   ├──── 0..∞ [orders] 1 ──── ∞ [order_items] ∞ ──── 1 [marketplace_products]
   │
   ├──── [fertilizers] (Categorized nutrient information)
   ├──── [crop_diseases] (Agricultural pathology & treatment guide)
   └──── [weather_records] (Environmental telemetry)
```

### Table Definitions & Primary / Foreign Keys:
1. **`users`**:
   - `id` (PK, INT AUTO_INCREMENT)
   - `full_name` (VARCHAR(100), NOT NULL)
   - `email` (VARCHAR(120), UNIQUE, NOT NULL)
   - `phone` (VARCHAR(20), NOT NULL)
   - `password_hash` (VARCHAR(255), NOT NULL, Bcrypt)
   - `user_type` (ENUM: 'Farmer', 'Crop Dealer', 'Agriculture Expert', 'Student')
   - `created_at` (TIMESTAMP)

2. **`farmers`**:
   - `id` (PK, INT AUTO_INCREMENT)
   - `user_id` (FK -> users.id, ON DELETE SET NULL)
   - `name`, `phone`, `email`, `address`, `farm_size_acres`, `experience_years`, `registered_date`

3. **`crops`**:
   - `id` (PK, INT AUTO_INCREMENT)
   - `crop_name`, `category`, `scientific_name`, `season`, `soil_type`, `water_requirement`, `expected_yield_per_acre`, `description`, `image_url`

4. **`fields`**:
   - `id` (PK, INT AUTO_INCREMENT)
   - `field_name`, `location`, `area_acres`, `soil_type`, `irrigation_type`
   - `farmer_id` (FK -> farmers.id, ON DELETE CASCADE)
   - `current_crop_id` (FK -> crops.id, ON DELETE SET NULL)

5. **`crop_production`**:
   - `id` (PK, INT AUTO_INCREMENT)
   - `farmer_id` (FK -> farmers.id, ON DELETE CASCADE)
   - `field_id` (FK -> fields.id, ON DELETE CASCADE)
   - `crop_id` (FK -> crops.id, ON DELETE CASCADE)
   - `planting_date`, `harvest_date`, `quantity_produced_kg`, `production_cost`, `expected_revenue`, `notes`

6. **`fertilizers`**:
   - `id` (PK, INT AUTO_INCREMENT)
   - `name`, `type` (Nitrogenous/Phosphatic/Potassic/Organic), `suitable_crops`, `recommended_quantity`, `price_per_unit`, `description`

7. **`crop_diseases`**:
   - `id` (PK, INT AUTO_INCREMENT)
   - `disease_name`, `affected_crops`, `symptoms`, `prevention`, `treatment_method`

8. **`marketplace_products`**:
   - `id` (PK, INT AUTO_INCREMENT)
   - `name`, `category` (Seeds, Fertilizers, Farming Tools, Irrigation Equipment, Pesticides, Agricultural Machinery), `price`, `seller_name`, `seller_contact`, `description`, `stock_quantity`

9. **`orders`**:
   - `id` (PK, INT AUTO_INCREMENT)
   - `user_id` (FK -> users.id, ON DELETE SET NULL)
   - `customer_name`, `customer_phone`, `delivery_address`, `total_amount`, `status`, `order_date`

10. **`order_items`**:
    - `id` (PK, INT AUTO_INCREMENT)
    - `order_id` (FK -> orders.id, ON DELETE CASCADE)
    - `product_id` (FK -> marketplace_products.id, ON DELETE CASCADE)
    - `product_name`, `quantity`, `unit_price`, `subtotal`

11. **`weather_records`**:
    - `id` (PK, INT AUTO_INCREMENT)
    - `location_name`, `temperature_c`, `humidity_pct`, `rain_probability_pct`, `wind_speed_kph`, `condition_text`, `recorded_at`

---

## 3. Required Android Permissions
Declared in `app/src/main/AndroidManifest.xml`:
- `android.permission.INTERNET`: Required to communicate with the XAMPP PHP REST API and fetch weather telemetry.
- `android.permission.ACCESS_NETWORK_STATE`: Monitors connection availability.
- `android:usesCleartextTraffic="true"`: Required by Android 9.0+ (API 28+) to permit HTTP traffic to local XAMPP servers (`http://10.0.2.2/` and `http://192.168.x.x/`).

---

## 4. XAMPP Setup Instructions

1. **Install XAMPP**: Download and install XAMPP with **Apache** and **MySQL**.
2. **Start Services**: Open XAMPP Control Panel and start **Apache** and **MySQL**.
3. **Deploy PHP REST API**:
   - Locate your XAMPP installation directory (typically `C:\xampp\htdocs\` on Windows or `/Applications/XAMPP/htdocs/` on macOS / Linux).
   - Copy the folder `/backend_api/` from this project into `htdocs` and rename it to `crop_hub_api`:
     ```
     C:\xampp\htdocs\crop_hub_api\
         ├── config\
         │     └── database.php
         ├── api\
         │     ├── test_connection.php
         │     ├── login.php
         │     ├── register.php
         │     ├── crops.php
         │     ├── farmers.php
         │     ├── fields.php
         │     ├── production.php
         │     ├── fertilizers.php
         │     ├── diseases.php
         │     ├── products.php
         │     ├── orders.php
         │     ├── weather.php
         │     └── reports.php
         └── database.sql
     ```
4. **Import Database in phpMyAdmin**:
   - Open your browser and navigate to `http://localhost/phpmyadmin/`.
   - Click on the **Import** tab.
   - Choose the file `database.sql` located inside `crop_hub_api/database.sql` (or `/backend_api/database.sql`).
   - Click **Go**. The database `crop_hub_db` will be created with all 11 tables and demo records.
5. **Verify API in Browser**:
   - Visit: `http://localhost/crop_hub_api/api/test_connection.php`
   - You should see:
     ```json
     {
       "status": "success",
       "message": "Crop Hub MySQL Backend is live and connected!",
       "data": {
         "database": "crop_hub_db",
         "tables_detected": 11
       }
     }
     ```

---

## 5. Connecting Android App with XAMPP

### Option A: Running on Android Studio Emulator
The Android emulator runs inside a virtual network where `10.0.2.2` represents your host PC's `localhost`:
- Default Base URL in app: `http://10.0.2.2/crop_hub_api/api/`
- Works automatically with zero extra setup!

### Option B: Running on a Physical Android Smartphone
1. Connect both your PC (running XAMPP) and your Android phone to the **same Wi-Fi network**.
2. Find your PC's local IP address:
   - On Windows: Run `ipconfig` in Command Prompt (look for IPv4 address, e.g. `192.168.1.15`).
   - On Mac/Linux: Run `ifconfig` or `ip a`.
3. In the Android App:
   - Tap the **Server Status chip / Settings icon** on the Login screen or Profile screen.
   - Enter your URL: `http://192.168.1.15/crop_hub_api/api/`
   - Tap **Test & Connect**. The status indicator will turn green `🟢 MySQL Live`!

---

## 6. Pre-Seeded Sample Database Credentials
All pre-seeded demo accounts use password: `password123`:
| Role | Email / Phone | Password |
|---|---|---|
| **Farmer** | `akram.farmer@crophub.com` | `password123` |
| **Crop Dealer** | `dealer@agricorp.com` | `password123` |
| **Agriculture Expert** | `ayesha.expert@crophub.com` | `password123` |
| **Student** | `arslan.student@crophub.com` | `password123` |

*Tip: The Login screen includes 1-tap instant demo fill chips for each role.*

---

## 7. APK Build Instructions

To build a release or debug APK from the command line or Android Studio:
```bash
# Debug APK
gradle :app:assembleDebug

# Release APK
gradle :app:assembleRelease
```
The compiled APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

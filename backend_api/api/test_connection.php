<?php
require_once __DIR__ . '/../config/database.php';

$database = new Database();
$db = $database->getConnection();

try {
    $stmt = $db->query("SELECT COUNT(*) AS total_tables FROM information_schema.tables WHERE table_schema = 'crop_hub_db'");
    $row = $stmt->fetch();
    
    sendResponse("success", "Crop Hub MySQL Backend is live and connected!", [
        "app_name" => "Advanced Agriculture Crop Hub",
        "server_time" => date('Y-m-d H:i:s'),
        "php_version" => phpversion(),
        "database" => "crop_hub_db",
        "tables_detected" => (int)$row['total_tables']
    ]);
} catch (Exception $e) {
    sendResponse("error", "Failed to query database: " . $e->getMessage(), null, 500);
}

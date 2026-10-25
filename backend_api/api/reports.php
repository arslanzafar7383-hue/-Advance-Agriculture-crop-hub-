<?php
require_once __DIR__ . '/../config/database.php';

$database = new Database();
$db = $database->getConnection();

try {
    // 1. Overall Totals
    $totalFarmers = (int)$db->query("SELECT COUNT(*) FROM farmers")->fetchColumn();
    $totalCrops = (int)$db->query("SELECT COUNT(*) FROM crops")->fetchColumn();
    $totalFields = (int)$db->query("SELECT COUNT(*) FROM fields")->fetchColumn();
    $totalProducts = (int)$db->query("SELECT COUNT(*) FROM marketplace_products")->fetchColumn();
    $totalOrders = (int)$db->query("SELECT COUNT(*) FROM orders")->fetchColumn();

    // Production metrics
    $prodStats = $db->query("SELECT 
        COALESCE(SUM(quantity_produced_kg), 0) AS total_production_kg,
        COALESCE(SUM(production_cost), 0) AS total_cost,
        COALESCE(SUM(expected_revenue), 0) AS total_revenue
        FROM crop_production")->fetch();

    $totalProductionKg = (float)$prodStats['total_production_kg'];
    $totalCost = (float)$prodStats['total_cost'];
    $totalRevenue = (float)$prodStats['total_revenue'];
    $netProfit = $totalRevenue - $totalCost;

    // 2. Crop-wise Production Breakdown
    $cropWiseStmt = $db->query("
        SELECT c.id, c.crop_name, c.category, 
               COALESCE(SUM(cp.quantity_produced_kg), 0) AS total_kg,
               COALESCE(SUM(cp.expected_revenue), 0) AS total_revenue,
               COUNT(cp.id) AS harvest_count
        FROM crops c
        LEFT JOIN crop_production cp ON c.id = cp.crop_id
        GROUP BY c.id, c.crop_name, c.category
        ORDER BY total_kg DESC
    ");
    $cropWiseProduction = $cropWiseStmt->fetchAll();

    // 3. Farmer-wise Production Breakdown
    $farmerWiseStmt = $db->query("
        SELECT f.id, f.name, f.farm_size_acres,
               COALESCE(SUM(cp.quantity_produced_kg), 0) AS total_kg,
               COALESCE(SUM(cp.expected_revenue), 0) AS total_revenue
        FROM farmers f
        LEFT JOIN crop_production cp ON f.id = cp.farmer_id
        GROUP BY f.id, f.name, f.farm_size_acres
        ORDER BY total_kg DESC
    ");
    $farmerWiseProduction = $farmerWiseStmt->fetchAll();

    sendResponse("success", "Analytics and database reports calculated", [
        "totals" => [
            "total_farmers" => $totalFarmers,
            "total_crops" => $totalCrops,
            "total_fields" => $totalFields,
            "total_products" => $totalProducts,
            "total_orders" => $totalOrders,
            "total_production_kg" => $totalProductionKg,
            "total_cost" => $totalCost,
            "total_revenue" => $totalRevenue,
            "net_profit" => $netProfit
        ],
        "crop_wise_production" => $cropWiseProduction,
        "farmer_wise_production" => $farmerWiseProduction
    ]);
} catch (Exception $e) {
    sendResponse("error", "Error generating report: " . $e->getMessage(), null, 500);
}

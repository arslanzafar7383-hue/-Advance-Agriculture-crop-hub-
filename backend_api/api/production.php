<?php
require_once __DIR__ . '/../config/database.php';

$database = new Database();
$db = $database->getConnection();
$method = $_SERVER['REQUEST_METHOD'];

switch ($method) {
    case 'GET':
        $sql = "SELECT cp.*, 
                       f.name AS farmer_name, 
                       fd.field_name, 
                       c.crop_name,
                       (cp.expected_revenue - cp.production_cost) AS net_profit
                FROM crop_production cp
                JOIN farmers f ON cp.farmer_id = f.id
                JOIN fields fd ON cp.field_id = fd.id
                JOIN crops c ON cp.crop_id = c.id
                ORDER BY cp.harvest_date DESC";
        $stmt = $db->query($sql);
        $records = $stmt->fetchAll();
        sendResponse("success", "Crop production records loaded", $records);
        break;

    case 'POST':
        $data = getJsonInput();
        $farmerId = (int)($data['farmer_id'] ?? 0);
        $fieldId = (int)($data['field_id'] ?? 0);
        $cropId = (int)($data['crop_id'] ?? 0);
        $plantingDate = $data['planting_date'] ?? date('Y-m-d');
        $harvestDate = $data['harvest_date'] ?? date('Y-m-d');
        $qty = (float)($data['quantity_produced_kg'] ?? 0);
        $cost = (float)($data['production_cost'] ?? 0);
        $revenue = (float)($data['expected_revenue'] ?? 0);
        $notes = trim($data['notes'] ?? '');

        if ($farmerId <= 0 || $fieldId <= 0 || $cropId <= 0 || $qty <= 0) {
            sendResponse("error", "Farmer, Field, Crop, and Quantity Produced are required", null, 400);
        }

        $stmt = $db->prepare("INSERT INTO crop_production (farmer_id, field_id, crop_id, planting_date, harvest_date, quantity_produced_kg, production_cost, expected_revenue, notes) VALUES (:farmer, :field, :crop, :plant, :harvest, :qty, :cost, :rev, :notes)");
        $stmt->execute([
            ':farmer' => $farmerId,
            ':field' => $fieldId,
            ':crop' => $cropId,
            ':plant' => $plantingDate,
            ':harvest' => $harvestDate,
            ':qty' => $qty,
            ':cost' => $cost,
            ':rev' => $revenue,
            ':notes' => $notes
        ]);
        sendResponse("success", "Production record logged successfully", ["id" => (int)$db->lastInsertId()], 201);
        break;

    case 'DELETE':
        $id = (int)($_GET['id'] ?? getJsonInput()['id'] ?? 0);
        if ($id <= 0) {
            sendResponse("error", "Valid Record ID is required", null, 400);
        }
        $stmt = $db->prepare("DELETE FROM crop_production WHERE id = :id");
        $stmt->execute([':id' => $id]);
        sendResponse("success", "Production record deleted successfully");
        break;

    default:
        sendResponse("error", "Method not supported", null, 405);
}

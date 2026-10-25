<?php
require_once __DIR__ . '/../config/database.php';

$database = new Database();
$db = $database->getConnection();
$method = $_SERVER['REQUEST_METHOD'];

switch ($method) {
    case 'GET':
        $search = trim($_GET['search'] ?? '');
        $type = trim($_GET['type'] ?? '');
        $sql = "SELECT * FROM fertilizers WHERE 1=1";
        $params = [];

        if (!empty($search)) {
            $sql .= " AND (name LIKE :search OR suitable_crops LIKE :search OR description LIKE :search)";
            $params[':search'] = "%$search%";
        }
        if (!empty($type) && $type !== 'All') {
            $sql .= " AND type = :type";
            $params[':type'] = $type;
        }
        $sql .= " ORDER BY name ASC";
        $stmt = $db->prepare($sql);
        $stmt->execute($params);
        $fertilizers = $stmt->fetchAll();
        sendResponse("success", "Fertilizers loaded", $fertilizers);
        break;

    case 'POST':
        $data = getJsonInput();
        $name = trim($data['name'] ?? '');
        $type = trim($data['type'] ?? 'Nitrogenous');
        $crops = trim($data['suitable_crops'] ?? 'All Crops');
        $dosage = trim($data['recommended_quantity'] ?? '50 kg / acre');
        $price = (float)($data['price_per_unit'] ?? 0);
        $desc = trim($data['description'] ?? '');

        if (empty($name)) {
            sendResponse("error", "Fertilizer name is required", null, 400);
        }

        $stmt = $db->prepare("INSERT INTO fertilizers (name, type, suitable_crops, recommended_quantity, price_per_unit, description) VALUES (:name, :type, :crops, :dose, :price, :desc)");
        $stmt->execute([
            ':name' => $name,
            ':type' => $type,
            ':crops' => $crops,
            ':dose' => $dosage,
            ':price' => $price,
            ':desc' => $desc
        ]);
        sendResponse("success", "Fertilizer added successfully", ["id" => (int)$db->lastInsertId()], 201);
        break;

    case 'DELETE':
        $id = (int)($_GET['id'] ?? getJsonInput()['id'] ?? 0);
        if ($id <= 0) {
            sendResponse("error", "Valid Fertilizer ID is required", null, 400);
        }
        $stmt = $db->prepare("DELETE FROM fertilizers WHERE id = :id");
        $stmt->execute([':id' => $id]);
        sendResponse("success", "Fertilizer deleted successfully");
        break;

    default:
        sendResponse("error", "Method not supported", null, 405);
}

<?php
require_once __DIR__ . '/../config/database.php';

$database = new Database();
$db = $database->getConnection();
$method = $_SERVER['REQUEST_METHOD'];

switch ($method) {
    case 'GET':
        if (isset($_GET['id'])) {
            $stmt = $db->prepare("SELECT * FROM crops WHERE id = :id");
            $stmt->execute([':id' => $_GET['id']]);
            $crop = $stmt->fetch();
            if ($crop) {
                sendResponse("success", "Crop retrieved successfully", $crop);
            } else {
                sendResponse("error", "Crop not found", null, 404);
            }
        } else {
            $search = trim($_GET['search'] ?? '');
            $category = trim($_GET['category'] ?? '');
            $sql = "SELECT * FROM crops WHERE 1=1";
            $params = [];

            if (!empty($search)) {
                $sql .= " AND (crop_name LIKE :search OR scientific_name LIKE :search OR description LIKE :search)";
                $params[':search'] = "%$search%";
            }
            if (!empty($category) && $category !== 'All') {
                $sql .= " AND category = :category";
                $params[':category'] = $category;
            }
            $sql .= " ORDER BY crop_name ASC";
            $stmt = $db->prepare($sql);
            $stmt->execute($params);
            $crops = $stmt->fetchAll();
            sendResponse("success", "Crops loaded", $crops);
        }
        break;

    case 'POST':
        $data = getJsonInput();
        $name = trim($data['crop_name'] ?? '');
        $category = trim($data['category'] ?? 'Cereals');
        $scientific = trim($data['scientific_name'] ?? '');
        $season = trim($data['season'] ?? 'Rabi');
        $soil = trim($data['soil_type'] ?? 'Loamy');
        $water = trim($data['water_requirement'] ?? 'Medium');
        $yield = trim($data['expected_yield_per_acre'] ?? '2000 kg');
        $desc = trim($data['description'] ?? '');
        $imageUrl = trim($data['image_url'] ?? 'crop_default.jpg');

        if (empty($name)) {
            sendResponse("error", "Crop name is required", null, 400);
        }

        $stmt = $db->prepare("INSERT INTO crops (crop_name, category, scientific_name, season, soil_type, water_requirement, expected_yield_per_acre, description, image_url) VALUES (:name, :cat, :sci, :season, :soil, :water, :yield, :desc, :img)");
        $stmt->execute([
            ':name' => $name,
            ':cat' => $category,
            ':sci' => $scientific,
            ':season' => $season,
            ':soil' => $soil,
            ':water' => $water,
            ':yield' => $yield,
            ':desc' => $desc,
            ':img' => $imageUrl
        ]);
        sendResponse("success", "Crop added successfully", ["id" => (int)$db->lastInsertId()], 201);
        break;

    case 'PUT':
        $data = getJsonInput();
        $id = (int)($data['id'] ?? $_GET['id'] ?? 0);
        if ($id <= 0) {
            sendResponse("error", "Valid Crop ID is required for update", null, 400);
        }

        $stmt = $db->prepare("UPDATE crops SET crop_name = :name, category = :cat, scientific_name = :sci, season = :season, soil_type = :soil, water_requirement = :water, expected_yield_per_acre = :yield, description = :desc WHERE id = :id");
        $stmt->execute([
            ':name' => trim($data['crop_name'] ?? ''),
            ':cat' => trim($data['category'] ?? 'Cereals'),
            ':sci' => trim($data['scientific_name'] ?? ''),
            ':season' => trim($data['season'] ?? 'Rabi'),
            ':soil' => trim($data['soil_type'] ?? 'Loamy'),
            ':water' => trim($data['water_requirement'] ?? 'Medium'),
            ':yield' => trim($data['expected_yield_per_acre'] ?? ''),
            ':desc' => trim($data['description'] ?? ''),
            ':id' => $id
        ]);
        sendResponse("success", "Crop updated successfully");
        break;

    case 'DELETE':
        $id = (int)($_GET['id'] ?? getJsonInput()['id'] ?? 0);
        if ($id <= 0) {
            sendResponse("error", "Valid Crop ID is required", null, 400);
        }
        $stmt = $db->prepare("DELETE FROM crops WHERE id = :id");
        $stmt->execute([':id' => $id]);
        sendResponse("success", "Crop deleted successfully");
        break;

    default:
        sendResponse("error", "Method not supported", null, 405);
}

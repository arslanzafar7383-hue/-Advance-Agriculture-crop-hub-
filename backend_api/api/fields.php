<?php
require_once __DIR__ . '/../config/database.php';

$database = new Database();
$db = $database->getConnection();
$method = $_SERVER['REQUEST_METHOD'];

switch ($method) {
    case 'GET':
        $sql = "SELECT f.*, fm.name AS farmer_name, c.crop_name AS current_crop_name 
                FROM fields f 
                LEFT JOIN farmers fm ON f.farmer_id = fm.id 
                LEFT JOIN crops c ON f.current_crop_id = c.id 
                ORDER BY f.id DESC";
        $stmt = $db->query($sql);
        $fields = $stmt->fetchAll();
        sendResponse("success", "Fields loaded", $fields);
        break;

    case 'POST':
        $data = getJsonInput();
        $name = trim($data['field_name'] ?? '');
        $farmerId = (int)($data['farmer_id'] ?? 0);
        $location = trim($data['location'] ?? '');
        $area = (float)($data['area_acres'] ?? 0);
        $soil = trim($data['soil_type'] ?? 'Loamy');
        $irrigation = trim($data['irrigation_type'] ?? 'Canal');
        $cropId = !empty($data['current_crop_id']) ? (int)$data['current_crop_id'] : null;

        if (empty($name) || $farmerId <= 0 || $area <= 0) {
            sendResponse("error", "Field name, valid farmer, and area are required", null, 400);
        }

        $stmt = $db->prepare("INSERT INTO fields (field_name, farmer_id, location, area_acres, soil_type, irrigation_type, current_crop_id) VALUES (:name, :fid, :loc, :area, :soil, :irrig, :cid)");
        $stmt->execute([
            ':name' => $name,
            ':fid' => $farmerId,
            ':loc' => $location,
            ':area' => $area,
            ':soil' => $soil,
            ':irrig' => $irrigation,
            ':cid' => $cropId
        ]);
        sendResponse("success", "Field registered successfully", ["id" => (int)$db->lastInsertId()], 201);
        break;

    case 'PUT':
        $data = getJsonInput();
        $id = (int)($data['id'] ?? $_GET['id'] ?? 0);
        if ($id <= 0) {
            sendResponse("error", "Valid Field ID is required", null, 400);
        }
        $stmt = $db->prepare("UPDATE fields SET field_name = :name, farmer_id = :fid, location = :loc, area_acres = :area, soil_type = :soil, irrigation_type = :irrig, current_crop_id = :cid WHERE id = :id");
        $stmt->execute([
            ':name' => trim($data['field_name'] ?? ''),
            ':fid' => (int)($data['farmer_id'] ?? 1),
            ':loc' => trim($data['location'] ?? ''),
            ':area' => (float)($data['area_acres'] ?? 1),
            ':soil' => trim($data['soil_type'] ?? 'Loamy'),
            ':irrig' => trim($data['irrigation_type'] ?? 'Canal'),
            ':cid' => !empty($data['current_crop_id']) ? (int)$data['current_crop_id'] : null,
            ':id' => $id
        ]);
        sendResponse("success", "Field updated successfully");
        break;

    case 'DELETE':
        $id = (int)($_GET['id'] ?? getJsonInput()['id'] ?? 0);
        if ($id <= 0) {
            sendResponse("error", "Valid Field ID is required", null, 400);
        }
        $stmt = $db->prepare("DELETE FROM fields WHERE id = :id");
        $stmt->execute([':id' => $id]);
        sendResponse("success", "Field deleted successfully");
        break;

    default:
        sendResponse("error", "Method not supported", null, 405);
}

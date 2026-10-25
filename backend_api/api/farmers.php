<?php
require_once __DIR__ . '/../config/database.php';

$database = new Database();
$db = $database->getConnection();
$method = $_SERVER['REQUEST_METHOD'];

switch ($method) {
    case 'GET':
        if (isset($_GET['id'])) {
            $stmt = $db->prepare("SELECT f.*, u.full_name AS user_account_name FROM farmers f LEFT JOIN users u ON f.user_id = u.id WHERE f.id = :id");
            $stmt->execute([':id' => $_GET['id']]);
            $farmer = $stmt->fetch();
            if ($farmer) {
                sendResponse("success", "Farmer retrieved successfully", $farmer);
            } else {
                sendResponse("error", "Farmer not found", null, 404);
            }
        } else {
            $search = trim($_GET['search'] ?? '');
            $sql = "SELECT f.*, (SELECT COUNT(*) FROM fields WHERE farmer_id = f.id) AS field_count FROM farmers f WHERE 1=1";
            $params = [];

            if (!empty($search)) {
                $sql .= " AND (f.name LIKE :search OR f.phone LIKE :search OR f.address LIKE :search)";
                $params[':search'] = "%$search%";
            }
            $sql .= " ORDER BY f.name ASC";
            $stmt = $db->prepare($sql);
            $stmt->execute($params);
            $farmers = $stmt->fetchAll();
            sendResponse("success", "Farmers loaded", $farmers);
        }
        break;

    case 'POST':
        $data = getJsonInput();
        $name = trim($data['name'] ?? '');
        $phone = trim($data['phone'] ?? '');
        $email = trim($data['email'] ?? '');
        $address = trim($data['address'] ?? '');
        $farmSize = (float)($data['farm_size_acres'] ?? 0.0);
        $experience = (int)($data['experience_years'] ?? 1);
        $regDate = !empty($data['registered_date']) ? $data['registered_date'] : date('Y-m-d');
        $userId = !empty($data['user_id']) ? (int)$data['user_id'] : null;

        if (empty($name) || empty($phone)) {
            sendResponse("error", "Farmer name and phone number are required", null, 400);
        }

        $stmt = $db->prepare("INSERT INTO farmers (user_id, name, phone, email, address, farm_size_acres, experience_years, registered_date) VALUES (:uid, :name, :phone, :email, :addr, :size, :exp, :reg)");
        $stmt->execute([
            ':uid' => $userId,
            ':name' => $name,
            ':phone' => $phone,
            ':email' => $email,
            ':addr' => $address,
            ':size' => $farmSize,
            ':exp' => $experience,
            ':reg' => $regDate
        ]);
        sendResponse("success", "Farmer added successfully", ["id" => (int)$db->lastInsertId()], 201);
        break;

    case 'PUT':
        $data = getJsonInput();
        $id = (int)($data['id'] ?? $_GET['id'] ?? 0);
        if ($id <= 0) {
            sendResponse("error", "Valid Farmer ID is required", null, 400);
        }

        $stmt = $db->prepare("UPDATE farmers SET name = :name, phone = :phone, email = :email, address = :addr, farm_size_acres = :size, experience_years = :exp WHERE id = :id");
        $stmt->execute([
            ':name' => trim($data['name'] ?? ''),
            ':phone' => trim($data['phone'] ?? ''),
            ':email' => trim($data['email'] ?? ''),
            ':addr' => trim($data['address'] ?? ''),
            ':size' => (float)($data['farm_size_acres'] ?? 0),
            ':exp' => (int)($data['experience_years'] ?? 1),
            ':id' => $id
        ]);
        sendResponse("success", "Farmer details updated successfully");
        break;

    case 'DELETE':
        $id = (int)($_GET['id'] ?? getJsonInput()['id'] ?? 0);
        if ($id <= 0) {
            sendResponse("error", "Valid Farmer ID is required", null, 400);
        }
        $stmt = $db->prepare("DELETE FROM farmers WHERE id = :id");
        $stmt->execute([':id' => $id]);
        sendResponse("success", "Farmer removed successfully");
        break;

    default:
        sendResponse("error", "Method not supported", null, 405);
}

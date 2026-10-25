<?php
require_once __DIR__ . '/../config/database.php';

$database = new Database();
$db = $database->getConnection();
$method = $_SERVER['REQUEST_METHOD'];

switch ($method) {
    case 'GET':
        $search = trim($_GET['search'] ?? '');
        $sql = "SELECT * FROM crop_diseases WHERE 1=1";
        $params = [];

        if (!empty($search)) {
            $sql .= " AND (disease_name LIKE :search OR affected_crops LIKE :search OR symptoms LIKE :search)";
            $params[':search'] = "%$search%";
        }
        $sql .= " ORDER BY disease_name ASC";
        $stmt = $db->prepare($sql);
        $stmt->execute($params);
        $diseases = $stmt->fetchAll();
        sendResponse("success", "Crop diseases loaded (General Agricultural Information)", $diseases);
        break;

    case 'POST':
        $data = getJsonInput();
        $name = trim($data['disease_name'] ?? '');
        $crops = trim($data['affected_crops'] ?? '');
        $symptoms = trim($data['symptoms'] ?? '');
        $prevention = trim($data['prevention'] ?? '');
        $treatment = trim($data['treatment_method'] ?? '');

        if (empty($name) || empty($symptoms)) {
            sendResponse("error", "Disease name and symptoms are required", null, 400);
        }

        $stmt = $db->prepare("INSERT INTO crop_diseases (disease_name, affected_crops, symptoms, prevention, treatment_method) VALUES (:name, :crops, :symptoms, :prev, :treat)");
        $stmt->execute([
            ':name' => $name,
            ':crops' => $crops,
            ':symptoms' => $symptoms,
            ':prev' => $prevention,
            ':treat' => $treatment
        ]);
        sendResponse("success", "Crop disease record added", ["id" => (int)$db->lastInsertId()], 201);
        break;

    default:
        sendResponse("error", "Method not supported", null, 405);
}

<?php
require_once __DIR__ . '/../config/database.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse("error", "Only POST method is accepted", null, 405);
}

$input = getJsonInput();
$fullName = trim($input['full_name'] ?? '');
$email = trim($input['email'] ?? '');
$phone = trim($input['phone'] ?? '');
$password = trim($input['password'] ?? '');
$userType = trim($input['user_type'] ?? 'Farmer');

if (empty($fullName) || empty($email) || empty($phone) || empty($password)) {
    sendResponse("error", "Full name, email, phone, and password are required", null, 400);
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    sendResponse("error", "Invalid email address format", null, 400);
}

$allowedTypes = ['Farmer', 'Crop Dealer', 'Agriculture Expert', 'Student'];
if (!in_array($userType, $allowedTypes)) {
    $userType = 'Farmer';
}

$database = new Database();
$db = $database->getConnection();

// Check if email already exists
$checkStmt = $db->prepare("SELECT id FROM users WHERE email = :email LIMIT 1");
$checkStmt->execute([':email' => $email]);
if ($checkStmt->fetch()) {
    sendResponse("error", "Email is already registered. Please log in or use another email.", null, 409);
}

// Hash password securely with bcrypt
$passwordHash = password_hash($password, PASSWORD_BCRYPT);

try {
    $insertStmt = $db->prepare("INSERT INTO users (full_name, email, phone, password_hash, user_type) VALUES (:full_name, :email, :phone, :hash, :type)");
    $insertStmt->execute([
        ':full_name' => $fullName,
        ':email' => $email,
        ':phone' => $phone,
        ':hash' => $passwordHash,
        ':type' => $userType
    ]);

    $newUserId = $db->lastInsertId();

    // If user is a farmer, also automatically initialize farmer profile
    if ($userType === 'Farmer') {
        $farmerStmt = $db->prepare("INSERT INTO farmers (user_id, name, phone, email, address, farm_size_acres, experience_years, registered_date) VALUES (:user_id, :name, :phone, :email, 'Local Farm', 1.0, 1, CURDATE())");
        $farmerStmt->execute([
            ':user_id' => $newUserId,
            ':name' => $fullName,
            ':phone' => $phone,
            ':email' => $email
        ]);
    }

    sendResponse("success", "Account created successfully", [
        "id" => (int)$newUserId,
        "full_name" => $fullName,
        "email" => $email,
        "phone" => $phone,
        "user_type" => $userType
    ], 201);
} catch (Exception $e) {
    sendResponse("error", "Failed to create user: " . $e->getMessage(), null, 500);
}

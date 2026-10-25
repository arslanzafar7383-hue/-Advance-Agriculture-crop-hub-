<?php
require_once __DIR__ . '/../config/database.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse("error", "Only POST method is accepted", null, 405);
}

$input = getJsonInput();
$identifier = trim($input['identifier'] ?? $input['email'] ?? '');
$password = trim($input['password'] ?? '');

if (empty($identifier) || empty($password)) {
    sendResponse("error", "Email/Username and Password are required", null, 400);
}

$database = new Database();
$db = $database->getConnection();

$stmt = $db->prepare("SELECT id, full_name, email, phone, password_hash, user_type, created_at FROM users WHERE email = :identifier OR phone = :identifier LIMIT 1");
$stmt->execute([':identifier' => $identifier]);
$user = $stmt->fetch();

if ($user && password_verify($password, $user['password_hash'])) {
    unset($user['password_hash']);
    sendResponse("success", "Login successful", $user);
} else {
    sendResponse("error", "Invalid credentials. Please check your email and password.", null, 401);
}

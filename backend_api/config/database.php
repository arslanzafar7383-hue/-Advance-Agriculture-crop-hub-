<?php
// =====================================================================
// Crop Hub Database Connection (PDO with MySQL)
// Default configuration is pre-configured for standard XAMPP installs
// =====================================================================

header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With");
header("Content-Type: application/json; charset=UTF-8");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

class Database {
    private $host = "127.0.0.1";
    private $port = "3306";
    private $db_name = "crop_hub_db";
    private $username = "root";
    private $password = ""; // Default XAMPP MySQL root password is empty
    public $conn = null;

    public function getConnection() {
        $this->conn = null;
        try {
            $dsn = "mysql:host=" . $this->host . ";port=" . $this->port . ";dbname=" . $this->db_name . ";charset=utf8mb4";
            $this->conn = new PDO($dsn, $this->username, $this->password, [
                PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                PDO::ATTR_EMULATE_PREPARES => false
            ]);
        } catch(PDOException $exception) {
            echo json_encode([
                "status" => "error",
                "message" => "Database connection error: " . $exception->getMessage()
            ]);
            exit();
        }
        return $this->conn;
    }
}

function sendResponse($status, $message, $data = null, $code = 200) {
    http_response_code($code);
    $response = [
        "status" => $status,
        "message" => $message
    ];
    if ($data !== null) {
        $response["data"] = $data;
    }
    echo json_encode($response, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
    exit();
}

function getJsonInput() {
    $raw = file_get_contents("php://input");
    $data = json_decode($raw, true);
    return is_array($data) ? $data : $_POST;
}

<?php
require_once __DIR__ . '/../config/database.php';

$database = new Database();
$db = $database->getConnection();
$method = $_SERVER['REQUEST_METHOD'];

switch ($method) {
    case 'GET':
        $search = trim($_GET['search'] ?? '');
        $category = trim($_GET['category'] ?? '');
        $sql = "SELECT * FROM marketplace_products WHERE 1=1";
        $params = [];

        if (!empty($search)) {
            $sql .= " AND (name LIKE :search OR description LIKE :search OR seller_name LIKE :search)";
            $params[':search'] = "%$search%";
        }
        if (!empty($category) && $category !== 'All') {
            $sql .= " AND category = :category";
            $params[':category'] = $category;
        }
        $sql .= " ORDER BY id DESC";
        $stmt = $db->prepare($sql);
        $stmt->execute($params);
        $products = $stmt->fetchAll();
        sendResponse("success", "Marketplace products loaded", $products);
        break;

    case 'POST':
        $data = getJsonInput();
        $name = trim($data['name'] ?? '');
        $category = trim($data['category'] ?? 'Seeds');
        $price = (float)($data['price'] ?? 0);
        $seller = trim($data['seller_name'] ?? '');
        $contact = trim($data['seller_contact'] ?? '');
        $desc = trim($data['description'] ?? '');
        $imageUrl = trim($data['image_url'] ?? 'product_default.png');
        $stock = (int)($data['stock_quantity'] ?? 10);

        if (empty($name) || $price <= 0 || empty($seller)) {
            sendResponse("error", "Product name, valid price, and seller name are required", null, 400);
        }

        $stmt = $db->prepare("INSERT INTO marketplace_products (name, category, price, seller_name, seller_contact, description, image_url, stock_quantity) VALUES (:name, :cat, :price, :seller, :contact, :desc, :img, :stock)");
        $stmt->execute([
            ':name' => $name,
            ':cat' => $category,
            ':price' => $price,
            ':seller' => $seller,
            ':contact' => $contact,
            ':desc' => $desc,
            ':img' => $imageUrl,
            ':stock' => $stock
        ]);
        sendResponse("success", "Marketplace product published", ["id" => (int)$db->lastInsertId()], 201);
        break;

    case 'DELETE':
        $id = (int)($_GET['id'] ?? getJsonInput()['id'] ?? 0);
        if ($id <= 0) {
            sendResponse("error", "Valid Product ID is required", null, 400);
        }
        $stmt = $db->prepare("DELETE FROM marketplace_products WHERE id = :id");
        $stmt->execute([':id' => $id]);
        sendResponse("success", "Product deleted successfully");
        break;

    default:
        sendResponse("error", "Method not supported", null, 405);
}

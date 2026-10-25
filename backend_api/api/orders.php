<?php
require_once __DIR__ . '/../config/database.php';

$database = new Database();
$db = $database->getConnection();
$method = $_SERVER['REQUEST_METHOD'];

switch ($method) {
    case 'GET':
        $userId = isset($_GET['user_id']) ? (int)$_GET['user_id'] : 0;
        $sql = "SELECT * FROM orders";
        if ($userId > 0) {
            $sql .= " WHERE user_id = :uid";
        }
        $sql .= " ORDER BY order_date DESC LIMIT 50";
        $stmt = $db->prepare($sql);
        if ($userId > 0) {
            $stmt->execute([':uid' => $userId]);
        } else {
            $stmt->execute();
        }
        $orders = $stmt->fetchAll();

        // Attach items for each order
        foreach ($orders as &$order) {
            $itemStmt = $db->prepare("SELECT * FROM order_items WHERE order_id = :oid");
            $itemStmt->execute([':oid' => $order['id']]);
            $order['items'] = $itemStmt->fetchAll();
        }
        sendResponse("success", "Orders loaded", $orders);
        break;

    case 'POST':
        $data = getJsonInput();
        $userId = !empty($data['user_id']) ? (int)$data['user_id'] : null;
        $customerName = trim($data['customer_name'] ?? '');
        $customerPhone = trim($data['customer_phone'] ?? '');
        $address = trim($data['delivery_address'] ?? '');
        $totalAmount = (float)($data['total_amount'] ?? 0);
        $items = $data['items'] ?? [];

        if (empty($customerName) || empty($customerPhone) || empty($address) || empty($items)) {
            sendResponse("error", "Customer name, phone, address, and cart items are required", null, 400);
        }

        try {
            $db->beginTransaction();

            $orderStmt = $db->prepare("INSERT INTO orders (user_id, customer_name, customer_phone, delivery_address, total_amount, status) VALUES (:uid, :cname, :cphone, :addr, :amt, 'Confirmed')");
            $orderStmt->execute([
                ':uid' => $userId,
                ':cname' => $customerName,
                ':cphone' => $customerPhone,
                ':addr' => $address,
                ':amt' => $totalAmount
            ]);
            $orderId = (int)$db->lastInsertId();

            $itemStmt = $db->prepare("INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price, subtotal) VALUES (:oid, :pid, :pname, :qty, :price, :sub)");
            foreach ($items as $item) {
                $pid = (int)($item['product_id'] ?? 0);
                $pname = trim($item['product_name'] ?? 'Product');
                $qty = (int)($item['quantity'] ?? 1);
                $price = (float)($item['unit_price'] ?? 0);
                $subtotal = $qty * $price;

                $itemStmt->execute([
                    ':oid' => $orderId,
                    ':pid' => $pid,
                    ':pname' => $pname,
                    ':qty' => $qty,
                    ':price' => $price,
                    ':sub' => $subtotal
                ]);

                // Reduce stock
                $stockStmt = $db->prepare("UPDATE marketplace_products SET stock_quantity = GREATEST(0, stock_quantity - :qty) WHERE id = :pid");
                $stockStmt->execute([':qty' => $qty, ':pid' => $pid]);
            }

            $db->commit();
            sendResponse("success", "Order placed successfully! Your order ID is #" . $orderId, [
                "order_id" => $orderId,
                "total_amount" => $totalAmount,
                "status" => "Confirmed"
            ], 201);
        } catch (Exception $e) {
            $db->rollBack();
            sendResponse("error", "Failed to place order: " . $e->getMessage(), null, 500);
        }
        break;

    default:
        sendResponse("error", "Method not supported", null, 405);
}

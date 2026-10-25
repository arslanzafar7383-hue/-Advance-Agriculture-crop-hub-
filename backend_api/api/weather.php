<?php
require_once __DIR__ . '/../config/database.php';

$database = new Database();
$db = $database->getConnection();
$method = $_SERVER['REQUEST_METHOD'];

switch ($method) {
    case 'GET':
        $location = trim($_GET['location'] ?? 'Central Farm Zone');
        $stmt = $db->prepare("SELECT * FROM weather_records WHERE location_name LIKE :loc ORDER BY recorded_at DESC LIMIT 1");
        $stmt->execute([':loc' => "%$location%"]);
        $record = $stmt->fetch();

        if (!$record) {
            // Default latest record
            $stmt = $db->query("SELECT * FROM weather_records ORDER BY id DESC LIMIT 1");
            $record = $stmt->fetch();
        }

        // Additional 5-day forecast cards
        $forecast = [
            ["day" => "Today", "temp_max" => 31, "temp_min" => 19, "condition" => "Sunny & Warm", "rain_chance" => 10],
            ["day" => "Tomorrow", "temp_max" => 29, "temp_min" => 18, "condition" => "Partly Cloudy", "rain_chance" => 25],
            ["day" => date('l', strtotime('+2 days')), "temp_max" => 27, "temp_min" => 17, "condition" => "Scattered Showers", "rain_chance" => 65],
            ["day" => date('l', strtotime('+3 days')), "temp_max" => 26, "temp_min" => 16, "condition" => "Clear Sky", "rain_chance" => 15],
            ["day" => date('l', strtotime('+4 days')), "temp_max" => 30, "temp_min" => 18, "condition" => "Sunny", "rain_chance" => 5]
        ];

        sendResponse("success", "Weather data retrieved", [
            "current" => $record,
            "forecast" => $forecast
        ]);
        break;

    case 'POST':
        $data = getJsonInput();
        $loc = trim($data['location_name'] ?? 'Farm Region');
        $temp = (float)($data['temperature_c'] ?? 25.0);
        $hum = (int)($data['humidity_pct'] ?? 50);
        $rain = (int)($data['rain_probability_pct'] ?? 0);
        $wind = (float)($data['wind_speed_kph'] ?? 10.0);
        $cond = trim($data['condition_text'] ?? 'Clear');

        $stmt = $db->prepare("INSERT INTO weather_records (location_name, temperature_c, humidity_pct, rain_probability_pct, wind_speed_kph, condition_text) VALUES (:loc, :temp, :hum, :rain, :wind, :cond)");
        $stmt->execute([
            ':loc' => $loc,
            ':temp' => $temp,
            ':hum' => $hum,
            ':rain' => $rain,
            ':wind' => $wind,
            ':cond' => $cond
        ]);
        sendResponse("success", "Weather record updated successfully");
        break;

    default:
        sendResponse("error", "Method not supported", null, 405);
}

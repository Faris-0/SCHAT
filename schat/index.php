<?php
$servername = "127.0.0.1";
$username = "root";
$password = "";
$databasename = "schat";

$conn = mysqli_connect($servername, $username, $password, $databasename);
if (!$conn) {
    die("Connection failed: " . mysqli_connect_error());
}

// Tabel
// CREATE TABLE `user` (
//     `id` int(11) NOT NULL,
//     `key` varchar(255) NOT NULL,
//     `tag` varchar(255) NOT NULL,
//     `name` varchar(255) NOT NULL,
//     `username` varchar(255) NOT NULL,
//     `password` varchar(255) NOT NULL,
//     `photo` varchar(255) NOT NULL
// ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

header("Content-Type: application/json");
$response = file_get_contents('php://input');
$data = json_decode($response, true);
if ($data == null) {
    die("Request not found!");
}

// Register
// {
//     "request" : "register",
//     "data" : {
//         "name" : "Faris",
//         "username" : "faris",
//         "password" : "faris"
//     }
// }
//
if ($data['request'] == "register") {
    $name = $data['data']['name'];
    $username = $data['data']['username'];
    $password = md5(md5($data['data']['password'], true));
    $key = md5(md5($username."+".$password, true));
    $query = mysqli_query($conn, "SELECT * FROM `user` WHERE `username`='$username'");
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => false, "message" => "Username has been taken!");
        echo json_encode($response);
    } else {
        $query = mysqli_query($conn, "INSERT INTO `user` (`key`, `name`, `username`, `password`) VALUES ('$key', '$name', '$username', '$password')");
        if (mysqli_affected_rows($conn)) {
            $response = array("status" => true, "key" => $key, "tag" => "", "name" => $name, "message" => "Register Success!");
            echo json_encode($response);
        } else {
            $response = array("status" => false, "message" => "Register Failed!");
            echo json_encode($response);
        }
    }
}

// Login
// {
//     "request" : "login",
//     "data" : {
//         "username" : "faris",
//         "password" : "faris"
//     }
// }
//
if ($data['request'] == "login") {
    $username = $data['data']['username'];
    $password = md5(md5($data['data']['password'], true));
    $query = mysqli_query($conn, "SELECT * FROM `user` WHERE `username`='$username' AND `password`='$password'");
    $object = mysqli_fetch_object($query);
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true, "key" => $object->key, "tag" => $object->tag, "name" => $object->name, "message" => "Login Success!");
        echo json_encode($response);
    } else {
        $response = array("status" => false, "message" => "Login Failed!");
        echo json_encode($response);
    }
}

$conn->close();
?>
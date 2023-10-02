<?php
$servername = "127.0.0.1";
$username = "root";
$password = "";
$databasename = "schat";

$conn = mysqli_connect($servername, $username, $password, $databasename);
if (!$conn) {
    die("Connection failed: " . mysqli_connect_error());
}

$response = file_get_contents('php://input');
$data = json_decode($response, true);

header("Content-Type: application/json");
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
    $query = mysqli_query($conn, "INSERT INTO `user` (`id`, `tag`, `name`, `username`, `password`) VALUES (NULL, '', '$name', '$username', '$password')");
    if (mysqli_affected_rows($conn)) {
        $data = array("status" => true);
        echo json_encode($data);
    } else {
        $data = array("status" => false);
        echo json_encode($data);
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
        $data = array("status" => true, "tag" => $object->tag, "name" => $object->name);
        echo json_encode($data);
    } else {
        $data = array("status" => false);
        echo json_encode($data);
    }
}

$conn->close();
?>
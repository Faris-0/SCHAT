<?php
$servername = "127.0.0.1";
$username = "root";
$password = "";
$databasename = "schat";

$conn = mysqli_connect($servername, $username, $password, $databasename);
if (!$conn) {
    die("Connection failed: " . mysqli_connect_error());
}

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

// Add Contact
// {
//     "request" : "add_contact",
//     "data" : {
//         "key" : "bc8b38b75a0785bf26f8e0d3508c82fe",
//         "tag" : "29142679597168f3ede4bec2c6a61013"
//     }
// }
//
if ($data['request'] == "add_contact") {
    $key = $data['data']['key'];
    $tag = $data['data']['tag'];
    $query = mysqli_query($conn, "SELECT * FROM `user` WHERE `tag`='$tag'");
    if (mysqli_affected_rows($conn) == 0) {
        $response = array("status" => false, "message" => "Tag Not Found!");
        echo json_encode($response);
    } else {
        $query = mysqli_query($conn, "INSERT INTO `contact` (`key`, `tag`) VALUES ('$key', '$tag')");
        if (mysqli_affected_rows($conn)) {
            $response = array("status" => true, "message" => "Add Contact Success!");
            echo json_encode($response);
        } else {
            $response = array("status" => false, "message" => "Add Contact Failed!");
            echo json_encode($response);
        }
    }
}

// Profile
// {
//     "request" : "profile",
//     "data" : {
//         "key" : "bc8b38b75a0785bf26f8e0d3508c82fe"
//     }
// }
//
if ($data['request'] == "profile") {
    $key = $data['data']['key'];
    $query = mysqli_query($conn, "SELECT * FROM `user` WHERE `key`='$key'");
    $object = mysqli_fetch_object($query);
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true, "tag" => $object->tag, "name" => $object->name, "photo" => $object->photo, "bio" => $object->bio);
        echo json_encode($response);
    } else {
        $response = array("status" => false, "message" => "Data Not Found!");
        echo json_encode($response);
    }
}

$conn->close();
?>
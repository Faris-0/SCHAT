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
//         "name" : "",
//         "username" : "",
//         "password" : ""
//     }
// }
//
if ($data['request'] == "register") {
    $name = $data['data']['name'];
    $username = $data['data']['username'];
    $password = md5(md5($data['data']['password'], true));
    $time = time();
    $key = md5(md5($username."+".$password."+".$time, true));
    $query = mysqli_query($conn, "SELECT * FROM `user` WHERE `username`='$username'");
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => false, "message" => "Username has been taken!");
        echo json_encode($response);
    } else {
        $query = mysqli_query($conn, "INSERT INTO `user` (`key`, `name`, `username`, `password`, `date_created`) VALUES ('$key', '$name', '$username', '$password', '$time')");
        if (mysqli_affected_rows($conn)) {
            $response = array("status" => true, "key" => $key, "name" => $name, "message" => "Register Success!");
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
//         "username" : "",
//         "password" : ""
//     }
// }
//
if ($data['request'] == "login") {
    $username = $data['data']['username'];
    $password = md5(md5($data['data']['password'], true));
    $query = mysqli_query($conn, "SELECT * FROM `user` WHERE `username`='$username' AND `password`='$password'");
    $object = mysqli_fetch_object($query);
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true, "key" => $object->key, "name" => $object->name, "photo" => $object->photo, "message" => "Login Success!");
        echo json_encode($response);
    } else {
        $response = array("status" => false, "message" => "Login Failed!");
        echo json_encode($response);
    }
}

// Profile
// {
//     "request" : "profile",
//     "data" : {
//         "key" : ""
//     }
// }
//
if ($data['request'] == "profile") {
    $key = $data['data']['key'];
    $query = mysqli_query($conn, "SELECT * FROM `user` WHERE `key`='$key'");
    $object = mysqli_fetch_object($query);
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true, "name" => $object->name, "photo" => $object->photo, "bio" => $object->bio, "private" => $object->private);
        echo json_encode($response);
    } else {
        $response = array("status" => false, "message" => "Data Not Found!");
        echo json_encode($response);
    }
}

// Add Contact
// {
//     "request" : "add_contact",
//     "data" : {
//         "key" : "",
//         "username" : ""
//     }
// }
//
if ($data['request'] == "add_contact") {
    $key = $data['data']['key'];
    $username = $data['data']['username'];
    $query = mysqli_query($conn, "SELECT * FROM `user` WHERE `username`='$username'");
    if (mysqli_affected_rows($conn) == 0) {
        $response = array("status" => false, "message" => "Username Not Found!");
        echo json_encode($response);
    } else {
        $query = mysqli_query($conn, "SELECT * FROM `contact` WHERE `key`='$key' AND `username`='$username'");
        if (mysqli_affected_rows($conn)) {
            $response = array("status" => false, "message" => "Username Already Added!");
            echo json_encode($response);
        } else {
            $query = mysqli_query($conn, "INSERT INTO `contact` (`key`, `username`) VALUES ('$key', '$username')");
            if (mysqli_affected_rows($conn)) {
                $response = array("status" => true, "message" => "Add Contact Success!");
                echo json_encode($response);
            } else {
                $response = array("status" => false, "message" => "Add Contact Failed!");
                echo json_encode($response);
            }
        }
    }
}

// Contact
// {
//     "request" : "contact",
//     "data" : {
//         "key" : ""
//     }
// }
//
if ($data['request'] == "contact") {
    $key = $data['data']['key'];
    $query = mysqli_query($conn, "SELECT `user`.`name`, `user`.`username`, `user`.`photo` FROM `contact`, `user` WHERE `contact`.`username`=`user`.`username` AND `contact`.`key`='$key'");
    $rows = array();
    while($r = mysqli_fetch_assoc($query)) $rows[] = $r;
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true, "contacts" => $rows);
        echo json_encode($response);
    } else {
        $response = array("status" => true, "contacts" => $rows);
        echo json_encode($response);
    }
}

// Add Message
// {
//     "request" : "add_message",
//     "data" : {
//         "key" : "",
//         "username" : ""
//     }
// }
//
if ($data['request'] == "add_message") {
    $key = $data['data']['key'];
    $username = $data['data']['username'];
    $query = mysqli_query($conn, "SELECT * FROM `user` WHERE `username`='$username'");
    $object = mysqli_fetch_object($query);
    if (mysqli_affected_rows($conn)) {
        $id1 = md5(md5($key."+".$object->key, true));
        $query = mysqli_query($conn, "SELECT * FROM `message` WHERE `id`='$id1'");
        if (mysqli_affected_rows($conn)) {
            $response = array("status" => true, "id" => $id1);
            echo json_encode($response);
        } else {
            $id2 = md5(md5($object->key."+".$key, true));
            $query = mysqli_query($conn, "SELECT * FROM `message` WHERE `id`='$id2'");
            if (mysqli_affected_rows($conn)) {
                $response = array("status" => true, "id" => $id2);
                echo json_encode($response);
            } else {
                $query = mysqli_query($conn, "INSERT INTO `message` (`key`, `id`, `open`, `send`) VALUES ('$key', '$id1', '0', '0'), ('$object->key', '$id1', '0', '1')");
                if (mysqli_affected_rows($conn)) {
                    $response = array("status" => true, "id" => $id1);
                    echo json_encode($response);
                } else {
                    $response = array("status" => false);
                    echo json_encode($response);
                }
            }
        }
    }
}

// Message
// {
//     "request" : "message",
//     "data" : {
//         "key" : ""
//     }
// }
//
if ($data['request'] == "message") {
    $key = $data['data']['key'];
    $query = mysqli_query($conn, "SELECT `message`.`id`, `message`.`send` FROM `message` WHERE `message`.`key`='$key' AND `message`.`open`='1'");
    $rows = array();
    while($r = mysqli_fetch_assoc($query)) $rows[] = $r;
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true, "messages" => $rows);
        echo json_encode($response);
    } else {
        $response = array("status" => true, "messages" => $rows);
        echo json_encode($response);
    }
}

// Message Detail
// {
//     "request" : "message_detail",
//     "data" : {
//         "key" : "",
//         "id" : ""
//     }
// }
//
if ($data['request'] == "message_detail") {
    $key = $data['data']['key'];
    $id = $data['data']['id'];
    $query = mysqli_query($conn, "SELECT `user`.`name`, `user`.`username`, `user`.`photo`, `user`.`last_online`, `user`.`private` FROM `message`, `user` WHERE `message`.`key`=`user`.`key` AND `message`.`id`='$id' AND `message`.`key`!='$key'");
    $object = mysqli_fetch_object($query);
    if (mysqli_affected_rows($conn)) {
        $query = mysqli_query($conn, "SELECT * FROM `message_detail` WHERE `message_detail`.`id`='$id'");
        $rows = array();
        while($r = mysqli_fetch_assoc($query)) $rows[] = $r;
        if (mysqli_affected_rows($conn)) {
            $response = array("status" => true, "name" => $object->name, "username" => $object->username, "photo" => $object->photo, "last_online" => $object->last_online, "private" => $object->private, "last_send" => end($rows)['send'], "last_chat" => end($rows)['chat'], "last_time" => end($rows)['time'], "last_view" => end($rows)['view']);
            echo json_encode($response);
        } else {
            // Tambahan
            $response = array("status" => true, "name" => $object->name, "username" => $object->username, "photo" => $object->photo, "last_online" => $object->last_online, "private" => $object->private);
            echo json_encode($response);
        }
    }
}

// Sender
// {
//     "request" : "sender",
//     "data" : {
//         "key" : "",
//         "id" : ""
//     }
// }
//
if ($data['request'] == "sender") {
    $key = $data['data']['key'];
    $id = $data['data']['id'];
    $query = mysqli_query($conn, "SELECT `message`.`send` FROM `message` WHERE `message`.`id`='$id' AND `message`.`key`='$key'");
    $object = mysqli_fetch_object($query);
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true, "send" => $object->send);
        echo json_encode($response);
    } else {
        $response = array("status" => true, "send" => "");
        echo json_encode($response);
    }
}

// Chats
// {
//     "request" : "chats",
//     "data" : {
//         "id" : ""
//     }
// }
//
if ($data['request'] == "chats") {
    $id = $data['data']['id'];
    $query = mysqli_query($conn, "SELECT * FROM `message_detail` WHERE `message_detail`.`id`='$id'");
    $rows = array();
    while($r = mysqli_fetch_assoc($query)) $rows[] = $r;
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true, "chats" => $rows);
        echo json_encode($response);
    } else {
        $response = array("status" => true, "chats" => $rows);
        echo json_encode($response);
    }
}

// Send Chat
// {
//     "request" : "send_chat",
//     "data" : {
//         "id" : "",
//         "chat" : "",
//         "send" : ""
//     }
// }
//
if ($data['request'] == "send_chat") {
    $id = $data['data']['id'];
    $chat = $data['data']['chat'];
    $send = $data['data']['send'];
    $time = time();
    $query = mysqli_query($conn, "INSERT INTO `message_detail` (`id`, `chat`, `send`, `time`) VALUES ('$id', '$chat', '$send', '$time')");
    if (mysqli_affected_rows($conn)) {
        $query = mysqli_query($conn, "UPDATE `message` SET `open` = '1' WHERE `id` = '$id'");
        if (mysqli_affected_rows($conn)) {
            $response = array("status" => true);
            echo json_encode($response);
        } else {
            $response = array("status" => true);
            echo json_encode($response);
        }
    } else {
        $response = array("status" => false);
        echo json_encode($response);
    }
}

// Edit View
// {
//     "request" : "edit_view",
//     "data" : {
//         "id" : "",
//         "send" : ""
//     }
// }
//
if ($data['request'] == "edit_view") {
    $id = $data['data']['id'];
    $send = $data['data']['send'];
    $query = mysqli_query($conn, "UPDATE `message_detail` SET `view` = '1' WHERE `id` = '$id' AND `send` != '$send'");
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true);
        echo json_encode($response);
    } else {
        $response = array("status" => true);
        echo json_encode($response);
    }
}

// Delete Chat
// {
//     "request" : "delete_chat",
//     "data" : {
//         "id" : "",
//         "send" : "",
//         "time" : ""
//     }
// }
//
if ($data['request'] == "delete_chat") {
    $id = $data['data']['id'];
    $send = $data['data']['send'];
    $time = $data['data']['time'];
    $query = mysqli_query($conn, "DELETE FROM `message_detail` WHERE `id` = '$id' AND `send` = '$send' AND `time` = '$time'");
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true);
        echo json_encode($response);
    } else {
        $response = array("status" => true);
        echo json_encode($response);
    }
}

// Check Chat
// {
//     "request" : "check_chat",
//     "data" : {
//         "id" : "",
//         "send" : ""
//     }
// }
//
if ($data['request'] == "check_chat") {
    $id = $data['data']['id'];
    $send = $data['data']['send'];
    $query = mysqli_query($conn, "SELECT `user`.`name`, `user`.`photo`, `user`.`date_created` FROM `message`, `user` WHERE `message`.`key`=`user`.`key` AND `message`.`id`='$id'");
    $object = mysqli_fetch_object($query);
    if (mysqli_affected_rows($conn)) {
        $query = mysqli_query($conn, "SELECT `message_detail`.`chat`, `message_detail`.`time` FROM `message_detail` WHERE `message_detail`.`id`='$id' AND `message_detail`.`view`='0' AND `message_detail`.`send`!='$send'");
        $rows = array();
        while($r = mysqli_fetch_assoc($query)) $rows[] = $r;
        if (mysqli_affected_rows($conn)) {
            $response = array("status" => true, "name" => $object->name, "photo" => $object->photo, "date_created" => $object->date_created, "messages" => $rows);
            echo json_encode($response);
        } else {
            // Tambahan
            $response = array("status" => true, "name" => $object->name, "photo" => $object->photo, "date_created" => $object->date_created, "messages" => $rows);
            echo json_encode($response);
        }
    }
}

// Edit Name
// {
//     "request" : "edit_name",
//     "data" : {
//         "key" : "",
//         "name" : ""
//     }
// }
//
if ($data['request'] == "edit_name") {
    $key = $data['data']['key'];
    $name = $data['data']['name'];
    $query = mysqli_query($conn, "UPDATE `user` SET `name` = '$name' WHERE `key` = '$key'");
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true, "message" => "Edit Name Success!");
        echo json_encode($response);
    } else {
        $response = array("status" => false);
        echo json_encode($response);
    }
}

// Edit Bio
// {
//     "request" : "edit_bio",
//     "data" : {
//         "key" : "",
//         "bio" : ""
//     }
// }
//
if ($data['request'] == "edit_bio") {
    $key = $data['data']['key'];
    $bio = $data['data']['bio'];
    $query = mysqli_query($conn, "UPDATE `user` SET `bio` = '$bio' WHERE `key` = '$key'");
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true, "message" => "Edit Bio Success!");
        echo json_encode($response);
    } else {
        $response = array("status" => false);
        echo json_encode($response);
    }
}

// Edit Photo
// {
//     "request" : "edit_photo",
//     "data" : {
//         "key" : "",
//         "photo" : ""
//     }
// }
//
if ($data['request'] == "edit_photo") {
    $key = $data['data']['key'];
    $photo = $data['data']['photo'];
    $sphoto = 'SCHAT-' . $key . ".jpeg";
    $path = './photo/' . $sphoto;
    file_put_contents($path, base64_decode($photo));
    $query = mysqli_query($conn, "UPDATE `user` SET `photo` = '$sphoto' WHERE `key` = '$key'");
}

// Edit Last Online
// {
//     "request" : "edit_last_online",
//     "data" : {
//         "key" : "",
//         "last_online" : ""
//     }
// }
//
if ($data['request'] == "edit_last_online") {
    $key = $data['data']['key'];
    $last_online = $data['data']['last_online'];
    $query = mysqli_query($conn, "UPDATE `user` SET `last_online` = '$last_online' WHERE `key` = '$key'");
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true);
        echo json_encode($response);
    } else {
        $response = array("status" => false);
        echo json_encode($response);
    }
}

// Edit Private
// {
//     "request" : "edit_private",
//     "data" : {
//         "key" : "",
//         "edit_private" : ""
//     }
// }
//
if ($data['request'] == "edit_private") {
    $key = $data['data']['key'];
    $private = $data['data']['private'];
    $query = mysqli_query($conn, "UPDATE `user` SET `private` = '$private' WHERE `key` = '$key'");
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true);
        echo json_encode($response);
    } else {
        $response = array("status" => false);
        echo json_encode($response);
    }
}

$conn->close();
?>
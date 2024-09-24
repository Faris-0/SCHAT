<?php
mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);
header("Content-Type: application/json");

$servername = "127.0.0.1";
$username = "root";
$password = "";
$databasename = "schat";
$body = json_decode(file_get_contents('php://input'), true);
$req = $body['request'] ?? null;
$data = $body['data'] ?? null;

// Check connection
try {
    $conn = mysqli_connect($servername, $username, $password, $databasename);
} catch (mysqli_sql_exception $e) {
    $response = array("status" => false, "message" => mysqli_connect_error());
    die(json_encode($response));
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
if ($req == "register") {
    $name = $data['name'];
    $username = $data['username'];
    $password = md5(md5($data['password'], true));
    $time = time();
    $key = md5(md5($username . "+" . $password . "+" . $time, true));
    $query = mysqli_query($conn, "SELECT * FROM `user` WHERE `username` = '$username'");
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => false, "message" => "Username has been taken!");
        echo json_encode($response);
    } else {
        $query = mysqli_query($conn, "INSERT INTO `user` (`key`, `name`, `username`, `password`, `photo`, `bio`, `last_online`, `private`, `date_created`) VALUES ('$key', '$name', '$username', '$password', '', '', '$time', '0', '$time')");
        if (mysqli_affected_rows($conn)) {
            $response = array("status" => true, "key" => $key, "name" => $name, "message" => "Register Success!");
            echo json_encode($response);
        } else {
            $response = array("status" => false, "message" => "Register Failed!");
            echo json_encode($response);
        }
    }
} else

// Login
// {
//     "request" : "login",
//     "data" : {
//         "username" : "",
//         "password" : ""
//     }
// }
//
if ($req == "login") {
    $username = $data['username'];
    $password = md5(md5($data['password'], true));
    $query = mysqli_query($conn, "SELECT * FROM `user` WHERE `username` = '$username' AND `password` = '$password'");
    $object = mysqli_fetch_object($query);
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true, "key" => $object->key, "name" => $object->name, "photo" => $object->photo, "message" => "Login Success!");
        echo json_encode($response);
    } else {
        $response = array("status" => false, "message" => "Login Failed!");
        echo json_encode($response);
    }
} else

// Profile
// {
//     "request" : "profile",
//     "data" : {
//         "key" : ""
//     }
// }
//
if ($req == "profile") {
    $key = $data['key'];
    $query = mysqli_query($conn, "SELECT * FROM `user` WHERE `key` = '$key'");
    $object = mysqli_fetch_object($query);
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true, "name" => $object->name, "photo" => $object->photo, "bio" => $object->bio, "private" => $object->private);
        echo json_encode($response);
    } else {
        $response = array("status" => false, "message" => "Data Not Found!");
        echo json_encode($response);
    }
} else

// Add Contact
// {
//     "request" : "add_contact",
//     "data" : {
//         "key" : "",
//         "username" : ""
//     }
// }
//
if ($req == "add_contact") {
    $key = $data['key'];
    $username = $data['username'];
    $query = mysqli_query($conn, "SELECT * FROM `user` WHERE `username` = '$username'");
    if (mysqli_affected_rows($conn) == 0) {
        $response = array("status" => false, "message" => "Username Not Found!");
        echo json_encode($response);
    } else {
        $query = mysqli_query($conn, "SELECT * FROM `contact` WHERE `key` = '$key' AND `username` = '$username'");
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
} else

// Contact
// {
//     "request" : "contact",
//     "data" : {
//         "key" : ""
//     }
// }
//
if ($req == "contact") {
    $key = $data['key'];
    $query = mysqli_query($conn, "SELECT `user`.`name`, `user`.`username`, `user`.`photo` FROM `contact`, `user` WHERE `contact`.`username` = `user`.`username` AND `contact`.`key` = '$key'");
    $rows = array();
    while($r = mysqli_fetch_assoc($query)) $rows[] = $r;
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true, "contacts" => $rows);
        echo json_encode($response);
    } else {
        $response = array("status" => true, "contacts" => $rows);
        echo json_encode($response);
    }
} else

// Add Message
// {
//     "request" : "add_message",
//     "data" : {
//         "key" : "",
//         "username" : ""
//     }
// }
//
if ($req == "add_message") {
    $key = $data['key'];
    $username = $data['username'];
    $query = mysqli_query($conn, "SELECT * FROM `user` WHERE `username` = '$username'");
    $object = mysqli_fetch_object($query);
    if (mysqli_affected_rows($conn)) {
        $id1 = md5(md5($key . "+" . $object->key, true));
        $query = mysqli_query($conn, "SELECT * FROM `message` WHERE `id` = '$id1'");
        if (mysqli_affected_rows($conn)) {
            $response = array("status" => true, "id" => $id1);
            echo json_encode($response);
        } else {
            $id2 = md5(md5($object->key . "+" . $key, true));
            $query = mysqli_query($conn, "SELECT * FROM `message` WHERE `id` = '$id2'");
            if (mysqli_affected_rows($conn)) {
                $response = array("status" => true, "id" => $id2);
                echo json_encode($response);
            } else {
                $query = mysqli_query($conn, "INSERT INTO `message` (`key`, `id`, `open`, `send`, `time`) VALUES ('$key', '$id1', '0', '0', '0'), ('$object->key', '$id1', '0', '1', '0')");
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
} else

// Message
// {
//     "request" : "message",
//     "data" : {
//         "key" : ""
//     }
// }
//
if ($req == "message") {
    $key = $data['key'];
    $query = mysqli_query($conn, "SELECT `id`, `send`, `time` FROM `message` WHERE `key` = '$key' AND `open` = '1'");
    $rows = array();
    while($r = mysqli_fetch_assoc($query)) $rows[] = $r;
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true, "messages" => $rows);
        echo json_encode($response);
    } else {
        $response = array("status" => true, "messages" => $rows);
        echo json_encode($response);
    }
} else

// Message Detail
// {
//     "request" : "message_detail",
//     "data" : {
//         "key" : "",
//         "id" : ""
//     }
// }
//
if ($req == "message_detail") {
    $key = $data['key'];
    $id = $data['id'];
    $query = mysqli_query($conn, "SELECT `user`.`name`, `user`.`username`, `user`.`photo`, `user`.`last_online`, `user`.`private` FROM `message`, `user` WHERE `message`.`key` = `user`.`key` AND `message`.`id` = '$id' AND `message`.`key` != '$key'");
    $object = mysqli_fetch_object($query);
    if (mysqli_affected_rows($conn)) {
        $query = mysqli_query($conn, "SELECT * FROM `message_detail` WHERE `id` = '$id'");
        $rows = array();
        while($r = mysqli_fetch_assoc($query)) $rows[] = $r;
        if (mysqli_affected_rows($conn)) {
            $response = array("status" => true, "name" => $object->name, "username" => $object->username, "photo" => $object->photo, "last_online" => $object->last_online, "private" => $object->private, "last_send" => end($rows)['send'], "last_chat" => end($rows)['chat'], "last_view" => end($rows)['view']);
            echo json_encode($response);
        } else {
            // Tambahan
            $response = array("status" => true, "name" => $object->name, "username" => $object->username, "photo" => $object->photo, "last_online" => $object->last_online, "private" => $object->private, "last_send" => 2, "last_chat" => "", "last_view" => 0);
            echo json_encode($response);
        }
    }
} else

// Sender
// {
//     "request" : "sender",
//     "data" : {
//         "key" : "",
//         "id" : ""
//     }
// }
//
if ($req == "sender") {
    $key = $data['key'];
    $id = $data['id'];
    $query = mysqli_query($conn, "SELECT `send` FROM `message` WHERE `id` = '$id' AND `key` = '$key'");
    $object = mysqli_fetch_object($query);
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true, "send" => $object->send);
        echo json_encode($response);
    } else {
        $response = array("status" => true, "send" => "");
        echo json_encode($response);
    }
} else

// Chats
// {
//     "request" : "chats",
//     "data" : {
//         "id" : ""
//     }
// }
//
if ($req == "chats") {
    $id = $data['id'];
    $query = mysqli_query($conn, "SELECT * FROM `message_detail` WHERE `id` = '$id'");
    $rows = array();
    while($r = mysqli_fetch_assoc($query)) $rows[] = $r;
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true, "chats" => $rows);
        echo json_encode($response);
    } else {
        $response = array("status" => true, "chats" => $rows);
        echo json_encode($response);
    }
} else

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
if ($req == "send_chat") {
    $id = $data['id'];
    $chat = rtrim($data['chat'], "\n");
    $send = $data['send'];
    $time = time();
    $query = mysqli_query($conn, "INSERT INTO `message_detail` (`id`, `chat`, `send`, `time`, `view`) VALUES ('$id', '$chat', '$send', '$time', '0')");
    if (mysqli_affected_rows($conn)) {
        $query = mysqli_query($conn, "UPDATE `message` SET `open` = '1', `time` = '$time' WHERE `id` = '$id'");
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
} else

// Edit View
// {
//     "request" : "edit_view",
//     "data" : {
//         "id" : "",
//         "send" : ""
//     }
// }
//
if ($req == "edit_view") {
    $id = $data['id'];
    $send = $data['send'];
    $query = mysqli_query($conn, "UPDATE `message_detail` SET `view` = '1' WHERE `id` = '$id' AND `send` != '$send'");
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true);
        echo json_encode($response);
    } else {
        $response = array("status" => true);
        echo json_encode($response);
    }
} else

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
if ($req == "delete_chat") {
    $id = $data['id'];
    $send = $data['send'];
    $time = $data['time'];
    $query = mysqli_query($conn, "DELETE FROM `message_detail` WHERE `id` = '$id' AND `send` = '$send' AND `time` = '$time'");
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true);
        echo json_encode($response);
    } else {
        $response = array("status" => true);
        echo json_encode($response);
    }
} else

// Check Chat
// {
//     "request" : "check_chat",
//     "data" : {
//         "key" : "",
//         "id" : "",
//         "send" : ""
//     }
// }
//
if ($req == "check_chat") {
    $key = $data['key'];
    $id = $data['id'];
    $send = $data['send'];
    $query = mysqli_query($conn, "SELECT `user`.`name`, `user`.`photo`, `user`.`date_created` FROM `message`, `user` WHERE `message`.`key` = `user`.`key` AND `user`.`key` != '$key' AND `message`.`id` = '$id'");
    $object = mysqli_fetch_object($query);
    if (mysqli_affected_rows($conn)) {
        $query = mysqli_query($conn, "SELECT `chat`, `time` FROM `message_detail` WHERE `id` = '$id' AND `view` = '0' AND `send` != '$send'");
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
} else

// Edit Name
// {
//     "request" : "edit_name",
//     "data" : {
//         "key" : "",
//         "name" : ""
//     }
// }
//
if ($req == "edit_name") {
    $key = $data['key'];
    $name = $data['name'];
    $query = mysqli_query($conn, "UPDATE `user` SET `name` = '$name' WHERE `key` = '$key'");
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true, "message" => "Edit Name Success!");
        echo json_encode($response);
    } else {
        $response = array("status" => false);
        echo json_encode($response);
    }
} else

// Edit Bio
// {
//     "request" : "edit_bio",
//     "data" : {
//         "key" : "",
//         "bio" : ""
//     }
// }
//
if ($req == "edit_bio") {
    $key = $data['key'];
    $bio = $data['bio'];
    $query = mysqli_query($conn, "UPDATE `user` SET `bio` = '$bio' WHERE `key` = '$key'");
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true, "message" => "Edit Bio Success!");
        echo json_encode($response);
    } else {
        $response = array("status" => false);
        echo json_encode($response);
    }
} else

// Edit Photo
// {
//     "request" : "edit_photo",
//     "data" : {
//         "key" : "",
//         "photo" : ""
//     }
// }
//
if ($req == "edit_photo") {
    $key = $data['key'];
    $photo = $data['photo'];
    $sphoto = "SCHAT-" . $key . ".jpeg";
    is_dir("photo") ? null : mkdir("photo");
    $path = "./photo/" . $sphoto;
    file_put_contents($path, base64_decode($photo));
    $query = mysqli_query($conn, "UPDATE `user` SET `photo` = '$sphoto' WHERE `key` = '$key'");
} else

// Edit Last Online
// {
//     "request" : "edit_last_online",
//     "data" : {
//         "key" : "",
//         "last_online" : ""
//     }
// }
//
if ($req == "edit_last_online") {
    $key = $data['key'];
    $last_online = $data['last_online'];
    $query = mysqli_query($conn, "UPDATE `user` SET `last_online` = '$last_online' WHERE `key` = '$key'");
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true);
        echo json_encode($response);
    } else {
        $response = array("status" => false);
        echo json_encode($response);
    }
} else

// Edit Private
// {
//     "request" : "edit_private",
//     "data" : {
//         "key" : "",
//         "edit_private" : ""
//     }
// }
//
if ($req == "edit_private") {
    $key = $data['key'];
    $private = $data['private'];
    $query = mysqli_query($conn, "UPDATE `user` SET `private` = '$private' WHERE `key` = '$key'");
    if (mysqli_affected_rows($conn)) {
        $response = array("status" => true);
        echo json_encode($response);
    } else {
        $response = array("status" => false);
        echo json_encode($response);
    }
} else

// Request not found!
{
    $response = array("status" => false, "message" => "Request not found!");
    die(json_encode($response));
}

$conn->close();
?>
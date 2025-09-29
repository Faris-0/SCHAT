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
if ($req === "register") {
    $name = trim($data['name']);
    $user = trim($data['username']);
    $pass = md5(md5($data['password'], true));
    $time = time();
    $key = md5(md5("$username+$password+$time", true));

    $stmt = mysqli_prepare($conn, "SELECT 1 FROM `user` WHERE `username` = ? LIMIT 1");
    mysqli_stmt_bind_param($stmt, "s", $user);
    mysqli_stmt_execute($stmt);
    mysqli_stmt_store_result($stmt);

    if (mysqli_stmt_num_rows($stmt)) {
        echo json_encode(["status" => false, "message" => "Username has been taken!"]);
        return;
    }
    mysqli_stmt_close($stmt);

    $stmt = mysqli_prepare($conn, "INSERT INTO user (`key`, `name`, `username`, `password`, `photo`, `bio`, `last_online`, `private`, `date_created`) VALUES (?, ?, ?, ?, '', '', ?, 0, ?)");
    mysqli_stmt_bind_param($stmt, "ssssii", $key, $name, $user, $pass, $time, $time);
    mysqli_stmt_execute($stmt);

    echo json_encode(mysqli_stmt_affected_rows($stmt) > 0 ? ["status" => true, "key" => $key, "name" => $name, "message" => "Register Success!"] : ["status" => false, "message" => "Register Failed!"]);
    mysqli_stmt_close($stmt);
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
if ($req === "login") {
    $user = trim($data['username']);
    $pass = md5(md5($data['password'], true));

    $stmt = mysqli_prepare($conn, "SELECT `key`, `name`, `photo` FROM `user` WHERE `username` = ? AND `password` = ? LIMIT 1");
    mysqli_stmt_bind_param($stmt, "ss", $user, $pass);
    mysqli_stmt_execute($stmt);
    $result = mysqli_stmt_get_result($stmt);
    $obj = mysqli_fetch_object($result);

    echo json_encode($obj ? ["status" => true, "key" => $obj->key, "name" => $obj->name, "photo" => $obj->photo, "message" => "Login Success!"] : ["status" => false, "message" => "Login Failed!"]);
    mysqli_stmt_close($stmt);
} else

// Profile
// {
//     "request" : "profile",
//     "data" : {
//         "key" : ""
//     }
// }
//
if ($req === "profile") {
    $key = trim($data['key']);

    $stmt = mysqli_prepare($conn, "SELECT `name`, `photo`, `bio`, `private` FROM `user` WHERE `key` = ? LIMIT 1");
    mysqli_stmt_bind_param($stmt, "s", $key);
    mysqli_stmt_execute($stmt);
    $result = mysqli_stmt_get_result($stmt);
    $obj = mysqli_fetch_object($result);

    echo json_encode($obj ? ["status" => true, "name" => $obj->name, "photo" => $obj->photo, "bio" => $obj->bio, "private" => $obj->private]
        : ["status" => false, "message" => "Data Not Found!"]);
    mysqli_stmt_close($stmt);
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
if ($req === "add_contact") {
    $key = trim($data['key']);
    $user = trim($data['username']);

    // Cek apakah username valid
    $stmt = mysqli_prepare($conn, "SELECT 1 FROM `user` WHERE `username` = ? LIMIT 1");
    mysqli_stmt_bind_param($stmt, "s", $user);
    mysqli_stmt_execute($stmt);
    mysqli_stmt_store_result($stmt);

    if (!mysqli_stmt_num_rows($stmt)) {
        echo json_encode(["status" => false, "message" => "Username Not Found!"]);
        mysqli_stmt_close($stmt);
        return;
    }
    mysqli_stmt_close($stmt);

    $stmt = mysqli_prepare($conn, "SELECT 1 FROM `contact` WHERE `key` = ? AND `username` = ? LIMIT 1");
    mysqli_stmt_bind_param($stmt, "ss", $key, $user);
    mysqli_stmt_execute($stmt);
    mysqli_stmt_store_result($stmt);

    if (mysqli_stmt_num_rows($stmt)) {
        echo json_encode(["status" => false, "message" => "Username Already Added!"]);
        mysqli_stmt_close($stmt);
        return;
    }
    mysqli_stmt_close($stmt);

    $stmt = mysqli_prepare($conn, "INSERT INTO contact (`key`, `username`) VALUES (?, ?)");
    mysqli_stmt_bind_param($stmt, "ss", $key, $user);
    $success = mysqli_stmt_execute($stmt);

    echo json_encode($success ? ["status" => true, "message" => "Add Contact Success!"] : ["status" => false, "message" => "Add Contact Failed!"]);
    mysqli_stmt_close($stmt);
} else

// Contact
// {
//     "request" : "contact",
//     "data" : {
//         "key" : ""
//     }
// }
//
if ($req === "contact") {
    $key = trim($data['key']);

    $stmt = mysqli_prepare($conn, "SELECT u.`name`, u.`username`, u.`photo` FROM `contact` c JOIN `user` u ON c.`username` = u.`username` WHERE c.`key` = ?");
    mysqli_stmt_bind_param($stmt, "s", $key);
    mysqli_stmt_execute($stmt);
    $result = mysqli_stmt_get_result($stmt);

    $contacts = [];
    while ($r = mysqli_fetch_assoc($result)) $contacts[] = $r;

    echo json_encode(["status" => true, "contacts" => $contacts]);
    mysqli_stmt_close($stmt);
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
if ($req === "add_message") {
    $key = trim($data['key']);
    $user = trim($data['username']);

    $stmt = mysqli_prepare($conn, "SELECT `key` FROM `user` WHERE `username` = ? LIMIT 1");
    mysqli_stmt_bind_param($stmt, "s", $user);
    mysqli_stmt_execute($stmt);
    $result = mysqli_stmt_get_result($stmt);
    $obj = mysqli_fetch_object($result);
    mysqli_stmt_close($stmt);

    if (!$obj) {
        echo json_encode(["status" => false]);
        return;
    }

    $id1 = md5(md5("$key+$obj->key", true));
    $id2 = md5(md5("$obj->key+$key", true));

    $stmt = mysqli_prepare($conn, "SELECT 1 FROM `message` WHERE `id` = ? LIMIT 1");
    mysqli_stmt_bind_param($stmt, "s", $id1);
    mysqli_stmt_execute($stmt);
    mysqli_stmt_store_result($stmt);

    if (mysqli_stmt_num_rows($stmt)) {
        echo json_encode(["status" => true, "id" => $id1]);
        mysqli_stmt_close($stmt);
        return;
    }
    mysqli_stmt_close($stmt);

    $stmt = mysqli_prepare($conn, "SELECT 1 FROM `message` WHERE `id` = ? LIMIT 1");
    mysqli_stmt_bind_param($stmt, "s", $id2);
    mysqli_stmt_execute($stmt);
    mysqli_stmt_store_result($stmt);

    if (mysqli_stmt_num_rows($stmt)) {
        echo json_encode(["status" => true, "id" => $id2]);
        mysqli_stmt_close($stmt);
        return;
    }
    mysqli_stmt_close($stmt);

    $stmt = mysqli_prepare($conn, "INSERT INTO message (`key`, `id`, `open`, `send`, `time`) VALUES (?, ?, 0, 0, 0), (?, ?, 0, 1, 0)");
    mysqli_stmt_bind_param($stmt, "ssss", $key, $id1, $obj->key, $id1);
    $success = mysqli_stmt_execute($stmt);
    mysqli_stmt_close($stmt);

    echo json_encode($success ? ["status" => true, "id" => $id1] : ["status" => false]);
} else

// Message
// {
//     "request" : "message",
//     "data" : {
//         "key" : ""
//     }
// }
//
if ($req === "message") {
    $key = trim($data['key']);

    $stmt = mysqli_prepare($conn, "SELECT `id`, `send`, `time` FROM `message` WHERE `key` = ? AND `open` = 1");
    mysqli_stmt_bind_param($stmt, "s", $key);
    mysqli_stmt_execute($stmt);
    $result = mysqli_stmt_get_result($stmt);

    $messages = [];
    while ($r = mysqli_fetch_assoc($result)) $messages[] = $r;

    echo json_encode(["status" => true, "messages" => $messages]);
    mysqli_stmt_close($stmt);
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
if ($req === "message_detail") {
    $key = trim($data['key']);
    $id = trim($data['id']);

    $stmt = mysqli_prepare($conn, "SELECT u.`name`, u.`username`, u.`photo`, u.`last_online`, u.`private` FROM `message` m JOIN `user` u ON m.`key` = u.`key` WHERE m.`id` = ? AND m.`key` != ? LIMIT 1");
    mysqli_stmt_bind_param($stmt, "ss", $id, $key);
    mysqli_stmt_execute($stmt);
    $result = mysqli_stmt_get_result($stmt);
    $obj = mysqli_fetch_object($result);
    mysqli_stmt_close($stmt);

    if (!$obj) return;

    $stmt = mysqli_prepare($conn, "SELECT `send`, `chat`, `view` FROM `message_detail` WHERE `id` = ?");
    mysqli_stmt_bind_param($stmt, "s", $id);
    mysqli_stmt_execute($stmt);
    $result = mysqli_stmt_get_result($stmt);

    $rows = [];
    while ($r = mysqli_fetch_assoc($result)) $rows[] = $r;
    mysqli_stmt_close($stmt);

    $last = end($rows) ?: ["send" => 2, "chat" => "", "view" => 0];

    echo json_encode(["status" => true, "name" => $obj->name, "username" => $obj->username, "photo" => $obj->photo, "last_online" => $obj->last_online, "private" => $obj->private, "last_send" => $last['send'], "last_chat" => $last['chat'], "last_view" => $last['view']]);
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
if ($req === "sender") {
    $key = trim($data['key']);
    $id = trim($data['id']);

    $stmt = mysqli_prepare($conn, "SELECT `send` FROM `message` WHERE `id` = ? AND `key` = ? LIMIT 1");
    mysqli_stmt_bind_param($stmt, "ss", $id, $key);
    mysqli_stmt_execute($stmt);
    $result = mysqli_stmt_get_result($stmt);
    $obj = mysqli_fetch_object($result);
    mysqli_stmt_close($stmt);

    echo json_encode(["status" => true, "send" => $obj ? $obj->send : ""]);
} else

// Chats
// {
//     "request" : "chats",
//     "data" : {
//         "id" : ""
//     }
// }
//
if ($req === "chats") {
    $id = trim($data['id']);

    $stmt = mysqli_prepare($conn, "SELECT * FROM `message_detail` WHERE `id` = ?");
    mysqli_stmt_bind_param($stmt, "s", $id);
    mysqli_stmt_execute($stmt);
    $result = mysqli_stmt_get_result($stmt);

    $chats = [];
    while ($r = mysqli_fetch_assoc($result)) $chats[] = $r;

    echo json_encode(["status" => true, "chats" => $chats]);
    mysqli_stmt_close($stmt);
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
if ($req === "send_chat") {
    $id = trim($data['id']);
    $chat = rtrim($data['chat'], "\n");
    $send = $data['send'];
    $time = time();

    $stmt = mysqli_prepare($conn, "INSERT INTO message_detail (`id`, `chat`, `send`, `time`, `view`) VALUES (?, ?, ?, ?, 0)");
    mysqli_stmt_bind_param($stmt, "ssii", $id, $chat, $send, $time);
    $success = mysqli_stmt_execute($stmt);
    mysqli_stmt_close($stmt);

    if (!$success) {
        echo json_encode(["status" => false]);
        return;
    }

    $stmt = mysqli_prepare($conn, "UPDATE message SET open=1, time=? WHERE id=?");
    mysqli_stmt_bind_param($stmt, "is", $time, $id);
    mysqli_stmt_execute($stmt);
    mysqli_stmt_close($stmt);

    echo json_encode(["status" => true]);
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
if ($req === "edit_view") {
    $id = trim($data['id']);
    $send = $data['send'];

    $stmt = mysqli_prepare($conn, "UPDATE message_detail SET view=1 WHERE id=? AND send!=?");
    mysqli_stmt_bind_param($stmt, "si", $id, $send);
    mysqli_stmt_execute($stmt);
    mysqli_stmt_close($stmt);

    echo json_encode(["status" => true]);
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
if ($req === "delete_chat") {
    $id = trim($data['id']);
    $send = $data['send'];
    $time = $data['time'];

    $stmt = mysqli_prepare($conn, "DELETE FROM `message_detail` WHERE `id` = ? AND `send` = ? AND `time` = ?");
    mysqli_stmt_bind_param($stmt, "sii", $id, $send, $time);
    mysqli_stmt_execute($stmt);
    mysqli_stmt_close($stmt);

    echo json_encode(["status" => true]);
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
if ($req === "check_chat") {
    $key = trim($data['key']);
    $id = trim($data['id']);
    $send = $data['send'];

    $stmt = mysqli_prepare($conn, "SELECT u.`name`, u.`photo`, u.`date_created` FROM `message` m JOIN `user` u ON m.`key` = u.`key` WHERE m.`id` = ? AND u.`key` != ? LIMIT 1");
    mysqli_stmt_bind_param($stmt, "ss", $id, $key);
    mysqli_stmt_execute($stmt);
    $result = mysqli_stmt_get_result($stmt);
    $obj = mysqli_fetch_object($result);
    mysqli_stmt_close($stmt);

    if (!$obj) return;

    $stmt = mysqli_prepare($conn, "SELECT `chat`, `time` FROM `message_detail` WHERE `id` = ? AND `view` = 0 AND `send` != ?");
    mysqli_stmt_bind_param($stmt, "si", $id, $send);
    mysqli_stmt_execute($stmt);
    $result = mysqli_stmt_get_result($stmt);

    $messages = [];
    while ($r = mysqli_fetch_assoc($result)) $messages[] = $r;
    mysqli_stmt_close($stmt);

    echo json_encode(["status" => true, "name" => $obj->name, "photo" => $obj->photo, "date_created" => $obj->date_created, "messages" => $messages]);
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
if ($req === "edit_name") {
    $key = trim($data['key']);
    $name = trim($data['name']);

    $stmt = mysqli_prepare($conn, "UPDATE user SET name=? WHERE `key`=?");
    mysqli_stmt_bind_param($stmt, "ss", $name, $key);
    mysqli_stmt_execute($stmt);

    echo json_encode(["status"  => mysqli_stmt_affected_rows($stmt) > 0, "message" => mysqli_stmt_affected_rows($stmt) > 0 ? "Edit Name Success!" : "Edit Name Failed!"]);
    mysqli_stmt_close($stmt);
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
if ($req === "edit_bio") {
    $key = trim($data['key']);
    $bio = trim($data['bio']);

    $stmt = mysqli_prepare($conn, "UPDATE user SET bio=? WHERE `key`=?");
    mysqli_stmt_bind_param($stmt, "ss", $bio, $key);
    mysqli_stmt_execute($stmt);

    echo json_encode(["status"  => mysqli_stmt_affected_rows($stmt) > 0, "message" => mysqli_stmt_affected_rows($stmt) > 0 ? "Edit Bio Success!" : "Edit Bio Failed!"]);
    mysqli_stmt_close($stmt);
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
if ($req === "edit_photo") {
    $key = trim($data['key']);
    $photo = trim($data['photo']);
    $name = "SCHAT-$key.jpeg";
    $dir = "photo";

    if (!is_dir($dir)) mkdir($dir);
    $path = "$dir/$name";

    file_put_contents($path, base64_decode($photo));

    $stmt = mysqli_prepare($conn, "UPDATE user SET photo=? WHERE `key`=?");
    mysqli_stmt_bind_param($stmt, "ss", $name, $key);
    mysqli_stmt_execute($stmt);
    mysqli_stmt_close($stmt);
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
if ($req === "edit_last_online") {
    $key = trim($data['key']);
    $last_online = (int) $data['last_online'];

    $stmt = mysqli_prepare($conn, "UPDATE user SET last_online=? WHERE `key`=?");
    mysqli_stmt_bind_param($stmt, "is", $last_online, $key);
    mysqli_stmt_execute($stmt);

    echo json_encode(["status" => mysqli_stmt_affected_rows($stmt) > 0]);
    mysqli_stmt_close($stmt);
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
if ($req === "edit_private") {
    $key = trim($data['key']);
    $private = (int) $data['private']; // pastikan boolean disimpan sebagai int

    $stmt = mysqli_prepare($conn, "UPDATE user SET private=? WHERE `key`=?");
    mysqli_stmt_bind_param($stmt, "is", $private, $key);
    mysqli_stmt_execute($stmt);

    echo json_encode(["status" => mysqli_stmt_affected_rows($stmt) > 0]);
    mysqli_stmt_close($stmt);
} else

// Request not found!
{
    $response = array("status" => false, "message" => "Request not found!");
    die(json_encode($response));
}

$conn->close();
?>
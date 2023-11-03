package com.yuuna.schat;

import static com.yuuna.schat.util.Client.BASE_PHOTO;
import static com.yuuna.schat.util.Client.BASE_URL;
import static com.yuuna.schat.util.SharedPref.SCHAT;
import static com.yuuna.schat.util.SharedPref.TAG_KEY;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yuuna.schat.adapter.ChatAdapter;
import com.yuuna.schat.util.Client;
import com.yuuna.schat.util.CustomLinearLayoutManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends Activity {
    
    private RecyclerView rvChats;

    private Context context;
    private Handler handler = new Handler();
    private Runnable refresh;

    private ChatAdapter chatAdapter;

    private ArrayList<JSONObject> jsonObjectArrayList;

    private String id, setKey;
    public static Integer send;
    private Boolean isBottom = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        context = ChatActivity.this;

        findViewById(R.id.cBack).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.cSend).setOnClickListener(v -> sendChat());

        rvChats = findViewById(R.id.cChats);

        rvChats.setLayoutManager(new CustomLinearLayoutManager(context));

        id = getIntent().getStringExtra("id");
        send = getIntent().getIntExtra("send", 0);
        setKey = getSharedPreferences(SCHAT, MODE_PRIVATE).getString(TAG_KEY, "");

        loadPhoto();

        refresh = () -> {
            loadProfile();
            loadChat();
            handler.postDelayed(refresh, 10000); // 1000 == 1sec
        };
        handler.post(refresh);
    }

    private void loadPhoto() {
        String message_detail = "{\"request\":\"message_detail\",\"data\":{\"key\":\""+setKey+"\",\"id\":\""+id+"\"}}";
        JsonObject jsonObject = JsonParser.parseString(message_detail).getAsJsonObject();
        try {
            new Client().getOkHttpClient(BASE_URL, String.valueOf(jsonObject), new Client.OKHttpNetwork() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        // Response
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("status")) {
                                CircleImageView civPhoto = findViewById(R.id.cPhoto);
                                String photo = BASE_PHOTO + jsonObject.getString("photo");
                                try {
                                    if (!photo.equals(BASE_PHOTO)) Glide.with(context)
                                            .load(photo)
                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                            .skipMemoryCache(true)
                                            .into(civPhoto);
                                    else civPhoto.setImageResource(R.drawable.photo);
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void onFailure(IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadChat() {
        String chats = "{\"request\":\"chats\",\"data\":{\"id\":\""+id+"\"}}";
        JsonObject jsonObject = JsonParser.parseString(chats).getAsJsonObject();
        try {
            new Client().getOkHttpClient(BASE_URL, String.valueOf(jsonObject), new Client.OKHttpNetwork() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        // Response
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("status")) {
                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                rvChats.getLayoutManager().onRestoreInstanceState(rvChats.getLayoutManager().onSaveInstanceState());
                                jsonObjectArrayList = new ArrayList<>();
                                for (int i = 0; i < jsonArray.length(); i++) jsonObjectArrayList.add(jsonArray.getJSONObject(i));
                                chatAdapter = new ChatAdapter(jsonObjectArrayList, context);
                                rvChats.setAdapter(chatAdapter);
//                                messageAdapter.setClickListener(ChatActivity.this);

                                // Auto Scroll to Bottom
                                if (!isBottom) if (jsonObjectArrayList.size() != 0) rvChats.scrollToPosition(jsonObjectArrayList.size() - 1);
                                isBottom = true;
                            } else Toast.makeText(context, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void onFailure(IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendChat() {
        EditText etChat = findViewById(R.id.cInputChat);
        String send_chat = "{\"request\":\"send_chat\",\"data\":{\"id\":\""+id+"\",\"chat\":\""+etChat.getText().toString()+"\",\"send\":\""+send+"\",\"time\":\""+(System.currentTimeMillis()/1000)+"\"}}";
        JsonObject jsonObject = JsonParser.parseString(send_chat).getAsJsonObject();
        try {
            new Client().getOkHttpClient(BASE_URL, String.valueOf(jsonObject), new Client.OKHttpNetwork() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        // Response
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("status")) {
                                etChat.setText("");
                                loadChat();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void onFailure(IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadProfile() {
        String message_detail = "{\"request\":\"message_detail\",\"data\":{\"key\":\""+setKey+"\",\"id\":\""+id+"\"}}";
        JsonObject jsonObject = JsonParser.parseString(message_detail).getAsJsonObject();
        try {
            new Client().getOkHttpClient(BASE_URL, String.valueOf(jsonObject), new Client.OKHttpNetwork() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        // Response
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("status")) {
                                TextView tvName = findViewById(R.id.cName);
                                tvName.setText(jsonObject.getString("name"));

                                TextView tvStatus = findViewById(R.id.cStatus);
                                Long last_online = jsonObject.getLong("last_online");
                                Integer iPrivate = jsonObject.getInt("private");
                                if (iPrivate == 1 || System.currentTimeMillis()/1000 > last_online + 10) {
                                    if (tvStatus.getText().toString().equals("Offline")) {
                                        tvStatus.setVisibility(View.GONE);
                                        tvStatus.setText("Offline");
                                    } else {
                                        tvStatus.setVisibility(View.VISIBLE);
                                        tvStatus.setText("Offline");
                                        tvStatus.postDelayed(() -> tvStatus.setVisibility(View.GONE), 10000);
                                    }
                                } else {
                                    tvStatus.setVisibility(View.VISIBLE);
                                    tvStatus.setText("Online");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void onFailure(IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(refresh);
    }
}

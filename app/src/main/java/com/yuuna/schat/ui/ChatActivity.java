package com.yuuna.schat.ui;

import static com.yuuna.schat.util.AppConstants.payload;
import static com.yuuna.schat.util.Client.BASE_PHOTO;
import static com.yuuna.schat.util.Client.BASE_URL;
import static com.yuuna.schat.util.AppConstants.SCHAT;
import static com.yuuna.schat.util.AppConstants.TAG_KEY;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yuuna.schat.R;
import com.yuuna.schat.adapter.ChatAdapter;
import com.yuuna.schat.util.Client;
import com.yuuna.schat.util.CustomLinearLayoutManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends Activity implements ChatAdapter.ItemClickListener {
    
    private RecyclerView rvChats;
    private LinearLayout llDown;

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

        llDown = findViewById(R.id.cDown);

        findViewById(R.id.cBack).setOnClickListener(v -> onBackPressed());
        llDown.setOnClickListener(v -> {
            if (!jsonObjectArrayList.isEmpty()) {
                rvChats.scrollToPosition(jsonObjectArrayList.size() - 1);
                llDown.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.cSend).setOnClickListener(v -> sendChat());

        rvChats = findViewById(R.id.cChats);

        rvChats.setLayoutManager(new CustomLinearLayoutManager(context));
        rvChats.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    llDown.setVisibility(!rvChats.canScrollVertically(1) ? View.GONE : View.VISIBLE);
                }
            }
        });

        id = getIntent().getStringExtra("id");
        send = getIntent().getIntExtra("send", 0);
    }

    private void setView() {
        JsonObject data = new JsonObject();
        data.addProperty("id", id);
        data.addProperty("send", send);
        new Client().getOkHttpClient(BASE_URL, payload("edit_view", data), new Client.OKHttpNetwork() {
            @Override
            public void onSuccess(String response) {

            }

            @Override
            public void onFailure(IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void loadPhoto() {
        JsonObject data = new JsonObject();
        data.addProperty("key", setKey);
        data.addProperty("id", id);
        new Client().getOkHttpClient(BASE_URL, payload("message_detail", data), new Client.OKHttpNetwork() {
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
    }

    private void loadChat() {
        JsonObject data = new JsonObject();
        data.addProperty("id", id);
        new Client().getOkHttpClient(BASE_URL, payload("chats", data), new Client.OKHttpNetwork() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    // Response
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("status")) {
                            JSONArray jsonArray = jsonObject.getJSONArray("chats");
                            rvChats.getLayoutManager().onRestoreInstanceState(rvChats.getLayoutManager().onSaveInstanceState());
                            jsonObjectArrayList = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) jsonObjectArrayList.add(jsonArray.getJSONObject(i));
                            chatAdapter = new ChatAdapter(jsonObjectArrayList);
                            rvChats.setAdapter(chatAdapter);
                            chatAdapter.setClickListener(ChatActivity.this);

                            // Auto Scroll to Bottom
                            if (!isBottom) {
                                if (!jsonObjectArrayList.isEmpty()) rvChats.scrollToPosition(jsonObjectArrayList.size() - 1);
                                llDown.setVisibility(View.GONE);
                            }
                            isBottom = true;
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
        // If you have a new chat, a scroll down will appear
        llDown.setVisibility(!rvChats.canScrollVertically(1) ? View.GONE : View.VISIBLE);
    }

    private void sendChat() {
        EditText etChat = findViewById(R.id.cInputChat);
        JsonObject data = new JsonObject();
        data.addProperty("id", id);
        data.addProperty("chat", etChat.getText().toString());
        data.addProperty("send", send);
        new Client().getOkHttpClient(BASE_URL, payload("send_chat", data), new Client.OKHttpNetwork() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    // Response
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("status")) {
                            etChat.setText("");
                            isBottom = false;
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
    }

    private void loadProfile() {
        JsonObject data = new JsonObject();
        data.addProperty("key", setKey);
        data.addProperty("id", id);
        new Client().getOkHttpClient(BASE_URL, payload("message_detail", data), new Client.OKHttpNetwork() {
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        setKey = getSharedPreferences(SCHAT, MODE_PRIVATE).getString(TAG_KEY, "");

        loadPhoto();
        refresh = () -> {
            loadProfile();
            loadChat();
            setView();
            handler.postDelayed(refresh, 10000); // 1000 == 1sec
        };
        handler.post(refresh);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(refresh);
    }

    @Override
    public void onItemClick(JSONObject jsonObject, View view) {
        try {
            Integer id = view.getId();
            if (id == R.id.cl1) {
                Log.d("SASASA", String.valueOf(jsonObject.getLong("time")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

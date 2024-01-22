package com.yuuna.schat.adapter;

import static com.yuuna.schat.ui.ChatActivity.send;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.yuuna.schat.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.Holder> implements Filterable {

    private ArrayList<JSONObject> jsonObjectDataList, listChat;
    private ItemClickListener clickListener;

    public ChatAdapter(ArrayList<JSONObject> jsonObjectArrayList) {
        this.jsonObjectDataList = jsonObjectArrayList;
        this.listChat = jsonObjectArrayList;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        try {
            holder.tvChat.setText(listChat.get(position).getString("chat"));
            Long lTime = listChat.get(position).getLong("time");
            String sTime = new SimpleDateFormat("HH:mm").format(new Date(lTime * 1000L));
            holder.tvTime.setText(sTime);
            if (listChat.get(position).getInt("view") == 0) holder.ivView.setImageResource(R.drawable.ic_check);
            else holder.ivView.setImageResource(R.drawable.ic_double_check);
            if (send == listChat.get(position).getInt("send")) {
                holder.ll1.setScaleX(1);
                holder.ll2.setScaleX(1);
                holder.ivView.setVisibility(View.VISIBLE);
            } else {
                holder.ll1.setScaleX(-1);
                holder.ll2.setScaleX(-1);
                holder.ivView.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return listChat.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String searchString = charSequence.toString().toLowerCase().trim();

                if (searchString.isEmpty()) listChat = jsonObjectDataList;
                else {
                    ArrayList<JSONObject> tempFilteredList = new ArrayList<>();
                    for (JSONObject jsonObject : jsonObjectDataList) {
                        try {
                            if (jsonObject.getString("chat").toLowerCase().contains(searchString)) tempFilteredList.add(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    listChat = tempFilteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = listChat;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                listChat = (ArrayList<JSONObject>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface ItemClickListener {
        void onItemClick(JSONObject jsonObject, View view);
    }

    public void setClickListener(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public class Holder extends RecyclerView.ViewHolder {
        private TextView tvChat, tvTime;
        private LinearLayout ll1, ll2;
        private ImageView ivView, iv3;

        public Holder(View itemView) {
            super(itemView);
            tvChat = itemView.findViewById(R.id.cChat);
            tvTime = itemView.findViewById(R.id.cTime);
            ll1 = itemView.findViewById(R.id.cl1);
            ll2 = itemView.findViewById(R.id.cl2);
            ivView = itemView.findViewById(R.id.cView);
            iv3 = itemView.findViewById(R.id.cl3);
            ll1.setOnLongClickListener(v -> {
                if (clickListener != null) clickListener.onItemClick(listChat.get(getBindingAdapterPosition()), v);
                return false;
            });
        }
    }
}

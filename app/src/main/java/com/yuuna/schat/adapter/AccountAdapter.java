package com.yuuna.schat.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.yuuna.schat.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.Holder> {

    private ArrayList<JSONObject> jsonObjectDataList;

    public AccountAdapter(ArrayList<JSONObject> jsonObjectArrayList) {
        this.jsonObjectDataList = jsonObjectArrayList;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        try {
            holder.tvName.setText(jsonObjectDataList.get(position).getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return jsonObjectDataList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private CircleImageView civPhoto;
        private TextView tvName;

        public Holder(View itemView) {
            super(itemView);
            civPhoto = itemView.findViewById(R.id.aPhoto);
            tvName = itemView.findViewById(R.id.aName);
            itemView.setOnClickListener(v -> {
                try {
                    Log.d("SASASA", jsonObjectDataList.get(getBindingAdapterPosition()).getString("key"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}

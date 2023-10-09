package com.yuuna.schat.adapter;

import static com.yuuna.schat.util.Client.BASE_PHOTO;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yuuna.schat.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.Holder> implements Filterable {

    private ArrayList<JSONObject> jsonObjectDataList, listContact;
    private Context mContext;
    private ItemClickListener clickListener;

    public ContactAdapter(ArrayList<JSONObject> jsonObjectArrayList2, Context context) {
        this.jsonObjectDataList = jsonObjectArrayList2;
        this.mContext = context;
        this.listContact = jsonObjectArrayList2;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        try {
            String photo = BASE_PHOTO + listContact.get(position).getString("photo");
            if (!photo.equals(BASE_PHOTO)) Glide.with(mContext)
                    .load(photo)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(holder.civPhoto);
            else holder.civPhoto.setImageResource(R.drawable.photo);
            holder.tvName.setText(listContact.get(position).getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return listContact.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String searchString = charSequence.toString().toLowerCase().trim();

                if (searchString.isEmpty()) listContact = jsonObjectDataList;
                else {
                    ArrayList<JSONObject> tempFilteredList = new ArrayList<>();
                    for (JSONObject jsonObject : jsonObjectDataList) {
                        try {
                            if (jsonObject.getString("name").toLowerCase().contains(searchString)) tempFilteredList.add(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    listContact = tempFilteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = listContact;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                listContact = (ArrayList<JSONObject>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface ItemClickListener {
        void onItemClick(JSONObject jsonObject);
    }

    public void setClickListener(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public class Holder extends RecyclerView.ViewHolder {
        private CircleImageView civPhoto;
        private TextView tvName;

        public Holder(View itemView) {
            super(itemView);
            civPhoto = itemView.findViewById(R.id.cPhoto);
            tvName = itemView.findViewById(R.id.cName);
            itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onItemClick(listContact.get(getBindingAdapterPosition()));
            });
        }
    }
}

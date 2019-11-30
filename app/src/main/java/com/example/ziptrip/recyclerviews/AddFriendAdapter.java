package com.example.ziptrip.recyclerviews;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ziptrip.R;

import java.util.ArrayList;

public class AddFriendAdapter extends RecyclerView.Adapter<AddFriendAdapter.AddFriendViewHolder> {
    private ArrayList<AddFriendItem> friendList;

    public static class AddFriendViewHolder extends RecyclerView.ViewHolder{
        public TextView fullName, username;
        public ImageButton deleteBtn;

        public AddFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            fullName = itemView.findViewById(R.id.fullNameTV);
            username = itemView.findViewById(R.id.usernameTv);
            deleteBtn = itemView.findViewById(R.id.deleteFriendBtn);
        }
    }

    public AddFriendAdapter(ArrayList<AddFriendItem> friendList){
        this.friendList = friendList;
    }

    @NonNull
    @Override
    public AddFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_friend_item, parent, false);
        AddFriendViewHolder addFriendVH = new AddFriendViewHolder(v);
        Log.i("Inflating view", "Inflating and returning view");
        return addFriendVH;
    }

    @Override
    public void onBindViewHolder(@NonNull AddFriendViewHolder holder, int position) {
        AddFriendItem currentItem = friendList.get(position);

        final AddFriendViewHolder tempHolder = holder;

        // Setting fields for added friends
        holder.fullName.setText(currentItem.getFname() + " " + currentItem.getLname());
        holder.username.setText(currentItem.getUsername());

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Delete from firebase here

                // Delete from view
                int newPosition = tempHolder.getAdapterPosition();
                friendList.remove(newPosition);
                notifyItemRemoved(newPosition);
                notifyItemRangeChanged(newPosition, friendList.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }
}

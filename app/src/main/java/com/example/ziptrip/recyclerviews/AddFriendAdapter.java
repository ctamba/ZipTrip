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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.core.Tag;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AddFriendAdapter extends RecyclerView.Adapter<AddFriendAdapter.AddFriendViewHolder> {
    private ArrayList<AddFriendItem> friendList;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

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
        final AddFriendItem currentItem = friendList.get(position);

        final AddFriendViewHolder tempHolder = holder;

        // Setting fields for added friends
        holder.fullName.setText(currentItem.getFname() + " " + currentItem.getLname());
        holder.username.setText(currentItem.getUsername());

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Delete from firebase here
                // Get list from firebase, remove item from list, update document in firebase
                Log.i("adapter", "trip you clicked on: " + currentItem.getTripId());
                db.collection("trips").document(currentItem.getTripId())
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            List<String> currentFriendList = (List<String>)task.getResult().get("friends");
                            if(currentFriendList.contains(currentItem.getUsername())){
                                currentFriendList.remove(currentFriendList.indexOf(currentItem.getUsername()));
                            }
                            // Update document
                            updateFriends(currentItem.getTripId(), currentFriendList);
                        }
                    }
                });

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

    private void updateFriends(String tripId, List<String> updatedFriendList){
        db.collection("trips").document(tripId).update("friends", updatedFriendList)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Friend adapter", "Friends was successfully updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Friend adapter", "Something went wrong when writing" + e);
                    }
                });
    }
}

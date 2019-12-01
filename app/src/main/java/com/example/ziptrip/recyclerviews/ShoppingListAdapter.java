package com.example.ziptrip.recyclerviews;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ziptrip.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ShoppingListViewHolder>{
    // Firebase objects
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ArrayList<ShoppingListItem> itemList;

    public static class ShoppingListViewHolder extends RecyclerView.ViewHolder{
        public TextView itemName, buyer, price;
        public ImageButton deleteBtn;

        public ShoppingListViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = (TextView)itemView.findViewById(R.id.itemNameTv);
            price = (TextView)itemView.findViewById(R.id.itemPriceTv);
            buyer = (TextView)itemView.findViewById(R.id.buyerTv);
            deleteBtn = (ImageButton)itemView.findViewById(R.id.deleteItemImgBtn);
        }
    }

    public ShoppingListAdapter(ArrayList<ShoppingListItem> itemList){
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ShoppingListAdapter.ShoppingListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.shopping_list_item, parent, false);
        ShoppingListAdapter.ShoppingListViewHolder itemVh = new ShoppingListAdapter.ShoppingListViewHolder(v);
        Log.i("Inflating view", "Inflating and returning shopping list view");
        return itemVh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ShoppingListAdapter.ShoppingListViewHolder holder, int position) {
        final ShoppingListItem currentItem = itemList.get(position);

        // Setting fields for added friends
        holder.itemName.setText(currentItem.getItemName());
        holder.buyer.setText("Bought by: " + currentItem.getBuyer());

        Double price = Double.valueOf(currentItem.getPrice());
        String roundedPrice = String.format("$%.2f", price);
        holder.price.setText(roundedPrice);

        final ShoppingListViewHolder tempHolder = holder;

        // Handle when delete button is pressed
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Delete from firebase
                //db.collection("trips").document(currentItem.getTripId());
                Log.i("adapter", "trip you clicked on: " + currentItem.getTripId());
                db.collection("trips").document(currentItem.getTripId()).collection("shoppinglist")
                        .document(currentItem.getItemName()).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("Adapter", "Deletion was successful");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("Adapter", "Error occured with deletion");
                            }
                        });

                // Delete item from recycler view
                int newPosition = tempHolder.getAdapterPosition();
                itemList.remove(newPosition);
                notifyItemRemoved(newPosition);
                notifyItemRangeChanged(newPosition, itemList.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}

package com.example.ziptrip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.ziptrip.recyclerviews.RetrieveTripItem;
import com.example.ziptrip.recyclerviews.RetrieveTripsAdapter;
import com.example.ziptrip.recyclerviews.ShoppingListAdapter;
import com.example.ziptrip.recyclerviews.ShoppingListItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShoppingListActivity extends AppCompatActivity {
    // Firebase components
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "Shopping list";

    // Recycler View components
    private RecyclerView sView;
    private RecyclerView.Adapter sAdapter;
    private RecyclerView.LayoutManager sManager;
    ArrayList<ShoppingListItem> itemList = new ArrayList<>();

    // Activity components
    TextView totalBillTv, noItemsTv;
    EditText itemNameInput, itemPriceInput;
    Button addItemBtn;
    Intent shoppingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        // Create and load view components
        totalBillTv = (TextView)findViewById(R.id.totalBillTv);
        noItemsTv = (TextView)findViewById(R.id.noItemsTv);
        itemNameInput = (EditText)findViewById(R.id.itemEditText);
        itemPriceInput = (EditText)findViewById(R.id.priceEditText);
        addItemBtn = (Button)findViewById(R.id.addItemBtn);
        shoppingIntent = getIntent();
        sView = findViewById(R.id.shoppingListRecyclerView);
        sManager = new LinearLayoutManager(ShoppingListActivity.this);


        // Add item when button is clicked
        addItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "About to add item " + itemNameInput.getText().toString());
                addItem(itemNameInput.getText().toString(), itemPriceInput.getText().toString());

                // reset edit text fields
                itemNameInput.setText("");
                itemPriceInput.setText("");
            }
        });

        // Load and change data when data changes
        CollectionReference shoppingRef = db.collection("trips").document(shoppingIntent.getStringExtra("tripId"))
                                        .collection("shoppinglist");
        shoppingRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (queryDocumentSnapshots != null){
                    Log.i(TAG, "Change on collection detected");

                    // Reset recycler view
                    itemList.clear();
                    sAdapter = new ShoppingListAdapter(itemList);
                    sView.setLayoutManager(sManager);
                    sView.setAdapter(sAdapter);

                    // Method to load all items here
                    Log.i(TAG, "About to call loadItems()");
                    loadItems();

                    // Load bill total
                    getBillTotal();
                }
            }
        });
    }

    private void loadItems(){
        // Get items from trip
        db.collection("trips").document(shoppingIntent.getStringExtra("tripId")).collection("shoppinglist")
            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task != null){
                        noItemsTv.setVisibility(noItemsTv.INVISIBLE);
                        List<DocumentSnapshot> tripItemList = task.getResult().getDocuments();

                        // For every item in the collection, append a view to the recycler view
                        for(DocumentSnapshot itemDoc : tripItemList){
                            Log.i(TAG, itemDoc.getId());
                            retrieveShoppingItem(itemDoc.getId());
                        }
                    }
                    else{
                        noItemsTv.setVisibility(noItemsTv.VISIBLE);
                    }
                }
            }
        });
        // If there are no items, append "No items" text
    }

    private void retrieveShoppingItem(String itemId){
        // Get item and create a cardview
        db.collection("trips").document(shoppingIntent.getStringExtra("tripId")).collection("shoppinglist")
                    .document(itemId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                String itemName = task.getResult().get("itemname").toString();
                String buyer = task.getResult().get("buyer").toString();
                String price = task.getResult().get("price").toString();

                // Create recycler items
                ShoppingListItem item = new ShoppingListItem(itemName, buyer, price, shoppingIntent.getStringExtra("tripId"));
                itemList.add(item);

                // Update the recycler view here by adding all items to adapter
                sAdapter = new ShoppingListAdapter(itemList);
                sView.setLayoutManager(sManager);
                sView.setAdapter(sAdapter);
            }
        });
    }

    private void addItem(String itemName, String price){
        Log.i(TAG, "Current user: " + shoppingIntent.getStringExtra("username") + " Current trip: " + shoppingIntent.getStringExtra("tripId"));
        // Create hashmap
        Map<String, Object> itemToAdd = new HashMap<>();
        itemToAdd.put("itemname", itemName);
        itemToAdd.put("buyer", shoppingIntent.getStringExtra("username"));
        itemToAdd.put("price", price);

        Log.i(TAG, "current map: " + itemToAdd);

        // Add item to the database (view will update with listener in onClick)
        db.collection("trips").document(shoppingIntent.getStringExtra("tripId")).collection("shoppinglist")
                .document(itemName).set(itemToAdd)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    private void getBillTotal(){
        // get collection items
        db.collection("trips").document(shoppingIntent.getStringExtra("tripId")).collection("shoppinglist")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    Double totalPrice = 0.00;
                    List<DocumentSnapshot> itemList = task.getResult().getDocuments();
                    for(DocumentSnapshot item : itemList){
                        totalPrice += Double.valueOf(item.get("price").toString());
                    }
                    String roundedPrice = String.format("$%.2f", totalPrice);
                    totalBillTv.setText("Bill total: " + roundedPrice);
                }
            }
        });
    }
}

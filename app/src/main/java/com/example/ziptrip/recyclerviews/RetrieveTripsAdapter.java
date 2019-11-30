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

public class RetrieveTripsAdapter extends RecyclerView.Adapter<RetrieveTripsAdapter.RetrieveTripsViewHolder> {
    private ArrayList<RetrieveTripItem> tripList;

    public static class RetrieveTripsViewHolder extends RecyclerView.ViewHolder{
        public TextView tripName, tripDestination, attendees;

        public RetrieveTripsViewHolder(@NonNull View itemView) {
            super(itemView);
            tripName = (TextView)itemView.findViewById(R.id.tripNameTv);
            tripDestination = (TextView)itemView.findViewById(R.id.destinationTv);
            attendees = (TextView)itemView.findViewById(R.id.tripAttendeesTv);
        }
    }

    public RetrieveTripsAdapter(ArrayList<RetrieveTripItem> tripList){
        this.tripList = tripList;
    }

    @NonNull
    @Override
    public RetrieveTripsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.get_trip_item, parent, false);
        RetrieveTripsViewHolder tripVH = new RetrieveTripsViewHolder(v);
        Log.i("Inflating view", "Inflating and returning view");
        return tripVH;
    }

    @Override
    public void onBindViewHolder(@NonNull RetrieveTripsViewHolder holder, int position) {
        RetrieveTripItem currentItem = tripList.get(position);

        // Setting fields for added friends
        holder.tripName.setText(currentItem.getTripName());
        holder.tripDestination.setText(currentItem.getTripDestination());

        // Turn the list of attendees into a string format
        StringBuilder friendString = new StringBuilder();
        for(String friend : currentItem.getAttendees()){
            friendString.append(friend + "\n");
        }
        holder.attendees.setText(friendString.toString());
    }

    @Override
    public int getItemCount() {
        return tripList.size();
    }
}
package com.s23010421.busmate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * BusArrivalAdapter - RecyclerView adapter for bus arrival information
 */
public class BusArrivalAdapter extends RecyclerView.Adapter<BusArrivalAdapter.ViewHolder> {

    private List<BusArrivalInterfaceActivity.BusArrival> arrivals;
    private OnArrivalClickListener clickListener;

    public interface OnArrivalClickListener {
        void onArrivalSelected(BusArrivalInterfaceActivity.BusArrival arrival);
    }

    public BusArrivalAdapter(List<BusArrivalInterfaceActivity.BusArrival> arrivals, OnArrivalClickListener listener) {
        this.arrivals = arrivals;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bus_arrival, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BusArrivalInterfaceActivity.BusArrival arrival = arrivals.get(position);
        holder.bind(arrival);
    }

    @Override
    public int getItemCount() {
        return arrivals.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView routeNumber, destination, busNumber, capacity;
        Button trackButton;

        ViewHolder(View itemView) {
            super(itemView);
            routeNumber = itemView.findViewById(R.id.textViewRouteNumber);
            destination = itemView.findViewById(R.id.textViewDestination);
            busNumber = itemView.findViewById(R.id.textViewBusNumber);
            capacity = itemView.findViewById(R.id.textViewCapacity);
            trackButton = itemView.findViewById(R.id.buttonTrackBus);
        }

        void bind(BusArrivalInterfaceActivity.BusArrival arrival) {
            routeNumber.setText("Route " + arrival.getRouteNumber());
            destination.setText(arrival.getDestination());
            busNumber.setText(arrival.getBusNumber());
            capacity.setText(arrival.getCapacity() + "% Full");

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onArrivalSelected(arrival);
                }
            });

            trackButton.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onArrivalSelected(arrival);
                }
            });
        }
    }
}

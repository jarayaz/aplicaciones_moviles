package jair.araya.verduritassa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class HarvestAdapter extends RecyclerView.Adapter<HarvestAdapter.HarvestViewHolder> {
    private ArrayList<Harvest> harvests;
    private OnHarvestClickListener listener;

    public interface OnHarvestClickListener {
        void onMenuClick(View view, Harvest harvest);
    }

    public HarvestAdapter(ArrayList<Harvest> harvests, OnHarvestClickListener listener) {
        this.harvests = harvests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HarvestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_harvest, parent, false);
        return new HarvestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HarvestViewHolder holder, int position) {
        Harvest harvest = harvests.get(position);
        holder.cropName.setText(harvest.getCropName());
        holder.harvestDate.setText(harvest.getHarvestDate());
        holder.menuButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMenuClick(holder.menuButton, harvest);
            }
        });
    }

    @Override
    public int getItemCount() {
        return harvests.size();
    }

    public void removeHarvest(Harvest harvest) {
        int position = harvests.indexOf(harvest);
        if (position != -1) {
            harvests.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class HarvestViewHolder extends RecyclerView.ViewHolder {
        TextView cropName;
        TextView harvestDate;
        ImageButton menuButton;

        HarvestViewHolder(View itemView) {
            super(itemView);
            cropName = itemView.findViewById(R.id.cropName);
            harvestDate = itemView.findViewById(R.id.harvestDate);
            menuButton = itemView.findViewById(R.id.menuButton);
        }
    }
}
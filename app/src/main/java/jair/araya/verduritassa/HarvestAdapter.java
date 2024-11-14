package jair.araya.verduritassa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HarvestAdapter extends RecyclerView.Adapter<HarvestAdapter.HarvestViewHolder> {
    private List<Harvest> harvests;
    private OnHarvestClickListener listener;

    public interface OnHarvestClickListener {
        void onMenuClick(View view, Harvest harvest);
    }

    public HarvestAdapter(List<Harvest> harvests, OnHarvestClickListener listener) {
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
        holder.bind(harvests.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return harvests != null ? harvests.size() : 0;
    }

    public void updateHarvests(List<Harvest> newHarvests) {
        this.harvests = newHarvests;
        notifyDataSetChanged();
    }

    public void removeHarvest(Harvest harvest) {
        int position = harvests.indexOf(harvest);
        if (position != -1) {
            harvests.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class HarvestViewHolder extends RecyclerView.ViewHolder {
        private final TextView cropName;
        private final TextView harvestDate;
        private final ImageButton menuButton;

        HarvestViewHolder(View itemView) {
            super(itemView);
            cropName = itemView.findViewById(R.id.cropName);
            harvestDate = itemView.findViewById(R.id.harvestDate);
            menuButton = itemView.findViewById(R.id.menuButton);
        }

        void bind(final Harvest harvest, final OnHarvestClickListener listener) {
            cropName.setText(harvest.getCropName());
            harvestDate.setText(harvest.getHarvestDate());

            if (listener != null) {
                menuButton.setOnClickListener(v -> listener.onMenuClick(menuButton, harvest));
            }
        }
    }
}
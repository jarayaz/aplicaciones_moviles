package jair.araya.verduritassa;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.PopupMenu;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Map;

public class ResultsActivity extends AppCompatActivity {
    private RecyclerView harvestList;
    private HarvestAdapter adapter;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        prefs = getSharedPreferences("Cultivos", MODE_PRIVATE);
        harvestList = findViewById(R.id.harvestList);
        FloatingActionButton addButton = findViewById(R.id.addButton);

        // Configurar RecyclerView
        harvestList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HarvestAdapter(loadHarvests(), this::showSettingsMenu);
        harvestList.setAdapter(adapter);

        // Configurar botón de agregar
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(ResultsActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private ArrayList<Harvest> loadHarvests() {
        ArrayList<Harvest> harvests = new ArrayList<>();
        Map<String, ?> allHarvests = prefs.getAll();

        for (Map.Entry<String, ?> entry : allHarvests.entrySet()) {
            String cropName = entry.getKey().replace("Cultivo_", "");
            String harvestDate = entry.getValue().toString();
            harvests.add(new Harvest(cropName, harvestDate));
        }

        return harvests;
    }

    private void showSettingsMenu(Harvest harvest) {
        PopupMenu popup = new PopupMenu(this, findViewById(R.id.settingsButton));
        popup.getMenuInflater().inflate(R.menu.harvest_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit) {
                // Implementar edición
                return true;
            } else if (itemId == R.id.action_delete) {
                deleteHarvest(harvest);
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void deleteHarvest(Harvest harvest) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("Cultivo_" + harvest.getCropName());
        editor.apply();
        adapter.removeHarvest(harvest);
    }
}
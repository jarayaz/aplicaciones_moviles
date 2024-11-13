package jair.araya.verduritassa;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Map;

public class ResultsActivity extends AppCompatActivity implements HarvestAdapter.OnHarvestClickListener {
    private RecyclerView harvestList;
    private HarvestAdapter adapter;
    private SharedPreferences prefs;
    private TextView welcomeText;
    private MaterialButton deleteAllButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        mAuth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences("Cultivos", MODE_PRIVATE);
        harvestList = findViewById(R.id.harvestList);
        FloatingActionButton addButton = findViewById(R.id.addButton);
        welcomeText = findViewById(R.id.welcomeText);
        deleteAllButton = findViewById(R.id.deleteAllButton);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userName = currentUser.getDisplayName();
            if (userName == null || userName.isEmpty()) {
                userName = currentUser.getEmail();
            }
            welcomeText.setText(getString(R.string.welcome_message, userName));
        }

        harvestList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HarvestAdapter(loadHarvests(), this);
        harvestList.setAdapter(adapter);

        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(ResultsActivity.this, MainActivity.class);
            startActivity(intent);
        });

        deleteAllButton.setOnClickListener(v -> confirmDeleteAll());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.results_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            mAuth.signOut();
            Intent intent = new Intent(ResultsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ArrayList<Harvest> loadHarvests() {
        ArrayList<Harvest> harvests = new ArrayList<>();
        Map<String, ?> allHarvests = prefs.getAll();

        for (Map.Entry<String, ?> entry : allHarvests.entrySet()) {
            String[] parts = entry.getKey().split("_");
            if (parts.length >= 3) {
                String cropName = parts[1];
                String id = parts[2];
                String harvestDate = entry.getValue().toString();
                harvests.add(new Harvest(id, cropName, harvestDate));
            }
        }

        return harvests;
    }

    @Override
    public void onMenuClick(View view, Harvest harvest) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.harvest_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit) {
                editHarvest(harvest);
                return true;
            } else if (item.getItemId() == R.id.action_delete) {
                confirmDelete(harvest);
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void editHarvest(Harvest harvest) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("edit_mode", true);
        intent.putExtra("crop_name", harvest.getCropName());
        intent.putExtra("harvest_date", harvest.getHarvestDate());
        intent.putExtra("harvest_id", harvest.getId());
        startActivity(intent);
    }

    private void confirmDelete(Harvest harvest) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteHarvest(harvest))
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void confirmDeleteAll() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_all)
                .setMessage(R.string.confirm_delete_all_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteAllHarvests())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void deleteHarvest(Harvest harvest) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("Cultivo_" + harvest.getCropName() + "_" + harvest.getId());
        editor.apply();
        adapter.removeHarvest(harvest);

        if (adapter.getItemCount() == 0) {
            Toast.makeText(this, R.string.no_crops, Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteAllHarvests() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        recreate();

        Toast.makeText(this, R.string.crops_deleted, Toast.LENGTH_SHORT).show();
    }
}
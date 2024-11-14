package jair.araya.verduritassa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import androidx.recyclerview.widget.DividerItemDecoration;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

public class ResultsActivity extends AppCompatActivity implements HarvestAdapter.OnHarvestClickListener {
    private static final String TAG = "ResultsActivity";
    private RecyclerView harvestList;
    private HarvestAdapter adapter;
    private TextView welcomeText;
    private MaterialButton deleteAllButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ArrayList<Harvest> harvests;
    private ListenerRegistration harvestListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        initializeViews();
        setupFirebase();
        setupRecyclerView();
        loadHarvests();

        FloatingActionButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(ResultsActivity.this, MainActivity.class);
            startActivity(intent);
        });

        deleteAllButton.setOnClickListener(v -> confirmDeleteAll());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (harvestListener != null) {
            harvestListener.remove();
        }
    }

    private void initializeViews() {
        harvestList = findViewById(R.id.harvestList);
        welcomeText = findViewById(R.id.welcomeText);
        deleteAllButton = findViewById(R.id.deleteAllButton);
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userName = currentUser.getDisplayName();
            if (userName == null || userName.isEmpty()) {
                userName = currentUser.getEmail();
            }
            welcomeText.setText(getString(R.string.welcome_message, userName));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
    }

    private void setupRecyclerView() {
        harvests = new ArrayList<>();
        adapter = new HarvestAdapter(harvests, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        harvestList.setLayoutManager(layoutManager);
        harvestList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        harvestList.setAdapter(adapter);
        harvestList.setHasFixedSize(true);
    }

    private void loadHarvests() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.d(TAG, "No user logged in");
            return;
        }

        if (harvestListener != null) {
            harvestListener.remove();
        }

        harvestListener = db.collection("harvests")
                .whereEqualTo("userId", user.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading harvests", error);
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        harvests.clear();
                        for (QueryDocumentSnapshot document : value) {
                            String id = document.getId();
                            String cropName = document.getString("cropName");
                            String harvestDate = document.getString("harvestDate");
                            String alias = document.getString("alias");

                            if (cropName != null && harvestDate != null) {
                                harvests.add(new Harvest(id, cropName, harvestDate, alias));
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        harvests.clear();
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.results_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            if (harvestListener != null) {
                harvestListener.remove();
            }
            mAuth.signOut();
            Intent intent = new Intent(ResultsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        intent.putExtra("alias", harvest.getAlias());
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
        db.collection("harvests")
                .document(harvest.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    adapter.removeHarvest(harvest);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting harvest", e);
                });
    }

    private void deleteAllHarvests() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("harvests")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                    harvests.clear();
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting all harvests", e);
                });
    }
}
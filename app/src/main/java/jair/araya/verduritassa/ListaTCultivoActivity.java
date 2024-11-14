package jair.araya.verduritassa;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;

public class ListaTCultivoActivity extends AppCompatActivity implements HarvestAdapter.OnHarvestClickListener {

    private TextView welcomeText;
    private TextView harvestTitle;
    private Button buttonNuevaCosecha;
    private Button buttonResultados;
    private Button buttonVolver;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView harvestList;
    private HarvestAdapter adapter;
    private ArrayList<Harvest> harvests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_tcultivo);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializar vistas
        welcomeText = findViewById(R.id.welcomeText);
        harvestTitle = findViewById(R.id.harvestTitle);
        buttonNuevaCosecha = findViewById(R.id.buttonNuevaCosecha);
        buttonResultados = findViewById(R.id.buttonResultados);
        buttonVolver = findViewById(R.id.buttonVolver);
        harvestList = findViewById(R.id.harvestList);

        // Configurar título
        harvestTitle.setText(R.string.todays_harvests);

        // Configurar RecyclerView
        harvests = new ArrayList<>();
        harvestList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HarvestAdapter(harvests, this);
        harvestList.setAdapter(adapter);

        // Configurar nombre de usuario
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userName = currentUser.getDisplayName();
            if (userName == null || userName.isEmpty()) {
                userName = currentUser.getEmail();
            }
            welcomeText.setText(getString(R.string.welcome_message, userName));
        }

        // Cargar cosechas
        loadHarvests();

        // Configurar listeners
        buttonNuevaCosecha.setOnClickListener(v -> agregarNuevaCosecha());
        buttonResultados.setOnClickListener(v -> mostrarResultados());
        buttonVolver.setOnClickListener(v -> volverAMain());
    }

    private void loadHarvests() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long endOfDay = calendar.getTimeInMillis();

        db.collection("harvests")
                .whereEqualTo("userId", user.getUid())
                .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                .whereLessThanOrEqualTo("timestamp", endOfDay)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    harvests.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String cropName = doc.getString("cropName");
                        String harvestDate = doc.getString("harvestDate");
                        String alias = doc.getString("alias");
                        if (cropName != null && harvestDate != null) {
                            harvests.add(new Harvest(id, cropName, harvestDate, alias));
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            mAuth.signOut();
            Intent intent = new Intent(ListaTCultivoActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void agregarNuevaCosecha() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void mostrarResultados() {
        Intent intent = new Intent(this, ResultsActivity.class);
        startActivity(intent);
    }

    private void volverAMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        volverAMain();
    }

    @Override
    public void onMenuClick(View view, Harvest harvest) {
        // Implementar si se necesita funcionalidad de menú
    }
}
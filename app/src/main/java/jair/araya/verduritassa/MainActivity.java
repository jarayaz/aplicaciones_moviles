package jair.araya.verduritassa;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean editMode = false;
    private String editCropName;
    private String editHarvestDate;
    private String editId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userName = currentUser.getDisplayName();
            if (userName == null || userName.isEmpty()) {
                userName = currentUser.getEmail();
            }
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(getString(R.string.welcome_message, userName));
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Spinner tipos_de_cultivos = findViewById(R.id.spinner);
        DatePicker fechaCultivo = findViewById(R.id.datePicker);
        Button siguiente = findViewById(R.id.button);

        editMode = getIntent().getBooleanExtra("edit_mode", false);
        if (editMode) {
            editCropName = getIntent().getStringExtra("crop_name");
            editHarvestDate = getIntent().getStringExtra("harvest_date");
            editId = getIntent().getStringExtra("harvest_id");

            for (int i = 0; i < tipos_de_cultivos.getCount(); i++) {
                if (tipos_de_cultivos.getItemAtPosition(i).toString().equals(editCropName)) {
                    tipos_de_cultivos.setSelection(i);
                    break;
                }
            }

            try {
                String[] dateParts = editHarvestDate.split("/");
                if (dateParts.length == 3) {
                    int day = Integer.parseInt(dateParts[0]);
                    int month = Integer.parseInt(dateParts[1]) - 1;
                    int year = Integer.parseInt(dateParts[2]);
                    fechaCultivo.updateDate(year, month, day);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error al cargar la fecha", Toast.LENGTH_SHORT).show();
            }

            siguiente.setText(R.string.button_save);
        }

        siguiente.setOnClickListener(view -> {
            String cultivoSeleccionado = tipos_de_cultivos.getSelectedItem().toString();
            int diasParaCosecha = getDiasParaCosecha(cultivoSeleccionado);

            int day = fechaCultivo.getDayOfMonth();
            int month = fechaCultivo.getMonth();
            int year = fechaCultivo.getYear();

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            calendar.add(Calendar.DAY_OF_YEAR, diasParaCosecha);

            String fechaCosecha = calendar.get(Calendar.DAY_OF_MONTH) + "/" +
                    (calendar.get(Calendar.MONTH) + 1) + "/" +
                    calendar.get(Calendar.YEAR);

            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
                return;
            }

            String userName = user.getDisplayName();
            if (userName == null || userName.isEmpty()) {
                userName = user.getEmail();
            }

            Map<String, Object> harvest = new HashMap<>();
            harvest.put("userId", user.getUid());
            harvest.put("userName", userName);  // Agregamos el nombre de usuario
            harvest.put("cropName", cultivoSeleccionado);
            harvest.put("harvestDate", fechaCosecha);
            harvest.put("plantingDate", day + "/" + (month + 1) + "/" + year);
            harvest.put("timestamp", Calendar.getInstance().getTimeInMillis());

            // Verificar si ya existe una cosecha igual
            db.collection("harvests")
                    .whereEqualTo("userId", user.getUid())
                    .whereEqualTo("cropName", cultivoSeleccionado)
                    .whereEqualTo("harvestDate", fechaCosecha)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty() && !editMode) {
                            Toast.makeText(MainActivity.this, R.string.duplicate_harvest, Toast.LENGTH_SHORT).show();
                        } else {
                            // Guardar en Firestore
                            if (editMode && editId != null) {
                                db.collection("harvests")
                                        .document(editId)
                                        .set(harvest, SetOptions.merge())
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(MainActivity.this, R.string.harvest_updated, Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(MainActivity.this, ResultsActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(MainActivity.this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                db.collection("harvests")
                                        .add(harvest)
                                        .addOnSuccessListener(documentReference -> {
                                            Toast.makeText(MainActivity.this, R.string.crop_registered, Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(MainActivity.this, ResultsActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(MainActivity.this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, "Error al verificar duplicados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private int getDiasParaCosecha(String cultivo) {
        switch (cultivo) {
            case "Tomates (80 días hasta la cosecha)": return 80;
            case "Cebollas (120 días hasta la cosecha)": return 120;
            case "Lechugas (60 días hasta la cosecha)": return 60;
            case "Apio (85 días hasta la cosecha)": return 85;
            case "Choclo (90 días hasta la cosecha)": return 90;
            default: return 0;
        }
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
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
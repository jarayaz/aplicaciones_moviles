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

import java.util.Calendar;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private boolean editMode = false;
    private String editCropName;
    private String editHarvestDate;
    private String editId;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userName = currentUser.getDisplayName();
            if (userName == null || userName.isEmpty()) {
                userName = currentUser.getEmail();
            }
            String welcomeMessage = getString(R.string.welcome_message, userName);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(welcomeMessage);
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

            Map<String, ?> cosechas = getSharedPreferences("Cultivos", MODE_PRIVATE).getAll();
            boolean cosechaExistente = false;

            // Verificar si existe una cosecha exactamente igual (mismo cultivo y fecha)
            for (Map.Entry<String, ?> entry : cosechas.entrySet()) {
                String[] parts = entry.getKey().split("_");
                if (parts.length >= 3) {
                    String cropName = parts[1];
                    String harvestDate = entry.getValue().toString();
                    if (cropName.equals(cultivoSeleccionado) && harvestDate.equals(fechaCosecha)) {
                        cosechaExistente = true;
                        break;
                    }
                }
            }

            if (cosechaExistente && !editMode) {
                Toast.makeText(MainActivity.this, R.string.duplicate_harvest, Toast.LENGTH_LONG).show();
                return;
            }

            String harvestId;
            if (editMode) {
                harvestId = editId;
                // Eliminar la entrada anterior
                getSharedPreferences("Cultivos", MODE_PRIVATE).edit()
                        .remove("Cultivo_" + editCropName + "_" + editId)
                        .apply();
            } else {
                harvestId = UUID.randomUUID().toString();
            }

            // Guardar la nueva entrada con ID único
            getSharedPreferences("Cultivos", MODE_PRIVATE).edit()
                    .putString("Cultivo_" + cultivoSeleccionado + "_" + harvestId, fechaCosecha)
                    .apply();

            String mensaje = editMode ? getString(R.string.harvest_updated) : getString(R.string.crop_registered);
            Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_SHORT).show();

            Intent listIntent = new Intent(MainActivity.this, ListaTCultivoActivity.class);
            listIntent.putExtra("cultivo_seleccionado", cultivoSeleccionado);
            startActivity(listIntent);
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
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
package jair.araya.verduritassa;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

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

        // Obtener el usuario actual
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Obtener el nombre del usuario o el email si el nombre no está disponible
            String userName = currentUser.getDisplayName();
            if (userName == null || userName.isEmpty()) {
                userName = currentUser.getEmail();
            }

            // Establecer el título de la ActionBar con el mensaje de bienvenida
            String welcomeMessage = getString(R.string.welcome_message, userName);
            getSupportActionBar().setTitle(welcomeMessage);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Spinner tipos_de_cultivos = findViewById(R.id.spinner);
        DatePicker fechaCultivo = findViewById(R.id.datePicker);
        Button siguiente = findViewById(R.id.button);

        siguiente.setOnClickListener(view -> {
            String cultivoSeleccionado = tipos_de_cultivos.getSelectedItem().toString();
            int diasParaCosecha = getDiasParaCosecha(cultivoSeleccionado);

            // Obtener la fecha seleccionada
            int day = fechaCultivo.getDayOfMonth();
            int month = fechaCultivo.getMonth();
            int year = fechaCultivo.getYear();

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);

            // Sumar días para la fecha de cosecha
            calendar.add(Calendar.DAY_OF_YEAR, diasParaCosecha);

            String fechaCosecha = calendar.get(Calendar.DAY_OF_MONTH) + "/" +
                    (calendar.get(Calendar.MONTH) + 1) + "/" +
                    calendar.get(Calendar.YEAR);

            // Guardar el cultivo en SharedPreferences
            getSharedPreferences("Cultivos", MODE_PRIVATE).edit()
                    .putString("Cultivo_" + cultivoSeleccionado, fechaCosecha)
                    .apply();

            // Mostrar un Toast indicando el éxito del registro
            Toast.makeText(MainActivity.this, "Cultivo registrado exitosamente", Toast.LENGTH_SHORT).show();

            // Navegar a la pantalla de listado y pasar el cultivo seleccionado
            Intent intent = new Intent(MainActivity.this, ListaTCultivoActivity.class);
            intent.putExtra("cultivo_seleccionado", cultivoSeleccionado);
            startActivity(intent);
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
            // Cerrar sesión en Firebase
            mAuth.signOut();

            // Redirigir al login
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
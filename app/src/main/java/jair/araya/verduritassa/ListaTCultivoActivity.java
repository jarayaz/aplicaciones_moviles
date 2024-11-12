package jair.araya.verduritassa;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

public class ListaTCultivoActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private String cultivoSeleccionado;
    private String fechaCosecha;
    private TextView texto;
    private Button buttonGuardar;
    private Button buttonResultados;
    private Button buttonBorrar;
    private Button buttonVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_tcultivo);

        // Inicializar SharedPreferences
        prefs = getSharedPreferences("Cultivos", MODE_PRIVATE);

        // Obtener el cultivo seleccionado y su fecha de cosecha desde el Intent
        Intent intent = getIntent();
        cultivoSeleccionado = intent.getStringExtra("cultivo_seleccionado");
        fechaCosecha = prefs.getString("Cultivo_" + cultivoSeleccionado, "No registrado");

        // Inicializar vistas
        texto = findViewById(R.id.textView);
        buttonGuardar = findViewById(R.id.buttonGuardar);
        buttonResultados = findViewById(R.id.buttonResultados);
        buttonBorrar = findViewById(R.id.buttonBorrar);
        buttonVolver = findViewById(R.id.buttonVolver);

        // Mostrar el cultivo seleccionado y su fecha de cosecha
        texto.setText(cultivoSeleccionado + ": " + fechaCosecha);

        // Configurar listeners
        buttonGuardar.setOnClickListener(v -> guardarCultivo());
        buttonResultados.setOnClickListener(v -> mostrarResultados());
        buttonBorrar.setOnClickListener(v -> borrarCultivos());
        buttonVolver.setOnClickListener(v -> finish());
    }

    private void guardarCultivo() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("Cultivo_" + cultivoSeleccionado, fechaCosecha);
        editor.apply();
        Toast.makeText(this, "Cultivo guardado exitosamente", Toast.LENGTH_SHORT).show();
    }

    private void mostrarResultados() {
        Map<String, ?> cultivosGuardados = prefs.getAll();
        if (cultivosGuardados.isEmpty()) {
            texto.setText("No hay cultivos registrados");
        } else {
            StringBuilder resultados = new StringBuilder();
            for (Map.Entry<String, ?> entry : cultivosGuardados.entrySet()) {
                String nombreCultivo = entry.getKey().replace("Cultivo_", "");
                String fecha = entry.getValue().toString();
                resultados.append(nombreCultivo).append(": ").append(fecha).append("\n");
            }
            texto.setText(resultados.toString());
        }
    }

    private void borrarCultivos() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        texto.setText("No hay cultivos registrados");
        Toast.makeText(this, "Todos los cultivos han sido borrados", Toast.LENGTH_SHORT).show();
    }
}
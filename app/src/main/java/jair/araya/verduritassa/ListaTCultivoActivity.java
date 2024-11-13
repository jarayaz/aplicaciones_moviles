package jair.araya.verduritassa;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ListaTCultivoActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private String cultivoSeleccionado;
    private String fechaCosecha;
    private TextView texto;
    private Button buttonNuevaCosecha;
    private Button buttonResultados;
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
        buttonNuevaCosecha = findViewById(R.id.buttonNuevaCosecha);
        buttonResultados = findViewById(R.id.buttonResultados);
        buttonVolver = findViewById(R.id.buttonVolver);

        // Mostrar el cultivo seleccionado y su fecha de cosecha
        texto.setText(cultivoSeleccionado + ": " + fechaCosecha);

        // Configurar listeners
        buttonNuevaCosecha.setOnClickListener(v -> agregarNuevaCosecha());
        buttonResultados.setOnClickListener(v -> mostrarResultados());
        buttonVolver.setOnClickListener(v -> volverAMain());
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
}
package jair.araya.verduritassa;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText emailEditText;
    private EditText nameEditText;
    private EditText countryEditText;
    private EditText genderEditText;
    private EditText passwordEditText;
    private Button registerButton;
    private Button backButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar Firebase Auth y Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializar vistas
        emailEditText = findViewById(R.id.emailEditText);
        nameEditText = findViewById(R.id.nameEditText);
        countryEditText = findViewById(R.id.countryEditText);
        genderEditText = findViewById(R.id.genderEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);
        backButton = findViewById(R.id.backButton);

        // Configurar click listener para el botón de registro
        registerButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String name = nameEditText.getText().toString().trim();
            String country = countryEditText.getText().toString().trim();
            String gender = genderEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || name.isEmpty() || country.isEmpty() ||
                    gender.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this,
                        "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear usuario con email y contraseña
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Actualizar el perfil del usuario con el nombre
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            mAuth.getCurrentUser().updateProfile(profileUpdates)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            // Guardar datos adicionales en Firestore
                                            String userId = mAuth.getCurrentUser().getUid();
                                            Map<String, Object> user = new HashMap<>();
                                            user.put("email", email);
                                            user.put("nombre", name);
                                            user.put("pais", country);
                                            user.put("genero", gender);

                                            db.collection("usuarios")
                                                    .document(userId)
                                                    .set(user)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(RegisterActivity.this,
                                                                "Registro exitoso", Toast.LENGTH_SHORT).show();
                                                        // Redirigir a ResultsActivity después del registro
                                                        Intent intent = new Intent(RegisterActivity.this, ResultsActivity.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(RegisterActivity.this,
                                                                "Error al guardar datos: " + e.getMessage(),
                                                                Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    });
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Error en el registro: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Configurar click listener para el botón de volver
        backButton.setOnClickListener(v -> {
            finish(); // Esto volverá a la actividad anterior (LoginActivity)
        });
    }
}
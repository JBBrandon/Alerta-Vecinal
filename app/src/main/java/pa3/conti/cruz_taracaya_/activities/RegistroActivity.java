package pa3.conti.cruz_taracaya_.activities;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;


import com.google.android.material.textfield.TextInputEditText;

import pa3.conti.cruz_taracaya_.network.ApiClient;
import pa3.conti.cruz_taracaya_.models.ApiResponse;
import pa3.conti.cruz_taracaya_.network.ApiService;
import pa3.conti.cruz_taracaya_.R;
import pa3.conti.cruz_taracaya_.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.IOException;
import android.widget.Toast;

public class RegistroActivity extends AppCompatActivity {
    private TextInputEditText etUsuario, etCelular, etEmail, etPassword;
    private Button btnRegistros;
    private TextView tvIniciaSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // Inicializar todos los elementos
        etUsuario = findViewById(R.id.etUsuario);
        etCelular = findViewById(R.id.etCelular);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegistros = findViewById(R.id.btnRegistros); // ¡ID correcto!
        tvIniciaSesion = findViewById(R.id.tvIniciaSesion);

        btnRegistros.setOnClickListener(v -> validarRegistro());
        tvIniciaSesion.setOnClickListener(v -> finish());
        tvIniciaSesion.setOnClickListener(v -> {
            startActivity(new Intent(RegistroActivity.this, LoginActivity.class));
            finish(); // Cierra RegistroActivity
        });
    }
    private void validarRegistro() {
        String usuario = etUsuario.getText().toString().trim();
        String celular = etCelular.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validarFormulario(usuario, celular, email, password)) return;

        // Llamar a API REST para registro
        User nuevoUsuario = new User(usuario, email, celular, password);
        ejecutarRegistro(nuevoUsuario);
    }

    private boolean validarFormulario(String usuario, String celular, String email, String password) {
        // Validar email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email inválido");
            return false;
        }

        // Validar contraseña (mínimo 8 caracteres, 1 mayúscula, 1 número)
        if (!password.matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            etPassword.setError("La contraseña debe tener al menos 8 caracteres, 1 mayúscula y 1 número");
            return false;
        }

        // Validar celular (ejemplo para Perú)
        if (!celular.matches("^9\\d{8}$")) {
            etCelular.setError("Celular inválido");
            return false;
        }

        return true;
    }
    // Dentro del método ejecutarRegistro
    private void ejecutarRegistro(User user) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ApiResponse> call = apiService.registerUser(user);

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    // Registro exitoso
                    Toast.makeText(RegistroActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegistroActivity.this, LoginActivity.class));
                    finish();
                } else {
                    try {
                        // Manejar errores HTTP (ej: 400, 500)
                        String errorBody = response.errorBody().string();
                        Toast.makeText(RegistroActivity.this, "Error del servidor: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Error de red
                Toast.makeText(RegistroActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
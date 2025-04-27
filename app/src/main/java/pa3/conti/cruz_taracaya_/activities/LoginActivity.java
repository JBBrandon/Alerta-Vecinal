package pa3.conti.cruz_taracaya_.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import pa3.conti.cruz_taracaya_.network.ApiClient;
import pa3.conti.cruz_taracaya_.network.ApiService;
import pa3.conti.cruz_taracaya_.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import pa3.conti.cruz_taracaya_.models.LoginRequest;

import pa3.conti.cruz_taracaya_.models.LoginResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsuario, etPassword;
    private Button btnLogin;
    private TextView tvRegistro;
    private SignInButton btnGoogle;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inicializarVistas();
        configurarListeners();
        configurarGoogleSignIn();

    }
    private void configurarGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                // Opcional: Si usas Firebase Auth
                .requestIdToken(getString(R.string.default_web_client_id)) // Agrega esto
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }
    private void inicializarVistas() {
        etUsuario = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegistro = findViewById(R.id.tvRegistro);
        btnGoogle = findViewById(R.id.btnGoogle);

    }

    private void configurarListeners() {
        btnLogin.setOnClickListener(v -> validarLogin());
        tvRegistro.setOnClickListener(v -> {
            startActivity(new Intent(this, RegistroActivity.class));
        });
        btnGoogle.setOnClickListener(v -> iniciarSesionGoogle());
    }
    private void iniciarSesionGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            manejarResultadoGoogle(task);
        }
    }

    private void manejarResultadoGoogle(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            autenticacionGoogleExitosa(account);
        } catch (ApiException e) {
            mostrarError("Error en autenticación Google: " + e.getStatusCode());
        }
    }

    private void autenticacionGoogleExitosa(GoogleSignInAccount account) {
        // Aquí obtienes los datos del usuario:
        String nombre = account.getDisplayName();
        String email = account.getEmail();
        String id = account.getId();

        // Guardar en SharedPreferences
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit()
                .putString("username", nombre)
                .putString("email", email)
                .apply();

        // Redirigir a MainActivity
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void validarLogin() {
        String identificador = etUsuario.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (identificador.isEmpty() || password.isEmpty()) {
            mostrarError("Todos los campos son requeridos");
            return;
        }

        ejecutarLogin(identificador, password);
    }

    private void ejecutarLogin(String identificador, String password) {
        LoginRequest loginRequest = new LoginRequest(identificador, password);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<LoginResponse> call = apiService.loginUser(loginRequest);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    manejarLoginExitoso(loginResponse);
                } else {
                    manejarErrorLogin(response.code());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                manejarErrorConexion(t);
            }
        });
    }

    private void manejarLoginExitoso(LoginResponse response) {
        // Guardar datos de usuario
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit()
                .putInt("userId", response.getUserId())
                .putString("username", response.getUsername())
                .putString("email", response.getEmail())
                .apply();

        // Redirigir a MainActivity
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void manejarErrorLogin(int statusCode) {
        String mensajeError;
        switch (statusCode) {
            case 401:
                mensajeError = "Credenciales incorrectas";
                break;
            case 404:
                mensajeError = "Usuario no encontrado";
                break;
            case 500:
                mensajeError = "Error del servidor";
                break;
            default:
                mensajeError = "Error desconocido: " + statusCode;
        }
        mostrarError(mensajeError);
    }

    private void manejarErrorConexion(Throwable t) {
        mostrarError("Error de conexión: " + t.getMessage());
        t.printStackTrace();
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }
}
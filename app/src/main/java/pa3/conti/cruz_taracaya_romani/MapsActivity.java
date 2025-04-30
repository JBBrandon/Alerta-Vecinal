package pa3.conti.cruz_taracaya_romani;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pa3.conti.cruz_taracaya_.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private LatLng ubicacionSeleccionada;
    private RecyclerView recyclerView;
    private ReporteAdapter reporteAdapter;
    private boolean listaVisible = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private DatabaseReference mDatabase;

    private final String[] TIPOS_INCIDENTES = {
            "Robo", "Accidente", "Vandalismo",
            "Persona sospechosa", "Otro"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerViewReportes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reporteAdapter = new ReporteAdapter(new ArrayList<>(), this::centrarEnReporte);
        recyclerView.setAdapter(reporteAdapter);

        // Configurar botón de lista
        FloatingActionButton fabLista = findViewById(R.id.fabReportsList);
        fabLista.setOnClickListener(v -> toggleListaReportes());

        // Inicializar Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Configurar botones
        FloatingActionButton fabMyLocation = findViewById(R.id.fabMyLocation);
        FloatingActionButton fabAddReport = findViewById(R.id.fabAddReport);
        fabAddReport.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        ubicacionSeleccionada = new LatLng(location.getLatitude(), location.getLongitude());
                        mostrarDialogoDescripcion(ubicacionSeleccionada);
                    } else {
                        Toast.makeText(this, "No se pudo obtener tu ubicación", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Se necesitan permisos de ubicación", Toast.LENGTH_SHORT).show();
            }
        });
        fabMyLocation.setOnClickListener(v -> centrarEnMiUbicacion());

        // Obtener el SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }
    private void toggleListaReportes() {
        listaVisible = !listaVisible;

        if (listaVisible) {
            // Mostrar la lista y cargar los reportes
            recyclerView.setVisibility(View.VISIBLE);
            cargarReportes();
        } else {
            // Ocultar la lista
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void cargarReportes() {
        DatabaseReference refReportes = FirebaseDatabase.getInstance().getReference("reportes");
        refReportes.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("DEBUG", "Datos recibidos: " + snapshot.getChildrenCount() + " reportes");
                List<Reporte> reportes = new ArrayList<>();
                for (DataSnapshot reporteSnapshot : snapshot.getChildren()) {
                    Reporte reporte = reporteSnapshot.getValue(Reporte.class);
                    if (reporte != null) {
                        reportes.add(reporte);
                    }
                }

                // Ordenar por timestamp (más recientes primero)
                Collections.sort(reportes, (r1, r2) -> Long.compare(r2.timestamp, r1.timestamp));

                // Actualizar el adaptador existente en lugar de crear uno nuevo
                reporteAdapter.actualizarDatos(reportes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DEBUG", "Error al cargar: " + error.getMessage());
                Toast.makeText(MapsActivity.this, "Error al cargar reportes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void centrarEnReporte(Reporte reporte) {
        LatLng ubicacion = new LatLng(reporte.latitud, reporte.longitud);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 16));

        // Ocultar lista después de seleccionar
        toggleListaReportes();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            centrarEnMiUbicacion();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        cargarReportesEnMapa();
    }

    private void centrarEnMiUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng miUbicacion = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(miUbicacion, 15));
                    }
                });
    }

    private void mostrarDialogoTipoReporte() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        // Mostrar directamente el diálogo de descripción con ubicación actual
                        mostrarDialogoDescripcion(
                                new LatLng(location.getLatitude(), location.getLongitude())
                        );
                    } else {
                        Toast.makeText(this, "No se pudo obtener ubicación", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mostrarDialogoDescripcion(LatLng ubicacion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nuevo Reporte de Incidente");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_reporte, null);
        builder.setView(dialogView);

        Spinner spinnerTipo = dialogView.findViewById(R.id.spinnerTipo);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        TextView tvUbicacion = dialogView.findViewById(R.id.tvUbicacion);

        // Mostrar la ubicación seleccionada en el cuadro de diálogo
        String ubicacionStr = String.format(Locale.getDefault(),
                "Ubicación: %.6f, %.6f", ubicacion.latitude, ubicacion.longitude);
        tvUbicacion.setText(ubicacionStr);

        // Configurar el Spinner con los tipos de incidente
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, TIPOS_INCIDENTES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapter);

        builder.setPositiveButton("Reportar", (dialog, which) -> {
            String descripcion = etDescripcion.getText().toString().trim();
            String tipoSeleccionado = spinnerTipo.getSelectedItem().toString();

            if (!descripcion.isEmpty()) {
                // Crear un nuevo reporte con las coordenadas y la descripción
                Reporte nuevoReporte = new Reporte(
                        ubicacion.latitude,
                        ubicacion.longitude,
                        descripcion,
                        tipoSeleccionado
                );
                guardarReporte(nuevoReporte);
            } else {
                Toast.makeText(this, "Por favor ingrese una descripción", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Personalizar los botones
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));

        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
    }


    private void guardarReporte(Reporte reporte) {
        String key = mDatabase.child("reportes").push().getKey();
        mDatabase.child("reportes").child(key).setValue(reporte)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "✅ Reporte enviado con éxito", Toast.LENGTH_SHORT).show();

                        // Actualizar lista y mapa
                        if (listaVisible) {
                            cargarReportes();
                        }
                        cargarReportesEnMapa();
                    } else {
                        Toast.makeText(this, "❌ Error al enviar reporte", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cargarReportesEnMapa() {
        mDatabase.child("reportes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMap.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Reporte reporte = snapshot.getValue(Reporte.class);
                    if (reporte != null) {
                        agregarMarcador(reporte);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MapsActivity.this, "Error al cargar reportes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void agregarMarcador(Reporte reporte) {
        LatLng ubicacion = new LatLng(reporte.latitud, reporte.longitud);

        // Determinar el color del marcador según el tipo de incidente
        float color = BitmapDescriptorFactory.HUE_RED;
        switch (reporte.tipo) {
            case "Robo": color = BitmapDescriptorFactory.HUE_RED; break;
            case "Accidente": color = BitmapDescriptorFactory.HUE_ORANGE; break;
            case "Vandalismo": color = BitmapDescriptorFactory.HUE_YELLOW; break;
            case "Persona sospechosa": color = BitmapDescriptorFactory.HUE_BLUE; break;
            case "Otro": color = BitmapDescriptorFactory.HUE_GREEN; break;
        }

        // Agregar el marcador con el ícono personalizado según el tipo
        mMap.addMarker(new MarkerOptions()
                .position(ubicacion)
                .title(reporte.tipo)
                .snippet(reporte.descripcion)
                .icon(BitmapDescriptorFactory.defaultMarker(color))); // Marcador con ícono de color según tipo
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        // Limpiar cualquier marcador anterior
        mMap.clear();

        // Colocar un marcador en la nueva ubicación
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Ubicación seleccionada"));

        // Guardar las coordenadas seleccionadas
        ubicacionSeleccionada = latLng;

        // Llamar al método para mostrar el cuadro de diálogo de reporte
        mostrarDialogoDescripcion(latLng);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                centrarEnMiUbicacion();
            }
        }
    }

}
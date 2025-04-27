package pa3.conti.cruz_taracaya_romani;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ReportarIncidente {
    private DatabaseReference mDatabase;

    public ReportarIncidente() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    // Versión con tipo por defecto ("Otro")
    public void reportarIncidente(double latitud, double longitud, String descripcion) {
        reportarIncidente(latitud, longitud, descripcion, "Otro");
    }

    // Versión con tipo específico
    public void reportarIncidente(double latitud, double longitud, String descripcion, String tipo) {
        String incidenteId = mDatabase.push().getKey();
        Reporte reporte = new Reporte(latitud, longitud, descripcion, tipo);
        mDatabase.child("reportes").child(incidenteId).setValue(reporte);
    }
}

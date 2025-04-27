package pa3.conti.cruz_taracaya_romani;

public class Reporte {
    public double latitud;
    public double longitud;
    public String descripcion;
    public String tipo;
    public long timestamp;

    // Constructor vacío requerido por Firebase
    public Reporte() {
        this.tipo = "Otro"; // Valor por defecto
        this.timestamp = System.currentTimeMillis();
    }

    // Constructor con 3 parámetros (sin tipo)
    public Reporte(double latitud, double longitud, String descripcion) {
        this(); // Llama al constructor vacío primero
        this.latitud = latitud;
        this.longitud = longitud;
        this.descripcion = descripcion;
    }

    // Constructor con 4 parámetros (con tipo)
    public Reporte(double latitud, double longitud, String descripcion, String tipo) {
        this(latitud, longitud, descripcion); // Llama al constructor de 3 parámetros
        this.tipo = tipo;
    }
}
package pa3.conti.cruz_taracaya_.models;

public class User {
    private String username;
    private String email;
    private String telefono;
    private String password;

    public User(String username, String email, String telefono, String password) {
        this.username = username;
        this.email = email;
        this.telefono = telefono;
        this.password = password;
    }

    // Getters y Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
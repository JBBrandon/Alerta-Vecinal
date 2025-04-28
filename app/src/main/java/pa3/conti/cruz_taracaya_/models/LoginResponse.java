package pa3.conti.cruz_taracaya_.models;
public class LoginResponse {
    private int userId;
    private String username;
    private String email;

    // Getters y Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
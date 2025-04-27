package pa3.conti.cruz_taracaya_.models;

public class ApiResponse {
    private String message;
    private User user;

    // Getters y Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
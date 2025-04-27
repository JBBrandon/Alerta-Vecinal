package pa3.conti.cruz_taracaya_.models;
import java.io.Serializable;

public class models {
    // User.java
    public class User {
        private String username;
        private String email;
        private String telefono;
        private String password;

        // Constructor y getters/setters
    }

    // LoginRequest.java
    public class LoginRequest {
        private String identifier;
        private String password;

        // Constructor y getters/setters
    }

    // ApiResponse.java
    public class ApiResponse {
        private String message;
        private User user;

        // Getters y setters
    }
}

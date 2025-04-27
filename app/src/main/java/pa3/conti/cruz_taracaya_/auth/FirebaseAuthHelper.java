/*package pa3.conti.cruz_taracaya_.auth;


import com.google.firebase.auth.FirebaseAuth;

public class FirebaseAuthHelper {
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public static void registerUser(String email, String password, OnAuthListener listener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onSuccess();
                    } else {
                        listener.onError(task.getException().getMessage());
                    }
                });
    }

    public interface OnAuthListener {
        void onSuccess();
        void onError(String error);
    }
}*/
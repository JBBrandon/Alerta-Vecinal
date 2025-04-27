package pa3.conti.cruz_taracaya_.network;

import pa3.conti.cruz_taracaya_.models.User;
import pa3.conti.cruz_taracaya_.models.ApiResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import pa3.conti.cruz_taracaya_.models.LoginRequest;
import pa3.conti.cruz_taracaya_.models.LoginResponse;

public interface ApiService {

    @POST("register.php")
    Call<ApiResponse> registerUser(@Body User user);

    @POST("login.php")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

}
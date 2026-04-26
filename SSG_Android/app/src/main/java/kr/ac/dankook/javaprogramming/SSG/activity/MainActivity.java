package kr.ac.dankook.javaprogramming.SSG.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import kr.ac.dankook.javaprogramming.SSG.R;
import kr.ac.dankook.javaprogramming.SSG.dto.UserRequest;
import kr.ac.dankook.javaprogramming.SSG.network.RetrofitClient;
import kr.ac.dankook.javaprogramming.SSG.service.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private EditText etId, etPassword;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etId = findViewById(R.id.et_id);
        etPassword = findViewById(R.id.et_password);
        btnRegister = findViewById(R.id.btn_register);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        btnRegister.setOnClickListener(v -> {
            String username = etId.getText().toString();
            String password = etPassword.getText().toString();
            UserRequest request = new UserRequest(username, password);

            apiService.signUp(request).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
                        // TODO: 주식 리스트 화면으로 이동
                    } else {
                        Toast.makeText(MainActivity.this, "가입 실패", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}

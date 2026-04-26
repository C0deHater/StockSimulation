package kr.ac.dankook.javaprogramming.SSG.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import kr.ac.dankook.javaprogramming.SSG.R;
import kr.ac.dankook.javaprogramming.SSG.dto.UserRequest;
import kr.ac.dankook.javaprogramming.SSG.network.RetrofitClient;
import kr.ac.dankook.javaprogramming.SSG.service.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText etId, etPw;
    private Button btnLogin;
    private TextView tvGoSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etId = findViewById(R.id.et_login_id);
        etPw = findViewById(R.id.et_login_pw);
        btnLogin = findViewById(R.id.btn_login);
        tvGoSignup = findViewById(R.id.tv_go_signup);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        btnLogin.setOnClickListener(v -> {
            String id = etId.getText().toString();
            String pw = etPw.getText().toString();
            UserRequest loginData = new UserRequest(id, pw);

            apiService.login(loginData).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                        // TODO: 주식 리스트 화면으로 이동
                    } else {
                        Toast.makeText(LoginActivity.this, "아이디 또는 비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
                }
            });
        });

        tvGoSignup.setOnClickListener(v -> {
            // TODO: 회원가입 화면으로 이동
        });
    }
}

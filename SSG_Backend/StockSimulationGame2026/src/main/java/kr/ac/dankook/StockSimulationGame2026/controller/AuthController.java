package kr.ac.dankook.StockSimulationGame2026.controller;

import kr.ac.dankook.StockSimulationGame2026.domain.User;
import kr.ac.dankook.StockSimulationGame2026.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController
{
    private final UserRepository userRepository;

    // 로그인 기능 추가
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User loginRequest) {
        // 서버 콘솔 확인용
        System.out.println("======= 로그인 요청 도착 =======");
        System.out.println("1. 앱에서 보낸 ID: [" + loginRequest.getUsername() + "]");
        System.out.println("2. 앱에서 보낸 PW: [" + loginRequest.getPassword() + "]");
        System.out.println("==============================");

        if (loginRequest.getUsername() == null) {
            System.out.println("에러: ID값이 서버로 전달되지 않았습니다! (null)");
        }

        return userRepository.findByUsername(loginRequest.getUsername())
                .filter(user -> {
                    System.out.println("3. DB에서 찾은 실제 PW: [" + user.getPassword() + "]");
                    return user.getPassword().equals(loginRequest.getPassword());
                })
                .map(user -> ResponseEntity.ok("success"))
                .orElseGet(() -> {
                    System.out.println("4. 결과: 로그인 실패 (ID가 없거나 PW 불일치)");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("fail");
                });
    }
}

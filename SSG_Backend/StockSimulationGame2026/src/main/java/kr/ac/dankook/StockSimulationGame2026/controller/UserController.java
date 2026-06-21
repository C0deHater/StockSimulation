package kr.ac.dankook.StockSimulationGame2026.controller;

import kr.ac.dankook.StockSimulationGame2026.domain.User;
import kr.ac.dankook.StockSimulationGame2026.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserController
{
    private final UserService userService;

    @PostMapping("/signup")
    public String signup(@RequestBody User user)
    {
        userService.signup(user);
        return "회원가입 성공! 자산 1,000만 원이 지급되었습니다.";
    }
}
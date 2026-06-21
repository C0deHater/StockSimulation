package kr.ac.dankook.StockSimulationGame2026.service;

import kr.ac.dankook.StockSimulationGame2026.domain.User;
import kr.ac.dankook.StockSimulationGame2026.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService
{
    private final UserRepository userRepository;

    @Transactional
    public User signup(User user)
    {
        return userRepository.save(user);
    }
}
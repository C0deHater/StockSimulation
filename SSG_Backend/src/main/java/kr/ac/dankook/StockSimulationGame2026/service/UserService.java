package kr.ac.dankook.StockSimulationGame2026.service;

import kr.ac.dankook.StockSimulationGame2026.domain.Account;
import kr.ac.dankook.StockSimulationGame2026.domain.User;
import kr.ac.dankook.StockSimulationGame2026.repository.AccountRepository;
import kr.ac.dankook.StockSimulationGame2026.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public User signup(User user) {
        User savedUser = userRepository.save(user);

        // 회원가입 시 초기 자금 1,000만 원 계좌 생성
        Account account = new Account();
        account.setUserId(savedUser.getId());
        account.setBalance(10_000_000.0);
        accountRepository.save(account);

        return savedUser;
    }
}

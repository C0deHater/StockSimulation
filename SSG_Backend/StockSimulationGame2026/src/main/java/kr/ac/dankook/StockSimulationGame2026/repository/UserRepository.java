package kr.ac.dankook.StockSimulationGame2026.repository;

import kr.ac.dankook.StockSimulationGame2026.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>
{
    Optional<User> findByUsername(String username); // 아이디로 중복 체크할 때 사용
}
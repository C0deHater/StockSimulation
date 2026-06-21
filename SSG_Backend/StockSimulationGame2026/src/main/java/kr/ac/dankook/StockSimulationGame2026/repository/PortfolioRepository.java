package kr.ac.dankook.StockSimulationGame2026.repository;

import kr.ac.dankook.StockSimulationGame2026.domain.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    // 특정 사용자가 가진 모든 주식 목록을 가져오는 메서드
    List<Portfolio> findAllByUserId(Long userId);

    Optional<Portfolio> findByUserIdAndStockCode(Long userId, String stockCode);
}
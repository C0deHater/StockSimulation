package kr.ac.dankook.StockSimulationGame2026.repository;

import kr.ac.dankook.StockSimulationGame2026.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

    // 🚀 [핵심] 역사 데이터(2년치) 중 특정 종목의 가장 마지막(최신) 데이터를 가져옵니다.
    Optional<Stock> findFirstByStockCodeOrderByStDateDesc(String stockCode);

    List<Stock> findAllByStockCodeOrderByStDateAsc(String stockCode);
}
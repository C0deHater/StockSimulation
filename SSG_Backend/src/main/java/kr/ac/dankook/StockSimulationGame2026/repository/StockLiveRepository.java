package kr.ac.dankook.StockSimulationGame2026.repository;

import kr.ac.dankook.StockSimulationGame2026.domain.StockLive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface StockLiveRepository extends JpaRepository<StockLive, Long> {
    Optional<StockLive> findFirstByStockCodeOrderByStDateDesc(String stockCode);

    @Query(value = "SELECT * FROM stock_live WHERE id IN " +
            "(SELECT MAX(id) FROM stock_live GROUP BY stock_code)", nativeQuery = true)
    List<StockLive> findCurrentLivePrices();

    List<StockLive> findTop100ByStockCodeOrderByStDateDesc(String stockCode);
}

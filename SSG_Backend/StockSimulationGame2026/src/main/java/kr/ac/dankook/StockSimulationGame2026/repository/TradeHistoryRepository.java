package kr.ac.dankook.StockSimulationGame2026.repository;

import kr.ac.dankook.StockSimulationGame2026.domain.TradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TradeHistoryRepository extends JpaRepository<TradeHistory, Long> {

    List<TradeHistory> findAllByUserIdOrderByTradeDateDesc(Long userId);

    // 매도 거래만 조회 (손익 집계용)
    List<TradeHistory> findAllByUserIdAndTradeType(Long userId, String tradeType);

    // 총 실현 손익
    @Query("SELECT COALESCE(SUM(t.profitLoss), 0) FROM TradeHistory t " +
           "WHERE t.userId = :userId AND t.tradeType = 'SELL'")
    Double sumProfitLossByUserId(@Param("userId") Long userId);

    // 매도 건수 (수익인 것만)
    @Query("SELECT COUNT(t) FROM TradeHistory t " +
           "WHERE t.userId = :userId AND t.tradeType = 'SELL' AND t.profitLoss > 0")
    long countWinsByUserId(@Param("userId") Long userId);

    // 전체 매도 건수
    @Query("SELECT COUNT(t) FROM TradeHistory t " +
           "WHERE t.userId = :userId AND t.tradeType = 'SELL'")
    long countSellsByUserId(@Param("userId") Long userId);

    void deleteAllByUserId(Long userId);
}

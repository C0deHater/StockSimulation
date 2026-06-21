package kr.ac.dankook.StockSimulationGame2026.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "trade_history")
@Getter @Setter @NoArgsConstructor
public class TradeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long   userId;
    private String stockCode;
    private String stockName;
    private String tradeType;      // "BUY" / "SELL"
    private Integer quantity;
    private Double price;          // 체결가
    private Double totalAmount;    // price × quantity

    // SELL 전용 — BUY 시 null
    private Double avgBuyPrice;    // 매수 평단가 (기록용)
    private Double profitLoss;     // 실현 손익 (원)
    private Double profitLossRate; // 실현 수익률 (%)

    private LocalDateTime tradeDate;
}

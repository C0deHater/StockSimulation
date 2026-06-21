package kr.ac.dankook.StockSimulationGame2026.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TradeHistoryDto {
    private Long    id;
    private String  stockCode;
    private String  stockName;
    private String  tradeType;      // "BUY" / "SELL"
    private Integer quantity;
    private Double  price;
    private Double  totalAmount;
    private Double  avgBuyPrice;    // SELL 시에만 존재
    private Double  profitLoss;     // SELL 시에만 존재
    private Double  profitLossRate; // SELL 시에만 존재
    private String  tradeDate;
}

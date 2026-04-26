package kr.ac.dankook.StockSimulationGame2026.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TradeRequest {
    private Long userId;
    private String stockCode;
    private String stockName;
    private int quantity;
}

package kr.ac.dankook.StockSimulationGame2026.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {
    // 🚀 안드로이드와 규격을 맞추기 위해 id와 userId를 추가합니다.
    private Long id;
    private Long userId;

    private String stockCode;
    private String stockName;
    private Integer quantity;
    private Double averagePrice;
    private Double currentPrice;
}
package kr.ac.dankook.StockSimulationGame2026.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioDto {
    private String stockCode;
    private String stockName;
    private Integer quantity;
    private Double averagePrice;
    private Double currentPrice;
}

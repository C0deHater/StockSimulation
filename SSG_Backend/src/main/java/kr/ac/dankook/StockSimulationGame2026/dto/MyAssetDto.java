package kr.ac.dankook.StockSimulationGame2026.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class MyAssetDto {
    private Double totalAsset;
    private Double cashBalance;
    private Double totalStockValue;
    private List<PortfolioDto> portfolioList;
}

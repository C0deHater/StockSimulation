package ssg.dto;

import java.util.ArrayList;
import java.util.List;

public class MyAssetDto {
    private Double totalAsset      = 0.0;
    private Double cashBalance     = 0.0;
    private Double totalStockValue = 0.0;
    private List<Portfolio> portfolioList = new ArrayList<>();

    private Double  totalProfitLoss = 0.0;
    private Double  totalReturnRate = 0.0;
    private Integer totalTrades     = 0;
    private Integer winTrades       = 0;
    private Double  winRate         = 0.0;

    public Double getTotalAsset()       { return totalAsset      != null ? totalAsset      : 0.0; }
    public Double getCashBalance()      { return cashBalance     != null ? cashBalance     : 0.0; }
    public Double getTotalStockValue()  { return totalStockValue != null ? totalStockValue : 0.0; }
    public List<Portfolio> getPortfolioList() {
        return portfolioList != null ? portfolioList : new ArrayList<>();
    }
    public Double  getTotalProfitLoss() { return totalProfitLoss != null ? totalProfitLoss : 0.0; }
    public Double  getTotalReturnRate() { return totalReturnRate != null ? totalReturnRate : 0.0; }
    public Integer getTotalTrades()     { return totalTrades     != null ? totalTrades     : 0; }
    public Integer getWinTrades()       { return winTrades       != null ? winTrades       : 0; }
    public Double  getWinRate()         { return winRate         != null ? winRate         : 0.0; }
}

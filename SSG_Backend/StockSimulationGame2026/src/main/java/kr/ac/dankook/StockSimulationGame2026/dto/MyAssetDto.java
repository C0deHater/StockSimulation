package kr.ac.dankook.StockSimulationGame2026.dto;

import java.util.ArrayList;
import java.util.List;

public class MyAssetDto {
    private Double totalAsset      = 0.0;
    private Double cashBalance     = 0.0;
    private Double totalStockValue = 0.0;
    private List<Portfolio> portfolioList = new ArrayList<>();

    // 성과 지표
    private Double  totalProfitLoss     = 0.0; // 총 실현 손익 (원)
    private Double  totalReturnRate     = 0.0; // 총 수익률 (%)
    private Integer totalTrades         = 0;   // 총 매도 거래 건수
    private Integer winTrades           = 0;   // 수익 매도 건수
    private Double  winRate             = 0.0; // 승률 (%)

    // ── Getters ───────────────────────────────────────────
    public Double getTotalAsset()        { return totalAsset      != null ? totalAsset      : 0.0; }
    public Double getCashBalance()       { return cashBalance     != null ? cashBalance     : 0.0; }
    public Double getTotalStockValue()   { return totalStockValue != null ? totalStockValue : 0.0; }
    public List<Portfolio> getPortfolioList() {
        return portfolioList != null ? portfolioList : new ArrayList<>();
    }
    public Double  getTotalProfitLoss()  { return totalProfitLoss  != null ? totalProfitLoss  : 0.0; }
    public Double  getTotalReturnRate()  { return totalReturnRate  != null ? totalReturnRate  : 0.0; }
    public Integer getTotalTrades()      { return totalTrades      != null ? totalTrades      : 0; }
    public Integer getWinTrades()        { return winTrades        != null ? winTrades        : 0; }
    public Double  getWinRate()          { return winRate          != null ? winRate          : 0.0; }

    // ── Setters ───────────────────────────────────────────
    public void setTotalAsset(Double v)        { this.totalAsset = v; }
    public void setCashBalance(Double v)       { this.cashBalance = v; }
    public void setTotalStockValue(Double v)   { this.totalStockValue = v; }
    public void setPortfolioList(List<Portfolio> v) { this.portfolioList = v; }
    public void setTotalProfitLoss(Double v)   { this.totalProfitLoss = v; }
    public void setTotalReturnRate(Double v)   { this.totalReturnRate = v; }
    public void setTotalTrades(Integer v)      { this.totalTrades = v; }
    public void setWinTrades(Integer v)        { this.winTrades = v; }
    public void setWinRate(Double v)           { this.winRate = v; }
}

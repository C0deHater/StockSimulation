package kr.ac.dankook.javaprogramming.SSG.dto;

import java.util.ArrayList;
import java.util.List;

public class MyAssetDto {
    private Double totalAsset = 0.0;
    private Double cashBalance = 0.0;
    private Double totalStockValue = 0.0;
    private List<Portfolio> portfolioList = new ArrayList<>();

    public Double getTotalAsset() { return totalAsset != null ? totalAsset : 0.0; }
    public Double getCashBalance() { return cashBalance != null ? cashBalance : 0.0; }
    public Double getTotalStockValue() { return totalStockValue != null ? totalStockValue : 0.0; }
    public List<Portfolio> getPortfolioList() { return portfolioList != null ? portfolioList : new ArrayList<>(); }
}

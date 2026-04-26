package kr.ac.dankook.javaprogramming.SSG.dto;

public class Portfolio {
    private String stockCode;
    private String stockName;
    private Integer quantity;
    private Double averagePrice;
    private Double currentPrice;

    public String getStockCode() { return stockCode; }
    public String getStockName() { return stockName; }
    public Integer getQuantity() { return quantity; }
    public Double getAveragePrice() { return averagePrice; }
    public Double getCurrentPrice() { return currentPrice; }
}

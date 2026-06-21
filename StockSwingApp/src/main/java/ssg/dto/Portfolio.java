package ssg.dto;

public class Portfolio {
    private String stockCode;
    private String stockName;
    private Integer quantity;
    private Double averagePrice;
    private Double currentPrice;

    public String  getStockCode()    { return stockCode    != null ? stockCode    : ""; }
    public String  getStockName()    { return stockName    != null ? stockName    : ""; }
    public Integer getQuantity()     { return quantity     != null ? quantity     : 0; }
    public Double  getAveragePrice() { return averagePrice != null ? averagePrice : 0.0; }
    public Double  getCurrentPrice() { return currentPrice != null ? currentPrice : 0.0; }
}

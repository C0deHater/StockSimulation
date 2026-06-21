package ssg.dto;

public class StockListResponse {
    private String stockName;
    private String stockCode;
    private Double currentPrice;
    private Double changeRate;

    public String getStockName()   { return stockName    != null ? stockName    : "이름 없음"; }
    public String getStockCode()   { return stockCode    != null ? stockCode    : ""; }
    public Double getCurrentPrice(){ return currentPrice != null ? currentPrice : 0.0; }
    public Double getChangeRate()  { return changeRate   != null ? changeRate   : 0.0; }
}

package kr.ac.dankook.javaprogramming.SSG.dto;

public class TradeRequest {
    private Long userId;
    private String stockCode;
    private String stockName;
    private int quantity;

    public void setUserId(Long userId) { this.userId = userId; }
    public void setStockCode(String stockCode) { this.stockCode = stockCode; }
    public void setStockName(String stockName) { this.stockName = stockName; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}

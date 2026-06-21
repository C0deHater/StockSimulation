package ssg.dto;

public class TradeRequest {
    private Long userId;
    private String stockCode;
    private String stockName;
    private int quantity;

    public void setUserId(Long userId)        { this.userId    = userId; }
    public void setStockCode(String code)     { this.stockCode = code; }
    public void setStockName(String name)     { this.stockName = name; }
    public void setQuantity(int quantity)     { this.quantity  = quantity; }
}

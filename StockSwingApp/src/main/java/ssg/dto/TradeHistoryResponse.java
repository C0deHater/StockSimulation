package ssg.dto;

public class TradeHistoryResponse {
    private Long    id;
    private String  stockCode;
    private String  stockName;
    private String  tradeType;
    private Integer quantity;
    private Double  price;
    private Double  totalAmount;
    private Double  avgBuyPrice;
    private Double  profitLoss;
    private Double  profitLossRate;
    private String  tradeDate;

    public Long    getId()             { return id; }
    public String  getStockCode()      { return stockCode    != null ? stockCode    : ""; }
    public String  getStockName()      { return stockName    != null ? stockName    : ""; }
    public String  getTradeType()      { return tradeType    != null ? tradeType    : ""; }
    public Integer getQuantity()       { return quantity     != null ? quantity     : 0; }
    public Double  getPrice()          { return price        != null ? price        : 0.0; }
    public Double  getTotalAmount()    { return totalAmount  != null ? totalAmount  : 0.0; }
    public Double  getAvgBuyPrice()    { return avgBuyPrice  != null ? avgBuyPrice  : 0.0; }
    public Double  getProfitLoss()     { return profitLoss; } // null 허용
    public Double  getProfitLossRate() { return profitLossRate; } // null 허용
    public String  getTradeDate()      { return tradeDate    != null ? tradeDate    : ""; }

    public boolean isSell()       { return "SELL".equals(tradeType); }
    public boolean isProfitable() { return profitLoss != null && profitLoss > 0; }
}

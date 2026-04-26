package kr.ac.dankook.javaprogramming.SSG.dto;

import com.google.gson.annotations.SerializedName;

public class StockListResponse {
    @SerializedName("stockName")
    private String stockName;

    @SerializedName("stockCode")
    private String stockCode;

    @SerializedName("currentPrice")
    private Double currentPrice;

    @SerializedName("changeRate")
    private Double changeRate;

    public String getStockName() { return stockName != null ? stockName : ""; }
    public String getStockCode() { return stockCode != null ? stockCode : ""; }
    public Double getCurrentPrice() { return currentPrice != null ? currentPrice : 0.0; }
    public Double getChangeRate() { return changeRate != null ? changeRate : 0.0; }
}

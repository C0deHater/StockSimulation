package ssg.dto;

public class StockHistoryResponse {
    private String date;   // yyyy-MM-dd HH:mm:ss 또는 yyyy-MM-dd
    private Double open;
    private Double high;
    private Double low;
    private Double close;

    // 기술적 지표 (초기 구간은 null)
    private Double ma5;
    private Double ma20;
    private Double rsi;
    private Double macdLine;
    private Double macdSignal;
    private Double bbUpper;
    private Double bbMiddle;
    private Double bbLower;

    public String getDate()       { return date   != null ? date   : ""; }
    public Double getOpen()       { return open   != null ? open   : 0.0; }
    public Double getHigh()       { return high   != null ? high   : 0.0; }
    public Double getLow()        { return low    != null ? low    : 0.0; }
    public Double getClose()      { return close  != null ? close  : 0.0; }

    public Double getMa5()        { return ma5; }
    public Double getMa20()       { return ma20; }
    public Double getRsi()        { return rsi; }
    public Double getMacdLine()   { return macdLine; }
    public Double getMacdSignal() { return macdSignal; }
    public Double getBbUpper()    { return bbUpper; }
    public Double getBbMiddle()   { return bbMiddle; }
    public Double getBbLower()    { return bbLower; }
}

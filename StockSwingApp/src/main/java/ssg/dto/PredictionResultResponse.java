package ssg.dto;

public class PredictionResultResponse {
    private double currentPrice;
    private String trend;
    private double ma5;
    private double ma20;
    private int    bullishProb;
    private int    neutralProb;
    private int    bearishProb;
    private String analysis;
    private String recommendation;
    private int    peakDay;
    private double peakPrice;

    public double getCurrentPrice()   { return currentPrice; }
    public String getTrend()          { return trend != null ? trend : ""; }
    public double getMa5()            { return ma5; }
    public double getMa20()           { return ma20; }
    public int    getBullishProb()    { return bullishProb; }
    public int    getNeutralProb()    { return neutralProb; }
    public int    getBearishProb()    { return bearishProb; }
    public String getAnalysis()       { return analysis != null ? analysis : ""; }
    public String getRecommendation() { return recommendation != null ? recommendation : ""; }
    public int    getPeakDay()        { return peakDay; }
    public double getPeakPrice()      { return peakPrice; }
}

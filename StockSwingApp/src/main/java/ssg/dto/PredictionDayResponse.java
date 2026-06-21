package ssg.dto;

public class PredictionDayResponse {
    private int    day;
    private double expected;
    private double high;
    private double low;

    public int    getDay()      { return day; }
    public double getExpected() { return expected; }
    public double getHigh()     { return high; }
    public double getLow()      { return low; }
}

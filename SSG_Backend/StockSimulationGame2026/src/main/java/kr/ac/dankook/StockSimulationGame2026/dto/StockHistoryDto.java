package kr.ac.dankook.StockSimulationGame2026.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class StockHistoryDto {

    public String date;
    public double open;
    public double high;
    public double low;
    public double close;

    // 기술적 지표 (초기 구간은 null)
    public Double ma5;
    public Double ma20;
    public Double rsi;
    public Double macdLine;
    public Double macdSignal;
    public Double bbUpper;
    public Double bbMiddle;
    public Double bbLower;

    // 캔들만 있는 기본 생성자 (기존 호환)
    public StockHistoryDto(String date, double open, double high, double low, double close) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }
}

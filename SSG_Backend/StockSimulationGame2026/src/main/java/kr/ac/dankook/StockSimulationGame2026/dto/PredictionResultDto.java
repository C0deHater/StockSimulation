package kr.ac.dankook.StockSimulationGame2026.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PredictionResultDto {
    private double currentPrice;   // 현재가
    private String trend;          // 강한 상승 / 완만한 상승 / 횡보 / 완만한 하락 / 강한 하락
    private double ma5;            // 5일 이동평균
    private double ma20;           // 20일 이동평균
    private int    bullishProb;    // 상승 확률 %
    private int    neutralProb;    // 횡보 확률 %
    private int    bearishProb;    // 하락 확률 %
    private String analysis;       // 기술적 분석 한 줄 코멘트
    private String recommendation; // 매도 타이밍 조언
    private int    peakDay;        // 예상 고점일 (D+N)
    private double peakPrice;      // 예상 고점가
}

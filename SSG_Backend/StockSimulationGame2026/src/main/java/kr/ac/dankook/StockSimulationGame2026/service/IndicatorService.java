package kr.ac.dankook.StockSimulationGame2026.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * 기술적 지표 계산 서비스
 * RSI(14), MACD(12,26,9), 볼린저 밴드(20,2), MA5/MA20
 *
 * 반환 리스트는 입력 prices와 같은 길이. 계산 불가 구간은 null 반환.
 */
@Service
public class IndicatorService {

    // ── MA ─────────────────────────────────────────────────

    public List<Double> calcMA(List<Double> prices, int period) {
        List<Double> result = new ArrayList<>();
        for (int i = 0; i < prices.size(); i++) {
            if (i < period - 1) {
                result.add(null);
            } else {
                double sum = 0;
                for (int j = i - period + 1; j <= i; j++) sum += prices.get(j);
                result.add(sum / period);
            }
        }
        return result;
    }

    // ── RSI(Wilder's smoothing) ────────────────────────────

    public List<Double> calcRSI(List<Double> prices, int period) {
        List<Double> result = new ArrayList<>();
        if (prices.size() < period + 1) {
            for (int i = 0; i < prices.size(); i++) result.add(null);
            return result;
        }

        List<Double> changes = new ArrayList<>();
        for (int i = 1; i < prices.size(); i++)
            changes.add(prices.get(i) - prices.get(i - 1));

        for (int i = 0; i < period; i++) result.add(null);

        double avgGain = 0, avgLoss = 0;
        for (int i = 0; i < period; i++) {
            double c = changes.get(i);
            if (c > 0) avgGain += c; else avgLoss -= c;
        }
        avgGain /= period;
        avgLoss /= period;
        result.add(rsi(avgGain, avgLoss));

        for (int i = period; i < changes.size(); i++) {
            double c = changes.get(i);
            avgGain = (avgGain * (period - 1) + (c > 0 ? c : 0)) / period;
            avgLoss = (avgLoss * (period - 1) + (c < 0 ? -c : 0)) / period;
            result.add(rsi(avgGain, avgLoss));
        }
        return result;
    }

    private double rsi(double avgGain, double avgLoss) {
        if (avgLoss == 0) return 100.0;
        double rs = avgGain / avgLoss;
        return 100 - 100.0 / (1 + rs);
    }

    // ── EMA ────────────────────────────────────────────────

    public List<Double> calcEMA(List<Double> prices, int period) {
        List<Double> result = new ArrayList<>();
        double k = 2.0 / (period + 1);
        double ema = 0;
        boolean started = false;
        double seedSum = 0;
        int seedCount = 0;

        for (int i = 0; i < prices.size(); i++) {
            double p = prices.get(i);
            if (!started) {
                seedSum += p;
                seedCount++;
                if (seedCount == period) {
                    ema = seedSum / period;
                    started = true;
                    result.add(ema);
                } else {
                    result.add(null);
                }
            } else {
                ema = p * k + ema * (1 - k);
                result.add(ema);
            }
        }
        return result;
    }

    // ── MACD(12, 26, 9) ────────────────────────────────────

    /** 반환: List<double[]> — [0]=MACD선, [1]=신호선, [2]=히스토그램 / null=계산 불가 */
    public List<double[]> calcMACD(List<Double> prices, int fast, int slow, int signalPeriod) {
        List<Double> emaFast = calcEMA(prices, fast);
        List<Double> emaSlow = calcEMA(prices, slow);

        // MACD 선 (null 포함)
        List<Double> macdRaw = new ArrayList<>();
        for (int i = 0; i < prices.size(); i++) {
            Double ef = emaFast.get(i), es = emaSlow.get(i);
            macdRaw.add((ef != null && es != null) ? ef - es : null);
        }

        // null이 아닌 MACD 값만 모아 EMA(9) 계산
        List<Double> macdNotNull = new ArrayList<>();
        int macdStartIdx = -1;
        for (int i = 0; i < macdRaw.size(); i++) {
            if (macdRaw.get(i) != null) {
                macdNotNull.add(macdRaw.get(i));
                if (macdStartIdx < 0) macdStartIdx = i;
            }
        }

        List<Double> signalEMA = macdNotNull.isEmpty()
                ? new ArrayList<>() : calcEMA(macdNotNull, signalPeriod);

        List<double[]> result = new ArrayList<>();
        int offset = 0;
        for (int i = 0; i < prices.size(); i++) {
            Double macd = macdRaw.get(i);
            if (macd == null) {
                result.add(null);
            } else {
                int relIdx = i - macdStartIdx;
                Double sig = (relIdx < signalEMA.size()) ? signalEMA.get(relIdx) : null;
                if (sig == null) result.add(new double[]{macd, Double.NaN, Double.NaN});
                else             result.add(new double[]{macd, sig, macd - sig});
                offset++;
            }
        }
        return result;
    }

    // ── 볼린저 밴드(20, 2σ) ────────────────────────────────

    /** 반환: List<double[]> — [0]=상단, [1]=중간(MA20), [2]=하단 / null=계산 불가 */
    public List<double[]> calcBollinger(List<Double> prices, int period, double mult) {
        List<Double> ma = calcMA(prices, period);
        List<double[]> result = new ArrayList<>();
        for (int i = 0; i < prices.size(); i++) {
            Double mid = ma.get(i);
            if (mid == null) {
                result.add(null);
                continue;
            }
            double variance = 0;
            for (int j = i - period + 1; j <= i; j++)
                variance += Math.pow(prices.get(j) - mid, 2);
            double std = Math.sqrt(variance / period);
            result.add(new double[]{mid + mult * std, mid, mid - mult * std});
        }
        return result;
    }

    // ── 종합 신호 생성 ─────────────────────────────────────

    /**
     * RSI, MACD, 볼린저 기반 매수/매도 신호 생성
     * @return int: +2=강한매수 +1=매수관심 0=관망 -1=매도관심 -2=강한매도
     */
    public int generateSignalScore(double rsi, double[] macd, double close, double[] bb) {
        int score = 0;

        // RSI 신호
        if (rsi < 30)      score += 2;  // 과매도: 강한 매수
        else if (rsi < 40) score += 1;
        else if (rsi > 70) score -= 2;  // 과매수: 강한 매도
        else if (rsi > 60) score -= 1;

        // MACD 신호 (신호선과의 교차)
        if (macd != null && !Double.isNaN(macd[1])) {
            double diff = macd[0] - macd[1]; // MACD - Signal
            if (diff > 0 && macd[2] > 0) score += 1;    // 골든크로스 + 양수 히스토
            else if (diff < 0 && macd[2] < 0) score -= 1; // 데드크로스 + 음수 히스토
        }

        // 볼린저 밴드 신호
        if (bb != null) {
            double upper = bb[0], lower = bb[2], width = upper - lower;
            if (width > 0) {
                double pos = (close - lower) / width; // 0=하단, 1=상단
                if (pos < 0.15)      score += 1; // 하단 근접
                else if (pos > 0.85) score -= 1; // 상단 근접
            }
        }

        return Math.max(-3, Math.min(3, score));
    }

    /** 신호 점수를 한국어 라벨로 변환 */
    public String scoreToLabel(int score) {
        if      (score >= 2)  return "▶ 매수 검토";
        else if (score == 1)  return "◐ 매수 관심";
        else if (score == 0)  return "━ 관망";
        else if (score == -1) return "◑ 매도 관심";
        else                  return "▶ 매도 검토";
    }
}

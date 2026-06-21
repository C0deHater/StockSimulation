package kr.ac.dankook.StockSimulationGame2026.service;

import kr.ac.dankook.StockSimulationGame2026.domain.Stock;
import kr.ac.dankook.StockSimulationGame2026.dto.PredictionResultDto;
import kr.ac.dankook.StockSimulationGame2026.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 기술적 지표(RSI, MACD, 볼린저 밴드) 기반 예측 및 매매 조언 서비스
 * Monte Carlo 난수 예측 대신 실제 지표 신호와 그 의미를 교육적으로 설명한다.
 */
@Service
@RequiredArgsConstructor
public class PredictionService {

    private final StockRepository  stockRepository;
    private final StockService     stockService;
    private final IndicatorService indicatorService;

    private static final int HISTORY = 80;
    private static final DecimalFormat NF = new DecimalFormat("#,###");

    public PredictionResultDto predict(String stockCode) {

        // ── 1. 가격 수집 ─────────────────────────────────
        List<Double> prices = stockRepository.findAllByStockCodeOrderByStDateAsc(stockCode)
                .stream()
                .filter(s -> s.getStClose() != null && s.getStClose() > 0)
                .map(Stock::getStClose)
                .collect(Collectors.toList());

        double currentPrice = stockService.getCurrentPriceMap()
                .getOrDefault(stockCode, prices.isEmpty() ? 50000.0 : prices.get(prices.size() - 1));

        if (prices.size() > HISTORY) prices = prices.subList(prices.size() - HISTORY, prices.size());

        if (prices.size() < 20) return buildDataInsufficient(currentPrice);

        // ── 2. 지표 계산 ──────────────────────────────────
        List<Double> ma5List  = indicatorService.calcMA(prices, 5);
        List<Double> ma20List = indicatorService.calcMA(prices, 20);
        List<Double> rsiList  = indicatorService.calcRSI(prices, 14);
        List<double[]> macdList = indicatorService.calcMACD(prices, 12, 26, 9);
        List<double[]> bbList   = indicatorService.calcBollinger(prices, 20, 2.0);

        int    last     = prices.size() - 1;
        Double ma5      = ma5List.get(last);
        Double ma20     = ma20List.get(last);
        Double rsi      = rsiList.get(last);
        double[] macd   = macdList.get(last);
        double[] bb     = bbList.get(last);

        if (ma5 == null || ma20 == null) return buildDataInsufficient(currentPrice);

        // ── 3. 추세 판단 ──────────────────────────────────
        String trend, trendAnalysis;
        if (currentPrice > ma5 && ma5 > ma20) {
            trend = "강한 상승"; trendAnalysis = "단기·중기 이동평균 모두 상향 배열 — 상승 모멘텀이 강합니다.";
        } else if (currentPrice > ma20) {
            trend = "완만한 상승"; trendAnalysis = "중기 이동평균 위에서 거래 중이나 단기 조정 가능성이 있습니다.";
        } else if (currentPrice < ma5 && ma5 < ma20) {
            trend = "강한 하락"; trendAnalysis = "단기·중기 이동평균 모두 하향 배열 — 추가 하락에 유의하세요.";
        } else if (currentPrice < ma20) {
            trend = "완만한 하락"; trendAnalysis = "중기 이동평균 아래에서 거래 중 — 반등 신호를 확인하세요.";
        } else {
            trend = "횡보"; trendAnalysis = "뚜렷한 방향성 없이 이동평균 부근에서 박스권 움직임입니다.";
        }

        // ── 4. 각 지표 신호 해석 + 교육 설명 ─────────────
        StringBuilder analysis = new StringBuilder();
        int buyScore = 0, sellScore = 0;

        // 4-1. RSI
        String rsiText;
        if (rsi != null) {
            if (rsi < 30) {
                rsiText = String.format("RSI(14): %.1f → 📗 과매도 구간 (30 이하)\n"
                        + "   매도 세력이 과도하게 강해진 상태로, 단기 반등 가능성이 높습니다.\n"
                        + "   ✔ 매수 진입을 고려해볼 시점입니다.", rsi);
                buyScore += 2;
            } else if (rsi < 40) {
                rsiText = String.format("RSI(14): %.1f → 📗 매수 관심 구간 (30~40)\n"
                        + "   하락 압력이 줄어드는 구간으로 저점 형성 가능성이 있습니다.", rsi);
                buyScore += 1;
            } else if (rsi > 70) {
                rsiText = String.format("RSI(14): %.1f → 📕 과매수 구간 (70 이상)\n"
                        + "   매수 세력이 과도하게 강해진 상태로, 단기 조정(하락)이 올 수 있습니다.\n"
                        + "   ✔ 이익 실현(매도)을 검토할 시점입니다.", rsi);
                sellScore += 2;
            } else if (rsi > 60) {
                rsiText = String.format("RSI(14): %.1f → 📕 매도 관심 구간 (60~70)\n"
                        + "   과매수 구간에 가까워지고 있어 상승 탄력이 약해질 수 있습니다.", rsi);
                sellScore += 1;
            } else {
                rsiText = String.format("RSI(14): %.1f → ➡ 중립 구간 (40~60)\n"
                        + "   명확한 방향성 신호 없음 — 추세를 확인하며 대응하세요.", rsi);
            }
        } else {
            rsiText = "RSI(14): 데이터 부족 (최소 15개 이상 필요)";
        }
        analysis.append(rsiText).append("\n\n");

        // 4-2. MACD
        String macdText;
        if (macd != null && !Double.isNaN(macd[1])) {
            double diff = macd[0] - macd[1]; // MACD - Signal
            if (diff > 0 && macd[2] > 0) {
                macdText = String.format("MACD(12,26,9): %.1f / 신호선 %.1f → 📗 골든크로스\n"
                        + "   MACD선이 신호선을 상향 돌파 — 상승 전환 신호입니다.\n"
                        + "   ✔ 매수 타이밍을 검토하세요.", macd[0], macd[1]);
                buyScore += 1;
            } else if (diff < 0 && macd[2] < 0) {
                macdText = String.format("MACD(12,26,9): %.1f / 신호선 %.1f → 📕 데드크로스\n"
                        + "   MACD선이 신호선을 하향 돌파 — 하락 전환 신호입니다.\n"
                        + "   ✔ 매도 또는 손절을 검토하세요.", macd[0], macd[1]);
                sellScore += 1;
            } else if (diff > 0) {
                macdText = String.format("MACD(12,26,9): %.1f / 신호선 %.1f → ↗ 상승 모멘텀 유지\n"
                        + "   MACD가 신호선 위에서 상승 중 — 현재 추세가 지속될 가능성이 높습니다.", macd[0], macd[1]);
            } else {
                macdText = String.format("MACD(12,26,9): %.1f / 신호선 %.1f → ↘ 하락 모멘텀 유지\n"
                        + "   MACD가 신호선 아래에서 하락 중 — 매도 우위 상황입니다.", macd[0], macd[1]);
            }
        } else {
            macdText = "MACD: 데이터 부족 (최소 35개 이상 필요)";
        }
        analysis.append(macdText).append("\n\n");

        // 4-3. 볼린저 밴드
        String bbText;
        if (bb != null) {
            double upper = bb[0], middle = bb[1], lower = bb[2];
            double bandwidth = upper - lower;
            double pos = bandwidth > 0 ? (currentPrice - lower) / bandwidth : 0.5;
            if (pos > 0.85) {
                bbText = String.format("볼린저 밴드(20,2σ): 상단 %s원 근접\n"
                        + "   가격이 상단 밴드에 근접 — 저항선에서 눌릴 가능성이 있습니다.\n"
                        + "   ✔ 단기 매도 또는 이익 실현을 고려하세요.", NF.format((long) upper));
                sellScore += 1;
            } else if (pos < 0.15) {
                bbText = String.format("볼린저 밴드(20,2σ): 하단 %s원 근접\n"
                        + "   가격이 하단 밴드에 근접 — 지지선 근방으로 반등 가능성이 있습니다.\n"
                        + "   ✔ 분할 매수를 고려해볼 시점입니다.", NF.format((long) lower));
                buyScore += 1;
            } else if (pos > 0.5) {
                bbText = String.format("볼린저 밴드(20,2σ): 중간~상단 (%.0f%%)\n"
                        + "   중간 이상에서 거래 중 — 상승 지속 또는 상단 저항 주의.", pos * 100);
            } else {
                bbText = String.format("볼린저 밴드(20,2σ): 중간~하단 (%.0f%%)\n"
                        + "   중간 이하에서 거래 중 — 하락 지속 또는 하단 지지 확인 필요.", pos * 100);
            }
            bbText += String.format("\n   (상단: %s원 / 중심: %s원 / 하단: %s원)",
                    NF.format((long) upper), NF.format((long) middle), NF.format((long) lower));
        } else {
            bbText = "볼린저 밴드: 데이터 부족 (최소 20개 이상 필요)";
        }
        analysis.append(bbText).append("\n\n");

        // ── 5. 종합 신호 + 확률 산출 ─────────────────────
        int    totalScore = buyScore - sellScore;
        int    bullishProb, bearishProb, neutralProb;
        String signalLabel;

        if      (totalScore >= 3)  { bullishProb = 75; bearishProb = 10; signalLabel = "강한 매수 신호"; }
        else if (totalScore == 2)  { bullishProb = 65; bearishProb = 15; signalLabel = "매수 검토 신호"; }
        else if (totalScore == 1)  { bullishProb = 55; bearishProb = 20; signalLabel = "매수 관심 신호"; }
        else if (totalScore == 0)  { bullishProb = 40; bearishProb = 30; signalLabel = "관망 (중립)"; }
        else if (totalScore == -1) { bullishProb = 20; bearishProb = 55; signalLabel = "매도 관심 신호"; }
        else if (totalScore == -2) { bullishProb = 15; bearishProb = 65; signalLabel = "매도 검토 신호"; }
        else                       { bullishProb = 10; bearishProb = 75; signalLabel = "강한 매도 신호"; }
        neutralProb = 100 - bullishProb - bearishProb;

        // ── 6. 매도 타이밍 조언 ──────────────────────────
        String recommendation = buildRecommendation(
                totalScore, signalLabel, currentPrice, rsi, macd, bb, trend);

        // ── 7. 투자 교육 한 줄 요약 (analysis 첫 줄) ─────
        String analysisStr = "[ 기술적 지표 분석 ]\n"
                + trendAnalysis + "\n\n"
                + analysis.toString().trim();

        return new PredictionResultDto(
                currentPrice, trend + " (" + signalLabel + ")", ma5, ma20,
                bullishProb, neutralProb, bearishProb,
                analysisStr, recommendation,
                0, currentPrice // peakDay, peakPrice - indicator 기반으로 의미 없어 0으로
        );
    }

    // ── 매도 타이밍 조언 생성 ───────────────────────────────

    private String buildRecommendation(int totalScore, String signal,
                                        double currentPrice, Double rsi,
                                        double[] macd, double[] bb, String trend) {
        StringBuilder sb = new StringBuilder();
        sb.append("[ 종합 의견: ").append(signal).append(" ]\n");
        sb.append("─────────────────────────────────\n");

        if (totalScore >= 2) {
            sb.append("복수의 지표가 매수 신호를 보이고 있습니다.\n\n");
            sb.append("▶ 행동 제안\n");
            sb.append("   현재가 진입 또는 분할 매수를 고려하세요.\n");
            if (rsi != null && rsi < 35)
                sb.append("   RSI가 과매도 구간이므로 반등 후 상승 시 수익이 날 수 있습니다.\n");
            sb.append("\n▶ 목표 매도 시점\n");
            sb.append("   RSI 60~70 구간 진입 시 (현재 대비 상당한 상승)\n");
            if (bb != null)
                sb.append("   볼린저 밴드 상단(").append(NF.format((long) bb[0])).append("원) 근접 시\n");
            sb.append("\n▶ 손절 기준\n");
            if (bb != null)
                sb.append("   볼린저 밴드 하단(").append(NF.format((long) bb[2])).append("원) 이탈 시\n");
            else
                sb.append("   진입가 대비 -5% 이탈 시 손절 고려\n");
        } else if (totalScore <= -2) {
            sb.append("복수의 지표가 매도/하락 신호를 보이고 있습니다.\n\n");
            sb.append("▶ 행동 제안\n");
            if (trend.contains("하락"))
                sb.append("   보유 중이라면 추가 손실 방지를 위해 매도를 검토하세요.\n");
            else
                sb.append("   신규 진입은 자제하고, 보유 물량 단계적 매도를 고려하세요.\n");
            if (rsi != null && rsi > 65)
                sb.append("   RSI 과매수 구간으로 이익 실현이 유리한 구간입니다.\n");
            sb.append("\n▶ 반등 시 재진입 조건\n");
            sb.append("   RSI 40 이하로 하락 후 반등 확인\n");
            sb.append("   MACD 골든크로스(신호선 상향 돌파) 확인 후 진입\n");
        } else if (totalScore == 1) {
            sb.append("약한 매수 신호가 있으나 아직 확신 구간은 아닙니다.\n\n");
            sb.append("▶ 행동 제안\n");
            sb.append("   소량 분할 매수를 시작하거나 추가 신호를 기다리세요.\n");
            sb.append("   MACD가 골든크로스를 보이거나 RSI가 30 이하로 떨어지면\n");
            sb.append("   추가 매수를 고려하세요.\n");
        } else if (totalScore == -1) {
            sb.append("약한 매도 신호가 있으나 즉각적인 매도보다는 관찰이 필요합니다.\n\n");
            sb.append("▶ 행동 제안\n");
            sb.append("   추가 상승은 제한적일 수 있습니다.\n");
            sb.append("   보유 중이라면 이익의 일부를 실현(부분 매도)하고\n");
            sb.append("   나머지는 손절가를 설정 후 관망하세요.\n");
        } else {
            sb.append("지표들이 명확한 방향성 없이 혼재하고 있습니다.\n\n");
            sb.append("▶ 행동 제안\n");
            sb.append("   새로운 매수 진입은 보류하고 관망하는 것이 유리합니다.\n");
            sb.append("   추세가 뚜렷해질 때까지 (RSI < 35 또는 > 65,\n");
            sb.append("   MACD 골든/데드크로스 발생) 기다리세요.\n");
        }

        sb.append("\n─────────────────────────────────\n");
        sb.append("※ 이 분석은 기술적 지표 기반 참고 정보입니다.\n");
        sb.append("  실제 투자는 기업 가치, 뉴스, 시장 상황 등을\n");
        sb.append("  종합적으로 고려해야 합니다.");
        return sb.toString();
    }

    private PredictionResultDto buildDataInsufficient(double price) {
        return new PredictionResultDto(
                price, "데이터 부족", price, price, 33, 34, 33,
                "과거 데이터가 부족합니다. 시뮬레이션이 더 진행된 후 다시 시도하세요.",
                "데이터가 최소 20개 이상 쌓인 후 다시 시도해 주세요.", 0, price
        );
    }
}

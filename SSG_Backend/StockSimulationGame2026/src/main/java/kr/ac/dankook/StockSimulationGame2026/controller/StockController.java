package kr.ac.dankook.StockSimulationGame2026.controller;

import kr.ac.dankook.StockSimulationGame2026.dto.PredictionResultDto;
import kr.ac.dankook.StockSimulationGame2026.dto.StockHistoryDto;
import kr.ac.dankook.StockSimulationGame2026.repository.StockRepository;
import kr.ac.dankook.StockSimulationGame2026.service.IndicatorService;
import kr.ac.dankook.StockSimulationGame2026.service.PredictionService;
import kr.ac.dankook.StockSimulationGame2026.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockRepository   stockRepository;
    private final StockService      stockService;
    private final PredictionService    predictionService;
    private final IndicatorService     indicatorService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ── 종목 목록 (lastPriceMap 직접 사용 — StockLive 불필요) ──

    @GetMapping("")
    public List<StockListDto> getStockList() {
        Map<String, Double> prices     = stockService.getCurrentPriceMap();
        Map<String, Double> openPrices = stockService.getOpenPriceMap();

        return prices.entrySet().stream()
                .map(e -> {
                    String code = e.getKey();
                    double cur  = e.getValue();
                    Double open = openPrices.get(code);
                    double changeRate = (open != null && open > 0)
                            ? (cur - open) / open * 100.0 : 0.0;
                    return new StockListDto(
                            stockService.getStockName(code), code, cur, changeRate);
                })
                .filter(s -> !s.stockName.equals("알 수 없는 종목"))
                .sorted(Comparator.comparing(s -> s.stockName))
                .collect(Collectors.toList());
    }

    // ── 게임 상태 ─────────────────────────────────────────

    @GetMapping("/market-status")
    public Map<String, Object> getMarketStatus() {
        java.time.LocalDateTime gdt = stockService.getGameDateTime();
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("isOpen",    stockService.isGameMarketOpen());
        result.put("gameTime",  gdt.toLocalTime().toString());
        result.put("gameDate",  gdt.toLocalDate().toString());
        result.put("gameDay",   stockService.getGameDay());
        result.put("timeSpeed", stockService.getTimeSpeed());
        result.put("message",   stockService.isGameMarketOpen() ? "장 중" : "장 마감");
        return result;
    }

    @GetMapping("/speed/{speed}")
    public String setSpeed(@PathVariable int speed) {
        stockService.setTimeSpeed(speed);
        return "배속 " + speed + "배로 설정됨";
    }

    @PostMapping("/reset")
    public String resetSimulation() {
        stockService.resetLiveSimulation();
        return "시뮬레이션이 성공적으로 초기화되었습니다.";
    }

    // ── 캔들 히스토리 + 기술적 지표 ──────────────────────
    //
    // 과거 데이터 재생 방식:
    //   현재 게임 날짜(gameDate)까지의 Stock 레코드만 보여준다.
    //   사용자는 아직 오지 않은 미래 캔들을 볼 수 없다.
    //   당일 진행 중인 봉은 현재가(intraday)로 실시간 갱신된다.

    @GetMapping("/{symbol}/history")
    public List<StockHistoryDto> getStockHistory(@PathVariable String symbol) {
        String code     = symbol.contains(".") ? symbol.split("\\.")[0] : symbol;
        LocalDate gameDate = stockService.getGameDateTime().toLocalDate();

        // ① 게임 현재 날짜까지만 과거 캔들 공개
        List<StockHistoryDto> combined = stockRepository
                .findAllByStockCodeOrderByStDateAsc(code).stream()
                .filter(h -> h.getStDate() != null
                        && !h.getStDate().toLocalDate().isAfter(gameDate))
                .map(h -> {
                    double c = h.getStClose() != null ? h.getStClose() : 0.0;
                    return new StockHistoryDto(
                            h.getStDate().format(formatter),
                            h.getStOpen()  != null ? h.getStOpen()  : c,
                            h.getStHigh()  != null ? h.getStHigh()  : c,
                            h.getStLow()   != null ? h.getStLow()   : c, c);
                })
                .collect(Collectors.toCollection(ArrayList::new));

        // ② 당일 봉 → 현재 인트라데이 가격으로 close/high/low 갱신
        Double livePrice = stockService.getCurrentPriceMap().get(code);
        if (livePrice != null && livePrice > 0 && !combined.isEmpty()) {
            StockHistoryDto last = combined.get(combined.size() - 1);
            combined.set(combined.size() - 1,
                    new StockHistoryDto(last.date, last.open,
                            Math.max(last.high, livePrice),
                            Math.min(last.low,  livePrice),
                            livePrice));
        }

        // ③ 기술적 지표 계산 및 주입
        if (!combined.isEmpty()) {
            List<Double> closes = combined.stream().map(c -> c.close).collect(Collectors.toList());
            attachIndicators(combined, closes);
        }

        return combined;
    }

    /** 캔들 리스트에 RSI, MACD, 볼린저, MA 값을 주입 */
    private void attachIndicators(List<StockHistoryDto> candles, List<Double> closes) {
        List<Double> ma5   = indicatorService.calcMA(closes, 5);
        List<Double> ma20  = indicatorService.calcMA(closes, 20);
        List<Double> rsi   = indicatorService.calcRSI(closes, 14);
        List<double[]> macd = indicatorService.calcMACD(closes, 12, 26, 9);
        List<double[]> bb   = indicatorService.calcBollinger(closes, 20, 2.0);

        for (int i = 0; i < candles.size(); i++) {
            StockHistoryDto c = candles.get(i);
            c.setMa5(ma5.get(i));
            c.setMa20(ma20.get(i));
            c.setRsi(rsi.get(i));
            double[] m = macd.get(i);
            if (m != null) {
                c.setMacdLine(m[0]);
                c.setMacdSignal(Double.isNaN(m[1]) ? null : m[1]);
            }
            double[] b = bb.get(i);
            if (b != null) {
                c.setBbUpper(b[0]);
                c.setBbMiddle(b[1]);
                c.setBbLower(b[2]);
            }
        }
    }

    // ── 예측 ──────────────────────────────────────────────

    @GetMapping("/{symbol}/predict")
    public PredictionResultDto predict(@PathVariable String symbol) {
        String code = symbol.contains(".") ? symbol.split("\\.")[0] : symbol;
        return predictionService.predict(code);
    }

    // ── 내부 DTO ──────────────────────────────────────────

    public static class StockListDto {
        public String stockName;
        public String stockCode;
        public Double currentPrice;
        public Double changeRate;

        public StockListDto(String stockName, String stockCode,
                            Double currentPrice, Double changeRate) {
            this.stockName    = stockName;
            this.stockCode    = stockCode;
            this.currentPrice = currentPrice;
            this.changeRate   = changeRate;
        }
    }
}

package kr.ac.dankook.StockSimulationGame2026.service;

import kr.ac.dankook.StockSimulationGame2026.domain.Account;
import kr.ac.dankook.StockSimulationGame2026.domain.Stock;
import kr.ac.dankook.StockSimulationGame2026.domain.StockMetadata;
import kr.ac.dankook.StockSimulationGame2026.repository.AccountRepository;
import kr.ac.dankook.StockSimulationGame2026.repository.PortfolioRepository;
import kr.ac.dankook.StockSimulationGame2026.repository.StockMetadataRepository;
import kr.ac.dankook.StockSimulationGame2026.repository.StockRepository;
import kr.ac.dankook.StockSimulationGame2026.repository.TradeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository         stockRepository;
    private final StockMetadataRepository metadataRepository;
    private final AccountRepository       accountRepository;
    private final PortfolioRepository     portfolioRepository;
    private final TradeHistoryRepository  tradeHistoryRepository;

    // ── 가격 맵 ──────────────────────────────────────────────
    private final Map<String, Double> volatilityMap = new ConcurrentHashMap<>();
    private final Map<String, Double> lastPriceMap  = new ConcurrentHashMap<>();
    private final Map<String, Double> openPriceMap  = new ConcurrentHashMap<>();
    private final Map<String, String> stockNameMap  = new ConcurrentHashMap<>();

    // ── 과거 데이터 재생용 ────────────────────────────────────
    private final Map<String, Map<LocalDate, Stock>> historicalDataMap = new ConcurrentHashMap<>();
    private List<LocalDate> allTradingDates = new ArrayList<>();
    private int currentDateIndex = 0;

    // ── 게임 시간 ──────────────────────────────────────────────
    private static final LocalTime MARKET_OPEN  = LocalTime.of(9, 0);
    private static final LocalTime MARKET_CLOSE = LocalTime.of(15, 30);

    private LocalDateTime gameDateTime;
    private int           gameDay   = 1;
    private int           timeSpeed = 60;

    // ── 공개 메서드 ───────────────────────────────────────────

    public LocalDateTime       getGameDateTime()    { return gameDateTime; }
    public int                 getGameDay()         { return gameDay; }
    public int                 getTimeSpeed()       { return timeSpeed; }
    public Map<String, Double> getOpenPriceMap()    { return openPriceMap; }
    public Map<String, Double> getCurrentPriceMap() { return lastPriceMap; }
    public List<LocalDate>     getAllTradingDates()  { return allTradingDates; }
    public int                 getCurrentDateIndex() { return currentDateIndex; }

    public boolean isGameMarketOpen() {
        LocalTime t = gameDateTime.toLocalTime();
        return !t.isBefore(MARKET_OPEN) && t.isBefore(MARKET_CLOSE);
    }

    public void setTimeSpeed(int speed) { this.timeSpeed = Math.max(1, speed); }

    public String getStockName(String code) {
        return stockNameMap.getOrDefault(code, "알 수 없는 종목");
    }

    // ── 초기화 ───────────────────────────────────────────────

    @PostConstruct
    public void init() {
        Set<LocalDate> allDates = new TreeSet<>();

        for (StockMetadata meta : metadataRepository.findAll()) {
            String code = meta.getStockCode();
            volatilityMap.put(code, meta.getBaseVolatility());
            stockNameMap.put(code, meta.getStockName());

            Map<LocalDate, Stock> dateMap = new TreeMap<>();
            for (Stock s : stockRepository.findAllByStockCodeOrderByStDateAsc(code)) {
                if (s.getStDate() != null && s.getStClose() != null && s.getStClose() > 0) {
                    LocalDate d = s.getStDate().toLocalDate();
                    dateMap.put(d, s);
                    allDates.add(d);
                }
            }
            historicalDataMap.put(code, dateMap);
        }

        allTradingDates = new ArrayList<>(allDates);

        if (!allTradingDates.isEmpty()) {
            gameDateTime = allTradingDates.get(0).atTime(MARKET_OPEN);
            updatePricesForCurrentDate();
            System.out.println("📅 [재생 시작] " + allTradingDates.get(0)
                    + " ~ " + allTradingDates.get(allTradingDates.size() - 1)
                    + " (총 " + allTradingDates.size() + "거래일)");
        } else {
            gameDateTime = nearestWeekday(LocalDate.now()).atTime(MARKET_OPEN);
        }
    }

    // ── 현재 날짜의 시가로 가격 맵 초기화 ────────────────────

    private void updatePricesForCurrentDate() {
        if (allTradingDates.isEmpty() || currentDateIndex >= allTradingDates.size()) return;
        LocalDate date = allTradingDates.get(currentDateIndex);
        for (Map.Entry<String, Map<LocalDate, Stock>> entry : historicalDataMap.entrySet()) {
            Stock s = entry.getValue().get(date);
            if (s == null) continue;
            double open = s.getStOpen() != null ? s.getStOpen() : s.getStClose();
            lastPriceMap.put(entry.getKey(), open);
            openPriceMap.put(entry.getKey(), open);
        }
    }

    // ── 시뮬레이션 엔진 (1초마다 실행) ───────────────────────

    @Scheduled(fixedRate = 1000)
    @Transactional
    public void runRealTimeSimulation() {
        if (allTradingDates.isEmpty()) return;

        gameDateTime = gameDateTime.plusSeconds(timeSpeed);

        // 장 마감 → 다음 거래일 전환
        if (!gameDateTime.toLocalTime().isBefore(MARKET_CLOSE)) {
            currentDateIndex++;
            if (currentDateIndex >= allTradingDates.size()) {
                currentDateIndex = 0;
                System.out.println("♻️ [히스토리 순환] 처음으로 돌아갑니다.");
            }
            LocalDate nextDate = allTradingDates.get(currentDateIndex);
            gameDateTime = nextDate.atTime(MARKET_OPEN);
            gameDay++;
            openPriceMap.clear();
            updatePricesForCurrentDate();
            System.out.println("📅 Day " + gameDay + " — " + nextDate);
            return;
        }

        if (!isGameMarketOpen()) return;

        // 장중 — 실제 OHLC 범위 안에서 현재가 보간
        LocalDate date     = allTradingDates.get(currentDateIndex);
        double    progress = calcDayProgress();
        Random    rng      = new Random();

        for (String code : lastPriceMap.keySet()) {
            Stock s = historicalDataMap.get(code) != null
                    ? historicalDataMap.get(code).get(date) : null;
            if (s == null || s.getStClose() == null) continue;

            double open  = s.getStOpen()  != null ? s.getStOpen()  : s.getStClose();
            double close = s.getStClose();
            double high  = s.getStHigh()  != null ? s.getStHigh()  : Math.max(open, close);
            double low   = s.getStLow()   != null ? s.getStLow()   : Math.min(open, close);

            double base  = open + (close - open) * progress;
            double noise = rng.nextGaussian() * (high - low) * 0.12;
            double price = Math.max(low, Math.min(high, base + noise));
            price = Math.max((long)(price / 10) * 10, 1000);
            lastPriceMap.put(code, price);
        }
    }

    private double calcDayProgress() {
        long openSec  = MARKET_OPEN.toSecondOfDay();
        long closeSec = MARKET_CLOSE.toSecondOfDay();
        long curSec   = gameDateTime.toLocalTime().toSecondOfDay();
        return Math.max(0, Math.min(1, (double)(curSec - openSec) / (closeSec - openSec)));
    }

    // ── 초기화 ─────────────────────────────────────────────────

    @Transactional
    public void resetLiveSimulation() {
        portfolioRepository.deleteAllInBatch();
        tradeHistoryRepository.deleteAll();

        List<Account> accounts = accountRepository.findAll();
        for (Account account : accounts) account.setBalance(10_000_000.0);
        accountRepository.saveAll(accounts);

        lastPriceMap.clear();
        openPriceMap.clear();
        currentDateIndex = 0;
        gameDay = 1;

        if (!allTradingDates.isEmpty()) {
            gameDateTime = allTradingDates.get(0).atTime(MARKET_OPEN);
            updatePricesForCurrentDate();
        } else {
            gameDateTime = nearestWeekday(LocalDate.now()).atTime(MARKET_OPEN);
        }
        System.out.println("♻️ [리셋] Day 1 — " + gameDateTime.toLocalDate() + " 재시작");
    }

    // ── 헬퍼 ───────────────────────────────────────────────────

    private LocalDate nearestWeekday(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY
            || date.getDayOfWeek() == DayOfWeek.SUNDAY)
            date = date.plusDays(1);
        return date;
    }
}

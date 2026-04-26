package kr.ac.dankook.StockSimulationGame2026.service;

import kr.ac.dankook.StockSimulationGame2026.domain.*;
import kr.ac.dankook.StockSimulationGame2026.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockLiveRepository stockLiveRepository;
    private final StockMetadataRepository metadataRepository;

    // 메모리 캐시: 실시간 주가 엔진의 핵심 데이터
    private final Map<String, Double> volatilityMap = new ConcurrentHashMap<>();
    private final Map<String, Double> lastPriceMap = new ConcurrentHashMap<>();
    private final Map<String, String> stockNameMap = new ConcurrentHashMap<>();

    private int simulationSpeed = 1;

    public String getStockName(String code) {
        return stockNameMap.getOrDefault(code, "알 수 없는 종목");
    }

    public Map<String, Double> getCurrentPriceMap() {
        return this.lastPriceMap;
    }

    public void setSimulationSpeed(int speed) {
        this.simulationSpeed = speed;
    }

    /**
     * 서버 시작 시 stock_metadata에서 종목 정보를 로드하여 시뮬레이션 준비
     */
    @PostConstruct
    public void init() {
        List<StockMetadata> metadataList = metadataRepository.findAll();
        for (StockMetadata meta : metadataList) {
            String code = meta.getStockCode();
            volatilityMap.put(code, meta.getBaseVolatility());
            stockNameMap.put(code, meta.getStockName());

            // 최신 가격 결정: stock_live → stock_history → 기본값 순서
            stockLiveRepository.findFirstByStockCodeOrderByStDateDesc(code)
                    .map(StockLive::getStClose)
                    .or(() -> stockRepository.findFirstByStockCodeOrderByStDateDesc(code).map(Stock::getStClose))
                    .ifPresentOrElse(
                            price -> lastPriceMap.put(code, price),
                            () -> lastPriceMap.put(code, 50000.0)
                    );
        }
    }

    /**
     * 1초마다 실행되는 실시간 주가 시뮬레이션 엔진
     * 기하 브라운 운동(GBM) 기반 가격 변동
     */
    @Scheduled(fixedRate = 1000)
    @Transactional
    public void runRealTimeSimulation() {
        Random random = new Random();

        for (String code : lastPriceMap.keySet()) {
            double currentPrice = lastPriceMap.get(code);
            double sigma = volatilityMap.getOrDefault(code, 0.01);

            // 배속만큼 변동 반복 적용
            for (int i = 0; i < simulationSpeed; i++) {
                currentPrice *= (1 + (sigma * random.nextGaussian()));
            }

            // 10원 단위 절사, 최소 100원 보장
            long finalPrice = Math.max((long) (currentPrice / 10) * 10, 100);
            lastPriceMap.put(code, (double) finalPrice);

            // DB에 실시간 기록 저장
            StockLive liveData = new StockLive();
            liveData.setStockCode(code);
            liveData.setStClose((double) finalPrice);
            liveData.setStDate(LocalDateTime.now());
            stockLiveRepository.save(liveData);
        }
    }
}

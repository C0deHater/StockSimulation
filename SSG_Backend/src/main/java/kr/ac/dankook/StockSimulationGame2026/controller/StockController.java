package kr.ac.dankook.StockSimulationGame2026.controller;

import kr.ac.dankook.StockSimulationGame2026.domain.StockLive;
import kr.ac.dankook.StockSimulationGame2026.dto.StockHistoryDto;
import kr.ac.dankook.StockSimulationGame2026.repository.StockLiveRepository;
import kr.ac.dankook.StockSimulationGame2026.repository.StockRepository;
import kr.ac.dankook.StockSimulationGame2026.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockRepository stockRepository;
    private final StockLiveRepository stockLiveRepository;
    private final StockService stockService;

    // 전 종목 현재가 리스트
    @GetMapping("")
    public List<StockListDto> getStockList() {
        List<StockLive> liveStocks = stockLiveRepository.findCurrentLivePrices();
        return liveStocks.stream()
                .map(s -> new StockListDto(
                        stockService.getStockName(s.getStockCode()),
                        s.getStockCode(),
                        s.getStClose() != null ? s.getStClose() : 0.0,
                        0.0 // TODO: 등락률 계산 구현 예정
                ))
                .collect(Collectors.toList());
    }

    // 종목별 차트 데이터
    @GetMapping("/{symbol}/history")
    public List<StockHistoryDto> getStockHistory(@PathVariable String symbol) {
        // TODO: 과거 데이터 + 실시간 데이터 결합 로직 구현 예정
        return List.of();
    }

    // 시뮬레이션 배속 변경
    @GetMapping("/speed/{speed}")
    public String setSimulationSpeed(@PathVariable int speed) {
        stockService.setSimulationSpeed(speed);
        return "시뮬레이션 속도가 " + speed + "배속으로 변경되었습니다.";
    }

    // 시뮬레이션 초기화
    @PostMapping("/reset")
    public String resetSimulation() {
        // TODO: 초기화 로직 구현 예정
        return "시뮬레이션이 초기화되었습니다.";
    }

    // 응답 DTO
    public static class StockListDto {
        public String stockName;
        public String stockCode;
        public Double currentPrice;
        public Double changeRate;

        public StockListDto(String stockName, String stockCode, Double currentPrice, Double changeRate) {
            this.stockName = stockName;
            this.stockCode = stockCode;
            this.currentPrice = currentPrice;
            this.changeRate = changeRate;
        }
    }
}

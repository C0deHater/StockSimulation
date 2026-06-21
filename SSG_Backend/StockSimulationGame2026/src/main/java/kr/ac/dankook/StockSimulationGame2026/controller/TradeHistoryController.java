package kr.ac.dankook.StockSimulationGame2026.controller;

import kr.ac.dankook.StockSimulationGame2026.domain.TradeHistory;
import kr.ac.dankook.StockSimulationGame2026.dto.TradeHistoryDto;
import kr.ac.dankook.StockSimulationGame2026.repository.TradeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/trades")
@RequiredArgsConstructor
public class TradeHistoryController {

    private final TradeHistoryRepository tradeHistoryRepository;
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/history/{userId}")
    public List<TradeHistoryDto> getHistory(@PathVariable Long userId) {
        return tradeHistoryRepository.findAllByUserIdOrderByTradeDateDesc(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private TradeHistoryDto toDto(TradeHistory t) {
        return new TradeHistoryDto(
                t.getId(),
                t.getStockCode(),
                t.getStockName(),
                t.getTradeType(),
                t.getQuantity(),
                t.getPrice(),
                t.getTotalAmount(),
                t.getAvgBuyPrice(),
                t.getProfitLoss(),
                t.getProfitLossRate(),
                t.getTradeDate() != null ? t.getTradeDate().format(FMT) : ""
        );
    }
}

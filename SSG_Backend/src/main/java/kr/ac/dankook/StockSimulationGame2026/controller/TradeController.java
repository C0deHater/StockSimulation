package kr.ac.dankook.StockSimulationGame2026.controller;

import kr.ac.dankook.StockSimulationGame2026.dto.TradeRequest;
import kr.ac.dankook.StockSimulationGame2026.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trade")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @PostMapping("/buy")
    public ResponseEntity<Void> buyStock(@RequestBody TradeRequest request) {
        // TODO: 매수 로직 구현 예정
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sell")
    public ResponseEntity<Void> sellStock(@RequestBody TradeRequest request) {
        // TODO: 매도 로직 구현 예정
        return ResponseEntity.ok().build();
    }
}

package kr.ac.dankook.StockSimulationGame2026.controller;

import kr.ac.dankook.StockSimulationGame2026.dto.TradeRequest;
import kr.ac.dankook.StockSimulationGame2026.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trade") // 🚀 안드로이드와 주소를 맞춥니다!
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @PostMapping("/buy")
    public ResponseEntity<String> buyStock(@RequestBody TradeRequest request) {
        try {
            tradeService.buyStock(request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // 실패 이유(잔액 부족 등)를 그대로 클라이언트에 전달
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<String> sellStock(@RequestBody TradeRequest request) {
        try {
            tradeService.sellStock(request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // 실패 이유(수량 부족 등)를 그대로 클라이언트에 전달
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
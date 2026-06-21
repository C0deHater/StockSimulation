package kr.ac.dankook.StockSimulationGame2026.controller;

import kr.ac.dankook.StockSimulationGame2026.dto.MyAssetDto;
import kr.ac.dankook.StockSimulationGame2026.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/asset") // 🚀 주소 앞부분 (중요!)
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    // 🚀 최종 주소: GET /api/v1/asset/{userId}
    @GetMapping("/{userId}")
    public MyAssetDto getMyAsset(@PathVariable Long userId) {
        return assetService.getMyTotalAsset(userId);
    }
}
package kr.ac.dankook.StockSimulationGame2026.controller;

import kr.ac.dankook.StockSimulationGame2026.dto.MyAssetDto;
import kr.ac.dankook.StockSimulationGame2026.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/asset")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @GetMapping("/{userId}")
    public MyAssetDto getMyAsset(@PathVariable Long userId) {
        return assetService.getMyTotalAsset(userId);
    }
}

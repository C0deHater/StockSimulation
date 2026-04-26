package kr.ac.dankook.StockSimulationGame2026.service;

import kr.ac.dankook.StockSimulationGame2026.dto.MyAssetDto;
import kr.ac.dankook.StockSimulationGame2026.repository.AccountRepository;
import kr.ac.dankook.StockSimulationGame2026.repository.UserStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AccountRepository accountRepository;
    private final UserStockRepository userStockRepository;
    private final StockService stockService;

    /**
     * 사용자 총 자산 조회
     * TODO: 현금 잔고 + 보유 주식 실시간 평가금 합산 구현 예정
     */
    @Transactional(readOnly = true)
    public MyAssetDto getMyTotalAsset(Long userId) {
        MyAssetDto dto = new MyAssetDto();
        // TODO: 자산 조회 로직 구현
        return dto;
    }
}

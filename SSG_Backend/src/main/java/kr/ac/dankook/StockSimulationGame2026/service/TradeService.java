package kr.ac.dankook.StockSimulationGame2026.service;

import kr.ac.dankook.StockSimulationGame2026.dto.TradeRequest;
import kr.ac.dankook.StockSimulationGame2026.repository.AccountRepository;
import kr.ac.dankook.StockSimulationGame2026.repository.UserStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final AccountRepository accountRepository;
    private final UserStockRepository userStockRepository;
    private final StockService stockService;

    /**
     * 주식 매수 처리
     * TODO: 잔액 확인 → 차감 → 포트폴리오 갱신 (평단가 계산) 구현 예정
     */
    @Transactional
    public void buyStock(TradeRequest request) {
        // TODO: 매수 로직 구현
    }

    /**
     * 주식 매도 처리
     * TODO: 보유 수량 확인 → 차감 → 잔액 증가 구현 예정
     */
    @Transactional
    public void sellStock(TradeRequest request) {
        // TODO: 매도 로직 구현
    }
}

package kr.ac.dankook.StockSimulationGame2026.service;

import kr.ac.dankook.StockSimulationGame2026.domain.Account;
import kr.ac.dankook.StockSimulationGame2026.domain.Portfolio;
import kr.ac.dankook.StockSimulationGame2026.dto.MyAssetDto;
import kr.ac.dankook.StockSimulationGame2026.repository.AccountRepository;
import kr.ac.dankook.StockSimulationGame2026.repository.PortfolioRepository;
import kr.ac.dankook.StockSimulationGame2026.repository.TradeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AccountRepository      accountRepository;
    private final PortfolioRepository    portfolioRepository;
    private final TradeHistoryRepository tradeHistoryRepository;
    private final StockService           stockService;

    @Transactional(readOnly = true)
    public MyAssetDto getMyTotalAsset(Long userId) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다."));
        double cash = account.getBalance();

        List<Portfolio> myStocks = portfolioRepository.findAllByUserId(userId);
        var priceMap = stockService.getCurrentPriceMap();

        List<kr.ac.dankook.StockSimulationGame2026.dto.Portfolio> dtoList = new ArrayList<>();
        double totalStockValue = 0;

        for (Portfolio p : myStocks) {
            Double cur = priceMap.getOrDefault(p.getStockCode(), p.getAveragePrice());

            kr.ac.dankook.StockSimulationGame2026.dto.Portfolio item =
                    new kr.ac.dankook.StockSimulationGame2026.dto.Portfolio();
            item.setStockCode(p.getStockCode());
            item.setStockName(p.getStockName());
            item.setQuantity(p.getQuantity());
            item.setAveragePrice(p.getAveragePrice());
            item.setCurrentPrice(cur);
            dtoList.add(item);
            totalStockValue += cur * p.getQuantity();
        }

        // ── 성과 지표 집계 ───────────────────────────────────
        Double totalPL    = tradeHistoryRepository.sumProfitLossByUserId(userId);
        long   totalSells = tradeHistoryRepository.countSellsByUserId(userId);
        long   winSells   = tradeHistoryRepository.countWinsByUserId(userId);

        double winRate       = totalSells > 0 ? winSells * 100.0 / totalSells : 0.0;
        double totalReturnRate = (totalPL != null && (cash + totalStockValue) > 0)
                ? totalPL / 10_000_000.0 * 100.0 : 0.0; // 초기 투자금 1천만원 기준

        MyAssetDto dto = new MyAssetDto();
        dto.setCashBalance(cash);
        dto.setTotalStockValue(totalStockValue);
        dto.setTotalAsset(cash + totalStockValue);
        dto.setPortfolioList(dtoList);
        dto.setTotalProfitLoss(totalPL != null ? totalPL : 0.0);
        dto.setTotalReturnRate(totalReturnRate);
        dto.setTotalTrades((int) totalSells);
        dto.setWinTrades((int) winSells);
        dto.setWinRate(winRate);
        return dto;
    }
}

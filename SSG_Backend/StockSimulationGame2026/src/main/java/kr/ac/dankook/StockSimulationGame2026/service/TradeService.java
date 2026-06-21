package kr.ac.dankook.StockSimulationGame2026.service;

import kr.ac.dankook.StockSimulationGame2026.domain.Account;
import kr.ac.dankook.StockSimulationGame2026.domain.Portfolio;
import kr.ac.dankook.StockSimulationGame2026.domain.TradeHistory;
import kr.ac.dankook.StockSimulationGame2026.dto.TradeRequest;
import kr.ac.dankook.StockSimulationGame2026.repository.AccountRepository;
import kr.ac.dankook.StockSimulationGame2026.repository.PortfolioRepository;
import kr.ac.dankook.StockSimulationGame2026.repository.TradeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final AccountRepository      accountRepository;
    private final PortfolioRepository    portfolioRepository;
    private final TradeHistoryRepository tradeHistoryRepository;
    private final StockService           stockService;

    @Transactional
    public void buyStock(TradeRequest request) {
        Double currentPrice = stockService.getCurrentPriceMap().get(request.getStockCode());
        if (currentPrice == null) throw new RuntimeException("주가 정보를 찾을 수 없는 종목입니다.");

        double totalCost = currentPrice * request.getQuantity();

        Account account = accountRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("계좌가 없습니다."));

        if (account.getBalance() < totalCost)
            throw new RuntimeException("잔액이 부족합니다! 필요: " + (long) totalCost + "원");

        account.setBalance(account.getBalance() - totalCost);

        Portfolio portfolio = portfolioRepository
                .findByUserIdAndStockCode(request.getUserId(), request.getStockCode())
                .orElseGet(() -> {
                    Portfolio p = new Portfolio();
                    p.setUserId(request.getUserId());
                    p.setStockCode(request.getStockCode());
                    p.setStockName(request.getStockName());
                    p.setQuantity(0);
                    p.setAveragePrice(0.0);
                    return p;
                });

        int    curQty = portfolio.getQuantity();
        double curAvg = portfolio.getAveragePrice();
        int    newQty = curQty + request.getQuantity();
        double newAvg = ((curAvg * curQty) + totalCost) / newQty;

        portfolio.setQuantity(newQty);
        portfolio.setAveragePrice(newAvg);
        portfolio.setStockName(request.getStockName());
        portfolioRepository.save(portfolio);

        // 매매 이력 저장
        TradeHistory history = new TradeHistory();
        history.setUserId(request.getUserId());
        history.setStockCode(request.getStockCode());
        history.setStockName(request.getStockName());
        history.setTradeType("BUY");
        history.setQuantity(request.getQuantity());
        history.setPrice(currentPrice);
        history.setTotalAmount(totalCost);
        history.setTradeDate(LocalDateTime.now());
        tradeHistoryRepository.save(history);
    }

    @Transactional
    public void sellStock(TradeRequest request) {
        Double currentPrice = stockService.getCurrentPriceMap().get(request.getStockCode());
        if (currentPrice == null) throw new RuntimeException("주가 정보를 찾을 수 없는 종목입니다.");

        Portfolio portfolio = portfolioRepository
                .findByUserIdAndStockCode(request.getUserId(), request.getStockCode())
                .orElseThrow(() -> new RuntimeException("보유하지 않은 종목입니다."));

        if (portfolio.getQuantity() < request.getQuantity())
            throw new RuntimeException("보유 수량 부족. 보유: " + portfolio.getQuantity() + "주");

        double avgBuyPrice  = portfolio.getAveragePrice();
        double totalRevenue = currentPrice * request.getQuantity();
        double profitLoss   = (currentPrice - avgBuyPrice) * request.getQuantity();
        double plRate       = avgBuyPrice > 0
                ? (currentPrice - avgBuyPrice) / avgBuyPrice * 100.0 : 0.0;

        Account account = accountRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("계좌가 없습니다."));
        account.setBalance(account.getBalance() + totalRevenue);

        int remainQty = portfolio.getQuantity() - request.getQuantity();
        if (remainQty == 0) portfolioRepository.delete(portfolio);
        else { portfolio.setQuantity(remainQty); portfolioRepository.save(portfolio); }

        // 매매 이력 저장 (손익 포함)
        TradeHistory history = new TradeHistory();
        history.setUserId(request.getUserId());
        history.setStockCode(request.getStockCode());
        history.setStockName(request.getStockName());
        history.setTradeType("SELL");
        history.setQuantity(request.getQuantity());
        history.setPrice(currentPrice);
        history.setTotalAmount(totalRevenue);
        history.setAvgBuyPrice(avgBuyPrice);
        history.setProfitLoss(profitLoss);
        history.setProfitLossRate(plRate);
        history.setTradeDate(LocalDateTime.now());
        tradeHistoryRepository.save(history);
    }
}

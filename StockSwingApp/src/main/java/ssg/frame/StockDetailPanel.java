package ssg.frame;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ssg.Theme;
import ssg.dto.PredictionResultResponse;
import ssg.dto.StockHistoryResponse;
import ssg.network.ApiClient;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class StockDetailPanel extends JPanel {

    private final MainWindow mainWindow;
    private String stockCode, stockName;

    private JLabel           lblTitle, lblCurrentPrice, lblEstimate;
    private CandleChartPanel chartPanel;
    private JTextField       tfQuantity;
    private JButton          btnBuy, btnSell;
    private Timer            timer;

    // 신호 스트립 레이블
    private JLabel lblRsi, lblMacd, lblBb, lblSignal;

    private double latestPrice   = 0.0;
    private String currentGameDate = ""; // yyyy-MM-dd (히스토리 마지막 캔들 날짜)
    private final DecimalFormat df = new DecimalFormat("#,###");

    public StockDetailPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);

        // ── 상단 헤더 ──
        JPanel pHeader = new JPanel(new BorderLayout());
        pHeader.setBackground(Theme.BG);
        pHeader.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.ACCENT),
            BorderFactory.createEmptyBorder(10, 12, 10, 16)
        ));

        JButton btnBack = new JButton("← 뒤로");
        Theme.btn(btnBack);
        btnBack.addActionListener(e -> mainWindow.navigateTo("stockList"));

        JPanel pInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        pInfo.setBackground(Theme.BG);
        lblTitle = new JLabel("-");
        lblTitle.setFont(Theme.fontBold(16));
        lblTitle.setForeground(Theme.FG);
        lblCurrentPrice = new JLabel("현재가: -");
        lblCurrentPrice.setFont(Theme.fontBold(15));
        lblCurrentPrice.setForeground(Theme.ACCENT);
        pInfo.add(lblTitle);
        pInfo.add(lblCurrentPrice);

        pHeader.add(btnBack, BorderLayout.WEST);
        pHeader.add(pInfo,   BorderLayout.CENTER);
        add(pHeader, BorderLayout.NORTH);

        // ── 캔들 차트 ──
        chartPanel = new CandleChartPanel();
        chartPanel.setBackground(new Color(0x08, 0x08, 0x08));
        add(chartPanel, BorderLayout.CENTER);

        // ── 하단 전체 (신호 스트립 + 매수/매도) ──
        JPanel pSouth = new JPanel(new BorderLayout());
        pSouth.setBackground(Theme.BG);

        // 신호 스트립
        JPanel pSignal = buildSignalStrip();
        pSouth.add(pSignal, BorderLayout.NORTH);

        // 매수/매도 패널
        JPanel pBottom = buildTradePanel();
        pSouth.add(pBottom, BorderLayout.SOUTH);

        add(pSouth, BorderLayout.SOUTH);

        timer = new Timer(1000, e -> fetchHistory());
    }

    // ── 신호 스트립 구성 ─────────────────────────────────

    private JPanel buildSignalStrip() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        p.setBackground(new Color(0x10, 0x10, 0x10));
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Theme.BORDER));

        lblRsi    = signalChip("RSI: -");
        lblMacd   = signalChip("MACD: -");
        lblBb     = signalChip("BB: -");
        lblSignal = signalChip("신호: 대기 중");

        JLabel sep1 = sep(), sep2 = sep(), sep3 = sep();
        p.add(lblRsi);   p.add(sep1);
        p.add(lblMacd);  p.add(sep2);
        p.add(lblBb);    p.add(sep3);
        p.add(lblSignal);
        return p;
    }

    private JLabel signalChip(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.font(11));
        l.setForeground(Theme.FG_DIM);
        return l;
    }

    private JLabel sep() {
        JLabel l = new JLabel("|");
        l.setForeground(new Color(0x44, 0x44, 0x44));
        l.setFont(Theme.font(11));
        return l;
    }

    // ── 매수/매도 패널 구성 ──────────────────────────────

    private JPanel buildTradePanel() {
        JPanel pBottom = new JPanel(new BorderLayout());
        pBottom.setBackground(Theme.BG_CARD);
        pBottom.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        JPanel pLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pLeft.setBackground(Theme.BG_CARD);

        JLabel lblQty = new JLabel("수량");
        lblQty.setFont(Theme.font(12));
        lblQty.setForeground(Theme.FG_DIM);

        tfQuantity = new JTextField(7);
        Theme.field(tfQuantity);

        lblEstimate = new JLabel("예상 금액: -");
        lblEstimate.setFont(Theme.font(12));
        lblEstimate.setForeground(Theme.FG_DIM);

        pLeft.add(lblQty);
        pLeft.add(tfQuantity);
        pLeft.add(lblEstimate);

        JPanel pRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pRight.setBackground(Theme.BG_CARD);

        btnBuy  = new JButton("매수 ▲");
        btnSell = new JButton("매도 ▼");

        btnBuy.setBackground(new Color(0xCC, 0x33, 0x33));
        btnBuy.setForeground(Color.WHITE);
        btnBuy.setFocusPainted(false);
        btnBuy.setFont(Theme.fontBold(13));
        btnBuy.setOpaque(true);
        btnBuy.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        btnSell.setBackground(new Color(0x22, 0x55, 0xCC));
        btnSell.setForeground(Color.WHITE);
        btnSell.setFocusPainted(false);
        btnSell.setFont(Theme.fontBold(13));
        btnSell.setOpaque(true);
        btnSell.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        JButton btnPredict = new JButton("📊 지표 분석");
        btnPredict.setBackground(new Color(0x22, 0x33, 0x00));
        btnPredict.setForeground(Theme.ACCENT);
        btnPredict.setFocusPainted(false);
        btnPredict.setFont(Theme.fontBold(12));
        btnPredict.setOpaque(true);
        btnPredict.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.ACCENT, 1),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));

        JButton btnNews = new JButton("📰 뉴스/공시");
        btnNews.setBackground(new Color(0x00, 0x22, 0x33));
        btnNews.setForeground(new Color(0x44, 0xCC, 0xFF));
        btnNews.setFocusPainted(false);
        btnNews.setFont(Theme.fontBold(12));
        btnNews.setOpaque(true);
        btnNews.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x44, 0xCC, 0xFF), 1),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));

        pRight.add(btnBuy);
        pRight.add(btnSell);
        pRight.add(btnPredict);
        pRight.add(btnNews);

        pBottom.add(pLeft,  BorderLayout.WEST);
        pBottom.add(pRight, BorderLayout.EAST);

        btnBuy.addActionListener(e  -> doTrade("매수"));
        btnSell.addActionListener(e -> doTrade("매도"));
        btnPredict.addActionListener(e -> fetchPrediction(btnPredict));
        btnNews.addActionListener(e -> showNewsMenu(btnNews));

        tfQuantity.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { updateEstimate(); }
            public void removeUpdate(DocumentEvent e)  { updateEstimate(); }
            public void changedUpdate(DocumentEvent e) { updateEstimate(); }
        });

        return pBottom;
    }

    // ── 공개 메서드 ──────────────────────────────────────

    public void setStock(String code, String name) {
        this.stockCode   = code;
        this.stockName   = name;
        this.latestPrice = 0.0;
        lblTitle.setText(name);
        lblCurrentPrice.setText("현재가: 조회 중...");
        lblEstimate.setText("예상 금액: -");
        chartPanel.setData(null);
        tfQuantity.setText("");
        resetSignalStrip();
    }

    public void startTimer() { fetchHistory(); timer.start(); }
    public void stopTimer()  { timer.stop(); }

    // ── 히스토리 + 지표 갱신 ────────────────────────────

    private void fetchHistory() {
        if (stockCode == null) return;
        new Thread(() -> {
            try {
                String body = ApiClient.get("/api/v1/stocks/" + stockCode + "/history").body();
                List<StockHistoryResponse> list = new Gson().fromJson(
                        body, new TypeToken<List<StockHistoryResponse>>(){});
                SwingUtilities.invokeLater(() -> {
                    if (list == null || list.isEmpty()) return;
                    chartPanel.setData(list);
                    StockHistoryResponse latest = list.get(list.size() - 1);
                    latestPrice = latest.getClose();
                    // 게임 날짜 저장 (yyyy-MM-dd 앞 10자리)
                    String rawDate = latest.getDate();
                    currentGameDate = (rawDate.length() >= 10) ? rawDate.substring(0, 10) : rawDate;
                    lblCurrentPrice.setText("현재가: " + df.format(latestPrice) + "원");
                    updateEstimate();
                    updateSignalStrip(latest);
                });
            } catch (Exception ex) { /* 조용히 */ }
        }).start();
    }

    // ── 신호 스트립 업데이트 ────────────────────────────

    private void updateSignalStrip(StockHistoryResponse d) {
        // RSI
        if (d.getRsi() != null) {
            double rsi = d.getRsi();
            String rsiText = String.format("RSI: %.1f", rsi);
            Color  rsiColor;
            if (rsi < 30) {
                rsiText += " 과매도";
                rsiColor = Theme.UP;
            } else if (rsi > 70) {
                rsiText += " 과매수";
                rsiColor = Theme.DOWN;
            } else {
                rsiText += " 중립";
                rsiColor = Theme.FG_DIM;
            }
            lblRsi.setText(rsiText);
            lblRsi.setForeground(rsiColor);
        }

        // MACD
        if (d.getMacdLine() != null && d.getMacdSignal() != null) {
            double diff = d.getMacdLine() - d.getMacdSignal();
            String macdText;
            Color  macdColor;
            if (diff > 0) {
                macdText  = "MACD: ↑ 상승";
                macdColor = Theme.UP;
            } else {
                macdText  = "MACD: ↓ 하락";
                macdColor = Theme.DOWN;
            }
            lblMacd.setText(macdText);
            lblMacd.setForeground(macdColor);
        }

        // 볼린저 밴드
        if (d.getBbUpper() != null && d.getBbLower() != null) {
            double upper = d.getBbUpper(), lower = d.getBbLower();
            double width = upper - lower;
            String bbText;
            Color  bbColor;
            if (width > 0) {
                double pos = (d.getClose() - lower) / width;
                if (pos > 0.85) {
                    bbText  = "BB: 상단 근접";
                    bbColor = Theme.DOWN;
                } else if (pos < 0.15) {
                    bbText  = "BB: 하단 근접";
                    bbColor = Theme.UP;
                } else {
                    bbText  = "BB: 중간대";
                    bbColor = Theme.FG_DIM;
                }
            } else {
                bbText = "BB: 집계중";
                bbColor = Theme.FG_DIM;
            }
            lblBb.setText(bbText);
            lblBb.setForeground(bbColor);
        }

        // 종합 신호
        int score = calcSignalScore(d);
        String sigText;
        Color  sigColor;
        if      (score >= 2)  { sigText = "신호: ▶ 매수 검토"; sigColor = Theme.UP; }
        else if (score == 1)  { sigText = "신호: ◐ 매수 관심"; sigColor = new Color(0x88, 0xCC, 0x44); }
        else if (score == 0)  { sigText = "신호: ━ 관망";       sigColor = Theme.FG_DIM; }
        else if (score == -1) { sigText = "신호: ◑ 매도 관심"; sigColor = new Color(0xFF, 0x88, 0x44); }
        else                  { sigText = "신호: ▶ 매도 검토"; sigColor = Theme.DOWN; }
        lblSignal.setText(sigText);
        lblSignal.setForeground(sigColor);
    }

    private int calcSignalScore(StockHistoryResponse d) {
        int score = 0;
        if (d.getRsi() != null) {
            if      (d.getRsi() < 30) score += 2;
            else if (d.getRsi() < 40) score += 1;
            else if (d.getRsi() > 70) score -= 2;
            else if (d.getRsi() > 60) score -= 1;
        }
        if (d.getMacdLine() != null && d.getMacdSignal() != null) {
            double diff = d.getMacdLine() - d.getMacdSignal();
            if (diff > 0) score += 1; else score -= 1;
        }
        if (d.getBbUpper() != null && d.getBbLower() != null) {
            double width = d.getBbUpper() - d.getBbLower();
            if (width > 0) {
                double pos = (d.getClose() - d.getBbLower()) / width;
                if      (pos < 0.15) score += 1;
                else if (pos > 0.85) score -= 1;
            }
        }
        return Math.max(-3, Math.min(3, score));
    }

    private void resetSignalStrip() {
        lblRsi.setText("RSI: -");    lblRsi.setForeground(Theme.FG_DIM);
        lblMacd.setText("MACD: -");  lblMacd.setForeground(Theme.FG_DIM);
        lblBb.setText("BB: -");      lblBb.setForeground(Theme.FG_DIM);
        lblSignal.setText("신호: 대기 중"); lblSignal.setForeground(Theme.FG_DIM);
    }

    // ── 예상 금액 계산 ────────────────────────────────────

    private void updateEstimate() {
        try {
            int qty = Integer.parseInt(tfQuantity.getText().trim());
            if (qty > 0 && latestPrice > 0) {
                long total = (long)(qty * latestPrice);
                lblEstimate.setText("예상 금액: " + df.format(total) + "원");
                lblEstimate.setForeground(Theme.FG);
                return;
            }
        } catch (NumberFormatException ignored) {}
        lblEstimate.setText("예상 금액: -");
        lblEstimate.setForeground(Theme.FG_DIM);
    }

    // ── 지표 분석 팝업 ────────────────────────────────────

    private void fetchPrediction(JButton btn) {
        if (stockCode == null) return;
        btn.setEnabled(false);
        btn.setText("분석 중...");

        new Thread(() -> {
            try {
                String body = ApiClient.get("/api/v1/stocks/" + stockCode + "/predict").body();
                PredictionResultResponse result = new Gson().fromJson(body, PredictionResultResponse.class);

                SwingUtilities.invokeLater(() -> {
                    if (result == null || result.getTrend().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "분석 데이터를 불러오지 못했습니다.");
                    } else {
                        showPredictionSummary(result);
                    }
                    btn.setEnabled(true);
                    btn.setText("📊 지표 분석");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "서버 오류: " + ex.getMessage());
                    btn.setEnabled(true);
                    btn.setText("📊 지표 분석");
                });
            }
        }).start();
    }

    private void showPredictionSummary(PredictionResultResponse r) {
        String msg = String.format(
            "[ %s  기술적 지표 분석 ]\n" +
            "─────────────────────────────────\n" +
            "현재가 : %s원\n" +
            "추  세 : %s\n" +
            "MA5   : %s원  /  MA20: %s원\n\n" +
            "상승 %d%%  /  횡보 %d%%  /  하락 %d%%\n\n" +
            "%s\n\n" +
            "%s",
            stockName,
            df.format(r.getCurrentPrice()),
            r.getTrend(),
            df.format(r.getMa5()), df.format(r.getMa20()),
            r.getBullishProb(), r.getNeutralProb(), r.getBearishProb(),
            r.getAnalysis(),
            r.getRecommendation()
        );

        JTextArea ta = new JTextArea(msg);
        ta.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        ta.setBackground(Theme.BG_CARD);
        ta.setForeground(Theme.FG);
        ta.setEditable(false);
        ta.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(520, 480));
        sp.setBorder(null);

        JOptionPane.showMessageDialog(this, sp,
                "기술적 지표 분석 — " + stockName, JOptionPane.PLAIN_MESSAGE);
    }

    // ── 매수/매도 실행 ────────────────────────────────────

    private void doTrade(String type) {
        String qStr = tfQuantity.getText().trim();
        if (qStr.isEmpty()) { JOptionPane.showMessageDialog(this, "수량을 입력하세요."); return; }
        int qty;
        try {
            qty = Integer.parseInt(qStr);
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "올바른 수량(양의 정수)을 입력하세요.");
            return;
        }

        btnBuy.setEnabled(false); btnSell.setEnabled(false);
        final int finalQty = qty;

        new Thread(() -> {
            try {
                ssg.dto.TradeRequest req = new ssg.dto.TradeRequest();
                req.setUserId(1L);
                req.setStockCode(stockCode);
                req.setStockName(stockName);
                req.setQuantity(finalQty);

                String path = type.equals("매수") ? "/api/v1/trade/buy" : "/api/v1/trade/sell";
                var    res  = ApiClient.post(path, new Gson().toJson(req));
                int    st   = res.statusCode();
                String msg  = res.body();

                SwingUtilities.invokeLater(() -> {
                    if (st == 200) {
                        JOptionPane.showMessageDialog(this,
                                type + " 체결!\n" + stockName + " " + finalQty + "주  @"
                                + df.format(latestPrice) + "원");
                        tfQuantity.setText("");
                    } else {
                        JOptionPane.showMessageDialog(this,
                                (msg != null && !msg.isBlank()) ? msg : type + " 실패");
                    }
                    btnBuy.setEnabled(true); btnSell.setEnabled(true);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "서버 연결 실패");
                    btnBuy.setEnabled(true); btnSell.setEnabled(true);
                });
            }
        }).start();
    }

    // ── 뉴스/공시 팝업 메뉴 ──────────────────────────────

    private void showNewsMenu(JButton btn) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(Theme.BG_CARD);
        menu.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        JMenuItem itemNaver = styledMenuItem("📰  네이버 뉴스  →");
        JMenuItem itemDart  = styledMenuItem("📋  DART 공시   →");

        itemNaver.addActionListener(e -> openNaverNews());
        itemDart.addActionListener(e -> openDartSearch());

        menu.add(itemNaver);
        menu.addSeparator();
        menu.add(itemDart);

        menu.show(btn, 0, -menu.getPreferredSize().height);
    }

    private JMenuItem styledMenuItem(String text) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(Theme.font(12));
        item.setForeground(Theme.FG);
        item.setBackground(Theme.BG_CARD);
        item.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        item.setOpaque(true);
        return item;
    }

    // ── 날짜 필터 적용 브라우저 열기 ─────────────────────

    private void openNaverNews() {
        try {
            // 게임 날짜 기준 30일 전 ~ 당일
            String endDate   = currentGameDate.isBlank() ? today() : currentGameDate;
            String startDate = shiftDate(endDate, -30);
            // 네이버 날짜 형식: YYYY.MM.DD
            String ns = startDate.replace("-", ".");
            String ne = endDate.replace("-", ".");
            String q  = java.net.URLEncoder.encode(stockName, "UTF-8");
            openBrowser("https://search.naver.com/search.naver"
                    + "?where=news&query=" + q
                    + "&sort=1"            // 날짜순
                    + "&pd=3"              // 기간 지정
                    + "&ds=" + ns
                    + "&de=" + ne);
        } catch (Exception ex) {
            openBrowser("https://news.naver.com");
        }
    }

    private void openDartSearch() {
        try {
            // DART 날짜 형식: YYYYMMDD
            String endDate   = currentGameDate.isBlank() ? today() : currentGameDate;
            String startDate = shiftDate(endDate, -30);
            String ds = startDate.replace("-", "");
            String de = endDate.replace("-", "");
            String q  = java.net.URLEncoder.encode(stockName, "UTF-8");
            openBrowser("https://dart.fss.or.kr/dsab001/search.ax"
                    + "?textCrpNm=" + q
                    + "&startDate=" + ds
                    + "&endDate="   + de);
        } catch (Exception ex) {
            openBrowser("https://dart.fss.or.kr");
        }
    }

    /** yyyy-MM-dd 날짜에 days를 더하거나 뺀 문자열 반환 */
    private String shiftDate(String yyyyMmDd, int days) {
        try {
            java.time.LocalDate d = java.time.LocalDate.parse(yyyyMmDd)
                    .plusDays(days);
            return d.toString(); // yyyy-MM-dd
        } catch (Exception e) {
            return yyyyMmDd;
        }
    }

    private String today() {
        return java.time.LocalDate.now().toString();
    }

    private void openBrowser(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "브라우저를 열 수 없습니다:\n" + url);
        }
    }
}

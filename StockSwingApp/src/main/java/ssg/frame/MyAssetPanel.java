package ssg.frame;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ssg.Theme;
import ssg.dto.MyAssetDto;
import ssg.dto.Portfolio;
import ssg.dto.TradeHistoryResponse;
import ssg.network.ApiClient;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

public class MyAssetPanel extends JPanel {

    private final MainWindow mainWindow;

    // ── 자산 요약 카드 ─────────────────────────────────────
    private JLabel lblTotal, lblCash, lblStock;

    // ── 성과 카드 ──────────────────────────────────────────
    private JLabel lblPnL, lblWinRate, lblTrades;

    // ── 포트폴리오 테이블 ──────────────────────────────────
    private DefaultTableModel tableModel;
    private List<Portfolio>   currentPortfolio;
    private Timer             timer;

    private static final String[] COLUMNS =
            {"종목명", "코드", "보유수량", "평균단가 (원)", "현재가 (원)", "평가손익", "수익률"};
    private final DecimalFormat df = new DecimalFormat("#,###");

    public MyAssetPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);

        // ── 헤더 ──
        JPanel pHeader = new JPanel(new BorderLayout());
        pHeader.setBackground(Theme.BG);
        pHeader.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.ACCENT),
            BorderFactory.createEmptyBorder(10, 12, 10, 16)
        ));
        JButton btnBack = new JButton("← 뒤로");
        Theme.btn(btnBack);
        btnBack.addActionListener(e -> mainWindow.navigateTo("stockList"));
        JLabel lblTitle = new JLabel("내 자산");
        lblTitle.setFont(Theme.fontBold(16));
        lblTitle.setForeground(Theme.ACCENT);
        pHeader.add(btnBack,  BorderLayout.WEST);
        pHeader.add(lblTitle, BorderLayout.CENTER);

        // ── 자산 요약 카드 (3칸) ──
        JPanel pAsset = new JPanel(new GridLayout(1, 3, 1, 0));
        pAsset.setBackground(Theme.BORDER);
        pAsset.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
        lblTotal = assetLabel("-");
        lblCash  = assetLabel("-");
        lblStock = assetLabel("-");
        pAsset.add(wrapCard("총 자산",    lblTotal));
        pAsset.add(wrapCard("현금 잔액",  lblCash));
        pAsset.add(wrapCard("주식 평가액", lblStock));

        // ── 성과 카드 (3칸) ──
        JPanel pPerf = new JPanel(new GridLayout(1, 3, 1, 0));
        pPerf.setBackground(Theme.BORDER);
        pPerf.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
        lblPnL     = assetLabel("-");
        lblWinRate = assetLabel("-");
        lblTrades  = assetLabel("-");
        pPerf.add(wrapCard("총 실현 손익",  lblPnL));
        pPerf.add(wrapCard("승률",          lblWinRate));
        pPerf.add(wrapCard("매도 거래 건수", lblTrades));

        // 헤더 + 두 카드 줄을 NORTH에 묶기
        JPanel pTop = new JPanel(new BorderLayout());
        pTop.setBackground(Theme.BG);
        pTop.add(pHeader, BorderLayout.NORTH);
        JPanel pCards = new JPanel(new GridLayout(2, 1, 0, 1));
        pCards.setBackground(Theme.BORDER);
        pCards.add(pAsset);
        pCards.add(pPerf);
        pTop.add(pCards, BorderLayout.SOUTH);
        add(pTop, BorderLayout.NORTH);

        // ── 포트폴리오 테이블 ──
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        Theme.table(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(5).setCellRenderer(new PnLRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new PnLRenderer());
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && currentPortfolio != null) {
                    int row = table.getSelectedRow();
                    if (row >= 0 && row < currentPortfolio.size()) {
                        Portfolio p = currentPortfolio.get(row);
                        mainWindow.openStockDetail(p.getStockCode(), p.getStockName());
                    }
                }
            }
        });
        JScrollPane scroll = new JScrollPane(table);
        Theme.scroll(scroll);
        add(scroll, BorderLayout.CENTER);

        // ── 하단 버튼 ──
        JPanel pFooter = new JPanel(new BorderLayout());
        pFooter.setBackground(Theme.BG_CARD);
        pFooter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));

        JLabel lblHint = new JLabel("종목 더블클릭 → 해당 주식 상세로 이동");
        lblHint.setFont(Theme.font(11));
        lblHint.setForeground(Theme.FG_DIM);

        JButton btnJournal = new JButton("📋 매매 일지");
        Theme.btn(btnJournal);
        btnJournal.setForeground(Theme.ACCENT);

        JButton btnReset = new JButton("⟳ 시뮬레이션 초기화");
        Theme.btn(btnReset);
        btnReset.setForeground(new Color(0xFF, 0x77, 0x77));

        JPanel pBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pBtns.setBackground(Theme.BG_CARD);
        pBtns.add(btnJournal);
        pBtns.add(btnReset);

        pFooter.add(lblHint, BorderLayout.WEST);
        pFooter.add(pBtns,   BorderLayout.EAST);
        add(pFooter, BorderLayout.SOUTH);

        btnReset.addActionListener(e -> confirmReset());
        btnJournal.addActionListener(e -> openTradeJournal());

        timer = new Timer(1000, e -> fetchMyAsset());
    }

    public void startTimer() { fetchMyAsset(); timer.start(); }
    public void stopTimer()  { timer.stop(); }

    // ── 자산 데이터 갱신 ──────────────────────────────────

    private void fetchMyAsset() {
        new Thread(() -> {
            try {
                MyAssetDto dto = new Gson().fromJson(
                        ApiClient.get("/api/v1/asset/1").body(), MyAssetDto.class);
                SwingUtilities.invokeLater(() -> {
                    if (dto == null) return;
                    lblTotal.setText(df.format(dto.getTotalAsset())      + " 원");
                    lblCash.setText( df.format(dto.getCashBalance())     + " 원");
                    lblStock.setText(df.format(dto.getTotalStockValue()) + " 원");

                    // 성과 카드
                    double pnl = dto.getTotalProfitLoss();
                    lblPnL.setText((pnl >= 0 ? "+" : "") + df.format((long) pnl) + " 원");
                    lblPnL.setForeground(pnl >= 0 ? Theme.UP : Theme.DOWN);

                    double wr = dto.getWinRate();
                    lblWinRate.setText(String.format("%.1f%%  (%d/%d)", wr,
                            dto.getWinTrades(), dto.getTotalTrades()));
                    lblWinRate.setForeground(wr >= 50 ? Theme.UP : Theme.DOWN);

                    lblTrades.setText(dto.getTotalTrades() + " 건");

                    // 포트폴리오 테이블
                    currentPortfolio = dto.getPortfolioList();
                    tableModel.setRowCount(0);
                    for (Portfolio p : currentPortfolio) {
                        double avg  = p.getAveragePrice();
                        double cur  = p.getCurrentPrice();
                        double rate = avg > 0 ? (cur - avg) / avg * 100.0 : 0.0;
                        double pnlPerStock = (cur - avg) * p.getQuantity();
                        tableModel.addRow(new Object[]{
                                p.getStockName(),
                                p.getStockCode(),
                                p.getQuantity() + "주",
                                df.format(avg),
                                df.format(cur),
                                (pnlPerStock >= 0 ? "+" : "") + df.format((long) pnlPerStock) + "원",
                                String.format("%+.2f%%", rate)
                        });
                    }
                });
            } catch (Exception ex) { /* 조용히 */ }
        }).start();
    }

    // ── 매매 일지 다이얼로그 ──────────────────────────────

    private void openTradeJournal() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "📋 매매 일지", true);
        dialog.setSize(780, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setBackground(Theme.BG);

        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBackground(Theme.BG);
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // 상태 표시
        JLabel lblStatus = new JLabel("불러오는 중...", JLabel.CENTER);
        lblStatus.setFont(Theme.font(12));
        lblStatus.setForeground(Theme.FG_DIM);
        root.add(lblStatus, BorderLayout.NORTH);

        // 테이블
        String[] cols = {"날짜", "종목명", "코드", "구분", "수량", "체결가", "금액", "평단가", "실현손익", "수익률"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        Theme.table(table);
        table.setFont(Theme.font(11));
        table.getColumnModel().getColumn(8).setCellRenderer(new PnLRenderer());
        table.getColumnModel().getColumn(9).setCellRenderer(new PnLRenderer());

        // 열 너비 설정
        int[] widths = {140, 90, 65, 45, 45, 85, 95, 85, 85, 65};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane sp = new JScrollPane(table);
        Theme.scroll(sp);
        root.add(sp, BorderLayout.CENTER);

        JButton btnClose = new JButton("닫기");
        Theme.btn(btnClose);
        btnClose.addActionListener(e -> dialog.dispose());
        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pBtn.setBackground(Theme.BG);
        pBtn.add(btnClose);
        root.add(pBtn, BorderLayout.SOUTH);

        dialog.add(root);
        dialog.setVisible(true); // 먼저 표시

        // 비동기 로드
        new Thread(() -> {
            try {
                List<TradeHistoryResponse> list = new Gson().fromJson(
                        ApiClient.get("/api/v1/trades/history/1").body(),
                        new TypeToken<List<TradeHistoryResponse>>(){});
                SwingUtilities.invokeLater(() -> {
                    if (list == null || list.isEmpty()) {
                        lblStatus.setText("매매 이력이 없습니다.");
                        return;
                    }
                    model.setRowCount(0);
                    for (TradeHistoryResponse t : list) {
                        String plStr  = t.getProfitLoss() != null
                                ? (t.isProfitable() ? "+" : "") + df.format(t.getProfitLoss().longValue()) + "원"
                                : "-";
                        String plRate = t.getProfitLossRate() != null
                                ? String.format("%+.2f%%", t.getProfitLossRate())
                                : "-";
                        String avgStr = t.getAvgBuyPrice() != null && t.getAvgBuyPrice() > 0
                                ? df.format(t.getAvgBuyPrice())
                                : "-";
                        model.addRow(new Object[]{
                                t.getTradeDate(),
                                t.getStockName(),
                                t.getStockCode(),
                                t.getTradeType().equals("BUY") ? "▲ 매수" : "▼ 매도",
                                t.getQuantity() + "주",
                                df.format(t.getPrice().longValue()) + "원",
                                df.format(t.getTotalAmount().longValue()) + "원",
                                avgStr,
                                plStr,
                                plRate
                        });
                    }
                    lblStatus.setText("총 " + list.size() + "건의 매매 이력");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> lblStatus.setText("데이터 불러오기 실패"));
            }
        }).start();
    }

    // ── 초기화 ────────────────────────────────────────────

    private void confirmReset() {
        int ok = JOptionPane.showConfirmDialog(this,
                "모든 보유 주식, 매매 이력, 잔고가 초기화됩니다.\n계속하시겠습니까?",
                "시뮬레이션 초기화", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        new Thread(() -> {
            try {
                int status = ApiClient.post("/api/v1/stocks/reset", "").statusCode();
                SwingUtilities.invokeLater(() -> {
                    if (status == 200) JOptionPane.showMessageDialog(this, "초기화 완료!");
                    else               JOptionPane.showMessageDialog(this, "초기화 실패 (코드: " + status + ")");
                    fetchMyAsset();
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "서버 연결 실패"));
            }
        }).start();
    }

    // ── UI 헬퍼 ──────────────────────────────────────────

    private JPanel wrapCard(String title, JLabel valueLabel) {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setBackground(Theme.BG_CARD);
        p.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        JLabel lblTitle = new JLabel(title, JLabel.LEFT);
        lblTitle.setFont(Theme.font(11));
        lblTitle.setForeground(Theme.FG_DIM);
        p.add(lblTitle);
        p.add(valueLabel);
        return p;
    }

    private JLabel assetLabel(String value) {
        JLabel lbl = new JLabel(value, JLabel.LEFT);
        lbl.setFont(Theme.fontBold(14));
        lbl.setForeground(Theme.ACCENT);
        return lbl;
    }

    // ── 손익 색상 렌더러 ──────────────────────────────────

    static class PnLRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            setHorizontalAlignment(JLabel.RIGHT);
            setBackground(sel ? new Color(0x33, 0x44, 0x00) : Theme.BG_CARD);
            if (val != null) {
                String s = val.toString();
                if      (s.startsWith("+")) setForeground(Theme.UP);
                else if (s.startsWith("-")) setForeground(Theme.DOWN);
                else                        setForeground(Theme.FG_DIM);
            }
            return this;
        }
    }
}

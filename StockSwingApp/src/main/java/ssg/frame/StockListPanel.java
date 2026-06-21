package ssg.frame;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ssg.Theme;
import ssg.dto.StockListResponse;
import ssg.network.ApiClient;

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

public class StockListPanel extends JPanel {

    private final MainWindow        mainWindow;
    private       DefaultTableModel tableModel;
    private       JTable            table;
    private       Timer             timer;
    private       JLabel            lblSummary;    // 요약 정보 (상승/하락 종목 수)
    private       JLabel            lblMarketStatus; // 장 상태 (장 중 / 장 마감)

    private static final String[] COLUMNS = {"종목명", "코드", "현재가 (원)", "등락률 (%)"};
    private final DecimalFormat df = new DecimalFormat("#,###");

    public StockListPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);

        // ── 상단 헤더 ──
        JPanel pHeader = new JPanel(new BorderLayout());
        pHeader.setBackground(Theme.BG);
        pHeader.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.ACCENT),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel lblTitle = new JLabel("LIVE  주식 목록");
        lblTitle.setFont(Theme.fontBold(16));
        lblTitle.setForeground(Theme.ACCENT);

        // 장 상태 레이블 (제목 옆에 표시)
        lblMarketStatus = new JLabel("● 장 중");
        lblMarketStatus.setFont(Theme.fontBold(12));
        lblMarketStatus.setForeground(Theme.ACCENT);
        lblMarketStatus.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        JPanel pTitleArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pTitleArea.setBackground(Theme.BG);
        pTitleArea.add(lblTitle);
        pTitleArea.add(lblMarketStatus);

        JPanel pBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pBtns.setBackground(Theme.BG);
        JButton btnMyAsset = new JButton("내 자산");
        JButton btnReset   = new JButton("⟳  초기화");
        Theme.btn(btnMyAsset);
        Theme.btn(btnReset);
        btnReset.setForeground(new Color(0xFF, 0x77, 0x77)); // 위험 버튼은 붉은 계열
        pBtns.add(btnMyAsset);
        pBtns.add(btnReset);

        pHeader.add(pTitleArea, BorderLayout.WEST);
        pHeader.add(pBtns,    BorderLayout.EAST);
        add(pHeader, BorderLayout.NORTH);

        // ── 테이블 ──
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        Theme.table(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 컬럼 너비 조정
        table.getColumnModel().getColumn(0).setPreferredWidth(180);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);

        // 현재가·등락률 가운데 정렬
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(new ChangeRateRenderer());

        JScrollPane scroll = new JScrollPane(table);
        Theme.scroll(scroll);
        add(scroll, BorderLayout.CENTER);

        // ── 하단 요약 바 ──
        JPanel pFooter = new JPanel(new BorderLayout());
        pFooter.setBackground(Theme.BG_CARD);
        pFooter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
            BorderFactory.createEmptyBorder(6, 16, 6, 16)
        ));

        lblSummary = new JLabel("데이터 로딩 중...");
        lblSummary.setFont(Theme.font(11));
        lblSummary.setForeground(Theme.FG_DIM);

        JLabel lblHint = new JLabel("더블클릭 → 상세 화면");
        lblHint.setFont(Theme.font(11));
        lblHint.setForeground(Theme.FG_DIM);

        pFooter.add(lblSummary, BorderLayout.WEST);
        pFooter.add(lblHint,    BorderLayout.EAST);
        add(pFooter, BorderLayout.SOUTH);

        // ── 이벤트 ──
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        String code = (String) tableModel.getValueAt(row, 1);
                        String name = (String) tableModel.getValueAt(row, 0);
                        mainWindow.openStockDetail(code, name);
                    }
                }
            }
        });

        btnMyAsset.addActionListener(e -> mainWindow.navigateTo("myAsset"));
        btnReset.addActionListener(e -> confirmReset());

        timer = new Timer(1000, e -> fetchStocks());
    }

    public void startTimer() { fetchStocks(); timer.start(); }
    public void stopTimer()  { timer.stop(); }

    private void fetchStocks() {
        new Thread(() -> {
            try {
                // 주식 목록 + 장 상태 동시 조회
                String stockBody  = ApiClient.get("/api/v1/stocks").body();
                String statusBody = ApiClient.get("/api/v1/stocks/market-status").body();

                List<StockListResponse> list = new Gson().fromJson(
                        stockBody, new TypeToken<List<StockListResponse>>(){});

                // 장 상태 파싱 ({"isOpen":true,"message":"..."})
                boolean isOpen = statusBody != null && statusBody.contains("\"isOpen\":true");

                SwingUtilities.invokeLater(() -> {
                    // 장 상태 표시
                    if (isOpen) {
                        lblMarketStatus.setText("  ●  장 중  (09:00 ~ 15:30)");
                        lblMarketStatus.setForeground(Theme.ACCENT);
                    } else {
                        lblMarketStatus.setText("  ○  장 마감");
                        lblMarketStatus.setForeground(Theme.FG_DIM);
                    }

                    if (list == null) return;
                    tableModel.setRowCount(0);

                    int up = 0, down = 0, flat = 0;
                    for (StockListResponse s : list) {
                        double rate = s.getChangeRate();
                        if      (rate > 0.0)  up++;
                        else if (rate < 0.0)  down++;
                        else                  flat++;

                        tableModel.addRow(new Object[]{
                                s.getStockName(),
                                s.getStockCode(),
                                df.format(s.getCurrentPrice()),
                                String.format("%+.2f%%", rate)
                        });
                    }
                    lblSummary.setText(String.format(
                        "총 %d종목  |  상승 %d▲  |  하락 %d▼  |  보합 %d",
                        list.size(), up, down, flat));
                });
            } catch (Exception ex) { /* 조용히 넘어감 */ }
        }).start();
    }

    private void confirmReset() {
        int ok = JOptionPane.showConfirmDialog(this,
                "실시간 시세와 모든 사용자의 보유 주식이 초기화됩니다.\n정말 초기화하시겠습니까?",
                "시뮬레이션 초기화",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        new Thread(() -> {
            try {
                int status = ApiClient.post("/api/v1/stocks/reset", "").statusCode();
                SwingUtilities.invokeLater(() -> {
                    if (status == 200) JOptionPane.showMessageDialog(this, "초기화 완료! ♻");
                    else               JOptionPane.showMessageDialog(this, "초기화 실패 (코드: " + status + ")");
                    fetchStocks();
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "서버 연결 실패"));
            }
        }).start();
    }

    // 등락률 색상 렌더러
    static class ChangeRateRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            setHorizontalAlignment(JLabel.CENTER);
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

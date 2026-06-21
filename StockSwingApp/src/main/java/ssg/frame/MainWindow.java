package ssg.frame;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ssg.Theme;
import ssg.network.ApiClient;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalTime;
import java.util.Map;
import javax.swing.*;

public class MainWindow extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel     cardPanel  = new JPanel(cardLayout);

    private final LoginPanel       loginPanel;
    private final SignUpPanel      signUpPanel;
    private final StockListPanel   stockListPanel;
    private final StockDetailPanel stockDetailPanel;
    private final MyAssetPanel     myAssetPanel;

    private String currentCard = "login";

    // 상단 바 컴포넌트
    private final JLabel  lblGameTime    = new JLabel("--:--:--", JLabel.CENTER);
    private final JLabel  lblGameDate    = new JLabel("", JLabel.CENTER);
    private final JLabel  lblMarketBadge = new JLabel("", JLabel.RIGHT);
    private final JLabel  lblAmPm        = new JLabel("", JLabel.CENTER);
    private final JButton btnLogout      = new JButton("로그아웃");   // 로그인 후에만 표시
    private Timer  clockTimer;
    private JPanel topBar;  // 상단 바 (로그인 후 표시)

    // 배속 옵션 [라벨, timeSpeed값]
    private static final Object[][] SPEED_OPTIONS = {
        {"×1\n(1초)", 1},
        {"×60\n(1분)", 60},
        {"×300\n(5분)", 300},
        {"×1800\n(30분)", 1800},
        {"×3600\n(1시간)", 3600}
    };

    public MainWindow() {
        // ── 네이티브 타이틀바 제거 ──
        setUndecorated(true);

        loginPanel       = new LoginPanel(this);
        signUpPanel      = new SignUpPanel(this);
        stockListPanel   = new StockListPanel(this);
        stockDetailPanel = new StockDetailPanel(this);
        myAssetPanel     = new MyAssetPanel(this);

        cardPanel.setBackground(Theme.BG);
        cardPanel.add(loginPanel,       "login");
        cardPanel.add(signUpPanel,      "signup");
        cardPanel.add(stockListPanel,   "stockList");
        cardPanel.add(stockDetailPanel, "stockDetail");
        cardPanel.add(myAssetPanel,     "myAsset");

        // 상단 바 (시계 + 배속) — 로그인 후에만 표시
        topBar = buildTopBar();
        topBar.setVisible(false);

        // ACCENT 구분선 (1px, 전체 폭 보장)
        JPanel pAccentLine = new JPanel();
        pAccentLine.setBackground(Theme.ACCENT);
        pAccentLine.setPreferredSize(new Dimension(0, 1));

        // 커스텀 타이틀바 + ACCENT 라인 + 기존 topBar 묶기
        JPanel pNorth = new JPanel(new BorderLayout());
        pNorth.setBackground(Theme.BG);

        JPanel pTitleAndLine = new JPanel(new BorderLayout());
        pTitleAndLine.setBackground(Theme.BG);
        pTitleAndLine.add(buildTitleBar(), BorderLayout.CENTER);
        pTitleAndLine.add(pAccentLine,     BorderLayout.SOUTH);

        pNorth.add(pTitleAndLine, BorderLayout.NORTH);
        pNorth.add(topBar,        BorderLayout.SOUTH);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.setBorder(BorderFactory.createLineBorder(new Color(0x44, 0x44, 0x44), 1));
        root.add(pNorth,    BorderLayout.NORTH);
        root.add(cardPanel, BorderLayout.CENTER);

        getContentPane().setBackground(Theme.BG);
        add(root);

        setTitle("주식 시뮬레이션 — SSG");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(880, 720);
        setLocationRelativeTo(null);

        // 타이머만 생성, 시작은 로그인 성공 후 startClock()에서
        clockTimer = new Timer(1000, e -> fetchGameTime());

        // undecorated 상태에서도 Alt+F4 같은 OS 종료 요청 처리
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { System.exit(0); }
        });

        setVisible(true);
    }

    // ── 커스텀 타이틀바 ────────────────────────────────────────

    private JPanel buildTitleBar() {
        Color titleBg = new Color(0x08, 0x08, 0x08);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(titleBg);
        bar.setPreferredSize(new Dimension(0, 38));

        // ── 왼쪽: 로고 영역 ──
        JPanel pLeft = new JPanel();
        pLeft.setLayout(new BoxLayout(pLeft, BoxLayout.X_AXIS));
        pLeft.setBackground(titleBg);
        pLeft.setOpaque(true);
        pLeft.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));

        // 로고: 로그인 화면과 동일한 📈 이모지
        JLabel logo = new JLabel("📈");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        logo.setAlignmentY(0.5f);

        JLabel lblApp = new JLabel("SSG");
        lblApp.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        lblApp.setForeground(Theme.ACCENT);
        lblApp.setAlignmentY(0.5f);

        JLabel lblSub = new JLabel("주식 투자 시뮬레이션");
        lblSub.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        lblSub.setForeground(new Color(0x66, 0x66, 0x66));
        lblSub.setAlignmentY(0.5f);

        pLeft.add(logo);
        pLeft.add(Box.createHorizontalStrut(10));
        pLeft.add(lblApp);
        pLeft.add(Box.createHorizontalStrut(8));
        pLeft.add(lblSub);

        // ── 오른쪽: 창 제어 버튼 ──
        JPanel pRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pRight.setBackground(titleBg);
        pRight.setOpaque(true);

        JButton btnMin   = makeTitleBtn("min",   new Color(0x55, 0x55, 0x55), new Color(0x33, 0x33, 0x33));
        JButton btnClose = makeTitleBtn("close", new Color(0xE8, 0x11, 0x23), new Color(0xC4, 0x0D, 0x1E));

        btnMin.addActionListener(e -> setState(JFrame.ICONIFIED));
        btnClose.addActionListener(e -> {
            onLeave(currentCard);
            int choice = JOptionPane.showOptionDialog(
                    MainWindow.this,
                    "주식 시뮬레이션을 종료하시겠습니까?",
                    "종료 확인",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"종료", "계속"},
                    "계속"
            );
            if (choice == JOptionPane.YES_OPTION) System.exit(0);
            else onEnter(currentCard);
        });

        pRight.add(btnMin);
        pRight.add(btnClose);

        bar.add(pLeft,  BorderLayout.WEST);
        bar.add(pRight, BorderLayout.EAST);

        // ── 드래그로 창 이동 ──
        final Point[] dragOrigin = {null};
        bar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e)  { dragOrigin[0] = e.getLocationOnScreen(); }
            public void mouseReleased(MouseEvent e) { dragOrigin[0] = null; }
        });
        bar.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (dragOrigin[0] == null) return;
                Point now = e.getLocationOnScreen();
                Point loc = getLocation();
                setLocation(loc.x + now.x - dragOrigin[0].x,
                            loc.y + now.y - dragOrigin[0].y);
                dragOrigin[0] = now;
            }
        });
        // 더블클릭 → 최대화 토글
        bar.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    setExtendedState(getExtendedState() == JFrame.MAXIMIZED_BOTH
                            ? JFrame.NORMAL : JFrame.MAXIMIZED_BOTH);
            }
        });

        return bar;
    }

    /** 타이틀바 버튼 생성 헬퍼 — type: "min" 또는 "close" */
    private JButton makeTitleBtn(String type, Color hoverBg, Color pressBg) {
        boolean isClose = "close".equals(type);
        JButton btn = new JButton() {
            private Color bg = null;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { bg = hoverBg; repaint(); }
                    public void mouseExited(MouseEvent e)  { bg = null;    repaint(); }
                    public void mousePressed(MouseEvent e) { bg = pressBg; repaint(); }
                    public void mouseReleased(MouseEvent e){ bg = hoverBg; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // 배경
                g2.setColor(bg != null ? bg : getParent().getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());
                // 아이콘 색상
                g2.setColor(bg != null ? Color.WHITE : new Color(0x99, 0x99, 0x99));
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2, cy = getHeight() / 2;
                if (isClose) {
                    // X 직접 그리기
                    int r = 5;
                    g2.drawLine(cx - r, cy - r, cx + r, cy + r);
                    g2.drawLine(cx + r, cy - r, cx - r, cy + r);
                } else {
                    // — 직접 그리기
                    g2.drawLine(cx - 6, cy, cx + 6, cy);
                }
                g2.setStroke(new BasicStroke(1));
            }
        };
        btn.setPreferredSize(new Dimension(46, 38));
        btn.setMaximumSize(new Dimension(46, 38));
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getDefaultCursor());
        return btn;
    }

    // ── 상단 바 구성 ───────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(0x08, 0x08, 0x08));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));

        // ── 윗줄: 앱명 | 게임 시각 | 장 상태 ──
        JPanel pTop = new JPanel(new BorderLayout());
        pTop.setBackground(new Color(0x08, 0x08, 0x08));
        pTop.setBorder(BorderFactory.createEmptyBorder(6, 16, 4, 16));

        // 왼쪽: 로그아웃 버튼 + 앱 이름 + 게임 날짜
        JPanel pLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pLeft.setBackground(new Color(0x08, 0x08, 0x08));

        // 로그아웃 버튼 (로그인 전엔 숨김)
        btnLogout.setFont(Theme.font(11));
        btnLogout.setBackground(new Color(0x08, 0x08, 0x08));
        btnLogout.setForeground(Theme.FG_DIM);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x44, 0x44, 0x44), 1),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));
        btnLogout.setVisible(false); // 로그인 후에만 표시
        btnLogout.addActionListener(e -> doLogout());

        JLabel lblApp = new JLabel("📈 SSG");
        lblApp.setFont(Theme.fontBold(12));
        lblApp.setForeground(Theme.ACCENT);
        lblGameDate.setFont(Theme.font(11));
        lblGameDate.setForeground(Theme.FG_DIM);

        pLeft.add(btnLogout);
        pLeft.add(lblApp);
        pLeft.add(lblGameDate);

        // 가운데: AM/PM + 게임 시각
        lblAmPm.setFont(Theme.fontBold(11));
        lblAmPm.setForeground(Theme.FG_DIM);
        lblGameTime.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        lblGameTime.setForeground(Theme.FG);

        JPanel pClock = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        pClock.setBackground(new Color(0x08, 0x08, 0x08));
        pClock.add(lblAmPm);
        pClock.add(lblGameTime);

        // 오른쪽: 장 상태
        lblMarketBadge.setFont(Theme.fontBold(11));
        lblMarketBadge.setForeground(Theme.FG_DIM);

        pTop.add(pLeft,          BorderLayout.WEST);
        pTop.add(pClock,         BorderLayout.CENTER);
        pTop.add(lblMarketBadge, BorderLayout.EAST);

        // ── 아랫줄: 배속 선택 버튼 ──
        JPanel pSpeed = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        pSpeed.setBackground(new Color(0x08, 0x08, 0x08));

        JLabel lblSpeedTitle = new JLabel("시간 배속:");
        lblSpeedTitle.setFont(Theme.font(11));
        lblSpeedTitle.setForeground(Theme.FG_DIM);
        pSpeed.add(lblSpeedTitle);

        String[][] labels = {{"1초", "1"}, {"1분", "60"}, {"5분", "300"}, {"30분", "1800"}, {"1시간", "3600"}};
        ButtonGroup grp = new ButtonGroup();

        for (String[] opt : labels) {
            JToggleButton tb = new JToggleButton(opt[0]);
            int speed = Integer.parseInt(opt[1]);
            tb.setFont(Theme.fontBold(11));
            tb.setBackground(Theme.BG_CARD);
            tb.setForeground(Theme.FG_DIM);
            tb.setFocusPainted(false);
            tb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1),
                BorderFactory.createEmptyBorder(3, 10, 3, 10)
            ));

            // 기본 선택: 60 (1분)
            if (speed == 60) {
                tb.setSelected(true);
                tb.setBackground(new Color(0x22, 0x33, 0x00));
                tb.setForeground(Theme.ACCENT);
            }

            tb.addActionListener(e -> {
                setSpeed(speed);
                // 선택된 버튼 강조
                for (Component c : pSpeed.getComponents()) {
                    if (c instanceof JToggleButton) {
                        JToggleButton b = (JToggleButton) c;
                        b.setBackground(b.isSelected()
                            ? new Color(0x22, 0x33, 0x00) : Theme.BG_CARD);
                        b.setForeground(b.isSelected() ? Theme.ACCENT : Theme.FG_DIM);
                    }
                }
            });

            grp.add(tb);
            pSpeed.add(tb);
        }

        bar.add(pTop,    BorderLayout.NORTH);
        bar.add(pSpeed,  BorderLayout.SOUTH);
        return bar;
    }

    // ── 서버에서 게임 시간 가져오기 ────────────────────────────

    /** 로그인/회원가입 성공 시 호출 — 상단 바 표시 + 시계 시작 */
    public void startClock() {
        topBar.setVisible(true);   // 상단 바 표시
        btnLogout.setVisible(true);
        if (!clockTimer.isRunning()) {
            clockTimer.start();
            fetchGameTime();
        }
    }

    /** 로그아웃 — 시계 중지 + 로그인 화면으로 */
    private void doLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "로그아웃 하시겠습니까?",
                "로그아웃",
                JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;

        // 타이머 중지
        clockTimer.stop();
        onLeave(currentCard);

        // 시계 초기화 + 상단 바 숨김
        lblAmPm.setText("");
        lblGameTime.setText("--:--:--");
        lblGameDate.setText("");
        lblMarketBadge.setText("");
        btnLogout.setVisible(false);
        topBar.setVisible(false);

        // 로그인 화면으로
        currentCard = "login";
        cardLayout.show(cardPanel, "login");
    }

    private void fetchGameTime() {
        new Thread(() -> {
            try {
                String body = ApiClient.get("/api/v1/stocks/market-status").body();
                Map<String, Object> data = new Gson().fromJson(
                        body, new TypeToken<Map<String, Object>>(){});
                if (data == null) return;

                String  gameTime = String.valueOf(data.getOrDefault("gameTime", "09:00:00"));
                String  gameDate = String.valueOf(data.getOrDefault("gameDate", ""));
                int     gameDay  = ((Number) data.getOrDefault("gameDay", 1)).intValue();
                boolean isOpen   = Boolean.parseBoolean(
                        String.valueOf(data.getOrDefault("isOpen", false)));

                // HH:MM:SS → 시간(int) 추출 (재할당 없이 final로 선언)
                int parsedHour;
                try { parsedHour = Integer.parseInt(gameTime.substring(0, 2)); }
                catch (Exception ignored) { parsedHour = 9; }
                final int hour = parsedHour;

                // 오전 / 오후 판단
                final String ampm        = hour < 12 ? "오전" : "오후";
                // 12시간제 (13시 → 01시, 12시는 그대로)
                final int    displayHour = hour <= 12 ? hour : hour - 12;
                // 분:초 추출 (gameTime = "HH:MM:SS")
                final String minSec = gameTime.length() >= 8
                        ? gameTime.substring(3, 8)   // "MM:SS"
                        : (gameTime.length() >= 5 ? gameTime.substring(3, 5) + ":00" : "00:00");
                final String timeDisplay = String.format("%02d:%s", displayHour, minSec);

                SwingUtilities.invokeLater(() -> {
                    lblAmPm.setText(ampm);
                    lblGameTime.setText(timeDisplay);
                    lblGameDate.setText("Day " + gameDay + "   " + gameDate);

                    if (isOpen) {
                        lblAmPm.setForeground(Theme.ACCENT);
                        lblGameTime.setForeground(Theme.FG);
                        lblMarketBadge.setText("● 장 중  09:00 ~ 15:30");
                        lblMarketBadge.setForeground(Theme.ACCENT);
                    } else {
                        lblAmPm.setForeground(Theme.FG_DIM);
                        lblGameTime.setForeground(Theme.FG_DIM);
                        lblMarketBadge.setText(hour < 9 ? "◐ 장 시작 전" : "○ 장 마감");
                        lblMarketBadge.setForeground(Theme.FG_DIM);
                    }
                });
            } catch (Exception ex) {
                // 서버 연결 전이면 조용히 넘어감
            }
        }).start();
    }

    // 배속 설정 API 호출
    private void setSpeed(int speed) {
        new Thread(() -> {
            try { ApiClient.get("/api/v1/stocks/speed/" + speed); }
            catch (Exception ex) { /* 조용히 */ }
        }).start();
    }

    // ── 화면 전환 ──────────────────────────────────────────────

    public void navigateTo(String card) {
        onLeave(currentCard);
        currentCard = card;
        cardLayout.show(cardPanel, card);
        onEnter(currentCard);
    }

    public void openStockDetail(String code, String name) {
        stockDetailPanel.setStock(code, name);
        navigateTo("stockDetail");
    }

    private void onLeave(String card) {
        if ("stockList".equals(card))   stockListPanel.stopTimer();
        if ("stockDetail".equals(card)) stockDetailPanel.stopTimer();
        if ("myAsset".equals(card))     myAssetPanel.stopTimer();
    }

    private void onEnter(String card) {
        if ("stockList".equals(card))   stockListPanel.startTimer();
        if ("stockDetail".equals(card)) stockDetailPanel.startTimer();
        if ("myAsset".equals(card))     myAssetPanel.startTimer();
    }
}

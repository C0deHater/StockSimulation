package ssg.frame;

import ssg.Theme;
import ssg.dto.StockHistoryResponse;

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class CandleChartPanel extends JPanel {

    private List<StockHistoryResponse> data;

    // ── 여백 / 크기 상수 ──────────────────────────────────
    private static final int PAD_L = 72, PAD_R = 16, PAD_T = 20, PAD_B = 16;
    private static final int VISIBLE_CANDLES = 60;
    private static final int RIGHT_PADDING   = 2;  // 최신 봉 오른쪽 여백
    private static final int RSI_H           = 70; // RSI 서브패널 높이
    private static final int RSI_GAP         = 6;  // 캔들↔RSI 간격

    // MA선 색상
    private static final Color MA5_COLOR  = new Color(0xFF, 0xCC, 0x00, 200); // 금색
    private static final Color MA20_COLOR = new Color(0x44, 0xAA, 0xFF, 200); // 하늘색

    // ── 뷰 상태 ───────────────────────────────────────────
    private double  pixelOffset = 0.0;
    private boolean autoScroll  = true;
    private int     dragLastX   = -1;

    private final DecimalFormat df = new DecimalFormat("#,###");

    public CandleChartPanel() {
        setBackground(new Color(0x08, 0x08, 0x08));
        setFocusable(true);

        addMouseWheelListener(e -> {
            if (data == null || data.isEmpty()) return;
            scrollBy(e.getWheelRotation() * getCandleWidth() * 3);
        });

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                dragLastX = e.getX();
            }
            public void mouseReleased(MouseEvent e) { dragLastX = -1; }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (data == null || dragLastX < 0) return;
                scrollBy(dragLastX - e.getX());
                dragLastX = e.getX();
            }
        });
    }

    // ── 외부 API ─────────────────────────────────────────

    public void setData(List<StockHistoryResponse> newData) {
        this.data = newData;
        if (autoScroll) pixelOffset = 0;
        repaint();
    }

    public void scrollToLatest() {
        pixelOffset = 0;
        autoScroll  = true;
        repaint();
    }

    // ── 스크롤 헬퍼 ──────────────────────────────────────

    private void scrollBy(double px) {
        double cw     = getCandleWidth();
        double maxOff = Math.max(0, (data.size() + RIGHT_PADDING - VISIBLE_CANDLES) * cw);
        pixelOffset = clamp(pixelOffset + px, 0, maxOff);
        autoScroll  = (pixelOffset < 1.0);
        repaint();
    }

    private double getCandleWidth() {
        int chartW = Math.max(1, getWidth() - PAD_L - PAD_R);
        return (double) chartW / VISIBLE_CANDLES;
    }

    // ── 전체 그리기 ──────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int chartW = w - PAD_L - PAD_R;
        int chartH = h - PAD_T - PAD_B;

        g2.setColor(getBackground());
        g2.fillRect(0, 0, w, h);

        if (data == null || data.isEmpty()) {
            drawMsg(g2, w, h, "차트 데이터를 불러오는 중...");
            return;
        }

        int    total    = data.size();
        double cw       = (double) chartW / VISIBLE_CANDLES;

        // RSI 패널 공간을 캔들 영역에서 분리
        int candleH = chartH - RSI_H - RSI_GAP;
        int rsiTop  = PAD_T + candleH + RSI_GAP;

        // ── 뷰 범위 계산 ─────────────────────────────────
        double exactOffset = pixelOffset / cw;
        int    bonOffset   = (int) exactOffset;
        double subPx       = (exactOffset - bonOffset) * cw;

        int virtualTotal = total + RIGHT_PADDING;
        int endIdx     = virtualTotal - bonOffset;
        int startIdx   = Math.max(0, endIdx - VISIBLE_CANDLES - 1);
        int drawEndIdx = Math.min(endIdx, total);

        // ── Y축 범위 (캔들) ───────────────────────────────
        double minP = Double.MAX_VALUE, maxP = -Double.MAX_VALUE;
        for (int i = startIdx; i < drawEndIdx; i++) {
            StockHistoryResponse d = data.get(i);
            minP = Math.min(minP, d.getLow());
            maxP = Math.max(maxP, d.getHigh());
        }
        // MA 선 범위도 포함
        for (int i = startIdx; i < drawEndIdx; i++) {
            Double ma5 = data.get(i).getMa5(), ma20 = data.get(i).getMa20();
            if (ma5  != null) { minP = Math.min(minP, ma5);  maxP = Math.max(maxP, ma5); }
            if (ma20 != null) { minP = Math.min(minP, ma20); maxP = Math.max(maxP, ma20); }
        }
        if (minP == Double.MAX_VALUE) { minP = 0; maxP = 1; }
        if (maxP <= minP) maxP = minP + 1;
        double pad   = (maxP - minP) * 0.08;
        minP -= pad;  maxP += pad;
        double range = maxP - minP;

        // ── Y축 그리드 ────────────────────────────────────
        g2.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        FontMetrics fm = g2.getFontMetrics();
        for (int i = 0; i <= 5; i++) {
            double frac  = (double) i / 5;
            int    gy    = PAD_T + (int)(candleH * frac);
            double price = maxP - range * frac;
            g2.setColor(new Color(0x22, 0x22, 0x22));
            g2.drawLine(PAD_L, gy, PAD_L + chartW, gy);
            g2.setColor(Theme.FG_DIM);
            String lbl = df.format(Math.round(price));
            g2.drawString(lbl, PAD_L - fm.stringWidth(lbl) - 4, gy + 4);
        }

        // ── 봉 그리기 ─────────────────────────────────────
        g2.setClip(PAD_L, PAD_T - 2, chartW, candleH + 4);

        for (int i = startIdx; i < endIdx; i++) {
            if (i >= total) break;
            StockHistoryResponse d = data.get(i);
            double open = d.getOpen(), close = d.getClose();
            double high = d.getHigh(), low   = d.getLow();

            double cx = PAD_L + (i - startIdx) * cw - subPx;
            if (cx + cw < PAD_L || cx > PAD_L + chartW) continue;

            int bw  = Math.max((int)(cw * 0.65), 2);
            int bx  = (int)(cx + (cw - bw) / 2.0);
            int mid = bx + bw / 2;

            int yH = PAD_T + (int)(clamp((maxP - high)  / range, 0, 1) * candleH);
            int yL = PAD_T + (int)(clamp((maxP - low)   / range, 0, 1) * candleH);
            int yO = PAD_T + (int)(clamp((maxP - open)  / range, 0, 1) * candleH);
            int yC = PAD_T + (int)(clamp((maxP - close) / range, 0, 1) * candleH);

            Color color = close >= open ? Theme.UP : Theme.DOWN;
            g2.setColor(color);
            g2.drawLine(mid, yH, mid, yL);
            g2.fillRect(bx, Math.min(yO, yC), bw, Math.max(Math.abs(yC - yO), 1));
        }

        // ── MA5 / MA20 선 그리기 ──────────────────────────
        drawMALine(g2, startIdx, drawEndIdx, cw, subPx, PAD_T, candleH, maxP, range,
                true,  MA5_COLOR);  // MA5 (금색)
        drawMALine(g2, startIdx, drawEndIdx, cw, subPx, PAD_T, candleH, maxP, range,
                false, MA20_COLOR); // MA20 (하늘색)

        g2.setClip(null);

        // ── 현재가 점선 ───────────────────────────────────
        if (!data.isEmpty()) {
            double last = data.get(total - 1).getClose();
            int yLast = PAD_T + (int)(clamp((maxP - last) / range, 0, 1) * candleH);
            g2.setColor(new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(),
                    Theme.ACCENT.getBlue(), 90));
            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10, new float[]{4f, 4f}, 0));
            g2.drawLine(PAD_L, yLast, PAD_L + chartW, yLast);
            g2.setStroke(new BasicStroke(1));
        }

        // ── RSI 서브패널 ──────────────────────────────────
        drawRsiPanel(g2, PAD_L, rsiTop, chartW, RSI_H, startIdx, drawEndIdx, cw, subPx);

        // ── MA 범례 ───────────────────────────────────────
        drawMALegend(g2, PAD_L + 4, PAD_T + 14);

        // ── 축 선 ─────────────────────────────────────────
        g2.setColor(Theme.BORDER);
        g2.drawLine(PAD_L, PAD_T + candleH, PAD_L + chartW, PAD_T + candleH);
        g2.setColor(new Color(0x33, 0x33, 0x33));
        g2.drawLine(PAD_L, PAD_T, PAD_L, PAD_T + candleH);
        g2.setColor(Theme.ACCENT);
        g2.drawLine(PAD_L, PAD_T, PAD_L + chartW, PAD_T);

    }

    // ── MA 선 그리기 ─────────────────────────────────────

    private void drawMALine(Graphics2D g2, int startIdx, int drawEndIdx,
                             double cw, double subPx, int padT, int candleH,
                             double maxP, double range, boolean isMA5, Color color) {
        List<int[]> points = new ArrayList<>();
        for (int i = startIdx; i < drawEndIdx; i++) {
            Double val = isMA5 ? data.get(i).getMa5() : data.get(i).getMa20();
            if (val == null) continue;
            double cx = PAD_L + (i - startIdx) * cw - subPx;
            int mx = (int)(cx + cw / 2);
            int my = padT + (int)(clamp((maxP - val) / range, 0, 1) * candleH);
            points.add(new int[]{mx, my});
        }
        if (points.size() < 2) return;

        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < points.size() - 1; i++) {
            g2.drawLine(points.get(i)[0], points.get(i)[1],
                        points.get(i+1)[0], points.get(i+1)[1]);
        }
        g2.setStroke(new BasicStroke(1));
    }

    // ── RSI 서브패널 ─────────────────────────────────────

    private void drawRsiPanel(Graphics2D g2, int x, int y, int w, int h,
                               int startIdx, int drawEndIdx, double cw, double subPx) {
        // 배경
        g2.setColor(new Color(0x0C, 0x0C, 0x0C));
        g2.fillRect(x, y, w, h);

        // 구분선
        g2.setColor(new Color(0x33, 0x33, 0x33));
        g2.drawLine(x, y, x + w, y);

        g2.setFont(new Font("맑은 고딕", Font.PLAIN, 9));
        FontMetrics fm = g2.getFontMetrics();

        // 30/70 참조선 + 존 배경
        int y70 = y + (int)((1.0 - 70.0 / 100) * h);
        int y30 = y + (int)((1.0 - 30.0 / 100) * h);

        // 과매수 존 (70 이상) — 반투명 빨강
        g2.setColor(new Color(0x99, 0x22, 0x22, 30));
        g2.fillRect(x, y, w, y70 - y);
        // 과매도 존 (30 이하) — 반투명 초록
        g2.setColor(new Color(0x22, 0x88, 0x22, 30));
        g2.fillRect(x, y30, w, h - (y30 - y));

        // 70선 / 30선
        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10, new float[]{4f, 3f}, 0));
        g2.setColor(new Color(0xCC, 0x44, 0x44, 160));
        g2.drawLine(x, y70, x + w, y70);
        g2.setColor(new Color(0x44, 0xCC, 0x44, 160));
        g2.drawLine(x, y30, x + w, y30);
        g2.setStroke(new BasicStroke(1));

        // Y축 레이블
        g2.setColor(Theme.FG_DIM);
        g2.drawString("70", x - fm.stringWidth("70") - 4, y70 + 4);
        g2.drawString("30", x - fm.stringWidth("30") - 4, y30 + 4);
        g2.drawString("RSI", x - fm.stringWidth("RSI") - 4, y + 10);

        // RSI 선 그리기
        g2.setClip(x, y, w, h);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int[] prevPt = null;
        for (int i = startIdx; i < drawEndIdx; i++) {
            Double rsi = data.get(i).getRsi();
            if (rsi == null) { prevPt = null; continue; }

            double cx = PAD_L + (i - startIdx) * cw - subPx;
            int mx  = (int)(cx + cw / 2);
            int ry  = y + (int)((1.0 - rsi / 100.0) * h);

            Color c = rsi > 70 ? Theme.DOWN
                    : rsi < 30 ? Theme.UP
                    : new Color(0xCC, 0xAA, 0x22);
            g2.setColor(c);

            if (prevPt != null) g2.drawLine(prevPt[0], prevPt[1], mx, ry);
            prevPt = new int[]{mx, ry};
        }
        g2.setStroke(new BasicStroke(1));
        g2.setClip(null);
    }

    // ── MA 범례 ───────────────────────────────────────────

    private void drawMALegend(Graphics2D g2, int x, int y) {
        g2.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        int bw = 18, bh = 3, gap = 4;

        g2.setColor(MA5_COLOR);
        g2.fillRect(x, y - bh, bw, bh);
        g2.setColor(Theme.FG_DIM);
        g2.drawString("MA5", x + bw + gap, y);

        g2.setColor(MA20_COLOR);
        g2.fillRect(x + bw + gap + 28 + gap, y - bh, bw, bh);
        g2.setColor(Theme.FG_DIM);
        g2.drawString("MA20", x + bw + gap + 28 + gap + bw + gap, y);
    }

    private void drawMsg(Graphics2D g2, int w, int h, String msg) {
        g2.setColor(Theme.FG_DIM);
        g2.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}

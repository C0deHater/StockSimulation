package ssg;

import javax.swing.*;
import java.awt.*;

// 전체 앱에서 사용하는 색상·폰트 상수 및 스타일 헬퍼
public class Theme {

    // ── 색상 ──────────────────────────────────────────
    public static final Color BG       = new Color(0x0F, 0x0F, 0x0F); // 메인 배경
    public static final Color BG_CARD  = new Color(0x1A, 0x1A, 0x1A); // 패널/카드
    public static final Color BG_INPUT = new Color(0x25, 0x25, 0x25); // 입력 필드
    public static final Color ACCENT   = new Color(0xCC, 0xFF, 0x00); // #CCFF00 형광 초록
    public static final Color FG       = new Color(0xF0, 0xF0, 0xF0); // 기본 텍스트
    public static final Color FG_DIM   = new Color(0x77, 0x77, 0x77); // 흐린 텍스트
    public static final Color UP       = new Color(0xFF, 0x55, 0x55); // 상승 (빨강)
    public static final Color DOWN     = new Color(0x55, 0xAA, 0xFF); // 하락 (파랑)
    public static final Color BORDER   = new Color(0x30, 0x30, 0x30); // 테두리

    // ── 폰트 ──────────────────────────────────────────
    public static Font font(int size)             { return new Font("맑은 고딕", Font.PLAIN, size); }
    public static Font fontBold(int size)         { return new Font("맑은 고딕", Font.BOLD,  size); }

    // ── UIManager 전역 적용 (Main에서 가장 먼저 호출) ──
    public static void apply() {
        UIManager.put("Panel.background",              BG);
        UIManager.put("OptionPane.background",         BG_CARD);
        UIManager.put("OptionPane.messageForeground",  FG);
        UIManager.put("Label.foreground",              FG);
        UIManager.put("Button.background",             BG_CARD);
        UIManager.put("Button.foreground",             ACCENT);
        UIManager.put("TextField.background",          BG_INPUT);
        UIManager.put("TextField.foreground",          FG);
        UIManager.put("TextField.caretForeground",     ACCENT);
        UIManager.put("TextField.border",              BorderFactory.createLineBorder(BORDER));
        UIManager.put("PasswordField.background",      BG_INPUT);
        UIManager.put("PasswordField.foreground",      FG);
        UIManager.put("PasswordField.caretForeground", ACCENT);
        UIManager.put("Table.background",              BG_CARD);
        UIManager.put("Table.foreground",              FG);
        UIManager.put("Table.gridColor",               BORDER);
        UIManager.put("Table.selectionBackground",     new Color(0xCC, 0xFF, 0x00, 60));
        UIManager.put("Table.selectionForeground",     FG);
        UIManager.put("TableHeader.background",        BG);
        UIManager.put("TableHeader.foreground",        ACCENT);
        UIManager.put("ScrollPane.background",         BG);
        UIManager.put("Viewport.background",           BG_CARD);
        UIManager.put("ScrollBar.background",          BG_CARD);
        UIManager.put("TitledBorder.titleColor",       ACCENT);
        UIManager.put("TitledBorder.border",           BorderFactory.createLineBorder(BORDER));
        UIManager.put("SplitPane.background",          BG);
        UIManager.put("MenuBar.background",            BG);
    }

    // ── 컴포넌트 스타일 헬퍼 ──────────────────────────

    /** 일반 버튼 (윤곽선 스타일) */
    public static void btn(JButton b) {
        b.setBackground(BG_CARD);
        b.setForeground(ACCENT);
        b.setFocusPainted(false);
        b.setFont(fontBold(12));
        b.setOpaque(true);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT, 1),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
    }

    /** 강조 버튼 (채워진 ACCENT 배경) */
    public static void btnAccent(JButton b) {
        b.setBackground(ACCENT);
        b.setForeground(Color.BLACK);
        b.setFocusPainted(false);
        b.setFont(fontBold(13));
        b.setOpaque(true);
        b.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
    }

    /** 텍스트 필드 스타일 */
    public static void field(JTextField f) {
        f.setBackground(BG_INPUT);
        f.setForeground(FG);
        f.setCaretColor(ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }

    /** JTable 스타일 */
    public static void table(JTable t) {
        t.setBackground(BG_CARD);
        t.setForeground(FG);
        t.setGridColor(BORDER);
        t.setSelectionBackground(new Color(0x33, 0x44, 0x00));
        t.setSelectionForeground(ACCENT);
        t.setRowHeight(28);
        t.setFont(font(13));
        t.getTableHeader().setBackground(BG);
        t.getTableHeader().setForeground(ACCENT);
        t.getTableHeader().setFont(fontBold(13));
        t.getTableHeader().setBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ACCENT));
    }

    /** JScrollPane 배경 */
    public static void scroll(JScrollPane sp) {
        sp.setBackground(BG);
        sp.getViewport().setBackground(BG_CARD);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
    }

    /** 패널 다크 배경 */
    public static void dark(JPanel p) {
        p.setBackground(BG);
        p.setForeground(FG);
    }

    /** 카드 패널 (살짝 밝은 배경) */
    public static void card(JPanel p) {
        p.setBackground(BG_CARD);
        p.setForeground(FG);
    }

    /** 구분선 (ACCENT 색상 1px) */
    public static JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(ACCENT);
        sep.setBackground(BG);
        return sep;
    }
}

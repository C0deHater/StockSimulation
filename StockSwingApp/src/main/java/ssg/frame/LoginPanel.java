package ssg.frame;

import com.google.gson.Gson;
import ssg.Theme;
import ssg.dto.UserRequest;
import ssg.network.ApiClient;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class LoginPanel extends JPanel {

    private final MainWindow  mainWindow;
    private JTextField        tfId;
    private JPasswordField    tfPw;
    private JButton           btnLogin;

    public LoginPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setLayout(new GridBagLayout()); // 수직 가운데 정렬용
        setBackground(Theme.BG);

        // ── 로그인 카드 패널 ──
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));

        // 로고/제목
        JLabel lblLogo = new JLabel("📈", JLabel.CENTER);
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        lblLogo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("주식 시뮬레이션", JLabel.CENTER);
        lblTitle.setFont(Theme.fontBold(22));
        lblTitle.setForeground(Theme.ACCENT);
        lblTitle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("실전처럼 거래하는 모의투자 플랫폼", JLabel.CENTER);
        lblSub.setFont(Theme.font(12));
        lblSub.setForeground(Theme.FG_DIM);
        lblSub.setAlignmentX(CENTER_ALIGNMENT);

        // 구분선
        JSeparator sep = Theme.separator();
        sep.setMaximumSize(new Dimension(280, 1));
        sep.setAlignmentX(CENTER_ALIGNMENT);

        // 입력 필드
        JLabel lblId = new JLabel("아이디");
        lblId.setForeground(Theme.FG_DIM);
        lblId.setFont(Theme.font(11));
        lblId.setAlignmentX(CENTER_ALIGNMENT);

        tfId = new JTextField(20);
        Theme.field(tfId);
        tfId.setMaximumSize(new Dimension(280, 36));
        tfId.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblPw = new JLabel("비밀번호");
        lblPw.setForeground(Theme.FG_DIM);
        lblPw.setFont(Theme.font(11));
        lblPw.setAlignmentX(CENTER_ALIGNMENT);

        tfPw = new JPasswordField(20);
        Theme.field(tfPw);
        tfPw.setMaximumSize(new Dimension(280, 36));
        tfPw.setAlignmentX(CENTER_ALIGNMENT);

        // 로그인 버튼
        btnLogin = new JButton("로그인");
        Theme.btnAccent(btnLogin);
        btnLogin.setMaximumSize(new Dimension(280, 42));
        btnLogin.setAlignmentX(CENTER_ALIGNMENT);

        // 회원가입 링크
        JLabel lblSignup = new JLabel("계정이 없으신가요?  회원가입 →", JLabel.CENTER);
        lblSignup.setFont(Theme.font(11));
        lblSignup.setForeground(Theme.ACCENT);
        lblSignup.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblSignup.setAlignmentX(CENTER_ALIGNMENT);

        // 카드에 컴포넌트 추가
        card.add(lblLogo);
        card.add(Box.createVerticalStrut(8));
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(4));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(20));
        card.add(sep);
        card.add(Box.createVerticalStrut(20));
        card.add(lblId);
        card.add(Box.createVerticalStrut(4));
        card.add(tfId);
        card.add(Box.createVerticalStrut(12));
        card.add(lblPw);
        card.add(Box.createVerticalStrut(4));
        card.add(tfPw);
        card.add(Box.createVerticalStrut(20));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(16));
        card.add(lblSignup);

        add(card);

        // ── 이벤트 ──
        btnLogin.addActionListener(e -> doLogin());
        tfPw.addActionListener(e -> doLogin());
        lblSignup.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { mainWindow.navigateTo("signup"); }
        });
    }

    private void doLogin() {
        String id = tfId.getText().trim();
        String pw = new String(tfPw.getPassword()).trim();
        if (id.isEmpty() || pw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 입력하세요.");
            return;
        }
        btnLogin.setEnabled(false);
        btnLogin.setText("로그인 중...");

        new Thread(() -> {
            try {
                int status = ApiClient.post("/api/v1/auth/login",
                        new Gson().toJson(new UserRequest(id, pw, 0L))).statusCode();
                SwingUtilities.invokeLater(() -> {
                    if (status == 200) {
                        tfId.setText(""); tfPw.setText("");
                        mainWindow.startClock(); // 로그인 성공 → 시계 시작
                        mainWindow.navigateTo("stockList");
                    } else {
                        JOptionPane.showMessageDialog(this, "아이디 또는 비밀번호가 틀렸습니다.");
                    }
                    btnLogin.setEnabled(true);
                    btnLogin.setText("로그인");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "서버 연결 실패\n(" + ex.getMessage() + ")");
                    btnLogin.setEnabled(true);
                    btnLogin.setText("로그인");
                });
            }
        }).start();
    }
}

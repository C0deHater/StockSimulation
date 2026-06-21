package ssg.frame;

import com.google.gson.Gson;
import ssg.Theme;
import ssg.dto.UserRequest;
import ssg.network.ApiClient;

import java.awt.*;
import javax.swing.*;

public class SignUpPanel extends JPanel {

    private final MainWindow  mainWindow;
    private JTextField        tfId;
    private JPasswordField    tfPw;
    private JButton           btnRegister;

    public SignUpPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setLayout(new GridBagLayout());
        setBackground(Theme.BG);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));

        // 뒤로가기
        JButton btnBack = new JButton("← 뒤로");
        Theme.btn(btnBack);
        btnBack.setAlignmentX(LEFT_ALIGNMENT);
        btnBack.addActionListener(e -> mainWindow.navigateTo("login"));

        // 제목
        JLabel lblTitle = new JLabel("회원가입", JLabel.CENTER);
        lblTitle.setFont(Theme.fontBold(20));
        lblTitle.setForeground(Theme.ACCENT);
        lblTitle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("초기 자본금 1억 원이 지급됩니다", JLabel.CENTER);
        lblSub.setFont(Theme.font(11));
        lblSub.setForeground(Theme.FG_DIM);
        lblSub.setAlignmentX(CENTER_ALIGNMENT);

        JSeparator sep = Theme.separator();
        sep.setMaximumSize(new Dimension(280, 1));
        sep.setAlignmentX(CENTER_ALIGNMENT);

        // 입력 필드
        JLabel lblId = new JLabel("아이디");
        lblId.setForeground(Theme.FG_DIM); lblId.setFont(Theme.font(11));
        lblId.setAlignmentX(CENTER_ALIGNMENT);

        tfId = new JTextField(20);
        Theme.field(tfId);
        tfId.setMaximumSize(new Dimension(280, 36));
        tfId.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblPw = new JLabel("비밀번호");
        lblPw.setForeground(Theme.FG_DIM); lblPw.setFont(Theme.font(11));
        lblPw.setAlignmentX(CENTER_ALIGNMENT);

        tfPw = new JPasswordField(20);
        Theme.field(tfPw);
        tfPw.setMaximumSize(new Dimension(280, 36));
        tfPw.setAlignmentX(CENTER_ALIGNMENT);

        btnRegister = new JButton("가입하기");
        Theme.btnAccent(btnRegister);
        btnRegister.setMaximumSize(new Dimension(280, 42));
        btnRegister.setAlignmentX(CENTER_ALIGNMENT);

        card.add(btnBack);
        card.add(Box.createVerticalStrut(16));
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
        card.add(btnRegister);

        add(card);

        btnRegister.addActionListener(e -> doSignUp());
    }

    private void doSignUp() {
        String id = tfId.getText().trim();
        String pw = new String(tfPw.getPassword()).trim();
        if (id.isEmpty() || pw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 입력하세요.");
            return;
        }
        btnRegister.setEnabled(false);
        btnRegister.setText("가입 중...");

        new Thread(() -> {
            try {
                int status = ApiClient.post("/api/v1/auth/signup",
                        new Gson().toJson(new UserRequest(id, pw, 100_000_000L))).statusCode();
                SwingUtilities.invokeLater(() -> {
                    if (status == 200) {
                        JOptionPane.showMessageDialog(this, "회원가입 성공!\n초기 자본금 1억 원이 지급되었습니다.");
                        tfId.setText(""); tfPw.setText("");
                        mainWindow.startClock(); // 회원가입 성공 → 시계 시작
                        mainWindow.navigateTo("stockList");
                    } else {
                        JOptionPane.showMessageDialog(this, "가입 실패 (코드: " + status + ")");
                    }
                    btnRegister.setEnabled(true);
                    btnRegister.setText("가입하기");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "서버 연결 실패\n(" + ex.getMessage() + ")");
                    btnRegister.setEnabled(true);
                    btnRegister.setText("가입하기");
                });
            }
        }).start();
    }
}

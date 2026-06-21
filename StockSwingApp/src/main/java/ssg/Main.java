package ssg;

import ssg.frame.MainWindow;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // 모든 컴포넌트 생성 전에 전역 테마 적용
        Theme.apply();
        SwingUtilities.invokeLater(() -> new MainWindow());
    }
}

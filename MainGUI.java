import javax.swing.*;
import java.awt.*;

public class MainGUI extends JFrame implements MainFrameController {
    private SqliteDatabase db;
    private RegistrationSystem system;

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainContainer = new JPanel(cardLayout);

    private Student currentStudent;
    private Teacher currentTeacher;

    private StudentPanel studentPanel;
    private TeacherPanel teacherPanel;
    private AdminPanel adminPanel;

    public MainGUI() {
        setTitle("School Administration System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize backend
        db = new SqliteDatabase();
        system = new RegistrationSystem(db);

        // Build Panels
        LoginPanel loginPanel = new LoginPanel(this);
        studentPanel = new StudentPanel(this);
        teacherPanel = new TeacherPanel(this);
        adminPanel = new AdminPanel(this);

        mainContainer.add(loginPanel, "LoginCard");
        mainContainer.add(studentPanel, "StudentCard");
        mainContainer.add(teacherPanel, "TeacherCard");
        mainContainer.add(adminPanel, "AdminCard");

        add(mainContainer);

        switchToPanel("LoginCard");
    }

    @Override
    public void switchToPanel(String panelName) {
        cardLayout.show(mainContainer, panelName);
        if ("StudentCard".equals(panelName)) {
            studentPanel.refreshStudentView();
        } else if ("TeacherCard".equals(panelName)) {
            teacherPanel.refreshTeacherView();
        } else if ("AdminCard".equals(panelName)) {
            adminPanel.refreshAdminView();
        }
    }

    @Override
    public void logout() {
        this.currentStudent = null;
        this.currentTeacher = null;
        switchToPanel("LoginCard");
    }

    @Override
    public SqliteDatabase getDatabase() {
        return db;
    }

    @Override
    public RegistrationSystem getSystem() {
        return system;
    }

    @Override
    public Student getCurrentStudent() {
        return currentStudent;
    }

    @Override
    public void setCurrentStudent(Student student) {
        this.currentStudent = student;
    }

    @Override
    public Teacher getCurrentTeacher() {
        return currentTeacher;
    }

    @Override
    public void setCurrentTeacher(Teacher teacher) {
        this.currentTeacher = teacher;
    }

    @Override
    public JFrame getFrame() {
        return this;
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Cannot load Nimbus look and feel, using system default.");
        }

        Font uiFont = new Font("Segoe UI", Font.PLAIN, 20);
        javax.swing.UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        for (Object key : defaults.keySet()) {
            if (key.toString().endsWith(".font")) {
                // 顏色調整
                UIManager.put("control", new Color(230, 230, 230)); // 面板底色
                UIManager.put("nimbusBase", new Color(150, 150, 150)); // 元件邊框、捲軸
                UIManager.put("nimbusBlueGrey", new Color(245, 245, 245)); // 按鈕與未選取分頁
                UIManager.put("nimbusLightBackground", new Color(255, 255, 255)); // 表格與輸入框
                UIManager.put("nimbusSelectionBackground", new Color(90, 150, 215)); // 反白選取的顏色
                UIManager.put("nimbusFocus", new Color(120, 180, 240)); // 點擊時的外框顏色
                UIManager.put("text", new Color(30, 30, 30)); // 文字顏色

                defaults.put(key, uiFont);
            }
        }

        SwingUtilities.invokeLater(() -> {
            new MainGUI().setVisible(true);
        });
    }
}

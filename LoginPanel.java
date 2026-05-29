import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
public class LoginPanel extends JPanel {
    private MainFrameController controller;
    public LoginPanel(MainFrameController controller) {
        this.controller = controller;
        buildPanel();
    }
    private void buildPanel() {
        this.setLayout(new GridBagLayout());
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        JLabel lblId = new JLabel("ID (Student/Professor/Admin):");
        JTextField txtId = new JTextField(15);
        JLabel lblPwd = new JLabel("Password:");
        JPasswordField txtPwd = new JPasswordField(15);
        JButton btnLogin = new JButton("Login");
        formPanel.add(lblId);
        formPanel.add(txtId);
        formPanel.add(lblPwd);
        formPanel.add(txtPwd);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        this.add(formPanel, gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(20, 0, 0, 0);
        this.add(btnLogin, gbc);
        // This works when the panel is added to a JFrame
        SwingUtilities.invokeLater(() -> controller.getFrame().getRootPane().setDefaultButton(btnLogin));
        btnLogin.addActionListener(e -> {
            String uid = txtId.getText().trim();
            String pwd = new String(txtPwd.getPassword()).trim();
            if (uid.isEmpty() || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both ID and password!", "Input Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            SqliteDatabase db = controller.getDatabase();
            Admin a = db.findAdmin(uid);
            Teacher t = db.findTeacher(uid);
            Student s = db.findStudent(uid);
            if (t != null && t.verifyPassword(pwd)) {
                List<Course> allCourses = db.getAllCourses();
                for (Course c : allCourses) {
                    if (c.getTeacher().getUid().equals(t.getUid())) {
                        db.hydrateCourseStudents(c);
                        t.assignCourse(c);
                    }
                }
                controller.setCurrentTeacher(t);
                JOptionPane.showMessageDialog(this, "Professor login successful! Welcome " + t.getName());
                controller.switchToPanel("TeacherCard");
                txtId.setText("");
                txtPwd.setText("");
            } else if (s != null && s.verifyPassword(pwd)) {
                Map<Course, Double> gradesMap = db.getStudentGradesMap(s.getUid());
                for (Map.Entry<Course, Double> entry : gradesMap.entrySet()) {
                    Course c = entry.getKey();
                    Double score = entry.getValue();
                    s.enrollInCourse(c);
                    if (score != null) {
                        s.setGrade(c, score);
                    }
                }
                controller.setCurrentStudent(s);
                JOptionPane.showMessageDialog(this, "Student login successful! Welcome " + s.getName());
                controller.switchToPanel("StudentCard");
                txtId.setText("");
                txtPwd.setText("");
            } else if (a != null && a.verifyPassword(pwd)) {
                controller.setCurrentAdmin(a);
                JOptionPane.showMessageDialog(this, "Admin login successful!");
                txtId.setText("");
                txtPwd.setText("");
                controller.switchToPanel("AdminCard");
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect ID or password!", "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}

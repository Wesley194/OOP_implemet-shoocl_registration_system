import javax.swing.*;
import java.awt.*;
public class RegisterPanel extends JPanel {
    private MainFrameController controller;

    public RegisterPanel(MainFrameController controller) {
        this.controller = controller;
        buildPanel();
    }

    private void buildPanel() {
        this.setLayout(new GridBagLayout());
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Register New Account"));
        JLabel lblRole = new JLabel("Role:");
        JComboBox<String> comboRole = new JComboBox<>(new String[] { "Student", "Professor" });
        JLabel lblId = new JLabel("ID (Student/Professor):");
        JTextField txtId = new JTextField(15);
        JLabel lblName = new JLabel("Name:");
        JTextField txtName = new JTextField(15);
        JLabel lblPwd = new JLabel("Password:");
        JPasswordField txtPwd = new JPasswordField(15);
        JButton btnRegister = new JButton("Confirm Registration");
        formPanel.add(lblRole);
        formPanel.add(comboRole);
        formPanel.add(lblId);
        formPanel.add(txtId);
        formPanel.add(lblName);
        formPanel.add(txtName);
        formPanel.add(lblPwd);
        formPanel.add(txtPwd);
        formPanel.add(new JLabel(""));
        formPanel.add(btnRegister);
        this.add(formPanel);

        btnRegister.addActionListener(e -> {
            String role = (String) comboRole.getSelectedItem();
            String uid = txtId.getText().trim();
            String name = txtName.getText().trim();
            String pwd = new String(txtPwd.getPassword()).trim();

            if (uid.isEmpty() || name.isEmpty() || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields!", "Registration Failed",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            SqliteDatabase db = controller.getDatabase();
            if ("Student".equals(role)) {
                if (db.findStudent(uid) != null) {
                    JOptionPane.showMessageDialog(this, "This student ID already exists!", "Registration Failed",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                db.registerStudent(new Student(uid, name, pwd));
            } else {
                if (db.findTeacher(uid) != null) {
                    JOptionPane.showMessageDialog(this, "This professor ID already exists!", "Registration Failed",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                db.registerTeacher(new Teacher(uid, name, pwd));
            }
            JOptionPane.showMessageDialog(this, "Registration successful!");
            txtId.setText("");
            txtName.setText("");
            txtPwd.setText("");
        });
    }
}

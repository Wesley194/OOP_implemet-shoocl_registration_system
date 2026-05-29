import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;

public class UserProfileDialog {
    public static void showDialog(JFrame parent, User user, SqliteDatabase db) {
        JDialog dialog = new JDialog(parent, "User Profile", true);
        dialog.setSize(500, 300);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(parent);

        String[] cols = {"", ""};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addRow(new Object[]{"ID", user.getUid()});
        model.addRow(new Object[]{"Name", user.getName()});
        
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setTableHeader(null);
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JButton btnChangeName = new JButton("Change User Name");
        JButton btnChangePassword = new JButton("Change Password");
        btnPanel.add(btnChangeName);
        btnPanel.add(btnChangePassword);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        UserManager userManager = new UserManager(db);

        btnChangeName.addActionListener(e -> {
            String currentPassword = JOptionPane.showInputDialog(dialog, "Enter current password to verify:");
            if (currentPassword == null) return;
            String newName = JOptionPane.showInputDialog(dialog, "Enter NEW name:");
            if (newName == null) return;
            
            boolean success = userManager.updateName(user, currentPassword, newName);
            if (success) {
                user.setName(newName);
                model.setValueAt(newName, 1, 1);
                JOptionPane.showMessageDialog(dialog, "Name successfully changed!");
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to update name. Check password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnChangePassword.addActionListener(e -> {
            JPasswordField pwdCurrent = new JPasswordField(10);
            JPasswordField pwdNew = new JPasswordField(10);
            JPasswordField pwdConfirm = new JPasswordField(10);
            
            JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
            panel.add(new JLabel("Current Password:"));
            panel.add(pwdCurrent);
            panel.add(new JLabel("New Password:"));
            panel.add(pwdNew);
            panel.add(new JLabel("Confirm New Password:"));
            panel.add(pwdConfirm);

            int result = JOptionPane.showConfirmDialog(dialog, panel, "Change Password", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String currentStr = new String(pwdCurrent.getPassword());
                String newStr = new String(pwdNew.getPassword());
                String confirmStr = new String(pwdConfirm.getPassword());
                boolean success = userManager.updatePassword(user, currentStr, newStr, confirmStr);
                if (success) {
                    user.setPassword(newStr);
                    JOptionPane.showMessageDialog(dialog, "Password successfully changed!");
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update password. Check inputs.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dialog.setVisible(true);
    }
}


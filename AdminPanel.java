import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminPanel extends JPanel {
    private MainFrameController controller;
    private JLabel lblAdminStatus = new JLabel();
    private JTabbedPane adminTabbedPane;
    private JPanel userListPanel;
    private JPanel courseListPanel;
    private JComboBox<String> comboViewRole;
    private DefaultTableModel userTableModel;
    private DefaultTableModel courseTableModel;

    public AdminPanel(MainFrameController controller) {
        this.controller = controller;
        buildPanel();
    }

    private void buildPanel() {
        this.setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton btnInfo = new JButton("ⓘ");
        btnInfo.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        btnInfo.setMargin(new Insets(0, 0, 0, 0));
        btnInfo.setContentAreaFilled(false);
        btnInfo.setBorderPainted(false);
        btnInfo.setFocusPainted(false);
        btnInfo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnInfo.addActionListener(e -> {
            if (controller.getCurrentAdmin() != null) {
                UserProfileDialog.showDialog(controller.getFrame(), controller.getCurrentAdmin(), controller.getDatabase());
                refreshAdminView();
            }
        });
        leftPanel.add(btnInfo);

        lblAdminStatus.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblAdminStatus.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
        leftPanel.add(lblAdminStatus);
        topPanel.add(leftPanel, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refreshAdminView());
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> controller.logout());
        rightPanel.add(btnRefresh);
        rightPanel.add(btnLogout);
        topPanel.add(rightPanel, BorderLayout.EAST);
        this.add(topPanel, BorderLayout.NORTH);
        // System Control Panel
        JPanel controlPanel = new JPanel(new GridLayout(4, 1, 20, 20));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(50, 150, 50, 150));

        RegistrationSystem system = controller.getSystem();
        JButton btnClosed = new JButton("Switch to: " + system.getPhaseName(SystemStateManager.SystemPhase.CLOSED));
        JButton btnPreEnroll = new JButton(
                "Switch to: " + system.getPhaseName(SystemStateManager.SystemPhase.PRE_ENROLL));
        JButton btnLottery = new JButton(
                "Switch to: " + system.getPhaseName(SystemStateManager.SystemPhase.LOTTERY_RUN));
        JButton btnAddDrop = new JButton("Switch to: " + system.getPhaseName(SystemStateManager.SystemPhase.ADD_DROP));
        btnClosed.addActionListener(e -> {
            system.setCurrentPhase(SystemStateManager.SystemPhase.CLOSED);
            refreshAdminView();
            JOptionPane.showMessageDialog(this,
                    "Switched to " + system.getPhaseName(SystemStateManager.SystemPhase.CLOSED));
        });
        btnPreEnroll.addActionListener(e -> {
            system.setCurrentPhase(SystemStateManager.SystemPhase.PRE_ENROLL);
            refreshAdminView();
            JOptionPane.showMessageDialog(this,
                    "Switched to " + system.getPhaseName(SystemStateManager.SystemPhase.PRE_ENROLL));
        });
        btnAddDrop.addActionListener(e -> {
            system.setCurrentPhase(SystemStateManager.SystemPhase.ADD_DROP);
            refreshAdminView();
            JOptionPane.showMessageDialog(this,
                    "Switched to " + system.getPhaseName(SystemStateManager.SystemPhase.ADD_DROP));
        });
        btnLottery.addActionListener(e -> {
            system.setCurrentPhase(SystemStateManager.SystemPhase.LOTTERY_RUN);
            refreshAdminView();
            JOptionPane.showMessageDialog(this, "System switched to "
                    + system.getPhaseName(SystemStateManager.SystemPhase.LOTTERY_RUN) + ", starting lottery...");
            try {
                system.runLotterySystem();
                JOptionPane.showMessageDialog(this, "Lottery completed!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lottery error: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        controlPanel.add(btnClosed);
        controlPanel.add(btnPreEnroll);
        controlPanel.add(btnLottery);
        controlPanel.add(btnAddDrop);

        // View Users Panel
        userListPanel = new JPanel(new BorderLayout());
        String[] viewOptions = { "Student", "Professor" };
        comboViewRole = new JComboBox<>(viewOptions);
        JPanel topBoxPanel = new JPanel();
        topBoxPanel.add(new JLabel("Please select a list to view: "));
        topBoxPanel.add(comboViewRole);
        userListPanel.add(topBoxPanel, BorderLayout.NORTH);
        String[] userCols = { "ID", "Name", "Password" };
        userTableModel = new DefaultTableModel(userCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable userTable = new JTable(userTableModel);
        userTable.setRowHeight(25);
        userTable.getTableHeader().setReorderingAllowed(false);
        userListPanel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        comboViewRole.addActionListener(e -> {
            userTableModel.setRowCount(0);
            SqliteDatabase db = controller.getDatabase();
            if ("Student".equals(comboViewRole.getSelectedItem())) {
                List<Student> students = db.getAllStudents();
                for (Student s : students) {
                    userTableModel.addRow(new Object[] { s.getUid(), s.getName(), s.getPassword() });
                }
            } else {
                List<Teacher> teachers = db.getAllTeachers();
                for (Teacher t : teachers) {
                    userTableModel.addRow(new Object[] { t.getUid(), t.getName(), t.getPassword() });
                }
            }
        });
        comboViewRole.setSelectedIndex(0);
        
        // View Courses Panel
        courseListPanel = new JPanel(new BorderLayout());
        String[] courseCols = { "Course ID", "Course Name", "Credits", "Capacity", "Professor", "Time", "Auth Code" };
        courseTableModel = new DefaultTableModel(courseCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable courseTable = new JTable(courseTableModel);
        courseTable.setRowHeight(25);
        courseTable.getTableHeader().setReorderingAllowed(false);
        courseTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = courseTable.rowAtPoint(e.getPoint());
                int col = courseTable.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 6) {
                    String courseId = (String) courseTableModel.getValueAt(row, 0);
                    String courseName = (String) courseTableModel.getValueAt(row, 1);
                    List<AuthCode> authCodesList = controller.getDatabase().getCourseAuthCodes(courseId);
                    if (authCodesList.isEmpty()) {
                        JOptionPane.showMessageDialog(courseListPanel,
                                "No Auth Codes generated for [" + courseName + "].",
                                "Auth Code Details", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    String[] acCols = { "Auth Code", "Status", "Used By" };
                    DefaultTableModel acModel = new DefaultTableModel(acCols, 0) {
                        @Override
                        public boolean isCellEditable(int r, int c) {
                            return false;
                        }
                    };
                    for (AuthCode ac : authCodesList) {
                        acModel.addRow(new Object[] {
                                ac.getCode(),
                                ac.isUsed() ? "Used" : "Unused",
                                ac.getUsedBy() == null ? "-" : ac.getUsedBy()
                        });
                    }
                    JTable acTable = new JTable(acModel);
                    acTable.setRowHeight(25);
                    acTable.getTableHeader().setReorderingAllowed(false);
                    JScrollPane scrollPane = new JScrollPane(acTable);
                    scrollPane.setPreferredSize(new Dimension(400, 300));
                    JOptionPane.showMessageDialog(courseListPanel, scrollPane,
                            "Auth Code Details - " + courseName, JOptionPane.PLAIN_MESSAGE);
                }
            }
        });
        courseListPanel.add(new JScrollPane(courseTable), BorderLayout.CENTER);
        adminTabbedPane = new JTabbedPane();
        adminTabbedPane.addTab("System Control Panel", controlPanel);
        adminTabbedPane.addTab("Register New Account", new RegisterPanel(controller));
        adminTabbedPane.addTab("View Users", userListPanel);
        adminTabbedPane.addTab("View Courses", courseListPanel);
        adminTabbedPane.addChangeListener(e -> refreshAdminTabbedPane());
        this.add(adminTabbedPane, BorderLayout.CENTER);
    }

    private void refreshAdminTabbedPane() {
        if (adminTabbedPane.getSelectedComponent() == userListPanel) {
            comboViewRole.setSelectedIndex(comboViewRole.getSelectedIndex());
        } else if (adminTabbedPane.getSelectedComponent() == courseListPanel) {
            courseTableModel.setRowCount(0);
            List<Course> allCourses = controller.getDatabase().getAllCourses();
            for (Course c : allCourses) {
                List<AuthCode> authCodesList = controller.getDatabase().getCourseAuthCodes(c.getCourseId());
                String hasAuthCode = !authCodesList.isEmpty() ? "Yes" : "No";
                courseTableModel.addRow(new Object[] {
                        c.getCourseId(), c.getCourseName(), c.getCredits(),
                        c.getMaxCapacity(), c.getTeacher().getName(),
                        c.getTimeSlot().toString(), hasAuthCode
                });
            }
        }
    }

    public void refreshAdminView() {
        lblAdminStatus.setText("Admin Control Panel | Phase: "
                + controller.getSystem().getPhaseName(controller.getSystem().getCurrentPhase()));
        if (adminTabbedPane != null) {
            refreshAdminTabbedPane();
        }
    }
}


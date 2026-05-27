import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class MainGUI extends JFrame {

    // --- 系統大腦與虛擬資料庫 ---
    private SqliteDatabase db = new SqliteDatabase();
    private RegistrationSystem system = new RegistrationSystem(db);

    // --- 畫面切換工具 (卡片佈局) ---
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainContainer = new JPanel(cardLayout);

    // --- 四大主要畫面 ---
    private JPanel loginPanel = new JPanel();
    private JPanel registerPanel = new JPanel();
    private JPanel studentPanel = new JPanel();
    private JPanel teacherPanel = new JPanel();
    private JPanel adminPanel = new JPanel(); // 🌟 組員新增的管理員面板

    // --- 目前登入的使用者 ---
    private Student currentStudent;
    private Teacher currentTeacher;

    // --- 需要動態更新的介面元件 ---
    private JLabel lblStudentWelcome = new JLabel();
    private JComboBox<String> courseComboBox;
    private DefaultTableModel studentsTableModel;
    private JTable studentsTable;
    private DefaultTableModel allCoursesModel;
    private DefaultTableModel myCoursesModel;
    private SchedulePanel studentSchedulePanel = new SchedulePanel();
    private SchedulePanel teacherSchedulePanel = new SchedulePanel();
    private JLabel lblTeacherWelcome = new JLabel();
    private JLabel lblAdminStatus = new JLabel();

    public MainGUI() {

        // --- 2. 設定主視窗 ---
        setTitle("School Administration System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 視窗置中顯示

        // ==========================================
        // 🌟 測試專屬時光機 (如果要正常測試登入，這行可留可不留)
        // system.setCurrentPhase(SystemStateManager.SystemPhase.ADD_DROP);
        // ==========================================

        // 關閉視窗時斷開資料庫連線
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (db != null) {
                    db.closeConnection();
                }
            }
        });

        // --- 3. 建立並加入卡片 ---
        buildLoginPanel();
        buildStudentPanel();
        buildRegisterPanel();
        buildTeacherPanel();
        buildAdminPanel(); // 🌟 載入組員的管理員介面

        mainContainer.add(loginPanel, "LoginCard");
        mainContainer.add(studentPanel, "StudentCard");
        mainContainer.add(teacherPanel, "TeacherCard");
        mainContainer.add(adminPanel, "AdminCard");

        add(mainContainer);

        // 啟動時先顯示登入畫面
        cardLayout.show(mainContainer, "LoginCard");
    }

    // ==================== 登入畫面 ====================
    private void buildLoginPanel() {
        loginPanel.setLayout(new GridBagLayout());
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        JLabel lblId = new JLabel("ID (Student/Professor/Admin):");
        JTextField txtId = new JTextField(15);
        JLabel lblPwd = new JLabel("Password:");
        JPasswordField txtPwd = new JPasswordField(15);
        JButton btnLogin = new JButton("Login");

        // 🌟 採用組員的版本：登入畫面不再有註冊按鈕
        formPanel.add(lblId);
        formPanel.add(txtId);
        formPanel.add(lblPwd);
        formPanel.add(txtPwd);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(formPanel, gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(20, 0, 0, 0);
        loginPanel.add(btnLogin, gbc);

        this.getRootPane().setDefaultButton(btnLogin);

        btnLogin.addActionListener(e -> {
            String uid = txtId.getText().trim();
            String pwd = new String(txtPwd.getPassword()).trim();
            if (uid.isEmpty() || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both ID and password!", "Input Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            Admin a = db.findAdmin(uid); // 管理員
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
                currentTeacher = t;
                JOptionPane.showMessageDialog(this, "Professor login successful! Welcome " + t.getName());
                refreshTeacherView();
                cardLayout.show(mainContainer, "TeacherCard");
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
                currentStudent = s;
                JOptionPane.showMessageDialog(this, "Student login successful! Welcome " + s.getName());
                refreshStudentView();
                cardLayout.show(mainContainer, "StudentCard");
                txtId.setText("");
                txtPwd.setText("");

            } else if (a != null && a.verifyPassword(pwd)) {
                JOptionPane.showMessageDialog(this, "Admin login successful!");
                txtId.setText("");
                txtPwd.setText("");
                refreshAdminView();
                cardLayout.show(mainContainer, "AdminCard"); // 🌟 進入組員寫的管理員介面
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect ID or password!", "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // ==================== 註冊畫面 (內嵌於管理員介面) ====================
    private void buildRegisterPanel() {
        registerPanel.setLayout(new GridBagLayout());
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
        registerPanel.add(formPanel);

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

    // ==================== 學生畫面 ====================
    private void buildStudentPanel() {
        studentPanel.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        lblStudentWelcome.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblStudentWelcome.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(lblStudentWelcome, BorderLayout.WEST);

        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            currentStudent = null;
            cardLayout.show(mainContainer, "LoginCard");
        });
        topPanel.add(btnLogout, BorderLayout.EAST);
        studentPanel.add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        // 分頁 1：瀏覽與選課
        JPanel enrollPanel = new JPanel(new BorderLayout());
        String[] allCols = { "Course ID", "Course Name", "Credits", "Professor", "Time" };
        allCoursesModel = new DefaultTableModel(allCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable allCoursesTable = new JTable(allCoursesModel);
        allCoursesTable.setRowHeight(25);
        allCoursesTable.getTableHeader().setReorderingAllowed(false);
        allCoursesTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        allCoursesTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        allCoursesTable.getColumnModel().getColumn(2).setPreferredWidth(30);
        allCoursesTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        allCoursesTable.getColumnModel().getColumn(4).setPreferredWidth(80);

        enrollPanel.add(new JScrollPane(allCoursesTable), BorderLayout.CENTER);

        JPanel bottomActionPanel = new JPanel();
        JButton btnEnroll = new JButton("Register for Lottery / Normal Add");
        JButton btnCancelPending = new JButton("Cancel Registration");
        JButton btnForceEnroll = new JButton("Force Add with Auth Code");
        bottomActionPanel.add(btnEnroll);
        bottomActionPanel.add(btnCancelPending);
        bottomActionPanel.add(btnForceEnroll);
        enrollPanel.add(bottomActionPanel, BorderLayout.SOUTH);

        // 🌟 你的心血：結合時間軸的智慧型選課按鈕
        btnEnroll.addActionListener(e -> {
            int row = allCoursesTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a course from the table first!", "Hint",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            Course selectedCourse = db.getAllCourses().get(row);
            try {
                if (system.getCurrentPhase() == SystemStateManager.SystemPhase.PRE_ENROLL) {
                    system.registerForLottery(currentStudent, selectedCourse);
                    JOptionPane.showMessageDialog(this, "Registration successful! Please wait for lottery results.");
                } else if (system.getCurrentPhase() == SystemStateManager.SystemPhase.ADD_DROP) {
                    system.normalEnroll(currentStudent, selectedCourse);
                    JOptionPane.showMessageDialog(this, "Course added successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "System is closed or running lottery, cannot enroll now!",
                            "Phase Error", JOptionPane.WARNING_MESSAGE);
                }
                refreshStudentView();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Operation Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 取消登記
        btnCancelPending.addActionListener(e -> {
            int row = allCoursesTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a course to cancel from the table!", "Hint",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            Course selectedCourse = db.getAllCourses().get(row);
            try {
                system.cancelPendingCourse(currentStudent, selectedCourse);
                JOptionPane.showMessageDialog(this, "Registration cancelled successfully!");
                refreshStudentView();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Operation Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 密碼卡加簽
        btnForceEnroll.addActionListener(e -> {
            int row = allCoursesTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a course to force add from the table!", "Hint",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            Course selectedCourse = db.getAllCourses().get(row);
            String inputCode = JOptionPane.showInputDialog(this,
                    "Please enter the auth code for [" + selectedCourse.getCourseName() + "]:\n(Cancel if none)",
                    "Force Add with Auth Code", JOptionPane.QUESTION_MESSAGE);

            if (inputCode != null && !inputCode.trim().isEmpty()) {
                try {
                    system.forceEnrollWithPassword(currentStudent, selectedCourse, inputCode.trim());
                    JOptionPane.showMessageDialog(this, "Force add successful!");
                    refreshStudentView();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Force Add Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 分頁 2：我的成績與退選
        JPanel myGradesPanel = new JPanel(new BorderLayout());
        String[] myCols = { "Course ID", "Course Name", "Credits", "Professor", "Time", "Grade" };
        myCoursesModel = new DefaultTableModel(myCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable myCoursesTable = new JTable(myCoursesModel);
        myCoursesTable.setRowHeight(25);
        myCoursesTable.getTableHeader().setReorderingAllowed(false);
        myCoursesTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        myCoursesTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        myCoursesTable.getColumnModel().getColumn(2).setPreferredWidth(10);
        myCoursesTable.getColumnModel().getColumn(3).setPreferredWidth(40);
        myCoursesTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        myCoursesTable.getColumnModel().getColumn(5).setPreferredWidth(70);
        myGradesPanel.add(new JScrollPane(myCoursesTable), BorderLayout.CENTER);

        JButton btnDrop = new JButton("Drop Course");
        myGradesPanel.add(btnDrop, BorderLayout.SOUTH);

        btnDrop.addActionListener(e -> {
            int row = myCoursesTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a course to drop from the table!", "Hint",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            Course selectedCourse = currentStudent.getMyCourses().get(row);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to drop [" + selectedCourse.getCourseName() + "]?", "Confirm Drop",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    system.dropEnrolledCourse(currentStudent, selectedCourse);
                    JOptionPane.showMessageDialog(this, "Course dropped successfully!");
                    refreshStudentView();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Drop Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        tabbedPane.addTab("Browse All Courses", enrollPanel);
        tabbedPane.addTab("My Schedule", studentSchedulePanel);
        tabbedPane.addTab("My Grades", myGradesPanel);
        studentPanel.add(tabbedPane, BorderLayout.CENTER);
    }

    private void refreshStudentView() {
        int totalCredits = 0;
        for (Course c : currentStudent.getMyCourses()) {
            totalCredits += c.getCredits();
        }
        lblStudentWelcome.setText("Student: " + currentStudent.getName() + " | Total Credits: " + totalCredits
                + " | GPA: " + system.calculateGPA(currentStudent) + " | Phase: "
                + system.getPhaseName(system.getCurrentPhase()));

        allCoursesModel.setRowCount(0);
        for (Course c : db.getAllCourses()) {
            allCoursesModel.addRow(new Object[] { c.getCourseId(), c.getCourseName(), c.getCredits(),
                    c.getTeacher().getName(), c.getTimeSlot().toString() });
        }

        myCoursesModel.setRowCount(0);
        Map<Course, Double> grades = currentStudent.getCourseGrades();
        for (Course c : currentStudent.getMyCourses()) {
            Double score = grades.get(c);
            String scoreStr = (score == null) ? "Not Graded" : score.toString();
            myCoursesModel.addRow(new Object[] { c.getCourseId(), c.getCourseName(), c.getCredits(),
                    c.getTeacher().getName(), c.getTimeSlot().toString(), scoreStr });
        }

        studentSchedulePanel.updateCourses(currentStudent.getMyCourses(), true);
    }

    // ==================== 老師畫面 ====================
    private void buildTeacherPanel() {
        teacherPanel.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        lblTeacherWelcome.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTeacherWelcome.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(lblTeacherWelcome, BorderLayout.WEST);

        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            currentTeacher = null;
            cardLayout.show(mainContainer, "LoginCard");
        });
        topPanel.add(btnLogout, BorderLayout.EAST);
        teacherPanel.add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        // ====== 分頁 1：新增課程 ======
        JPanel formContainer = new JPanel(new GridBagLayout());
        JPanel formPanel = new JPanel(new BorderLayout(0, 15));
        formPanel.setBorder(BorderFactory.createTitledBorder("Create New Course"));

        JPanel fieldsPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        JTextField txtCode = new JTextField(12);
        JTextField txtName = new JTextField(12);
        JTextField txtCredits = new JTextField(12);
        JTextField txtDay = new JTextField(12);
        JTextField txtStart = new JTextField(12);
        JTextField txtEnd = new JTextField(12);
        JTextField txtCapacity = new JTextField("50", 12);
        JButton btnAddAuthCode = new JButton("Add Auth Code");
        fieldsPanel.add(new JLabel("Course ID (e.g., CS103):"));
        fieldsPanel.add(txtCode);
        fieldsPanel.add(new JLabel("Course Name:"));
        fieldsPanel.add(txtName);
        fieldsPanel.add(new JLabel("Credits:"));
        fieldsPanel.add(txtCredits);
        fieldsPanel.add(new JLabel("Day of Week (1~5):"));
        fieldsPanel.add(txtDay);
        fieldsPanel.add(new JLabel("Start Period (e.g., 2):"));
        fieldsPanel.add(txtStart);
        fieldsPanel.add(new JLabel("End Period (e.g., 4):"));
        fieldsPanel.add(txtEnd);
        fieldsPanel.add(new JLabel("Capacity (default 50):"));
        fieldsPanel.add(txtCapacity);
        fieldsPanel.add(new JLabel("Auth Code Setting:"));
        fieldsPanel.add(btnAddAuthCode);
        formPanel.add(fieldsPanel, BorderLayout.CENTER);

        // 密碼卡新增按鈕（目前僅為介面）
        btnAddAuthCode.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(teacherPanel, "Please enter new auth code:");
            if (input != null && !input.trim().isEmpty()) {
                JOptionPane.showMessageDialog(teacherPanel, "Auth code recorded: " + input);
            }
        });

        JPanel buttonPanel = new JPanel();
        JButton btnAddCourse = new JButton("Confirm Course Creation");
        buttonPanel.add(btnAddCourse);
        formPanel.add(buttonPanel, BorderLayout.SOUTH);

        formContainer.add(formPanel);
        JScrollPane addCoursePanel = new JScrollPane(formContainer);
        addCoursePanel.setBorder(BorderFactory.createEmptyBorder());

        btnAddCourse.addActionListener(e -> {
            String code = txtCode.getText().trim();
            String name = txtName.getText().trim();
            String authCode = ""; // txtAuthCode.getText().trim(); (改成密碼卡按鈕後，這裡先帶空字串)
            String creditsStr = txtCredits.getText().trim();
            String dayStr = txtDay.getText().trim();
            String startStr = txtStart.getText().trim();
            String endStr = txtEnd.getText().trim();

            if (code.isEmpty() || name.isEmpty() || creditsStr.isEmpty() || dayStr.isEmpty() || startStr.isEmpty()
                    || endStr.isEmpty()) {
                JOptionPane.showMessageDialog(teacherPanel, "Please fill in all course information!", "Incomplete Data",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                int credits = Integer.parseInt(creditsStr);
                int day = Integer.parseInt(dayStr);
                int start = Integer.parseInt(startStr);
                int end = Integer.parseInt(endStr);
                int maxCapacity = Integer.parseInt(txtCapacity.getText().trim());
                TimeSlot time = new TimeSlot(day, start, end);

                system.createCourse(currentTeacher, code, name, credits, maxCapacity, time, authCode);
                JOptionPane.showMessageDialog(teacherPanel, " Course [" + name + "] created successfully!");

                txtCode.setText("");
                txtName.setText("");
                txtCredits.setText("");
                txtDay.setText("");
                txtStart.setText("");
                txtEnd.setText("");
                txtCapacity.setText("50");
                refreshTeacherView();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(teacherPanel,
                        "Please check input format, credits and periods must be numbers!", "Format Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(teacherPanel, ex.getMessage(), "Course Creation Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // ====== 分頁 2：登記成績 ======
        JPanel gradePanel = new JPanel(new BorderLayout());
        JPanel selectCoursePanel = new JPanel();
        selectCoursePanel.add(new JLabel("Please select your course: "));
        courseComboBox = new JComboBox<>();
        selectCoursePanel.add(courseComboBox);
        gradePanel.add(selectCoursePanel, BorderLayout.NORTH);

        String[] cols = { "Student ID", "Student Name", "Current Grade" };
        studentsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        studentsTable = new JTable(studentsTableModel);
        studentsTable.setRowHeight(25);
        studentsTable.getTableHeader().setReorderingAllowed(false);
        gradePanel.add(new JScrollPane(studentsTable), BorderLayout.CENTER);

        JPanel inputGradePanel = new JPanel();
        inputGradePanel.add(new JLabel("Enter grade for selected student (0~100): "));
        JTextField txtScore = new JTextField(5);
        inputGradePanel.add(txtScore);
        JButton btnGrade = new JButton("Submit Grade");
        inputGradePanel.add(btnGrade);
        gradePanel.add(inputGradePanel, BorderLayout.SOUTH);

        courseComboBox.addActionListener(e -> updateStudentsTable());

        btnGrade.addActionListener(e -> {
            int selectedCourseIndex = courseComboBox.getSelectedIndex();
            int selectedStudentRow = studentsTable.getSelectedRow();

            if (selectedCourseIndex == -1 || selectedStudentRow == -1) {
                JOptionPane.showMessageDialog(teacherPanel, "Please select a course and a student from the table!");
                return;
            }

            String scoreStr = txtScore.getText().trim();
            if (scoreStr.isEmpty()) {
                JOptionPane.showMessageDialog(teacherPanel, "Please enter a grade!", "Input Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                double score = Double.parseDouble(scoreStr);
                Course selectedCourse = currentTeacher.getTeachingCourses().get(selectedCourseIndex);
                Student targetStudent = selectedCourse.getEnrolledStudents().get(selectedStudentRow);

                boolean success = system.gradeStudent(currentTeacher, targetStudent, selectedCourse, score);
                if (success) {
                    JOptionPane.showMessageDialog(teacherPanel, " Grade submitted successfully!");
                    txtScore.setText("");
                    updateStudentsTable();
                } else {
                    JOptionPane.showMessageDialog(teacherPanel, " System rejected submission.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(teacherPanel, "Grade must be a number!", "Format Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(teacherPanel, ex.getMessage(), "Grade Submission Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        tabbedPane.addTab("Create Course", addCoursePanel);
        tabbedPane.addTab("Student List & Grading", gradePanel);
        tabbedPane.addTab("My Schedule", teacherSchedulePanel);

        teacherPanel.add(tabbedPane, BorderLayout.CENTER);
    }

    private void refreshTeacherView() {
        if (currentTeacher == null)
            return;
        lblTeacherWelcome.setText("Professor: " + currentTeacher.getName() + " | Phase: "
                + system.getPhaseName(system.getCurrentPhase()));

        courseComboBox.removeAllItems();
        List<Course> myCourses = currentTeacher.getTeachingCourses();
        for (Course c : myCourses) {
            courseComboBox.addItem(c.getCourseName() + " (" + c.getCourseId() + ")");
        }
        updateStudentsTable();
        teacherSchedulePanel.updateCourses(myCourses, false);
    }

    private void updateStudentsTable() {
        studentsTableModel.setRowCount(0);
        int selectedIndex = courseComboBox.getSelectedIndex();
        if (selectedIndex >= 0 && currentTeacher != null) {
            Course selectedCourse = currentTeacher.getTeachingCourses().get(selectedIndex);
            List<Student> students = selectedCourse.getEnrolledStudents();

            for (Student s : students) {
                Double currentScore = s.getCourseGrades().get(selectedCourse);
                String scoreDisplay = (currentScore == null) ? "Not Graded" : String.valueOf(currentScore);
                studentsTableModel.addRow(new Object[] { s.getUid(), s.getName(), scoreDisplay });
            }
        }
    }

    // ==================== 管理員畫面 (組員開發) ====================
    private void buildAdminPanel() {
        adminPanel.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        lblAdminStatus.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblAdminStatus.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(lblAdminStatus, BorderLayout.WEST);

        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            cardLayout.show(mainContainer, "LoginCard");
        });
        topPanel.add(btnLogout, BorderLayout.EAST);
        adminPanel.add(topPanel, BorderLayout.NORTH);

        // --- 控制台按鈕 ---
        JPanel controlPanel = new JPanel(new GridLayout(4, 1, 20, 20));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(50, 150, 50, 150));
        JButton btnClosed = new JButton("Switch to: " + system.getPhaseName(SystemStateManager.SystemPhase.CLOSED));
        JButton btnPreEnroll = new JButton(
                "Switch to: " + system.getPhaseName(SystemStateManager.SystemPhase.PRE_ENROLL));
        JButton btnLottery = new JButton(
                "Switch to: " + system.getPhaseName(SystemStateManager.SystemPhase.LOTTERY_RUN));
        JButton btnAddDrop = new JButton("Switch to: " + system.getPhaseName(SystemStateManager.SystemPhase.ADD_DROP));

        // 🌟 你的心血：套用最新的 SystemStateManager
        btnClosed.addActionListener(e -> {
            system.setCurrentPhase(SystemStateManager.SystemPhase.CLOSED);
            refreshAdminView();
            JOptionPane.showMessageDialog(this, "Switched to " + system.getPhaseName(SystemStateManager.SystemPhase.CLOSED));
        });
        btnPreEnroll.addActionListener(e -> {
            system.setCurrentPhase(SystemStateManager.SystemPhase.PRE_ENROLL);
            refreshAdminView();
            JOptionPane.showMessageDialog(this, "Switched to " + system.getPhaseName(SystemStateManager.SystemPhase.PRE_ENROLL));
        });
        btnAddDrop.addActionListener(e -> {
            system.setCurrentPhase(SystemStateManager.SystemPhase.ADD_DROP);
            refreshAdminView();
            JOptionPane.showMessageDialog(this, "Switched to " + system.getPhaseName(SystemStateManager.SystemPhase.ADD_DROP));
        });
        btnLottery.addActionListener(e -> {
            system.setCurrentPhase(SystemStateManager.SystemPhase.LOTTERY_RUN);
            refreshAdminView();
            JOptionPane.showMessageDialog(this, "System switched to " + system.getPhaseName(SystemStateManager.SystemPhase.LOTTERY_RUN) + ", starting lottery...");
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

        // --- 名單查看分頁 ---
        JPanel userListPanel = new JPanel(new BorderLayout());
        String[] viewOptions = { "Student", "Professor" };
        JComboBox<String> comboViewRole = new JComboBox<>(viewOptions);
        JPanel topBoxPanel = new JPanel();
        topBoxPanel.add(new JLabel("Please select a list to view: "));
        topBoxPanel.add(comboViewRole);
        userListPanel.add(topBoxPanel, BorderLayout.NORTH);

        String[] userCols = { "ID", "Name", "Password" };
        DefaultTableModel userTableModel = new DefaultTableModel(userCols, 0) {
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

        // --- 課程查看分頁 ---
        JPanel courseListPanel = new JPanel(new BorderLayout());
        String[] courseCols = { "Course ID", "Course Name", "Credits", "Capacity", "Professor", "Time", "Auth Code" };
        DefaultTableModel courseTableModel = new DefaultTableModel(courseCols, 0) {
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
                if (row >= 0 && col == 6) { // 加簽密碼卡欄位
                    String courseName = (String) courseTableModel.getValueAt(row, 1);

                    // 顯示密碼卡資訊與使用狀況（目前僅為介面）
                    JOptionPane.showMessageDialog(courseListPanel,
                            "Auth code details and usage for [" + courseName + "]:\n\n",
                            "Auth Code Details", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        courseListPanel.add(new JScrollPane(courseTable), BorderLayout.CENTER);

        JTabbedPane adminTabbedPane = new JTabbedPane();
        adminTabbedPane.addTab("System Control Panel", controlPanel);
        adminTabbedPane.addTab("Register New Account", registerPanel); // 🌟 組員把註冊介面搬來這裡了
        adminTabbedPane.addTab("View Users", userListPanel);
        adminTabbedPane.addTab("View Courses", courseListPanel);

        adminTabbedPane.addChangeListener(e -> {
            if (adminTabbedPane.getSelectedComponent() == userListPanel) {
                comboViewRole.setSelectedIndex(comboViewRole.getSelectedIndex());
            } else if (adminTabbedPane.getSelectedComponent() == courseListPanel) {
                courseTableModel.setRowCount(0);
                List<Course> allCourses = db.getAllCourses();
                for (Course c : allCourses) {
                    String authCode = c.getAuthCode();

                    // 判斷「有或無」密碼卡
                    String hasAuthCode = (authCode != null && !authCode.trim().isEmpty()) ? "Yes" : "No";

                    courseTableModel.addRow(new Object[] {
                            c.getCourseId(), c.getCourseName(), c.getCredits(),
                            c.getMaxCapacity(), c.getTeacher().getName(),
                            c.getTimeSlot().toString(), hasAuthCode
                    });
                }
            }
        });
        adminPanel.add(adminTabbedPane, BorderLayout.CENTER);
    }

    private void refreshAdminView() {
        lblAdminStatus.setText("Admin Control Panel | Phase: " + system.getPhaseName(system.getCurrentPhase()));
    }

    // ==================== 程式執行起點 ====================
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
                defaults.put(key, uiFont);
            }
        }

        SwingUtilities.invokeLater(() -> {
            new MainGUI().setVisible(true);
        });
    }
}
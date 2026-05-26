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
        setTitle("學校行政管理系統");
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

        JLabel lblId = new JLabel("帳號 (學號/教師/管理員編號):");
        JTextField txtId = new JTextField(15);
        JLabel lblPwd = new JLabel("密碼:");
        JPasswordField txtPwd = new JPasswordField(15);
        JButton btnLogin = new JButton("登入系統");

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
                JOptionPane.showMessageDialog(this, "請輸入完整帳號與密碼！", "輸入錯誤", JOptionPane.WARNING_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "教授登入成功！歡迎 " + t.getName());
                refreshTeacherView();
                cardLayout.show(mainContainer, "TeacherCard");
                txtId.setText(""); txtPwd.setText(""); 

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
                JOptionPane.showMessageDialog(this, "學生登入成功！歡迎 " + s.getName());
                refreshStudentView();
                cardLayout.show(mainContainer, "StudentCard");
                txtId.setText(""); txtPwd.setText(""); 

            } else if (a != null && a.verifyPassword(pwd)) {
                JOptionPane.showMessageDialog(this, "管理員登入成功！");
                txtId.setText(""); txtPwd.setText(""); 
                refreshAdminView();
                cardLayout.show(mainContainer, "AdminCard"); // 🌟 進入組員寫的管理員介面
            } else {
                JOptionPane.showMessageDialog(this, "帳號或密碼錯誤！", "登入失敗", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // ==================== 註冊畫面 (內嵌於管理員介面) ====================
    private void buildRegisterPanel() {
        registerPanel.setLayout(new GridBagLayout());
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("註冊新帳號"));
        JLabel lblRole = new JLabel("身分:");
        JComboBox<String> comboRole = new JComboBox<>(new String[]{"學生", "教授"});
        JLabel lblId = new JLabel("帳號 (學號/教師編號):");
        JTextField txtId = new JTextField(15);
        JLabel lblName = new JLabel("姓名:");
        JTextField txtName = new JTextField(15);
        JLabel lblPwd = new JLabel("密碼:");
        JPasswordField txtPwd = new JPasswordField(15);
        JButton btnRegister = new JButton("確認註冊");
        
        formPanel.add(lblRole); formPanel.add(comboRole);
        formPanel.add(lblId);   formPanel.add(txtId);
        formPanel.add(lblName); formPanel.add(txtName);
        formPanel.add(lblPwd);  formPanel.add(txtPwd);
        formPanel.add(new JLabel("")); 
        formPanel.add(btnRegister);
        registerPanel.add(formPanel);

        btnRegister.addActionListener(e -> {
            String role = (String) comboRole.getSelectedItem();
            String uid = txtId.getText().trim();
            String name = txtName.getText().trim();
            String pwd = new String(txtPwd.getPassword()).trim();
            if (uid.isEmpty() || name.isEmpty() || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(this, "請填寫完整資訊！", "註冊失敗", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if ("學生".equals(role)) {
                if (db.findStudent(uid) != null) {
                    JOptionPane.showMessageDialog(this, "該學號已存在！", "註冊失敗", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                db.registerStudent(new Student(uid, name, pwd));
            } else {
                if (db.findTeacher(uid) != null) {
                    JOptionPane.showMessageDialog(this, "該教師編號已存在！", "註冊失敗", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                db.registerTeacher(new Teacher(uid, name, pwd));
            }
            JOptionPane.showMessageDialog(this, "註冊成功！");
            txtId.setText(""); txtName.setText(""); txtPwd.setText("");
        });
    }

    // ==================== 學生畫面 ====================
    private void buildStudentPanel() {
        studentPanel.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        lblStudentWelcome.setFont(new Font("微軟正黑體", Font.BOLD, 16));
        lblStudentWelcome.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(lblStudentWelcome, BorderLayout.WEST);
        
        JButton btnLogout = new JButton("登出");
        btnLogout.addActionListener(e -> {
            currentStudent = null;
            cardLayout.show(mainContainer, "LoginCard");
        });
        topPanel.add(btnLogout, BorderLayout.EAST);
        studentPanel.add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        // 分頁 1：瀏覽與選課
        JPanel enrollPanel = new JPanel(new BorderLayout());
        String[] allCols = {"課程代碼", "課程名稱", "學分", "授課教師", "星期（節次）"};
        allCoursesModel = new DefaultTableModel(allCols, 0){
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
        JButton btnEnroll = new JButton("登記抽籤 / 一般加選");
        JButton btnCancelPending = new JButton("取消登記");
        JButton btnForceEnroll = new JButton("密碼卡加簽");
        bottomActionPanel.add(btnEnroll);
        bottomActionPanel.add(btnCancelPending);
        bottomActionPanel.add(btnForceEnroll);
        enrollPanel.add(bottomActionPanel, BorderLayout.SOUTH);

        // 🌟 你的心血：結合時間軸的智慧型選課按鈕
        btnEnroll.addActionListener(e -> {
            int row = allCoursesTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "請先在表格中點選一門課程！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Course selectedCourse = db.getAllCourses().get(row);
            try {
                if (system.getCurrentPhase() == SystemStateManager.SystemPhase.PRE_ENROLL) {
                    system.registerForLottery(currentStudent, selectedCourse);
                    JOptionPane.showMessageDialog(this, "✅ 登記成功！請等待抽籤結果。");
                } else if (system.getCurrentPhase() == SystemStateManager.SystemPhase.ADD_DROP) {
                    system.normalEnroll(currentStudent, selectedCourse);
                    JOptionPane.showMessageDialog(this, "✅ 加選成功！您已成功搶到名額！");
                } else {
                    JOptionPane.showMessageDialog(this, "⛔ 現在系統關閉或正在抽籤，無法選課！", "時段錯誤", JOptionPane.WARNING_MESSAGE);
                }
                refreshStudentView(); 
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "操作失敗", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 取消登記
        btnCancelPending.addActionListener(e -> {
            int row = allCoursesTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "請先在表格中點選一門想取消的課程！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Course selectedCourse = db.getAllCourses().get(row);
            try {
                system.cancelPendingCourse(currentStudent, selectedCourse);
                JOptionPane.showMessageDialog(this, "✅ 取消登記成功！");
                refreshStudentView(); 
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "操作失敗", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 密碼卡加簽
        btnForceEnroll.addActionListener(e -> {
            int row = allCoursesTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "請先在表格中點選一門要加簽的課程！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Course selectedCourse = db.getAllCourses().get(row);
            String inputCode = JOptionPane.showInputDialog(this, 
                "請輸入 [" + selectedCourse.getCourseName() + "] 的加簽密碼：\n(若無密碼請按取消)", 
                "密碼卡強制加簽", JOptionPane.QUESTION_MESSAGE);
            
            if (inputCode != null && !inputCode.trim().isEmpty()) {
                try {
                    system.forceEnrollWithPassword(currentStudent, selectedCourse, inputCode.trim());
                    JOptionPane.showMessageDialog(this, "🎉 加簽成功！您已使用密碼卡強制加入該課程！");
                    refreshStudentView(); 
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "加簽失敗", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 分頁 2：我的成績與退選
        JPanel myGradesPanel = new JPanel(new BorderLayout());
        String[] myCols = {"課程代碼", "課程名稱", "學分", "授課教師", "星期（節次）", "成績"};
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

        JButton btnDrop = new JButton("正式退選該課程");
        myGradesPanel.add(btnDrop, BorderLayout.SOUTH);

        btnDrop.addActionListener(e -> {
            int row = myCoursesTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "請先在表格中點選一門要退選的課程！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Course selectedCourse = currentStudent.getMyCourses().get(row);
            int confirm = JOptionPane.showConfirmDialog(this, "確定要退選 [" + selectedCourse.getCourseName() + "] 嗎？", "退選確認", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    system.dropEnrolledCourse(currentStudent, selectedCourse);
                    JOptionPane.showMessageDialog(this, "退選成功！");
                    refreshStudentView(); 
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "退選失敗", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        tabbedPane.addTab("瀏覽全校課程", enrollPanel);
        tabbedPane.addTab("我的課表", studentSchedulePanel);
        tabbedPane.addTab("我的成績", myGradesPanel);
        studentPanel.add(tabbedPane, BorderLayout.CENTER);
    }

    private void refreshStudentView() {
        int totalCredits = 0;
        for (Course c : currentStudent.getMyCourses()) {
            totalCredits += c.getCredits();
        }
        lblStudentWelcome.setText("學生：" + currentStudent.getName() + " | 總學分: " + totalCredits + " | GPA: " + system.calculateGPA(currentStudent) + " | 階段: " + system.getCurrentPhase());
        
        allCoursesModel.setRowCount(0);
        for (Course c : db.getAllCourses()) {
            allCoursesModel.addRow(new Object[]{c.getCourseId(), c.getCourseName(), c.getCredits(), c.getTeacher().getName(), c.getTimeSlot().toString()});
        }

        myCoursesModel.setRowCount(0);
        Map<Course, Double> grades = currentStudent.getCourseGrades();
        for (Course c : currentStudent.getMyCourses()) {
            Double score = grades.get(c);
            String scoreStr = (score == null) ? "尚未評分" : score.toString();
            myCoursesModel.addRow(new Object[]{c.getCourseId(), c.getCourseName(), c.getCredits(), c.getTeacher().getName(), c.getTimeSlot().toString(), scoreStr});
        }

        studentSchedulePanel.updateCourses(currentStudent.getMyCourses(), true);
    }

    // ==================== 老師畫面 ====================
    private void buildTeacherPanel() {
        teacherPanel.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        lblTeacherWelcome.setFont(new Font("微軟正黑體", Font.BOLD, 14));
        lblTeacherWelcome.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(lblTeacherWelcome, BorderLayout.WEST);

        JButton btnLogout = new JButton("登出");
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
        formPanel.setBorder(BorderFactory.createTitledBorder("開設新課程"));

        JPanel fieldsPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        JTextField txtCode = new JTextField(12);
        JTextField txtName = new JTextField(12);
        JTextField txtCredits = new JTextField(12);
        JTextField txtDay = new JTextField(12);
        JTextField txtStart = new JTextField(12);
        JTextField txtEnd = new JTextField(12);
        JTextField txtCapacity = new JTextField("50", 12);
        JTextField txtAuthCode = new JTextField(12);
        fieldsPanel.add(new JLabel("課程代碼 (例 CS103):")); fieldsPanel.add(txtCode);
        fieldsPanel.add(new JLabel("課程名稱:")); fieldsPanel.add(txtName);
        fieldsPanel.add(new JLabel("學分數:")); fieldsPanel.add(txtCredits);
        fieldsPanel.add(new JLabel("上課星期 (1~5):")); fieldsPanel.add(txtDay);
        fieldsPanel.add(new JLabel("開始節次 (例如 2):")); fieldsPanel.add(txtStart);
        fieldsPanel.add(new JLabel("結束節次 (例如 4):")); fieldsPanel.add(txtEnd);
        fieldsPanel.add(new JLabel("人數上限 (預設50):")); fieldsPanel.add(txtCapacity);
        fieldsPanel.add(new JLabel("加簽密碼(不開放請留白):")); fieldsPanel.add(txtAuthCode);
        formPanel.add(fieldsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(); 
        JButton btnAddCourse = new JButton("確認開課");
        buttonPanel.add(btnAddCourse);
        formPanel.add(buttonPanel, BorderLayout.SOUTH);

        formContainer.add(formPanel);
        JScrollPane addCoursePanel = new JScrollPane(formContainer);
        addCoursePanel.setBorder(BorderFactory.createEmptyBorder());

        btnAddCourse.addActionListener(e -> {
            String code = txtCode.getText().trim();
            String name = txtName.getText().trim();
            String authCode = txtAuthCode.getText().trim();
            String creditsStr = txtCredits.getText().trim();
            String dayStr = txtDay.getText().trim();
            String startStr = txtStart.getText().trim();
            String endStr = txtEnd.getText().trim();
            
            if (code.isEmpty() || name.isEmpty() || creditsStr.isEmpty() || dayStr.isEmpty() || startStr.isEmpty() || endStr.isEmpty()) {
                JOptionPane.showMessageDialog(teacherPanel, "請填寫完整的所有課程資訊！", "資料不完整", JOptionPane.WARNING_MESSAGE);
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
                JOptionPane.showMessageDialog(teacherPanel, " 課程 [" + name + "] 新增成功！");
                
                txtCode.setText(""); txtName.setText(""); txtCredits.setText("");
                txtDay.setText(""); txtStart.setText(""); txtEnd.setText("");
                txtCapacity.setText("50"); txtAuthCode.setText("");
                refreshTeacherView();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(teacherPanel, "請檢查輸入格式，學分與時間必須為數字！", "格式錯誤", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(teacherPanel, ex.getMessage(), "開課失敗", JOptionPane.ERROR_MESSAGE);
            }
        });

        // ====== 分頁 2：登記成績 ======
        JPanel gradePanel = new JPanel(new BorderLayout());
        JPanel selectCoursePanel = new JPanel();
        selectCoursePanel.add(new JLabel("請選擇您的課程: "));
        courseComboBox = new JComboBox<>();
        selectCoursePanel.add(courseComboBox);
        gradePanel.add(selectCoursePanel, BorderLayout.NORTH);

        String[] cols = {"學生學號", "學生姓名", "目前成績"};
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
        inputGradePanel.add(new JLabel("為表格中選取的學生輸入分數 (0~100): "));
        JTextField txtScore = new JTextField(5);
        inputGradePanel.add(txtScore);
        JButton btnGrade = new JButton("登記成績");
        inputGradePanel.add(btnGrade);
        gradePanel.add(inputGradePanel, BorderLayout.SOUTH);

        courseComboBox.addActionListener(e -> updateStudentsTable());

        btnGrade.addActionListener(e -> {
            int selectedCourseIndex = courseComboBox.getSelectedIndex();
            int selectedStudentRow = studentsTable.getSelectedRow();

            if (selectedCourseIndex == -1 || selectedStudentRow == -1) {
                JOptionPane.showMessageDialog(teacherPanel, "請選擇課程並在表格中點選一位學生！");
                return;
            }

            String scoreStr = txtScore.getText().trim();
            if (scoreStr.isEmpty()) {
                JOptionPane.showMessageDialog(teacherPanel, "請輸入分數！", "輸入錯誤", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                double score = Double.parseDouble(scoreStr);
                Course selectedCourse = currentTeacher.getTeachingCourses().get(selectedCourseIndex);
                Student targetStudent = selectedCourse.getEnrolledStudents().get(selectedStudentRow);

                boolean success = system.gradeStudent(currentTeacher, targetStudent, selectedCourse, score);
                if (success) {
                    JOptionPane.showMessageDialog(teacherPanel, " 成績登記成功！");
                    txtScore.setText(""); 
                    updateStudentsTable();
                } else {
                    JOptionPane.showMessageDialog(teacherPanel, " 系統拒絕登記。", "錯誤", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(teacherPanel, "分數必須為數字！", "格式錯誤", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(teacherPanel, ex.getMessage(), "成績登記失敗", JOptionPane.ERROR_MESSAGE);
            }
        });

        tabbedPane.addTab("新增課程", addCoursePanel);
        tabbedPane.addTab("學生名單與登記成績", gradePanel);
        tabbedPane.addTab("我的課表", teacherSchedulePanel);

        teacherPanel.add(tabbedPane, BorderLayout.CENTER);
    }

    private void refreshTeacherView() {
        if (currentTeacher == null) return;
        lblTeacherWelcome.setText("教授：" + currentTeacher.getName() + " | 目前階段: " + system.getCurrentPhase());

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
                String scoreDisplay = (currentScore == null) ? "尚未評分" : String.valueOf(currentScore);
                studentsTableModel.addRow(new Object[]{s.getUid(), s.getName(), scoreDisplay});
            }
        }
    }

    // ==================== 管理員畫面 (組員開發) ====================
    private void buildAdminPanel() {
        adminPanel.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        lblAdminStatus.setFont(new Font("微軟正黑體", Font.BOLD, 18));
        lblAdminStatus.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(lblAdminStatus, BorderLayout.WEST);
        
        JButton btnLogout = new JButton("登出");
        btnLogout.addActionListener(e -> {
            cardLayout.show(mainContainer, "LoginCard");
        });
        topPanel.add(btnLogout, BorderLayout.EAST);
        adminPanel.add(topPanel, BorderLayout.NORTH);
        
        // --- 控制台按鈕 ---
        JPanel controlPanel = new JPanel(new GridLayout(4, 1, 20, 20));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(50, 150, 50, 150));
        JButton btnClosed = new JButton("切換為：系統關閉 (CLOSED)");
        JButton btnPreEnroll = new JButton("切換為：初選期 (PRE_ENROLL)");
        JButton btnLottery = new JButton("切換為：抽籤階段 (LOTTERY_RUN)");
        JButton btnAddDrop = new JButton("切換為：加退選期 (ADD_DROP)");

        // 🌟 你的心血：套用最新的 SystemStateManager
        btnClosed.addActionListener(e -> {
            system.setCurrentPhase(SystemStateManager.SystemPhase.CLOSED);
            refreshAdminView();
            JOptionPane.showMessageDialog(this, "已切換為系統關閉狀態！");
        });
        btnPreEnroll.addActionListener(e -> {
            system.setCurrentPhase(SystemStateManager.SystemPhase.PRE_ENROLL);
            refreshAdminView();
            JOptionPane.showMessageDialog(this, "已切換為初選期狀態！");
        });
        btnAddDrop.addActionListener(e -> {
            system.setCurrentPhase(SystemStateManager.SystemPhase.ADD_DROP);
            refreshAdminView();
            JOptionPane.showMessageDialog(this, "已切換為加退選期狀態！");
        });
        btnLottery.addActionListener(e -> {
            system.setCurrentPhase(SystemStateManager.SystemPhase.LOTTERY_RUN);
            refreshAdminView();
            JOptionPane.showMessageDialog(this, "系統已切換至抽籤階段，即將開始全校抽籤！");
            try {
                system.runLotterySystem();
                JOptionPane.showMessageDialog(this, "✅ 全校抽籤分發完成！");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "抽籤過程發生錯誤: " + ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });

        controlPanel.add(btnClosed);
        controlPanel.add(btnPreEnroll);
        controlPanel.add(btnLottery);
        controlPanel.add(btnAddDrop);

        // --- 名單查看分頁 ---
        JPanel userListPanel = new JPanel(new BorderLayout());
        String[] viewOptions = {"學生", "老師"};
        JComboBox<String> comboViewRole = new JComboBox<>(viewOptions);
        JPanel topBoxPanel = new JPanel();
        topBoxPanel.add(new JLabel("請選擇要查看的名單："));
        topBoxPanel.add(comboViewRole);
        userListPanel.add(topBoxPanel, BorderLayout.NORTH);

        String[] userCols = {"帳號/編號", "姓名", "密碼"};
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
            if ("學生".equals(comboViewRole.getSelectedItem())) {
                List<Student> students = db.getAllStudents();
                for (Student s : students) {
                    userTableModel.addRow(new Object[]{s.getUid(), s.getName(), s.getPassword()});
                }
            } else {
                List<Teacher> teachers = db.getAllTeachers();
                for (Teacher t : teachers) {
                    userTableModel.addRow(new Object[]{t.getUid(), t.getName(), t.getPassword()});
                }
            }
        });
        comboViewRole.setSelectedIndex(0); 

        // --- 課程查看分頁 ---
        JPanel courseListPanel = new JPanel(new BorderLayout());
        String[] courseCols = {"課程代碼", "課程名稱", "學分", "人數上限", "授課教師", "星期（節次）", "加簽密碼"};
        DefaultTableModel courseTableModel = new DefaultTableModel(courseCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable courseTable = new JTable(courseTableModel);
        courseTable.setRowHeight(25);
        courseTable.getTableHeader().setReorderingAllowed(false);
        courseListPanel.add(new JScrollPane(courseTable), BorderLayout.CENTER);

        JTabbedPane adminTabbedPane = new JTabbedPane();
        adminTabbedPane.addTab("系統狀態控制台", controlPanel);
        adminTabbedPane.addTab("註冊新帳號", registerPanel); // 🌟 組員把註冊介面搬來這裡了
        adminTabbedPane.addTab("查看全校師生名單", userListPanel);
        adminTabbedPane.addTab("查看全校課程", courseListPanel);

        adminTabbedPane.addChangeListener(e -> {
            if (adminTabbedPane.getSelectedComponent() == userListPanel) {
                comboViewRole.setSelectedIndex(comboViewRole.getSelectedIndex());
            } else if (adminTabbedPane.getSelectedComponent() == courseListPanel) {
                courseTableModel.setRowCount(0);
                List<Course> allCourses = db.getAllCourses();
                for (Course c : allCourses) {
                    String authCode = c.getAuthCode();
                    if (authCode == null || authCode.trim().isEmpty()) authCode = "無";
                    courseTableModel.addRow(new Object[]{
                        c.getCourseId(), c.getCourseName(), c.getCredits(), 
                        c.getMaxCapacity(), c.getTeacher().getName(), 
                        c.getTimeSlot().toString(), authCode
                    });
                }
            }
        });
        adminPanel.add(adminTabbedPane, BorderLayout.CENTER);
    }
    
    private void refreshAdminView() {
        lblAdminStatus.setText("系統管理員控制台 | 目前狀態: " + system.getCurrentPhase());
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
            System.out.println("無法載入 Nimbus 主題，將使用系統預設外觀。");
        }

        Font uiFont = new Font("微軟正黑體", Font.PLAIN, 20);
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
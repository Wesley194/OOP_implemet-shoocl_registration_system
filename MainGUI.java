import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
    private DefaultTableModel scheduleModel;
    private JTable scheduleTable;
    private JLabel lblTeacherWelcome = new JLabel();

    public MainGUI() {

        // --- 2. 設定主視窗 ---
        setTitle("學校行政管理系統");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 視窗置中顯示

        // --- 3. 建立並加入卡片 ---
        buildLoginPanel();
        buildStudentPanel();
        buildRegisterPanel();
        buildTeacherPanel();

        mainContainer.add(loginPanel, "LoginCard");
        mainContainer.add(registerPanel, "RegisterCard");
        mainContainer.add(studentPanel, "StudentCard");
        mainContainer.add(teacherPanel, "TeacherCard");

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
        //JButton btnGoRegister = new JButton("註冊帳號");

        formPanel.add(lblId);
        formPanel.add(txtId);
        formPanel.add(lblPwd);
        formPanel.add(txtPwd);
        //formPanel.add(btnGoRegister);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(formPanel, gbc);
        gbc.gridy = 1; // 把按鈕放在 formPanel 的正下方
        gbc.insets = new Insets(20, 0, 0, 0); // 加上方距 20px
        loginPanel.add(btnLogin, gbc);

        // 綁定鍵盤的 Enter/Return 鍵到登入按鈕上
        this.getRootPane().setDefaultButton(btnLogin);

        //註冊按鈕（已移除）
        /*btnGoRegister.addActionListener(e -> {
            cardLayout.show(mainContainer, "RegisterCard");
        });*/

        // 登入按鈕邏輯
        btnLogin.addActionListener(e -> {
            String uid = txtId.getText();
            String pwd = new String(txtPwd.getPassword());

            //Admin a = db.findAdmin(uid); // 管理員
            Teacher t = db.findTeacher(uid);
            Student s = db.findStudent(uid);


            if (t != null && t.verifyPassword(pwd)) {
                // 把資料庫裡屬於這位老師的課都拿出來
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
                txtId.setText(""); txtPwd.setText(""); // 清空輸入框

            } else if (s != null && s.verifyPassword(pwd)) {
                Map<Course, Double> gradesMap = db.getStudentGradesMap(s.getUid());
                for (Map.Entry<Course, Double> entry : gradesMap.entrySet()) {
                    Course c = entry.getKey();
                    Double score = entry.getValue();
                    
                    s.enrollInCourse(c); // 恢復選課紀錄
                    if (score != null) {
                        s.setGrade(c, score); // 恢復成績紀錄
                    }
                }
                currentStudent = s;
                JOptionPane.showMessageDialog(this, "學生登入成功！歡迎 " + s.getName());
                refreshStudentView();
                cardLayout.show(mainContainer, "StudentCard");
                txtId.setText(""); txtPwd.setText(""); // 清空輸入框

            } /*else if (a != null && a.verifyPassword(pwd)) {
                JOptionPane.showMessageDialog(this, "管理員登入成功！");
                txtId.setText(""); txtPwd.setText(""); // 清空輸入框
                cardLayout.show(mainContainer, "RegisterCard");
            }*/ else {
                JOptionPane.showMessageDialog(this, "帳號或密碼錯誤！", "登入失敗", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // ==================== 註冊畫面 ====================
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
        JButton btnBack = new JButton("返回登入");
        formPanel.add(lblRole); formPanel.add(comboRole);
        formPanel.add(lblId);   formPanel.add(txtId);
        formPanel.add(lblName); formPanel.add(txtName);
        formPanel.add(lblPwd);  formPanel.add(txtPwd);
        formPanel.add(btnBack); formPanel.add(btnRegister);
        registerPanel.add(formPanel);
        btnBack.addActionListener(e -> {
            cardLayout.show(mainContainer, "LoginCard");
        });
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
            cardLayout.show(mainContainer, "LoginCard");
        });
    }


    // ==================== 學生畫面 ====================
    private void buildStudentPanel() {
        studentPanel.setLayout(new BorderLayout());

        // 上方：歡迎與登出
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

        // 中間：分頁標籤
        JTabbedPane tabbedPane = new JTabbedPane();

        // 分頁 1：瀏覽與選課
        JPanel enrollPanel = new JPanel(new BorderLayout());
        String[] allCols = {"課程代碼", "課程名稱", "學分", "授課教師", "星期（節次）"};
        allCoursesModel = new DefaultTableModel(allCols, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 讓所有儲存格都變成唯讀，無法雙擊修改
            }
        };
        JTable allCoursesTable = new JTable(allCoursesModel);
        allCoursesTable.setRowHeight(25);
        allCoursesTable.getTableHeader().setReorderingAllowed(false);
        // 設定各欄位的預設寬度
        allCoursesTable.getColumnModel().getColumn(0).setPreferredWidth(70);  // 課程代碼
        allCoursesTable.getColumnModel().getColumn(1).setPreferredWidth(130); // 課程名稱
        allCoursesTable.getColumnModel().getColumn(2).setPreferredWidth(30);  // 學分
        allCoursesTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // 授課教師
        allCoursesTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // 星期（節次）
        
        enrollPanel.add(new JScrollPane(allCoursesTable), BorderLayout.CENTER);
        
        JButton btnEnroll = new JButton("確認選取該課程");
        enrollPanel.add(btnEnroll, BorderLayout.SOUTH);

        btnEnroll.addActionListener(e -> {
            int row = allCoursesTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "請先在表格中點選一門課程！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Course selectedCourse = db.getAllCourses().get(row);
            try {
                system.enroll(currentStudent, selectedCourse);
                JOptionPane.showMessageDialog(this, "選課成功！");
                refreshStudentView(); // 選課成功後立即重整畫面
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "選課失敗", JOptionPane.ERROR_MESSAGE);
            }
        });


        // 分頁 2：我的成績
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
        // 設定各欄位的預設寬度
        myCoursesTable.getColumnModel().getColumn(0).setPreferredWidth(60);  // 課程代碼
        myCoursesTable.getColumnModel().getColumn(1).setPreferredWidth(130); // 課程名稱
        myCoursesTable.getColumnModel().getColumn(2).setPreferredWidth(10);  // 學分
        myCoursesTable.getColumnModel().getColumn(3).setPreferredWidth(40);  // 授課教師
        myCoursesTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // 星期（節次）
        myCoursesTable.getColumnModel().getColumn(5).setPreferredWidth(70);  // 成績
        myGradesPanel.add(new JScrollPane(myCoursesTable), BorderLayout.CENTER);


        // 分頁 3：我的課表
        JPanel myScheduleGridPanel = new JPanel(new BorderLayout());
        String[] scheduleCols = {"節次", "星期一", "星期二", "星期三", "星期四", "星期五"};
        scheduleModel = new DefaultTableModel(scheduleCols, 8) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        scheduleTable = new JTable(scheduleModel);
        scheduleTable.setRowHeight(55);
        scheduleTable.getTableHeader().setReorderingAllowed(false);
        scheduleTable.getColumnModel().getColumn(0).setPreferredWidth(20);
        
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setVerticalAlignment(SwingConstants.TOP);
        for (int i = 0; i < scheduleTable.getColumnCount(); i++) {
            scheduleTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // 點擊儲存格顯示完整資訊
        scheduleTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = scheduleTable.rowAtPoint(e.getPoint());
                int col = scheduleTable.columnAtPoint(e.getPoint());
                if (row >= 0 && col > 0) { // 第0欄是節次，不處理
                    Object value = scheduleTable.getValueAt(row, col);
                    if (value != null && !value.toString().trim().isEmpty()) {
                        String cleanText = value.toString().replace("<html><center>", "").replace("</center></html>", "").replace("<br>", "\n").replace("<html>", "").replace("</html>", "");
                        JOptionPane.showMessageDialog(studentPanel, cleanText, "課程資訊", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });
        myScheduleGridPanel.add(new JScrollPane(scheduleTable), BorderLayout.CENTER);

        tabbedPane.addTab("瀏覽全校課程", enrollPanel);
        tabbedPane.addTab("我的課表", myScheduleGridPanel);
        tabbedPane.addTab("我的成績", myGradesPanel);
        studentPanel.add(tabbedPane, BorderLayout.CENTER);
    }

    private void refreshStudentView() {
        int totalCredits = 0;
        for (Course c : currentStudent.getMyCourses()) {
            totalCredits += c.getCredits();
        }
        lblStudentWelcome.setText("學生：" + currentStudent.getName() + " (" + currentStudent.getUid() + ") | 總學分數: " + totalCredits + " | 目前 GPA: " + system.calculateGPA(currentStudent));
        
        // 更新全校課程
        allCoursesModel.setRowCount(0);
        for (Course c : db.getAllCourses()) {
            allCoursesModel.addRow(new Object[]{c.getCourseId(), c.getCourseName(), c.getCredits(), c.getTeacher().getName(), c.getTimeSlot().toString()});
        }

        // 更新我的成績與課表
        myCoursesModel.setRowCount(0);

        // 初始化課表網格
        for (int r = 0; r < 8; r++) {
            scheduleModel.setValueAt((r + 1), r, 0);
            for (int c = 1; c <= 5; c++) {
                scheduleModel.setValueAt("", r, c);
            }
        }

        Map<Course, Double> grades = currentStudent.getCourseGrades();
        for (Course c : currentStudent.getMyCourses()) {
            // 填入成績表
            Double score = grades.get(c);
            String scoreStr = (score == null) ? "尚未評分" : score.toString();
            myCoursesModel.addRow(new Object[]{c.getCourseId(), c.getCourseName(), c.getCredits(), c.getTeacher().getName(), c.getTimeSlot().toString(), scoreStr});

            // 填入視覺化課表
            TimeSlot ts = c.getTimeSlot();
            int day = ts.getDayOfWeek();
            if (day >= 1 && day <= 5) {
                int start = ts.getStartPeriod();
                int end = ts.getEndPeriod();
                String cellText = "<html><center>" + c.getCourseName() + "<br>" + c.getCourseId() + "<br>" + c.getTeacher().getName() + "</center></html>";
                for (int period = start; period <= end; period++) {
                    if (period >= 1 && period <= 8) {
                        scheduleModel.setValueAt(cellText, period - 1, day);
                    }
                }
            }
        }
    }

    // ==================== 老師畫面 ====================
    private void buildTeacherPanel() {
        teacherPanel.setLayout(new BorderLayout());

        // --- 上方：歡迎標語與登出按鈕 ---
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

        // --- 中間：分頁標籤 ---
        JTabbedPane tabbedPane = new JTabbedPane();

        // ====== 分頁 1：新增課程 ======
        JPanel addCoursePanel = new JPanel(new GridBagLayout()); 
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("開設新課程"));

        JTextField txtCode = new JTextField(10);
        JTextField txtName = new JTextField(10);
        JTextField txtCredits = new JTextField(10);
        JTextField txtDay = new JTextField(10);
        JTextField txtStart = new JTextField(10);
        JTextField txtEnd = new JTextField(10);

        formPanel.add(new JLabel("課程代碼 (例 CS103):")); formPanel.add(txtCode);
        formPanel.add(new JLabel("課程名稱:")); formPanel.add(txtName);
        formPanel.add(new JLabel("學分數:")); formPanel.add(txtCredits);
        formPanel.add(new JLabel("上課星期 (1~5):")); formPanel.add(txtDay);
        formPanel.add(new JLabel("開始節次 (例如 2):")); formPanel.add(txtStart);
        formPanel.add(new JLabel("結束節次 (例如 4):")); formPanel.add(txtEnd);

        JButton btnAddCourse = new JButton("確認開課");
        formPanel.add(new JLabel("")); formPanel.add(btnAddCourse);

        btnAddCourse.addActionListener(e -> {
            try {
                String code = txtCode.getText().trim();
                String name = txtName.getText().trim();
                int credits = Integer.parseInt(txtCredits.getText().trim());
                int day = Integer.parseInt(txtDay.getText().trim());
                int start = Integer.parseInt(txtStart.getText().trim());
                int end = Integer.parseInt(txtEnd.getText().trim());

                TimeSlot time = new TimeSlot(day, start, end);
                system.createCourse(currentTeacher, code, name, credits, 50, time);

                JOptionPane.showMessageDialog(teacherPanel, " 課程 [" + name + "] 新增成功！");
                
                // 清空輸入框
                txtCode.setText(""); txtName.setText(""); txtCredits.setText("");
                txtDay.setText(""); txtStart.setText(""); txtEnd.setText("");
                
                // 開課成功後，刷新下拉選單，讓成績分頁馬上能看到新課！
                refreshTeacherView(); 
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(teacherPanel, "請檢查輸入格式，學分與時間必須為數字！", "格式錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });
        addCoursePanel.add(formPanel); // 將表單加入分頁

        // ====== 分頁 2：登記成績 ======
        JPanel gradePanel = new JPanel(new BorderLayout());

        // 頂部：選擇課程的下拉選單
        JPanel selectCoursePanel = new JPanel();
        selectCoursePanel.add(new JLabel("請選擇您的課程: "));
        courseComboBox = new JComboBox<>();
        selectCoursePanel.add(courseComboBox);
        gradePanel.add(selectCoursePanel, BorderLayout.NORTH);

        // 中部：學生名單與成績表格
        String[] cols = {"學生學號", "學生姓名", "目前成績"};
        studentsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 讓學生名單表格唯讀
            }
        };
        studentsTable = new JTable(studentsTableModel);
        studentsTable.setRowHeight(25);
        studentsTable.getTableHeader().setReorderingAllowed(false);
        gradePanel.add(new JScrollPane(studentsTable), BorderLayout.CENTER);

        // 底部：輸入分數區塊
        JPanel inputGradePanel = new JPanel();
        inputGradePanel.add(new JLabel("為表格中選取的學生輸入分數 (0~100): "));
        JTextField txtScore = new JTextField(5);
        inputGradePanel.add(txtScore);
        JButton btnGrade = new JButton("登記成績");
        inputGradePanel.add(btnGrade);
        gradePanel.add(inputGradePanel, BorderLayout.SOUTH);

        // --- 成績分頁的互動事件 ---
        
        // 1. 當下拉選單切換課程時，自動更新下方的學生表格
        courseComboBox.addActionListener(e -> {
            updateStudentsTable();
        });

        // 2. 按下「登記成績」按鈕時的邏輯
        btnGrade.addActionListener(e -> {
            int selectedCourseIndex = courseComboBox.getSelectedIndex();
            int selectedStudentRow = studentsTable.getSelectedRow();

            if (selectedCourseIndex == -1) {
                JOptionPane.showMessageDialog(teacherPanel, "請先選擇一門課程！");
                return;
            }
            if (selectedStudentRow == -1) {
                JOptionPane.showMessageDialog(teacherPanel, "請在表格中點選一位學生！");
                return;
            }

            try {
                double score = Double.parseDouble(txtScore.getText().trim());
                
                // 找出對應的課程與學生物件
                Course selectedCourse = currentTeacher.getTeachingCourses().get(selectedCourseIndex);
                Student targetStudent = selectedCourse.getEnrolledStudents().get(selectedStudentRow);

                // 呼叫組員的打分數功能
                boolean success = system.gradeStudent(currentTeacher, targetStudent, selectedCourse, score);
                
                if (success) {
                    JOptionPane.showMessageDialog(teacherPanel, " 成績登記成功！");
                    txtScore.setText(""); // 清空分數框
                    updateStudentsTable(); // 重新整理表格，顯示最新分數
                } else {
                    JOptionPane.showMessageDialog(teacherPanel, " 系統拒絕登記。", "錯誤", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(teacherPanel, "請輸入有效的數字分數！", "格式錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });

        tabbedPane.addTab("新增課程", addCoursePanel);
        tabbedPane.addTab("學生名單與登記成績", gradePanel);

        teacherPanel.add(tabbedPane, BorderLayout.CENTER);
    }

    private void refreshTeacherView() {
        if (currentTeacher == null) return;
        lblTeacherWelcome.setText("教授：" + currentTeacher.getName() + " 您好！");

        // 更新下拉選單中的課程
        courseComboBox.removeAllItems();
        List<Course> myCourses = currentTeacher.getTeachingCourses();
        for (Course c : myCourses) {
            courseComboBox.addItem(c.getCourseName() + " (" + c.getCourseId() + ")");
        }
        
        // 更新表格
        updateStudentsTable();
    }

    // 負責從目前選定的課程中，撈出學生並畫在表格上的方法
    private void updateStudentsTable() {
        studentsTableModel.setRowCount(0); // 先清空表格舊資料
        
        int selectedIndex = courseComboBox.getSelectedIndex();
        if (selectedIndex >= 0 && currentTeacher != null) {
            Course selectedCourse = currentTeacher.getTeachingCourses().get(selectedIndex);
            List<Student> students = selectedCourse.getEnrolledStudents();
            
            for (Student s : students) {
                Double currentScore = s.getCourseGrades().get(selectedCourse);
                String scoreDisplay = (currentScore == null) ? "尚未評分" : String.valueOf(currentScore);
                
                // 將學生資料新增到表格中
                studentsTableModel.addRow(new Object[]{s.getUid(), s.getName(), scoreDisplay});
            }
        }
    }

    // ==================== 程式執行起點 ====================
    public static void main(String[] args) {
        
        // 1. 先啟動 Nimbus 主題 
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

        // 2. 主題啟動後，利用迴圈強制修改 Nimbus 內部的所有字體設定
        
        Font uiFont = new Font("微軟正黑體", Font.PLAIN, 20); // 設定你想要的字體與大小
        
        // 取得 Nimbus 專屬的預設資料庫
        javax.swing.UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        
        // 設定字體大小
        for (Object key : defaults.keySet()) {
            if (key.toString().endsWith(".font")) {
                defaults.put(key, uiFont);
            }
        }

        // 3. 啟動 GUI 視窗
        SwingUtilities.invokeLater(() -> {
            new MainGUI().setVisible(true);
        });
    }
}

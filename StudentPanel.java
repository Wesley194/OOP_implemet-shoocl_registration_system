import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.regex.Pattern;
public class StudentPanel extends JPanel {
    private MainFrameController controller;
    private JLabel lblStudentWelcome = new JLabel();
    private JLabel lblStudentGradesSummary = new JLabel();
    private DefaultTableModel allCoursesModel;
    private DefaultTableModel myCoursesModel;
    private DefaultTableModel pendingCoursesModel;
    private SchedulePanel studentSchedulePanel = new SchedulePanel();

    public StudentPanel(MainFrameController controller) {
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
            if (controller.getCurrentStudent() != null) {
                UserProfileDialog.showDialog(controller.getFrame(), controller.getCurrentStudent(), controller.getDatabase());
                refreshStudentView();
            }
        });
        leftPanel.add(btnInfo);
        
        lblStudentWelcome.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblStudentWelcome.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
        leftPanel.add(lblStudentWelcome);
        topPanel.add(leftPanel, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refreshStudentView());
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> controller.logout());
        rightPanel.add(btnRefresh);
        rightPanel.add(btnLogout);
        topPanel.add(rightPanel, BorderLayout.EAST);
        this.add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        // Browse All Courses
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

        TableRowSorter<DefaultTableModel> allCoursesSorter = new TableRowSorter<>(allCoursesModel);
        allCoursesTable.setRowSorter(allCoursesSorter);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search by:"));
        JComboBox<String> searchTypeCombo = new JComboBox<>(new String[] { "Course ID", "Course Name", "Professor" });
        JTextField txtCourseSearch = new JTextField(20);
        JButton btnClearSearch = new JButton("Clear");
        searchPanel.add(searchTypeCombo);
        searchPanel.add(txtCourseSearch);
        searchPanel.add(btnClearSearch);
        enrollPanel.add(searchPanel, BorderLayout.NORTH);

        Runnable updateCourseFilter = () -> applyCourseFilter(allCoursesSorter, searchTypeCombo, txtCourseSearch);
        searchTypeCombo.addActionListener(e -> updateCourseFilter.run());
        txtCourseSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateCourseFilter.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateCourseFilter.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateCourseFilter.run();
            }
        });
        btnClearSearch.addActionListener(e -> txtCourseSearch.setText(""));

        enrollPanel.add(new JScrollPane(allCoursesTable), BorderLayout.CENTER);

        JPanel bottomActionPanel = new JPanel();
        JButton btnEnroll = new JButton("Register for Lottery / Normal Add");
        JButton btnForceEnroll = new JButton("Force Add with Auth Code");
        bottomActionPanel.add(btnEnroll);
        bottomActionPanel.add(btnForceEnroll);
        enrollPanel.add(bottomActionPanel, BorderLayout.SOUTH);

        btnEnroll.addActionListener(e -> {
            int row = allCoursesTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a course from the table first!", "Hint",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            Course selectedCourse = getSelectedAllCourse(allCoursesTable);
            if (selectedCourse == null) {
                JOptionPane.showMessageDialog(this, "Selected course was not found.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                SystemStateManager.SystemPhase phase = controller.getSystem().getCurrentPhase();
                if (phase == SystemStateManager.SystemPhase.PRE_ENROLL) {
                    controller.getSystem().registerForLottery(controller.getCurrentStudent(), selectedCourse);
                    JOptionPane.showMessageDialog(this, "Registration successful! Please wait for lottery results.");
                } else if (phase == SystemStateManager.SystemPhase.ADD_DROP) {
                    controller.getSystem().normalEnroll(controller.getCurrentStudent(), selectedCourse);
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

        btnForceEnroll.addActionListener(e -> {
            int row = allCoursesTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a course to force add from the table!", "Hint",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            Course selectedCourse = getSelectedAllCourse(allCoursesTable);
            if (selectedCourse == null) {
                JOptionPane.showMessageDialog(this, "Selected course was not found.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            String inputCode = JOptionPane.showInputDialog(this,
                    "Please enter the auth code for [" + selectedCourse.getCourseName() + "]:\n(Cancel if none)",
                    "Force Add with Auth Code", JOptionPane.QUESTION_MESSAGE);
            if (inputCode != null && !inputCode.trim().isEmpty()) {
                try {
                    controller.getSystem().forceEnrollWithPassword(controller.getCurrentStudent(), selectedCourse, inputCode.trim());
                    JOptionPane.showMessageDialog(this, "Force add successful!");
                    refreshStudentView();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Force Add Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // My Grades
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

        JPanel bottomPanel = new JPanel(new BorderLayout());
        lblStudentGradesSummary.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        bottomPanel.add(lblStudentGradesSummary, BorderLayout.WEST);
        JPanel myCourseActions = new JPanel();
        JButton btnDrop = new JButton("Drop Course");
        JButton btnViewAnnouncements = new JButton("View Announcements");
        myCourseActions.add(btnDrop);
        myCourseActions.add(btnViewAnnouncements);
        bottomPanel.add(myCourseActions, BorderLayout.EAST);
        myGradesPanel.add(bottomPanel, BorderLayout.SOUTH);

        btnDrop.addActionListener(e -> {
            int row = myCoursesTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a course to drop from the table!", "Hint",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            Course selectedCourse = controller.getCurrentStudent().getMyCourses().get(row);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to drop [" + selectedCourse.getCourseName() + "]?", "Confirm Drop",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    controller.getSystem().dropEnrolledCourse(controller.getCurrentStudent(), selectedCourse);
                    JOptionPane.showMessageDialog(this, "Course dropped successfully!");
                    refreshStudentView();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Drop Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnViewAnnouncements.addActionListener(e -> {
            int row = myCoursesTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a course first.", "Hint",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            showCourseAnnouncements(controller.getCurrentStudent().getMyCourses().get(row));
        });

        // Pending Courses
        JPanel pendingPanel = new JPanel(new BorderLayout());
        String[] pendingCols = { "Course ID", "Course Name", "Credits", "Professor", "Time" };
        pendingCoursesModel = new DefaultTableModel(pendingCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable pendingCoursesTable = new JTable(pendingCoursesModel);
        pendingCoursesTable.setRowHeight(25);
        pendingCoursesTable.getTableHeader().setReorderingAllowed(false);
        pendingPanel.add(new JScrollPane(pendingCoursesTable), BorderLayout.CENTER);
        JPanel pendingActions = new JPanel();
        JButton btnCancelPending = new JButton("Cancel Registration");
        pendingActions.add(btnCancelPending);
        pendingPanel.add(pendingActions, BorderLayout.SOUTH);

        btnCancelPending.addActionListener(e -> {
            int row = pendingCoursesTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a course to cancel from the table!", "Hint",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String courseId = (String) pendingCoursesModel.getValueAt(row, 0);
            Course selectedCourse = controller.getDatabase().getAllCourses().stream()
                    .filter(c -> c.getCourseId().equals(courseId)).findFirst().orElse(null);
            if (selectedCourse != null) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to cancel pending for [" + selectedCourse.getCourseName() + "]?",
                        "Confirm Cancel",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        controller.getSystem().cancelPendingCourse(controller.getCurrentStudent(), selectedCourse);
                        JOptionPane.showMessageDialog(this, "Registration cancelled successfully!");
                        refreshStudentView();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Operation Failed",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        tabbedPane.addTab("Browse All Courses", enrollPanel);
        tabbedPane.addTab("Pending Courses", pendingPanel);
        tabbedPane.addTab("My Schedule", studentSchedulePanel);
        tabbedPane.addTab("My Grades", myGradesPanel);
        this.add(tabbedPane, BorderLayout.CENTER);
    }

    public void refreshStudentView() {
        Student currentStudent = controller.getCurrentStudent();
        if(currentStudent == null) return;
        SqliteDatabase db = controller.getDatabase();
        RegistrationSystem system = controller.getSystem();

        int totalCredits = 0;
        for (Course c : currentStudent.getMyCourses()) {
            totalCredits += c.getCredits();
        }

        lblStudentWelcome.setText("Student: " + currentStudent.getName() + " | Phase: "
                + system.getPhaseName(system.getCurrentPhase()));
        lblStudentGradesSummary
                .setText("Total Credits: " + totalCredits + " | GPA: " + system.calculateGPA(currentStudent));

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

        pendingCoursesModel.setRowCount(0);
        for (Course c : db.getAllCourses()) {
            if (c.getPendingStudents().contains(currentStudent)) {
                pendingCoursesModel.addRow(new Object[] { c.getCourseId(), c.getCourseName(), c.getCredits(),
                        c.getTeacher().getName(), c.getTimeSlot().toString() });
            }
        }

        studentSchedulePanel.updateCourses(currentStudent.getMyCourses(), true);
    }

    private void applyCourseFilter(TableRowSorter<DefaultTableModel> sorter, JComboBox<String> searchTypeCombo,
            JTextField txtCourseSearch) {
        String keyword = txtCourseSearch.getText().trim();
        if (keyword.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }

        int column = 0;
        String selectedType = (String) searchTypeCombo.getSelectedItem();
        if ("Course Name".equals(selectedType)) {
            column = 1;
        } else if ("Professor".equals(selectedType)) {
            column = 3;
        }

        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(keyword), column));
    }

    private Course getSelectedAllCourse(JTable allCoursesTable) {
        int viewRow = allCoursesTable.getSelectedRow();
        if (viewRow == -1) {
            return null;
        }

        int modelRow = allCoursesTable.convertRowIndexToModel(viewRow);
        String courseId = (String) allCoursesModel.getValueAt(modelRow, 0);
        for (Course course : controller.getDatabase().getAllCourses()) {
            if (course.getCourseId().equals(courseId)) {
                return course;
            }
        }
        return null;
    }

    private void showCourseAnnouncements(Course course) {
        String[] cols = { "ID", "Title", "Content", "Created" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(25);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        addAnnouncementDoubleClickHandler(table);
        for (Announcement a : controller.getDatabase().getCourseAnnouncements(course.getCourseId())) {
            model.addRow(announcementRow(a));
        }
        JButton readButton = new JButton("Read Selected");
        JPanel content = new JPanel(new BorderLayout());
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        content.add(readButton, BorderLayout.SOUTH);
        JDialog dialog = new JDialog(controller.getFrame(), "Announcements - " + course.getCourseName(), true);
        readButton.addActionListener(e -> readSelectedAnnouncement(table));
        dialog.setContentPane(content);
        dialog.setSize(650, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void addAnnouncementDoubleClickHandler(JTable table) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1 || e.getClickCount() != 2)
                    return;
                int row = table.rowAtPoint(e.getPoint());
                if (row == -1)
                    return;
                table.setRowSelectionInterval(row, row);
                readSelectedAnnouncement(table);
            }
        });
    }

    private void readSelectedAnnouncement(JTable table) {
        Integer id = selectedAnnouncementId(table);
        if (id == null)
            return;
        Announcement a = controller.getDatabase().getAnnouncementById(id);
        if (a == null) {
            JOptionPane.showMessageDialog(this, "Announcement not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JTextArea content = new JTextArea(a.getContent(), 12, 40);
        content.setEditable(false);
        content.setLineWrap(true);
        content.setWrapStyleWord(true);
        String professor = a.getProfessorName() == null ? a.getProfessorId()
                : a.getProfessorName() + " (" + a.getProfessorId() + ")";
        Object[] detail = {
                "Title: " + a.getTitle(),
                "Course: " + (a.getCourseName() == null ? "" : a.getCourseName()),
                "Professor: " + professor,
                "Created: " + a.getCreatedAt(),
                "Updated: " + a.getUpdatedAt(),
                new JScrollPane(content)
        };
        JOptionPane.showMessageDialog(this, detail, "Announcement Detail", JOptionPane.INFORMATION_MESSAGE);
    }

    private Integer selectedAnnouncementId(JTable table) {
        if (table == null || table.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Please select an announcement first.", "Hint",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return (Integer) table.getValueAt(table.getSelectedRow(), 0);
    }
    
    private Object[] announcementRow(Announcement a) {
        return new Object[] {
                a.getId(),
                a.getTitle(),
                a.getContent(),
                a.getCreatedAt()
        };
    }
}

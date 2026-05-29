import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
public class TeacherPanel extends JPanel {
    private MainFrameController controller;
    private JLabel lblTeacherWelcome = new JLabel();
    private JComboBox<String> courseComboBox;
    private JComboBox<String> teacherAnnouncementCourseCombo;
    private DefaultTableModel studentsTableModel;
    private JTable studentsTable;
    private DefaultTableModel teacherAnnouncementModel;
    private JTable teacherAnnouncementTable;
    private SchedulePanel teacherSchedulePanel = new SchedulePanel();
    public TeacherPanel(MainFrameController controller) {
        this.controller = controller;
        buildPanel();
    }
    private void buildPanel() {
        this.setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        lblTeacherWelcome.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTeacherWelcome.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(lblTeacherWelcome, BorderLayout.WEST);
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refreshTeacherView());
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> controller.logout());
        rightPanel.add(btnRefresh);
        rightPanel.add(btnLogout);
        topPanel.add(rightPanel, BorderLayout.EAST);
        this.add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        // Create Course
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
        JTextField txtAuthCardCount = new JTextField("0", 12);
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
        fieldsPanel.add(new JLabel("Auth Card Count:"));
        fieldsPanel.add(txtAuthCardCount);
        formPanel.add(fieldsPanel, BorderLayout.CENTER);

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
            String authCardCountStr = txtAuthCardCount.getText().trim();
            String creditsStr = txtCredits.getText().trim();
            String dayStr = txtDay.getText().trim();
            String startStr = txtStart.getText().trim();
            String endStr = txtEnd.getText().trim();
            String capacityStr = txtCapacity.getText().trim();
            if (code.isEmpty() || name.isEmpty() || creditsStr.isEmpty() || dayStr.isEmpty() || startStr.isEmpty()
                    || endStr.isEmpty() || capacityStr.isEmpty() || authCardCountStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all course information!", "Incomplete Data",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                int credits = Integer.parseInt(creditsStr);
                int day = Integer.parseInt(dayStr);
                int start = Integer.parseInt(startStr);
                int end = Integer.parseInt(endStr);
                int maxCapacity = Integer.parseInt(capacityStr);
                int authCardCount = Integer.parseInt(authCardCountStr);
                TimeSlot time = new TimeSlot(day, start, end);
                List<String> authCodes = controller.getSystem().createCourseWithAuthCodes(controller.getCurrentTeacher(), code, name, credits,
                        maxCapacity, time, authCardCount);
                String successMsg = " Course [" + name + "] created successfully!";
                if (authCardCount > 0 && authCodes != null && !authCodes.isEmpty()) {
                    successMsg += "\nGenerated Auth Codes:\n" + String.join("\n", authCodes);
                }
                JOptionPane.showMessageDialog(this, successMsg);
                txtCode.setText("");
                txtName.setText("");
                txtCredits.setText("");
                txtDay.setText("");
                txtStart.setText("");
                txtEnd.setText("");
                txtCapacity.setText("50");
                txtAuthCardCount.setText("0");
                refreshTeacherView();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Please check input format!", "Format Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Course Creation Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Student List & Grading
        JPanel gradePanel = new JPanel(new BorderLayout());
        JPanel selectCoursePanel = new JPanel();
        selectCoursePanel.add(new JLabel("Please select your course: "));
        courseComboBox = new JComboBox<>();
        selectCoursePanel.add(courseComboBox);
        gradePanel.add(selectCoursePanel, BorderLayout.NORTH);
        JButton btnViewAuthCodes = new JButton("Auth Codes");
        selectCoursePanel.add(btnViewAuthCodes);

        // Auth Codes Details
        btnViewAuthCodes.addActionListener(e -> {
            int selectedCourseIndex = courseComboBox.getSelectedIndex();
            Teacher currentTeacher = controller.getCurrentTeacher();
            if (selectedCourseIndex == -1 || currentTeacher == null) {
                JOptionPane.showMessageDialog(this, "Please select a course first!");
                return;
            }

            Course selectedCourse = currentTeacher.getTeachingCourses().get(selectedCourseIndex);
            List<AuthCode> authCodesList = controller.getDatabase().getCourseAuthCodes(selectedCourse.getCourseId());
            if (authCodesList.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No Auth Codes generated for [" + selectedCourse.getCourseName() + "].",
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
            JOptionPane.showMessageDialog(this, scrollPane,
                    "Auth Code Details - " + selectedCourse.getCourseName(), JOptionPane.PLAIN_MESSAGE);
        });

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
            Teacher currentTeacher = controller.getCurrentTeacher();
            if (selectedCourseIndex == -1 || selectedStudentRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a course and a student from the table!");
                return;
            }

            String scoreStr = txtScore.getText().trim();
            if (scoreStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a grade!", "Input Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                double score = Double.parseDouble(scoreStr);
                Course selectedCourse = currentTeacher.getTeachingCourses().get(selectedCourseIndex);
                Student targetStudent = selectedCourse.getEnrolledStudents().get(selectedStudentRow);
                boolean success = controller.getSystem().gradeStudent(currentTeacher, targetStudent, selectedCourse, score);
                if (success) {
                    JOptionPane.showMessageDialog(this, " Grade submitted successfully!");
                    txtScore.setText("");
                    updateStudentsTable();
                } else {
                    JOptionPane.showMessageDialog(this, " System rejected submission.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Grade must be a number!", "Format Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Grade Submission Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        tabbedPane.addTab("Create Course", addCoursePanel);
        tabbedPane.addTab("Student List & Grading", gradePanel);
        tabbedPane.addTab("My Schedule", teacherSchedulePanel);
        tabbedPane.addTab("Announcements", buildTeacherAnnouncementsPanel());
        this.add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel buildTeacherAnnouncementsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel selector = new JPanel();
        selector.add(new JLabel("Course: "));
        teacherAnnouncementCourseCombo = new JComboBox<>();
        selector.add(teacherAnnouncementCourseCombo);
        panel.add(selector, BorderLayout.NORTH);
        String[] cols = { "ID", "Title", "Content", "Created" };
        teacherAnnouncementModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        teacherAnnouncementTable = new JTable(teacherAnnouncementModel);
        teacherAnnouncementTable.setRowHeight(25);
        teacherAnnouncementTable.getTableHeader().setReorderingAllowed(false);
        teacherAnnouncementTable.getColumnModel().getColumn(0).setMinWidth(0);
        teacherAnnouncementTable.getColumnModel().getColumn(0).setMaxWidth(0);
        teacherAnnouncementTable.getColumnModel().getColumn(0).setWidth(0);
        addAnnouncementDoubleClickHandler(teacherAnnouncementTable);
        panel.add(new JScrollPane(teacherAnnouncementTable), BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        JButton btnPublish = new JButton("Publish");
        JButton btnRead = new JButton("Read Selected");
        JButton btnEdit = new JButton("Edit Selected");
        JButton btnDelete = new JButton("Delete Selected");
        buttons.add(btnPublish);
        buttons.add(btnRead);
        buttons.add(btnEdit);
        buttons.add(btnDelete);
        panel.add(buttons, BorderLayout.SOUTH);
        btnPublish.addActionListener(e -> publishAnnouncement());
        btnRead.addActionListener(e -> readSelectedTeacherAnnouncement());
        btnEdit.addActionListener(e -> editSelectedTeacherAnnouncement());
        btnDelete.addActionListener(e -> deleteSelectedTeacherAnnouncement());
        teacherAnnouncementCourseCombo.addActionListener(e -> refreshTeacherAnnouncements());
        return panel;
    }

    public void refreshTeacherView() {
        Teacher currentTeacher = controller.getCurrentTeacher();
        if (currentTeacher == null)
            return;
        lblTeacherWelcome.setText("Professor: " + currentTeacher.getName() + " | Phase: "
                + controller.getSystem().getPhaseName(controller.getSystem().getCurrentPhase()));
        courseComboBox.removeAllItems();
        List<Course> myCourses = currentTeacher.getTeachingCourses();
        for (Course c : myCourses) {
            courseComboBox.addItem(c.getCourseName() + " (" + c.getCourseId() + ")");
        }
        if (teacherAnnouncementCourseCombo != null) {
            teacherAnnouncementCourseCombo.removeAllItems();
            for (Course c : myCourses) {
                teacherAnnouncementCourseCombo.addItem(c.getCourseName() + " (" + c.getCourseId() + ")");
            }
        }
        updateStudentsTable();
        teacherSchedulePanel.updateCourses(myCourses, false);
        refreshTeacherAnnouncements();
    }

    private Course selectedTeacherAnnouncementCourse() {
        Teacher currentTeacher = controller.getCurrentTeacher();
        if (teacherAnnouncementCourseCombo == null || currentTeacher == null)
            return null;
        int selectedIndex = teacherAnnouncementCourseCombo.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= currentTeacher.getTeachingCourses().size())
            return null;
        return currentTeacher.getTeachingCourses().get(selectedIndex);
    }

    private void refreshTeacherAnnouncements() {
        Teacher currentTeacher = controller.getCurrentTeacher();
        if (teacherAnnouncementModel == null || currentTeacher == null)
            return;
        teacherAnnouncementModel.setRowCount(0);
        Course selectedCourse = selectedTeacherAnnouncementCourse();
        if (selectedCourse == null)
            return;
        for (Announcement a : controller.getDatabase().getCourseAnnouncements(selectedCourse.getCourseId())) {
            if (!currentTeacher.getUid().equals(a.getProfessorId()))
                continue;
            teacherAnnouncementModel.addRow(announcementRow(a));
        }
    }

    private void publishAnnouncement() {
        Teacher currentTeacher = controller.getCurrentTeacher();
        if (currentTeacher == null)
            return;
        Course selectedCourse = selectedTeacherAnnouncementCourse();
        if (selectedCourse == null) {
            JOptionPane.showMessageDialog(this, "Please select a course before publishing.", "Input Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        JTextField titleField = new JTextField();
        JTextArea contentArea = new JTextArea(8, 30);
        Object[] form = {
                "Title:", titleField,
                "Content:", new JScrollPane(contentArea)
        };
        int result = JOptionPane.showConfirmDialog(this, form, "Publish Announcement",
                JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION)
            return;
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        if (title.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title and content cannot be empty.", "Input Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean created = controller.getDatabase().createAnnouncement(new Announcement(title, content, selectedCourse.getCourseId(),
                currentTeacher.getUid()));
        if (created) {
            JOptionPane.showMessageDialog(this, "Announcement published successfully.");
            refreshTeacherAnnouncements();
        } else {
            JOptionPane.showMessageDialog(this, "Announcement publish failed.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void readSelectedTeacherAnnouncement() {
        readSelectedAnnouncement(teacherAnnouncementTable);
    }

    private void editSelectedTeacherAnnouncement() {
        Teacher currentTeacher = controller.getCurrentTeacher();
        Integer id = selectedAnnouncementId(teacherAnnouncementTable);
        if (id == null || currentTeacher == null)
            return;
        Announcement a = controller.getDatabase().getAnnouncementById(id);
        if (a == null || !currentTeacher.getUid().equals(a.getProfessorId())) {
            JOptionPane.showMessageDialog(this, "You can only edit your own announcements.", "Permission Denied",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        JTextField titleField = new JTextField(a.getTitle());
        JTextArea contentArea = new JTextArea(a.getContent(), 8, 30);
        Object[] form = {
                "Title:", titleField,
                "Content:", new JScrollPane(contentArea)
        };
        int result = JOptionPane.showConfirmDialog(this, form, "Edit Announcement",
                JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION)
            return;
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        if (title.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title and content cannot be empty.", "Input Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        a.setTitle(title);
        a.setContent(content);
        boolean updated = controller.getDatabase().updateAnnouncement(a);
        if (updated) {
            JOptionPane.showMessageDialog(this, "Announcement updated successfully.");
            refreshTeacherAnnouncements();
        } else {
            JOptionPane.showMessageDialog(this, "Update failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedTeacherAnnouncement() {
        Teacher currentTeacher = controller.getCurrentTeacher();
        Integer id = selectedAnnouncementId(teacherAnnouncementTable);
        if (id == null || currentTeacher == null)
            return;
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected announcement?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;
        boolean deleted = controller.getDatabase().deleteAnnouncement(id, currentTeacher.getUid());
        if (deleted) {
            JOptionPane.showMessageDialog(this, "Announcement deleted successfully.");
            refreshTeacherAnnouncements();
        } else {
            JOptionPane.showMessageDialog(this, "You can only delete your own announcements.", "Permission Denied",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateStudentsTable() {
        Teacher currentTeacher = controller.getCurrentTeacher();
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

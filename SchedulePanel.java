import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class SchedulePanel extends JPanel {
    private DefaultTableModel scheduleModel;
    private JTable scheduleTable;

    public SchedulePanel() {
        setLayout(new BorderLayout());

        String[] scheduleCols = {"節次", "星期一", "星期二", "星期三", "星期四", "星期五"};
        scheduleModel = new DefaultTableModel(scheduleCols, 14) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        scheduleTable = new JTable(scheduleModel);
        scheduleTable.setRowHeight(55);
        scheduleTable.getTableHeader().setReorderingAllowed(false);
        scheduleTable.getColumnModel().getColumn(0).setPreferredWidth(20);
        
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JComponent c = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (column == 0) {
                    c.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY),
                        c.getBorder()
                    ));
                    ((JLabel) c).setVerticalAlignment(SwingConstants.CENTER);
                } else {
                    ((JLabel) c).setVerticalAlignment(SwingConstants.TOP);
                }
                return c;
            }
        };

        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < scheduleTable.getColumnCount(); i++) {
            scheduleTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        final javax.swing.table.TableCellRenderer defaultHeaderRenderer = scheduleTable.getTableHeader().getDefaultRenderer();
        if (defaultHeaderRenderer != null) {
            scheduleTable.getTableHeader().setDefaultRenderer(new javax.swing.table.TableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component comp = defaultHeaderRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (comp instanceof JLabel) {
                        ((JLabel) comp).setHorizontalAlignment(SwingConstants.CENTER);
                        ((JLabel) comp).setVerticalAlignment(SwingConstants.CENTER);
                    }
                    if (comp instanceof JComponent) {
                        JComponent c = (JComponent) comp;
                        if (column == 0) {
                            c.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(0, 0, 0, 2, Color.BLACK),
                                c.getBorder()
                            ));
                        }
                    }
                    return comp;
                }
            });
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
                        JOptionPane.showMessageDialog(SchedulePanel.this, cleanText, "課程資訊", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });
        
        add(new JScrollPane(scheduleTable), BorderLayout.CENTER);
    }

    public void updateCourses(List<Course> courses, boolean showTeacherName) {
        // 初始化課表網格
        for (int r = 0; r < 14; r++) {
            scheduleModel.setValueAt((r + 1), r, 0);
            for (int c = 1; c <= 5; c++) {
                scheduleModel.setValueAt("", r, c);
            }
        }

        for (Course c : courses) {
            TimeSlot ts = c.getTimeSlot();
            int day = ts.getDayOfWeek();
            if (day >= 1 && day <= 5) {
                int start = ts.getStartPeriod();
                int end = ts.getEndPeriod();
                
                String cellText;
                if (showTeacherName) {
                    cellText = "<html><center>" + c.getCourseName() + "<br>" + c.getCourseId() + "<br>" + c.getTeacher().getName() + "<br>" + c.getCredits() + "學分" + "<br>" + c.getTimeSlot().toString() + "</center></html>";
                } else {
                    cellText = "<html><center>" + c.getCourseName() + "<br>" + c.getCourseId() + "<br>" + c.getCredits() + "學分" + "<br>" + c.getTimeSlot().toString() + "</center></html>";
                }

                for (int period = start; period <= end; period++) {
                    if (period >= 1 && period <= 14) {
                        scheduleModel.setValueAt(cellText, period - 1, day);
                    }
                }
            }
        }
    }
}

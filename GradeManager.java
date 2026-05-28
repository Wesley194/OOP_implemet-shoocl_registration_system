import java.util.Map;

// 負責登記成績與計算 GPA。
public class GradeManager {
    private SqliteDatabase database;

    // 建立成績管理器。
    public GradeManager(SqliteDatabase database) {
        this.database = database;
    }

    // 登記或更新學生的課程成績。
    public boolean gradeStudent(Teacher teacher, Student student, Course course, double score) throws Exception {
        if (score < 0.0 || score > 100.0) {
            throw new Exception("Grade update failed: score must be between 0 and 100.");
        }

        boolean success = database.updateGrade(student, course, score);
        if (success) {
            student.setGrade(course, score);
            return true;
        }
        return false;
    }

    // 根據已登記的成績計算 GPA。
    public double calculateGPA(Student student) {
        Map<Course, Double> grades = student.getCourseGrades();
        if (grades.isEmpty()) {
            return 0.0;
        }

        double totalGradePoints = 0;
        int totalCredits = 0;

        for (Map.Entry<Course, Double> entry : grades.entrySet()) {
            Course course = entry.getKey();
            double score = entry.getValue();
            int credits = course.getCredits();

            double point = convertToGPA(score);
            totalGradePoints += (point * credits);
            totalCredits += credits;
        }

        double finalGPA = totalGradePoints / totalCredits;
        return Math.round(finalGPA * 100.0) / 100.0;
    }

    // 將百分制成績換成 GPA 點數。
    private double convertToGPA(double score) {
        if (score >= 90) {
            return 4.3;
        }
        if (score >= 85) {
            return 4.0;
        }
        if (score >= 80) {
            return 3.7;
        }
        if (score >= 77) {
            return 3.3;
        }
        if (score >= 73) {
            return 3.0;
        }
        if (score >= 70) {
            return 2.7;
        }
        if (score >= 60) {
            return 2.0;
        }
        return 0.0;
    }
}

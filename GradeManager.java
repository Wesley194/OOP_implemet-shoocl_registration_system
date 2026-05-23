import java.util.Map;

public class GradeManager {
    
    public boolean gradeStudent(Teacher teacher, Student student, Course course, double score) throws Exception {
        if (score < 0.0 || score > 100.0) {
            throw new Exception("登記失敗：學生成績必須介於 0 到 100 之間！");
        }

        if (!teacher.getTeachingCourses().contains(course)) return false;
        if (!course.getEnrolledStudents().contains(student)) return false;

        student.setGrade(course, score);
        return true;
    }

    public double calculateGPA(Student student) {
        Map<Course, Double> grades = student.getCourseGrades();
        if (grades.isEmpty()) return 0.0;

        double totalGradePoints = 0;
        int totalCredits = 0;

        for (Map.Entry<Course, Double> entry : grades.entrySet()) {
            Course c = entry.getKey();
            double score = entry.getValue(); 
            int credits = c.getCredits();    

            double point = convertToGPA(score);
            totalGradePoints += (point * credits);
            totalCredits += credits;
        }

        double finalGPA = totalGradePoints / totalCredits;
        return Math.round(finalGPA * 100.0) / 100.0;
    }

    private double convertToGPA(double score) {
        if (score >= 90) return 4.3;
        if (score >= 85) return 4.0;
        if (score >= 80) return 3.7;
        if (score >= 77) return 3.3;
        if (score >= 73) return 3.0;
        if (score >= 70) return 2.7;
        if (score >= 60) return 2.0;
        return 0.0;                  
    }
}
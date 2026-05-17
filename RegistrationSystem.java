import java.util.Map;

public class RegistrationSystem {
    private FakeDatabase database;

    public RegistrationSystem(FakeDatabase database) {
        this.database = database;
    }

    public void createCourse(Teacher teacher, String courseId, String courseName, int credits, int maxCapacity, TimeSlot time) {
        Course newCourse = new Course(courseId, courseName, credits, maxCapacity, time, teacher);
        teacher.assignCourse(newCourse);
        database.addCourseToSystem(newCourse);
    }
    // 1. 自訂例外類別
    public class CourseFullException extends Exception {
        public CourseFullException(String message) { super(message); }
    }
    public class TimeConflictException extends Exception {
        public TimeConflictException(String message) { super(message); }
    }

    // 2. 升級你的系統引擎 (RegistrationSystem.java)
    public void enroll(Student student, Course course) throws CourseFullException, TimeConflictException, Exception {
        if (course.isFull()) {
            throw new CourseFullException("選課失敗：【" + course.getCourseName() + "】人數已滿！");
        }
        if (student.hasTimeConflict(course)) {
            throw new TimeConflictException("選課失敗：與您目前的課表衝堂！");
        }
        if (student.getMyCourses().contains(course)) {
            throw new Exception("選課失敗：您已經選修過此課程。");
        }
        
        student.enrollInCourse(course);
        course.addStudent(student);
    }

    public boolean gradeStudent(Teacher teacher, Student student, Course course, double score) {
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
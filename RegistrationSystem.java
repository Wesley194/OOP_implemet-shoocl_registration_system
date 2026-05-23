public class RegistrationSystem {
    // 系統底層依賴的三大專責模組
    private CourseManager courseManager;
    private EnrollmentManager enrollmentManager;
    private GradeManager gradeManager;

    // 建構子：在系統通電啟動時，把各個次模組實體化並接好線
    public RegistrationSystem(SqliteDatabase database) {
        this.courseManager = new CourseManager(database);
        this.enrollmentManager = new EnrollmentManager(database);
        this.gradeManager = new GradeManager(database);
    }

    // ----------------------------------------------------
    // 對外開放的 API 接口 (前端 Controller 只會跟這個類別溝通)
    // ----------------------------------------------------

    public void createCourse(Teacher teacher, String courseId, String courseName, int credits, int maxCapacity, TimeSlot time) {
        // 委託給 CourseManager 處理
        courseManager.createCourse(teacher, courseId, courseName, credits, maxCapacity, time);
    }

    public void enroll(Student student, Course course) throws CourseFullException, TimeConflictException, Exception {
        // 委託給 EnrollmentManager 處理
        enrollmentManager.enroll(student, course);
    }

    public boolean gradeStudent(Teacher teacher, Student student, Course course, double score) {
        // 委託給 GradeManager 處理
        return gradeManager.gradeStudent(teacher, student, course, score);
    }

    public double calculateGPA(Student student) {
        // 委託給 GradeManager 處理
        return gradeManager.calculateGPA(student);
    }
}
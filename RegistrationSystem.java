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

    // 加上 throws Exception
    public void createCourse(Teacher teacher, String courseId, String courseName, int credits, int maxCapacity, TimeSlot time) throws Exception {
        courseManager.createCourse(teacher, courseId, courseName, credits, maxCapacity, time);
    }

    // 加上 throws Exception
    public boolean gradeStudent(Teacher teacher, Student student, Course course, double score) throws Exception {
        return gradeManager.gradeStudent(teacher, student, course, score);
    }
    public void enroll(Student student, Course course) throws CourseFullException, TimeConflictException, Exception {
        // 委託給 EnrollmentManager 處理
        enrollmentManager.enroll(student, course);
    }

    

    public double calculateGPA(Student student) {
        // 委託給 GradeManager 處理
        return gradeManager.calculateGPA(student);
    }
}
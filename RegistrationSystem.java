import java.util.List;

// 給前端呼叫的系統入口，負責檢查階段並轉交給各個 Manager。
public class RegistrationSystem {
    private CourseManager courseManager;
    private GradeManager gradeManager;
    private LotteryManager lotteryManager;
    private SqliteDatabase database;
    private EnrollmentManager enrollmentManager;
    private SpecialEnrollmentManager specialEnrollmentManager;
    private SystemStateManager stateManager;

    // 建立系統需要用到的 Manager。
    public RegistrationSystem(SqliteDatabase database) {
        this.database = database;
        this.stateManager = new SystemStateManager();
        this.courseManager = new CourseManager(database);
        this.gradeManager = new GradeManager(database);
        this.lotteryManager = new LotteryManager(database);
        this.enrollmentManager = new EnrollmentManager(database);
        this.specialEnrollmentManager = new SpecialEnrollmentManager(database);
    }

    // 控制目前系統階段。
    public void setCurrentPhase(SystemStateManager.SystemPhase phase) {
        stateManager.setCurrentPhase(phase);
    }

    public SystemStateManager.SystemPhase getCurrentPhase() {
        return stateManager.getCurrentPhase();
    }

    public String getPhaseName(SystemStateManager.SystemPhase phase) {
        return stateManager.getPhaseName(phase);
    }

    // 初選階段的抽籤登記。
    public void registerForLottery(Student student, Course course) throws Exception {
        stateManager.requirePhase(SystemStateManager.SystemPhase.PRE_ENROLL, "lottery registration");
        lotteryManager.registerIntent(student, course);
    }

    // 加退選階段的一般加選。
    public void normalEnroll(Student student, Course course) throws Exception {
        stateManager.requirePhase(SystemStateManager.SystemPhase.ADD_DROP, "normal enrollment");
        enrollmentManager.enroll(student, course);
    }

    // 加退選階段的密碼卡加簽。
    public void forceEnrollWithPassword(Student student, Course course, String inputCode) throws Exception {
        stateManager.requirePhase(SystemStateManager.SystemPhase.ADD_DROP, "auth-code enrollment");
        specialEnrollmentManager.forceEnrollWithPassword(student, course, inputCode);
    }

    // 加退選階段的正式退選。
    public void dropEnrolledCourse(Student student, Course course) throws Exception {
        stateManager.requirePhase(SystemStateManager.SystemPhase.ADD_DROP, "drop course");
        enrollmentManager.drop(student, course);
    }

    // 初選階段取消抽籤登記。
    public void cancelPendingCourse(Student student, Course course) throws Exception {
        stateManager.requirePhase(SystemStateManager.SystemPhase.PRE_ENROLL, "cancel registration");

        if (!course.getPendingStudents().contains(student)) {
            throw new Exception("Cancel registration failed: you are not registered for this course lottery.");
        }
        lotteryManager.dropCourse(student, course);
    }

    // 教師開課。
    public void createCourse(
            Teacher teacher,
            String courseId,
            String courseName,
            int credits,
            int maxCapacity,
            TimeSlot time,
            String authCode) throws Exception {
        courseManager.createCourse(teacher, courseId, courseName, credits, maxCapacity, time, authCode);
    }

    // 教師開課，並產生指定張數的密碼卡。
    public List<String> createCourseWithAuthCodes(
            Teacher teacher,
            String courseId,
            String courseName,
            int credits,
            int maxCapacity,
            TimeSlot time,
            int authCodeCount) throws Exception {
        return courseManager.createCourseWithAuthCodes(
                teacher, courseId, courseName, credits, maxCapacity, time, authCodeCount);
    }

    // 教師登記學生成績。
    public boolean gradeStudent(Teacher teacher, Student student, Course course, double score) throws Exception {
        return gradeManager.gradeStudent(teacher, student, course, score);
    }

    // 計算學生 GPA。
    public double calculateGPA(Student student) {
        return gradeManager.calculateGPA(student);
    }

    // 執行所有課程的抽籤。
    public void runLotterySystem() {
        lotteryManager.executeAllLotteries(this.database.getAllCourses());
    }
}

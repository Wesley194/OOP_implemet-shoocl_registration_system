public class RegistrationSystem {
    // 系統底層依賴的四大專責模組
    private CourseManager courseManager;
    private GradeManager gradeManager;
    private LotteryManager lotteryManager;
    private SqliteDatabase database;
    private EnrollmentManager enrollmentManager;
    
    private SpecialEnrollmentManager specialEnrollmentManager;
    private SystemStateManager stateManager;


    // 建構子：在系統通電啟動時，把各個次模組實體化並接好線
    public RegistrationSystem(SqliteDatabase database) {
        this.database = database;
        this.stateManager = new SystemStateManager();
        this.courseManager = new CourseManager(database);
        this.gradeManager = new GradeManager(database);
        this.lotteryManager = new LotteryManager(database);
        this.enrollmentManager = new EnrollmentManager(database);

        
        this.specialEnrollmentManager = new SpecialEnrollmentManager(database);
    }
        /*public RegistrationSystem(SqliteDatabase database) {
        this.database = database;
        this.courseManager = new CourseManager(database);
        this.gradeManager = new GradeManager(database);
        this.lotteryManager = new LotteryManager(database);
    }*/

    /////////////////////////////////
    // 時間軸控制 (轉交給 SystemStateManager)
    public void setCurrentPhase(SystemStateManager.SystemPhase phase) {
        stateManager.setCurrentPhase(phase);
    }
    
    public SystemStateManager.SystemPhase getCurrentPhase() {
        return stateManager.getCurrentPhase();
    }


    

    // ----------------------------------------------------
    // 對外開放的 API 接口 (前端 Controller 只會跟這個類別溝通)
    // ----------------------------------------------------

    

   

    /////////////////////////////////////////////
    // 通道一：初選登記
    public void registerForLottery(Student student, Course course) throws Exception {
        // 讓 stateManager 負責檢查時間
        stateManager.requirePhase(SystemStateManager.SystemPhase.PRE_ENROLL, "登記抽籤");
        lotteryManager.registerIntent(student, course);
    }

    // 通道二：一般加選 (先搶先贏)
    public void normalEnroll(Student student, Course course) throws Exception {
        stateManager.requirePhase(SystemStateManager.SystemPhase.ADD_DROP, "一般加選");
        enrollmentManager.enroll(student, course); 
    }

    // 通道三：密碼卡加簽 (VIP 特權)
    public void forceEnrollWithPassword(Student student, Course course, String inputCode) throws Exception {
        stateManager.requirePhase(SystemStateManager.SystemPhase.ADD_DROP, "密碼卡加簽");
        specialEnrollmentManager.forceEnrollWithPassword(student, course, inputCode);
    }

    ///////////////////////////////
     // 提供給 GUI 退選用
    public void dropEnrolledCourse(Student student, Course course) throws Exception {
        stateManager.requirePhase(SystemStateManager.SystemPhase.ADD_DROP, "退選");
        
        if (!student.getMyCourses().contains(course)) {
            throw new Exception("⛔ 您並未正式選修此課程，無法退選！");
        }
        lotteryManager.dropCourse(student, course);
    }
    public void cancelPendingCourse(Student student, Course course) throws Exception {
        stateManager.requirePhase(SystemStateManager.SystemPhase.PRE_ENROLL, "取消登記");
        
        if (!course.getPendingStudents().contains(student)) {
            throw new Exception("⛔ 您並未登記排隊這門課！");
        }
        lotteryManager.dropCourse(student, course); 
    }
    //////////////////////////////
    // 加上 throws Exception
    public void createCourse(Teacher teacher, String courseId, String courseName, int credits, int maxCapacity, TimeSlot time, String authCode) throws Exception {
        courseManager.createCourse(teacher, courseId, courseName, credits, maxCapacity, time, authCode);
    }
    // 加上 throws Exception
    public boolean gradeStudent(Teacher teacher, Student student, Course course, double score) throws Exception {
        return gradeManager.gradeStudent(teacher, student, course, score);
    }
    // 委託給 GradeManager 處理
    public double calculateGPA(Student student) {
        return gradeManager.calculateGPA(student);
    }
    // 提供給 Admin 用
    public void runLotterySystem() {
        lotteryManager.executeAllLotteries(this.database.getAllCourses()); 
    }


}
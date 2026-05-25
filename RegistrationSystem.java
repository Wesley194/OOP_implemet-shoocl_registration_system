public class RegistrationSystem {
    // 系統底層依賴的四大專責模組
    private CourseManager courseManager;
    private GradeManager gradeManager;
    private LotteryManager lotteryManager;
    private SqliteDatabase database;
    
    // 1. 定義系統的四種狀態
    public enum SystemPhase {
        CLOSED,       // 系統關閉 (只能看課表)
        PRE_ENROLL,   // 初選期 (只能登記抽籤)
        LOTTERY_RUN,  // 抽籤分發中 (暫停所有操作)
        ADD_DROP      // 加退選期 (可以退選、密碼卡加簽)
    }

    // 2. 新增一個變數記錄現在的狀態 (預設為關閉)
    private SystemPhase currentPhase = SystemPhase.CLOSED;
    // 建構子：在系統通電啟動時，把各個次模組實體化並接好線
    public RegistrationSystem(SqliteDatabase database) {
        this.database = database;
        this.courseManager = new CourseManager(database);
        this.gradeManager = new GradeManager(database);
        this.lotteryManager = new LotteryManager(database);
    }

    // 3. 開放給 Admin 切換時段的方法
    public void setCurrentPhase(SystemPhase phase) {
        this.currentPhase = phase;
        System.out.println("📢 系統廣播：目前選課階段已切換為 [" + phase + "]");
    }
    
    public SystemPhase getCurrentPhase() {
        return this.currentPhase;
    }

    


    
    /*public RegistrationSystem(SqliteDatabase database) {
        this.database = database;
        this.courseManager = new CourseManager(database);
        this.gradeManager = new GradeManager(database);
        this.lotteryManager = new LotteryManager(database);
    }*/

    // ----------------------------------------------------
    // 對外開放的 API 接口 (前端 Controller 只會跟這個類別溝通)
    // ----------------------------------------------------

    // 加上 throws Exception
    public void createCourse(Teacher teacher, String courseId, String courseName, int credits, int maxCapacity, TimeSlot time, String authCode) throws Exception {
        courseManager.createCourse(teacher, courseId, courseName, credits, maxCapacity, time, authCode);
    }

    // 加上 throws Exception
    public boolean gradeStudent(Teacher teacher, Student student, Course course, double score) throws Exception {
        return gradeManager.gradeStudent(teacher, student, course, score);
    }
    ////////////////
    public void registerForLottery(Student student, Course course) throws Exception {
        if (this.currentPhase != SystemPhase.PRE_ENROLL) {
            throw new Exception("⛔ 目前不是「初選登記」時段，無法登記抽籤！");
        }
        lotteryManager.registerIntent(student, course);
    }
    ////////////////
    

    public double calculateGPA(Student student) {
        // 委託給 GradeManager 處理
        return gradeManager.calculateGPA(student);
    }
    // 提供給 GUI 退選用
    public void dropCourse(Student student, Course course) throws Exception {
        lotteryManager.dropCourse(student, course);
    }
    // 提供給 Admin 用
    public void runLotterySystem() {
        lotteryManager.executeAllLotteries(this.database.getAllCourses()); 
    }
    //密碼卡強制加簽功能
    public void forceEnrollWithPassword(Student student, Course course, String inputCode) throws Exception {
        if (this.currentPhase != SystemPhase.ADD_DROP) {
            throw new Exception("⛔ 目前不是「加退選」時段，無法使用密碼卡！");
        }
        String correctCode = course.getAuthCode();
        
        // 1. 檢查這門課有沒有開放密碼卡
        if (correctCode == null || correctCode.trim().isEmpty()) {
            throw new Exception("這門課沒有開放密碼卡加簽喔！");
        }
        
        // 2. 核對密碼是否正確
        if (!correctCode.equals(inputCode)) {
            throw new Exception("密碼錯誤，加簽失敗！");
        }
        
        // 3. 檢查是否已經在課表內了 (防呆)
        for (Course c : student.getMyCourses()) {
            if (c.getCourseId().equals(course.getCourseId())) {
                throw new Exception("你已經選上這門課了，不需要再加簽囉！");
            }
        }
        
        // 4. 🌟 無視抽籤與人數上限，直接寫入正式資料庫！
        database.saveEnrollment(student, course);
        
        // 5. 更新記憶體，讓畫面馬上看得到
        student.enrollInCourse(course);
        course.addStudent(student);
    }
}
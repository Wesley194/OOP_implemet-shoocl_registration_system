public class CourseManager {
    private SqliteDatabase database;

    public CourseManager(SqliteDatabase database) {
        this.database = database;
    }

    // 加上 throws Exception 讓錯誤訊息可以傳給前端
    public void createCourse(Teacher teacher, String courseId, String courseName, int credits, int maxCapacity, TimeSlot time) throws Exception {
        
        // 防呆 1: 檢查課號是否重複
        for (Course c : database.getAllCourses()) {
            if (c.getCourseId().equals(courseId)) {
                throw new Exception("開課失敗：課號 [" + courseId + "] 已存在於系統中！");
            }
        }

        // 防呆 2: 學分數限制 (1~3學分)
        if (credits < 1 || credits > 3) {
            throw new Exception("開課失敗：學分數必須介於 1 到 3 之間！");
        }

        // 防呆 3: 星期與節次合理性檢查
        if (time.getDayOfWeek() < 1 || time.getDayOfWeek() > 5) {
            throw new Exception("開課失敗：上課星期必須為 1 到 5 (週一至週五)！");
        }
        if (time.getStartPeriod() < 1 || time.getEndPeriod() > 14) {
            throw new Exception("開課失敗：節次必須介於 1 到 14 節之間！");
        }
        if (time.getEndPeriod() < time.getStartPeriod()) {
            throw new Exception("開課失敗：結束節次不能在開始節次之前！");
        }

        // 防呆 4: 檢查老師自己是否衝堂
        for (Course c : teacher.getTeachingCourses()) {
            if (c.getTimeSlot().isConflictWith(time)) {
                throw new Exception("開課失敗：您在此時段已開設 [" + c.getCourseName() + "]，請選擇其他時間！");
            }
        }

        // 所有檢查皆通過，正式建立並指派課程
        Course newCourse = new Course(courseId, courseName, credits, maxCapacity, time, teacher);
        teacher.assignCourse(newCourse);
        database.addCourseToSystem(newCourse);
        database.addCourseToSystem(newCourse);
    }
}
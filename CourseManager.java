// 負責教師開課與開課相關檢查。
public class CourseManager {
    private SqliteDatabase database;

    // 建立課程管理器。
    public CourseManager(SqliteDatabase database) {
        this.database = database;
    }

    // 檢查資料後建立新課程。
    public void createCourse(
            Teacher teacher,
            String courseId,
            String courseName,
            int credits,
            int maxCapacity,
            TimeSlot time,
            String authCode) throws Exception {

        // 檢查課號是否重複。
        for (Course c : database.getAllCourses()) {
            if (c.getCourseId().equals(courseId)) {
                throw new Exception("Course creation failed: course ID [" + courseId + "] already exists.");
            }
        }

        // 檢查學分、星期與節次是否合理。
        if (credits < 1 || credits > 3) {
            throw new Exception("Course creation failed: credits must be between 1 and 3.");
        }
        if (time.getDayOfWeek() < 1 || time.getDayOfWeek() > 5) {
            throw new Exception("Course creation failed: day of week must be between 1 and 5.");
        }
        if (time.getStartPeriod() < 1 || time.getEndPeriod() > 14) {
            throw new Exception("Course creation failed: class periods must be between 1 and 14.");
        }
        if (time.getEndPeriod() < time.getStartPeriod()) {
            throw new Exception("Course creation failed: end period cannot be earlier than start period.");
        }

        // 檢查教師自己的授課時間是否衝堂。
        for (Course c : teacher.getTeachingCourses()) {
            if (c.getTimeSlot().isConflictWith(time)) {
                throw new Exception("Course creation failed: this time conflicts with [" + c.getCourseName() + "].");
            }
        }

        Course newCourse = new Course(courseId, courseName, credits, maxCapacity, time, teacher);
        newCourse.setAuthCode(authCode);
        teacher.assignCourse(newCourse);
        database.addCourseToSystem(newCourse);
    }
}

public class SpecialEnrollmentManager {
    private SqliteDatabase database;

    public SpecialEnrollmentManager(SqliteDatabase database) {
        this.database = database;
    }

    public void forceEnrollWithPassword(Student student, Course course, String inputCode) throws Exception {
        String correctCode = course.getAuthCode();

        // 1. 檢查這門課有沒有開放密碼卡
        if (correctCode == null || correctCode.trim().isEmpty()) {
            throw new Exception("⛔ 這門課沒有開放密碼卡加簽喔！");
        }

        // 2. 核對密碼是否正確
        if (!correctCode.equals(inputCode)) {
            throw new Exception("⛔ 密碼錯誤，加簽失敗！");
        }

        // 3. 檢查是否已經在課表內了 (防呆)
        if (student.getMyCourses().contains(course)) {
            throw new Exception("⛔ 你已經選上這門課了，不需要再加簽囉！");
        }


        if (student.hasTimeConflict(course)) {
            throw new TimeConflictException("⛔ 加簽失敗：與您目前的課表衝堂！");
        }

        // 5. 執行寫入 (不檢查滿員)
        database.saveEnrollment(student, course);
        student.enrollInCourse(course);
        course.addStudent(student);
    }
}
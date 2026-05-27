public class SpecialEnrollmentManager {
    private SqliteDatabase database;

    public SpecialEnrollmentManager(SqliteDatabase database) {
        this.database = database;
    }

    public void forceEnrollWithPassword(Student student, Course course, String inputCode) throws Exception {

        //  檢查是否已經在課表內了
        if (student.getMyCourses().contains(course)) {
            throw new Exception(" 你已經選上這門課了，不需要再加簽囉！");
        }

        // 檢查是否衝堂
        if (student.hasTimeConflict(course)) {
            throw new TimeConflictException(" 加簽失敗：與您目前的課表衝堂！"); 
        }

        // (密碼卡無效、被用過，會直接 throw Exception
        database.verifyAndConsumeAuthCode(student, course, inputCode);

        // 5. 執行寫入 (不檢查滿員)
        database.saveEnrollment(student, course);
        student.enrollInCourse(course);
        course.addStudent(student);
    }
}
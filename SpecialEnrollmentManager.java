public class SpecialEnrollmentManager {
    private SqliteDatabase database;

    public SpecialEnrollmentManager(SqliteDatabase database) {
        this.database = database;
    }

    public void forceEnrollWithPassword(Student student, Course course, String inputCode) throws Exception {

        //  檢查是否已經在課表內了
        if (student.getMyCourses().contains(course)) {
            throw new Exception(" You've already selected this course; no further signatures are needed.！");
        }

        // 檢查是否衝堂
        if (student.hasTimeConflict(course)) {
            throw new TimeConflictException("Signing failed: It clashes with your current class schedule.！");
        }

        // (密碼卡無效、被用過，會直接 throw Exception
        database.verifyAndConsumeAuthCode(student, course, inputCode);

        // 5. 執行寫入 (不檢查滿員)
        database.saveEnrollment(student, course);
        student.enrollInCourse(course);
        course.addStudent(student);
    }
}
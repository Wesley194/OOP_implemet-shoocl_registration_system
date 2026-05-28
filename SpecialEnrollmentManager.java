public class SpecialEnrollmentManager {
    private SqliteDatabase database;

    public SpecialEnrollmentManager(SqliteDatabase database) {
        this.database = database;
    }

    public void forceEnrollWithPassword(Student student, Course course, String inputCode) throws Exception {
        String correctCode = course.getAuthCode();

        // 1. 檢查這門課有沒有開放密碼卡
        if (correctCode == null || correctCode.trim().isEmpty()) {
            throw new Exception("This course does not offer password card signing!");
        }

        // 2. 核對密碼是否正確
        if (!correctCode.equals(inputCode)) {
            throw new Exception("Incorrect password, signature failed!");
        }

        // 3. 檢查是否已經在課表內了 (防呆)
        if (student.getMyCourses().contains(course)) {
            throw new Exception("You've already selected this course; no further signatures are needed.！");
        }


        if (student.hasTimeConflict(course)) {
            throw new TimeConflictException("Signing failed: It clashes with your current class schedule.！");
        }

        // 5. 執行寫入 (不檢查滿員)
        database.saveEnrollment(student, course);
        student.enrollInCourse(course);
        course.addStudent(student);
    }
}
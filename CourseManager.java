import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

// 負責教師開課與開課相關檢查。
public class CourseManager {
    private static final String AUTH_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int AUTH_CODE_LENGTH = 6;

    private SqliteDatabase database;
    private Random random;

    // 建立課程管理器。
    public CourseManager(SqliteDatabase database) {
        this.database = database;
        this.random = new Random();
    }

    // 新版開課方法，會依張數產生密碼卡並回傳給 GUI。
    public List<String> createCourseWithAuthCodes(
            Teacher teacher,
            String courseId,
            String courseName,
            int credits,
            int maxCapacity,
            TimeSlot time,
            int authCodeCount) throws Exception {
        validateCourse(teacher, courseId, credits, time);
        validateAuthCodeCount(authCodeCount);

        Course newCourse = new Course(courseId, courseName, credits, maxCapacity, time, teacher);
        teacher.assignCourse(newCourse);
        database.addCourseToSystem(newCourse);

        return createAuthCodes(courseId, authCodeCount);
    }

    // 檢查開課資料是否合理。
    private void validateCourse(Teacher teacher, String courseId, int credits, TimeSlot time) throws Exception {
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
    }

    // 檢查密碼卡張數。
    private void validateAuthCodeCount(int authCodeCount) throws Exception {
        if (authCodeCount < 0) {
            throw new Exception("Course creation failed: auth code count cannot be negative.");
        }
    }

    // 產生指定數量的密碼卡並寫入資料庫。
    private List<String> createAuthCodes(String courseId, int authCodeCount) {
        List<String> authCodes = new ArrayList<>();
        Set<String> generatedCodes = new HashSet<>();

        while (authCodes.size() < authCodeCount) {
            String authCode = generateAuthCode();
            if (generatedCodes.add(authCode)) {
                database.insertAuthCode(courseId, authCode);
                authCodes.add(authCode);
            }
        }

        return authCodes;
    }

    // 產生一組密碼卡亂碼。
    private String generateAuthCode() {
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < AUTH_CODE_LENGTH; i++) {
            int index = random.nextInt(AUTH_CODE_CHARS.length());
            code.append(AUTH_CODE_CHARS.charAt(index));
        }

        return code.toString();
    }
}

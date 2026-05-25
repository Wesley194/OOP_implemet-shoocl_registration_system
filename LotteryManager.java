import java.util.Collections;
import java.util.List;

public class LotteryManager {
    private SqliteDatabase database;

    public LotteryManager(SqliteDatabase database) {
        this.database = database;
    }

    // ==========================================
    // 1. 學生端：登記加選 (加入抽籤名單)
    // ==========================================
    public void registerIntent(Student student, Course course) throws Exception {
        if (student.getMyCourses().contains(course)) {
            throw new Exception("選課失敗：您已經正式選修此課程！");
        }
        if (student.hasTimeConflict(course)) {
            throw new TimeConflictException("登記失敗：與您目前的課表衝堂！");
        }

        database.savePendingEnrollment(student, course);
        course.addPendingStudent(student);
    }

    // ==========================================
    // 2. 學生端：退選 / 取消登記
    // ==========================================
    public void dropCourse(Student student, Course course) throws Exception {
        // 情況 A：如果已經正式選上 -> 執行退選
        if (student.getMyCourses().contains(course)) {
            boolean success = database.deleteEnrollment(student, course);
            if (success) {
                student.getMyCourses().remove(course);
                student.getCourseGrades().remove(course);
                course.removeStudent(student);
            } else {
                throw new Exception("系統異常，退選失敗。");
            }
        } 
        // 情況 B：如果還在排隊抽籤 -> 取消登記
        else if (course.getPendingStudents().contains(student)) {
            database.deletePendingEnrollment(student, course);
            course.removePendingStudent(student);
        } else {
            throw new Exception("您並未選修或登記此課程！");
        }
    }

    // ==========================================
    // 3. 管理員端：執行全校大抽籤 (留給 Admin 同學去接按鈕)
    // ==========================================
    public void executeAllLotteries(List<Course> allCourses) {
        for (Course course : allCourses) {
            List<Student> pending = course.getPendingStudents();
            int availableSeats = course.getMaxCapacity() - course.getEnrolledStudents().size();

            if (pending.isEmpty()) continue;

            if (pending.size() <= availableSeats) {
                // 不足額：全上
                for (Student s : pending) finalizeEnrollment(s, course);
            } else {
                // 超額：打亂後抽籤
                Collections.shuffle(pending);
                for (int i = 0; i < availableSeats; i++) {
                    finalizeEnrollment(pending.get(i), course);
                }
            }
            
            // 抽籤完畢後清空該課程的排隊名單
            pending.clear();
        }
    }

    private void finalizeEnrollment(Student student, Course course) {
        database.saveEnrollment(student, course);
        student.enrollInCourse(course);
        course.addStudent(student);
    }
}
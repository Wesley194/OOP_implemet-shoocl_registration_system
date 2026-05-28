import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 記錄學生的課表與成績。
public class Student extends User {
    private List<Course> myCourses;
    private Map<Course, Double> courseGrades;

    // 建立學生帳號。
    public Student(String uid, String name, String password) {
        super(uid, name, password);
        this.myCourses = new ArrayList<>();
        this.courseGrades = new HashMap<>();
    }

    @Override
    public String getRole() {
        return "Student";
    }

    // 檢查新課程是否和目前課表衝堂。
    public boolean hasTimeConflict(Course newCourse) {
        for (Course c : myCourses) {
            if (c.getTimeSlot().isConflictWith(newCourse.getTimeSlot())) {
                return true;
            }
        }
        return false;
    }

    // 管理學生已選上的課程。
    public void enrollInCourse(Course course) {
        myCourses.add(course);
    }

    public List<Course> getMyCourses() {
        return myCourses;
    }

    // 管理學生各課程成績。
    public void setGrade(Course course, double grade) {
        courseGrades.put(course, grade);
    }

    public Map<Course, Double> getCourseGrades() {
        return courseGrades;
    }
}

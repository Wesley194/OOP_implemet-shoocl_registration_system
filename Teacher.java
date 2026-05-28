import java.util.ArrayList;
import java.util.List;

// 記錄教師基本資料與授課清單。
public class Teacher extends User {
    private List<Course> teachingCourses;

    // 建立教師帳號。
    public Teacher(String uid, String name, String password) {
        super(uid, name, password);
        this.teachingCourses = new ArrayList<>();
    }

    @Override
    public String getRole() {
        return "Teacher";
    }

    // 管理教師開設的課程。
    public void assignCourse(Course course) {
        teachingCourses.add(course);
    }

    public List<Course> getTeachingCourses() {
        return teachingCourses;
    }
}

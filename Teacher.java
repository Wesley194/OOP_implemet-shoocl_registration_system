import java.util.ArrayList;
import java.util.List;

public class Teacher extends User {
    private List<Course> teachingCourses;

    public Teacher(String uid, String name, String password) {
        super(uid, name, password);
        this.teachingCourses = new ArrayList<>();
    }

    @Override
    public String getRole() { return "Teacher"; }

    public void assignCourse(Course course) { teachingCourses.add(course); }
    public List<Course> getTeachingCourses() { return teachingCourses; }
}
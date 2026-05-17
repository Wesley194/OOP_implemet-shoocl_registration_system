import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Student extends User {
    private List<Course> myCourses;
    private Map<Course, Double> courseGrades;

    public Student(String uid, String name, String password) {
        super(uid, name, password);
        this.myCourses = new ArrayList<>();
        this.courseGrades = new HashMap<>();
    }

    @Override
    public String getRole() { return "Student"; }

    public boolean hasTimeConflict(Course newCourse) {
        for (Course c : myCourses) {
            if (c.getTimeSlot().isConflictWith(newCourse.getTimeSlot())) return true;
        }
        return false;
    }

    public void enrollInCourse(Course course) { myCourses.add(course); }
    public void setGrade(Course course, double grade) { courseGrades.put(course, grade); }

    public List<Course> getMyCourses() { return myCourses; }
    public Map<Course, Double> getCourseGrades() { return courseGrades; }
}
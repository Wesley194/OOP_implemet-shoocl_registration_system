import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeDatabase {
    private Map<String, Student> studentTable = new HashMap<>();
    private Map<String, Teacher> teacherTable = new HashMap<>();
    private List<Course> allCourses = new ArrayList<>(); 

    public List<Course> getAllCourses() { return allCourses; }
    public void addCourseToSystem(Course course) { allCourses.add(course); }

    public Student findStudent(String uid) { return studentTable.get(uid); }
    public Teacher findTeacher(String uid) { return teacherTable.get(uid); }
    
    public void registerStudent(Student s) { studentTable.put(s.getUid(), s); }
    public void registerTeacher(Teacher t) { teacherTable.put(t.getUid(), t); }
}
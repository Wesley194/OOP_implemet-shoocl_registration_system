import java.util.ArrayList;
import java.util.List;

public class Course {
    private String courseId;
    private String courseName;
    private int credits;
    private int maxCapacity;
    private TimeSlot timeSlot;
    private Teacher teacher;
    private List<Student> enrolledStudents;

    public Course(String courseId, String courseName, int credits, int maxCapacity, TimeSlot timeSlot, Teacher teacher) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.credits = credits;
        this.maxCapacity = maxCapacity;
        this.timeSlot = timeSlot;
        this.teacher = teacher;
        this.enrolledStudents = new ArrayList<>();
    }

    public boolean isFull() { return enrolledStudents.size() >= maxCapacity; }
    public String getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public int getCredits() { return credits; }
    public int getMaxCapacity() { return maxCapacity; }
    public TimeSlot getTimeSlot() { return timeSlot; }
    public Teacher getTeacher() { return teacher; } // 修復了警告
    public List<Student> getEnrolledStudents() { return enrolledStudents; }

    public void addStudent(Student student) {
        if (!isFull()) enrolledStudents.add(student);
    }
}
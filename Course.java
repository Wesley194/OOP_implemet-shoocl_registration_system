import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Course {
    private String courseId;
    private String courseName;
    private int credits;
    private int maxCapacity;
    private TimeSlot timeSlot;
    private Teacher teacher;
    private List<Student> enrolledStudents;
    private List<Student> pendingStudents;//等待抽籤名單
    private String authCode;

    public Course(String courseId, String courseName, int credits, int maxCapacity, TimeSlot timeSlot, Teacher teacher) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.credits = credits;
        this.maxCapacity = maxCapacity;
        this.timeSlot = timeSlot;
        this.teacher = teacher;
        this.enrolledStudents = new ArrayList<>();
        this.pendingStudents = new ArrayList<>();
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
        if (!isFull()) {
            // 如果有學號一樣的舊資料就直接丟掉
            this.enrolledStudents.removeIf(s -> s.getUid().equals(student.getUid()));
            this.enrolledStudents.add(student);
            this.enrolledStudents.sort(Comparator.comparing(Student::getUid));
        }
    }
    // 【新增以下方法】提供給加退選機制使用
    public List<Student> getPendingStudents() { return pendingStudents; }
    
    public void addPendingStudent(Student student) {
        this.pendingStudents.removeIf(s -> s.getUid().equals(student.getUid()));
        this.pendingStudents.add(student);
        this.enrolledStudents.sort(Comparator.comparing(Student::getUid));
    }
    
    public void removePendingStudent(Student student) {
        pendingStudents.remove(student);
    }

    public void removeStudent(Student student) {
        enrolledStudents.remove(student); // 用於正式退選
    }
    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Course otherCourse = (Course) obj;
        return this.getCourseId().equals(otherCourse.getCourseId());
    }

    @Override
    public int hashCode() {
        return this.getCourseId().hashCode();
    }
}
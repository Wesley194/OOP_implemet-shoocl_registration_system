import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// 記錄課程基本資料、正式選課名單與候補名單。
public class Course {
    private String courseId;
    private String courseName;
    private int credits;
    private int maxCapacity;
    private TimeSlot timeSlot;
    private Teacher teacher;
    private List<Student> enrolledStudents;
    private List<Student> pendingStudents;
    private String authCode;

    // 建立課程並初始化學生名單。
    public Course(
            String courseId,
            String courseName,
            int credits,
            int maxCapacity,
            TimeSlot timeSlot,
            Teacher teacher) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.credits = credits;
        this.maxCapacity = maxCapacity;
        this.timeSlot = timeSlot;
        this.teacher = teacher;
        this.enrolledStudents = new ArrayList<>();
        this.pendingStudents = new ArrayList<>();
    }

    // 判斷課程是否已滿。
    public boolean isFull() {
        return enrolledStudents.size() >= maxCapacity;
    }

    // 提供課程基本資料給其他物件使用。
    public String getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public int getCredits() {
        return credits;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public List<Student> getEnrolledStudents() {
        return enrolledStudents;
    }

    // 管理正式選課名單。
    public void addStudent(Student student) {
        if (!isFull()) {
            this.enrolledStudents.removeIf(s -> s.getUid().equals(student.getUid()));
            this.enrolledStudents.add(student);
            this.enrolledStudents.sort(Comparator.comparing(Student::getUid));
        }
    }

    public void removeStudent(Student student) {
        enrolledStudents.remove(student);
    }

    // 管理抽籤候補名單。
    public List<Student> getPendingStudents() {
        return pendingStudents;
    }

    public void addPendingStudent(Student student) {
        this.pendingStudents.removeIf(s -> s.getUid().equals(student.getUid()));
        this.pendingStudents.add(student);
        this.enrolledStudents.sort(Comparator.comparing(Student::getUid));
    }

    public void removePendingStudent(Student student) {
        pendingStudents.remove(student);
    }

    // 管理課程加簽授權碼。
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

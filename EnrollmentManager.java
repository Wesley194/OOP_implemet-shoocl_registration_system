// 負責一般加選與正式退選。
public class EnrollmentManager {
    private SqliteDatabase database;

    // 建立選課管理器。
    public EnrollmentManager(SqliteDatabase database) {
        this.database = database;
    }

    // 處理加退選階段的一般加選。
    public void enroll(Student student, Course course) throws CourseFullException, TimeConflictException, Exception {
        if (isEnrolledInCourse(student, course)) {
            throw new Exception("Enrollment failed: you are already enrolled in this course.");
        }
        if (course.isFull()) {
            throw new CourseFullException("Enrollment failed: " + course.getCourseName() + " is full.");
        }
        if (student.hasTimeConflict(course)) {
            throw new TimeConflictException("Enrollment failed: this course conflicts with your current schedule.");
        }

        database.saveEnrollment(student, course);
        student.enrollInCourse(course);
        course.addStudent(student);
    }

    // 處理加退選階段的正式退選。
    public void drop(Student student, Course course) throws Exception {
        if (!isEnrolledInCourse(student, course)) {
            throw new Exception("Drop failed: you are not enrolled in this course.");
        }

        boolean success = database.deleteEnrollment(student, course);
        if (!success) {
            throw new Exception("Drop failed: the system could not remove this enrollment.");
        }

        student.getMyCourses().removeIf(enrolledCourse -> isSameCourse(enrolledCourse, course));
        student.getCourseGrades().keySet().removeIf(gradedCourse -> isSameCourse(gradedCourse, course));
        course.getEnrolledStudents().removeIf(enrolledStudent -> isSameStudent(enrolledStudent, student));
    }

    // 用課號確認學生是否已選過這門課。
    private boolean isEnrolledInCourse(Student student, Course course) {
        for (Course enrolledCourse : student.getMyCourses()) {
            if (isSameCourse(enrolledCourse, course)) {
                return true;
            }
        }
        return false;
    }

    // 用課號比對課程。
    private boolean isSameCourse(Course firstCourse, Course secondCourse) {
        return firstCourse.getCourseId().equals(secondCourse.getCourseId());
    }

    // 用學號比對學生。
    private boolean isSameStudent(Student firstStudent, Student secondStudent) {
        return firstStudent.getUid().equals(secondStudent.getUid());
    }
}

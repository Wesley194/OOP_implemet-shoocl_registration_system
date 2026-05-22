public class EnrollmentManager {
    public void enroll(Student student, Course course) throws CourseFullException, TimeConflictException, Exception {
        if (course.isFull()) {
            throw new CourseFullException("選課失敗：【" + course.getCourseName() + "】人數已滿！");
        }
        if (student.hasTimeConflict(course)) {
            throw new TimeConflictException("選課失敗：與您目前的課表衝堂！");
        }
        if (student.getMyCourses().contains(course)) {
            throw new Exception("選課失敗：您已經選修過此課程。");
        }
        
        student.enrollInCourse(course);
        course.addStudent(student);
    }
}
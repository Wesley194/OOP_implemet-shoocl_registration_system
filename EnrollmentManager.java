public class EnrollmentManager {
    public void enroll(Student student, Course course) throws CourseFullException, TimeConflictException, Exception {
        // 1. 優先檢查：是否已經選過這門課？ (放到最前面)
        if (student.getMyCourses().contains(course)) {
            throw new Exception("選課失敗：您已經選修過此課程。");
        }
        
        // 2. 檢查：人數是否已滿？
        if (course.isFull()) {
            throw new CourseFullException("選課失敗：【" + course.getCourseName() + "】人數已滿！");
        }
        
        // 3. 檢查：是否與目前課表衝堂？
        if (student.hasTimeConflict(course)) {
            throw new TimeConflictException("選課失敗：與您目前的課表衝堂！");
        }
        
        // 檢查皆通過，正式加入課表
        student.enrollInCourse(course);
        course.addStudent(student);
    }
}
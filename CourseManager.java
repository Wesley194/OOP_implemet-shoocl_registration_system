public class CourseManager {
    private SqliteDatabase database;

    public CourseManager(SqliteDatabase database) {
        this.database = database;
    }

    public void createCourse(Teacher teacher, String courseId, String courseName, int credits, int maxCapacity, TimeSlot time) {
        Course newCourse = new Course(courseId, courseName, credits, maxCapacity, time, teacher);
        teacher.assignCourse(newCourse);
        database.addCourseToSystem(newCourse);
        database.addCourseToSystem(newCourse);
    }
}
public class SqlQueries {

    // 建表與初始化語法 DDL
    public static final String ENABLE_FOREIGN_KEYS = "PRAGMA foreign_keys = ON;";
    
    public static final String CREATE_ADMINS = "CREATE TABLE IF NOT EXISTS admins (" +
            "uid TEXT PRIMARY KEY, name TEXT NOT NULL, password TEXT NOT NULL);";

    public static final String CREATE_TEACHERS = "CREATE TABLE IF NOT EXISTS teachers (" +
            "uid TEXT PRIMARY KEY, name TEXT NOT NULL, password TEXT NOT NULL);";

    public static final String CREATE_STUDENTS = "CREATE TABLE IF NOT EXISTS students (" +
            "uid TEXT PRIMARY KEY, name TEXT NOT NULL, password TEXT NOT NULL);";

    public static final String CREATE_COURSES = "CREATE TABLE IF NOT EXISTS courses (" +
            "course_id TEXT PRIMARY KEY, course_name TEXT NOT NULL, credits INTEGER NOT NULL, " +
            "max_capacity INTEGER NOT NULL DEFAULT 50, day_of_week INTEGER NOT NULL, " +
            "start_period INTEGER NOT NULL, end_period INTEGER NOT NULL, auth_code TEXT, " +
            "teacher_id TEXT NOT NULL, FOREIGN KEY (teacher_id) REFERENCES teachers(uid) " +
            "ON UPDATE CASCADE ON DELETE RESTRICT);";

    public static final String CREATE_AUTH_CODES = "CREATE TABLE IF NOT EXISTS auth_codes (" +
            "code TEXT PRIMARY KEY, course_id TEXT NOT NULL, is_used INTEGER DEFAULT 0, " +
            "used_by TEXT, FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE, " +
            "FOREIGN KEY (used_by) REFERENCES students(uid) ON DELETE SET NULL);";

    public static final String CREATE_ANNOUNCEMENTS = "CREATE TABLE IF NOT EXISTS announcements (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "course_id TEXT NOT NULL, " +
            "professor_id TEXT NOT NULL, " +
            "title TEXT NOT NULL, " +
            "content TEXT NOT NULL, " +
            "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
            "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP, " + 
            "FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE, " +
            "FOREIGN KEY (professor_id) REFERENCES teachers(uid) ON DELETE CASCADE);";

    public static final String CREATE_ENROLLMENTS = "CREATE TABLE IF NOT EXISTS enrollments (" +
            "student_id TEXT, course_id TEXT, score REAL, " +
            "PRIMARY KEY (student_id, course_id), " +
            "FOREIGN KEY (student_id) REFERENCES students(uid) ON UPDATE CASCADE ON DELETE CASCADE, " +
            "FOREIGN KEY (course_id) REFERENCES courses(course_id) ON UPDATE CASCADE ON DELETE CASCADE);";

    public static final String CREATE_PENDING = "CREATE TABLE IF NOT EXISTS pending_enrollments (" +
            "student_id TEXT, course_id TEXT, PRIMARY KEY (student_id, course_id));";

    public static final String INSERT_DEFAULT_ADMIN = "INSERT OR IGNORE INTO admins (uid, name, password) " +
            "VALUES ('admin', 'admin', 'admin123');";


    // 新增資料 INSERT
    public static final String INSERT_STUDENT = "INSERT INTO students (uid, name, password) VALUES (?, ?, ?)";
    
    public static final String INSERT_TEACHER = "INSERT INTO teachers (uid, name, password) VALUES (?, ?, ?)";
    
    public static final String INSERT_COURSE = "INSERT INTO courses (course_id, course_name, credits, max_capacity, day_of_week, start_period, end_period, teacher_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    public static final String INSERT_AUTH_CODE = "INSERT INTO auth_codes (code, course_id, is_used) VALUES (?, ?, 0)";
    
    public static final String INSERT_ANNOUNCEMENT = "INSERT INTO announcements (course_id, professor_id, title, content) " +
            "VALUES (?, ?, ?, ?)";

    public static final String INSERT_ENROLLMENT = "INSERT INTO enrollments (student_id, course_id) VALUES (?, ?)";
    
    public static final String INSERT_PENDING_ENROLLMENT = "INSERT INTO pending_enrollments (student_id, course_id) VALUES (?, ?)";

    // 查詢資料 SELECT
    public static final String FIND_STUDENT = "SELECT uid, name, password FROM students WHERE uid = ?";
    
    public static final String FIND_TEACHER = "SELECT uid, name, password FROM teachers WHERE uid = ?";
    
    public static final String FIND_ADMIN = "SELECT uid, name, password FROM admins WHERE uid = ?";

    public static final String SEARCH_COURSES = "SELECT c.* FROM courses c " +
            "JOIN teachers t ON c.teacher_id = t.uid " +
            "WHERE c.course_name LIKE ? OR c.course_id LIKE ? OR t.name LIKE ?";
    
    public static final String CHECK_AUTH_CODE = "SELECT course_id, is_used FROM auth_codes WHERE code = ?";
    
    public static final String GET_ALL_COURSES = "SELECT * FROM courses";
    
    public static final String GET_ALL_STUDENTS = "SELECT * FROM students";
    
    public static final String GET_ALL_TEACHERS = "SELECT * FROM teachers";
    
    public static final String GET_STUDENT_COURSES = "SELECT c.* FROM courses c JOIN enrollments e ON c.course_id = e.course_id WHERE e.student_id = ?";
    
    public static final String GET_STUDENT_GRADES = "SELECT c.*, e.score FROM courses c JOIN enrollments e ON c.course_id = e.course_id WHERE e.student_id = ?";
    
    public static final String GET_COURSE_STUDENTS_WITH_GRADES = "SELECT s.uid, s.name, s.password, e.score FROM students s JOIN enrollments e ON s.uid = e.student_id WHERE e.course_id = ?";
    
    public static final String GET_COURSE_ANNOUNCEMENTS = "SELECT a.*, c.course_name, t.name AS professor_name FROM announcements a " +
            "JOIN courses c ON a.course_id = c.course_id " +
            "LEFT JOIN teachers t ON a.professor_id = t.uid WHERE a.course_id = ? ORDER BY a.created_at DESC";
    
    public static final String INSERT_GENERAL_ANNOUNCEMENT = "INSERT INTO announcements (title, content, course_id, professor_id) " +
            "SELECT ?, ?, course_id, teacher_id FROM courses WHERE course_id = ? AND teacher_id = ?";
    
    public static final String GET_ALL_ANNOUNCEMENTS = "SELECT a.*, c.course_name, t.name AS professor_name FROM announcements a LEFT JOIN courses c ON a.course_id = c.course_id LEFT JOIN teachers t ON a.professor_id = t.uid ORDER BY a.created_at DESC";
    
    public static final String GET_ANNOUNCEMENTS_BY_PROFESSOR = "SELECT a.*, c.course_name, t.name AS professor_name FROM announcements a LEFT JOIN courses c ON a.course_id = c.course_id LEFT JOIN teachers t ON a.professor_id = t.uid WHERE a.professor_id = ? ORDER BY a.created_at DESC";
    
    public static final String GET_ANNOUNCEMENT_BY_ID = "SELECT a.*, c.course_name, t.name AS professor_name FROM announcements a LEFT JOIN courses c ON a.course_id = c.course_id LEFT JOIN teachers t ON a.professor_id = t.uid WHERE a.id = ?";
    
    public static final String GET_PENDING_STUDENTS = "SELECT student_id FROM pending_enrollments WHERE course_id = ?";
    
    public static final String GET_ENROLLED_STUDENTS = "SELECT student_id FROM enrollments WHERE course_id = ?";

    

    // 更新資料 UPDATE
    public static final String UPDATE_GRADE = "UPDATE enrollments SET score = ? WHERE student_id = ? AND course_id = ?";
    
    public static final String UPDATE_AUTH_CODE_USED = "UPDATE auth_codes SET is_used = 1, used_by = ? WHERE code = ?";
    
    public static final String UPDATE_ANNOUNCEMENT = "UPDATE announcements SET title = ?, content = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND professor_id = ?";

    // 刪除資料 DELETE
    public static final String DELETE_PENDING_ENROLLMENT = "DELETE FROM pending_enrollments WHERE student_id = ? AND course_id = ?";
    
    public static final String DELETE_ENROLLMENT = "DELETE FROM enrollments WHERE student_id = ? AND course_id = ?";
    
    public static final String DELETE_ANNOUNCEMENT = "DELETE FROM announcements WHERE id = ? AND professor_id = ?";
}

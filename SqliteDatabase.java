import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


public class SqliteDatabase {

    private static final String DB_URL = "jdbc:sqlite:school_system.db";
    private Connection connection;

    public SqliteDatabase() {
        connect();
        initializeTables();
    }

    // 基本連線設定
    private void connect() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("成功連線至 SQLite 資料庫！");
        } catch (SQLException e) {
            System.err.println("資料庫連線失敗: " + e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeTables() {
        if (connection == null) return;
        String createTeachersTable = "CREATE TABLE IF NOT EXISTS teachers (uid TEXT PRIMARY KEY, name TEXT NOT NULL, password TEXT NOT NULL);";
        String createStudentsTable = "CREATE TABLE IF NOT EXISTS students (uid TEXT PRIMARY KEY, name TEXT NOT NULL, password TEXT NOT NULL);";
        String createCoursesTable = "CREATE TABLE IF NOT EXISTS courses (course_id TEXT PRIMARY KEY, course_name TEXT NOT NULL, credits INTEGER NOT NULL, max_capacity INTEGER NOT NULL DEFAULT 50, day_of_week INTEGER NOT NULL, start_period INTEGER NOT NULL, end_period INTEGER NOT NULL, teacher_id TEXT NOT NULL, FOREIGN KEY (teacher_id) REFERENCES teachers(uid) ON UPDATE CASCADE ON DELETE RESTRICT);";
        String createEnrollmentsTable = "CREATE TABLE IF NOT EXISTS enrollments (student_id TEXT, course_id TEXT, score REAL, PRIMARY KEY (student_id, course_id), FOREIGN KEY (student_id) REFERENCES students(uid) ON UPDATE CASCADE ON DELETE CASCADE, FOREIGN KEY (course_id) REFERENCES courses(course_id) ON UPDATE CASCADE ON DELETE CASCADE);";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;"); // 開啟外鍵保護
            stmt.execute(createTeachersTable);
            stmt.execute(createStudentsTable);
            stmt.execute(createCoursesTable);
            stmt.execute(createEnrollmentsTable);
        } catch (SQLException e) {
            System.err.println(" 建表失敗: " + e.getMessage());
        }
    }


    // 2. 寫入資料 (INSERT)
    public void registerStudent(Student s) {
        String sql = "INSERT INTO students (uid, name, password) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, s.getUid());
            pstmt.setString(2, s.getName());
            pstmt.setString(3, s.getPassword());
            
            pstmt.executeUpdate(); // 把資料塞進資料庫
        } catch (SQLException e) {
            System.out.println(" 寫入學生失敗 (可能是帳號已存在): " + e.getMessage());
        }
    }

    public void registerTeacher(Teacher t) {
        String sql = "INSERT INTO teachers (uid, name, password) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, t.getUid());
            pstmt.setString(2, t.getName());
            pstmt.setString(3, t.getPassword());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(" 寫入教師失敗: " + e.getMessage());
        }
    }


    // 3. 查詢資料 (SELECT)
    public Student findStudent(String uid) {
        String sql = "SELECT uid, name, password FROM students WHERE uid = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uid);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) { // 如果有找到資料
                    // 把 SQL 裡的欄位拿出來，重新組裝成 Java 的 Student 物件
                    return new Student(rs.getString("uid"), rs.getString("name"), rs.getString("password"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // 找不到就回傳 null
    }

    public Teacher findTeacher(String uid) {
        String sql = "SELECT uid, name, password FROM teachers WHERE uid = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uid);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Teacher(rs.getString("uid"), rs.getString("name"), rs.getString("password"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    // 新增課程 (INSERT)
    public void addCourseToSystem(Course c) {
        String sql = "INSERT INTO courses (course_id, course_name, credits, max_capacity, day_of_week, start_period, end_period, teacher_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, c.getCourseId());
            pstmt.setString(2, c.getCourseName());
            pstmt.setInt(3, c.getCredits());
            pstmt.setInt(4, c.getMaxCapacity());
            
            // 把 TimeSlot 物件拆解成 3 個數字存入
            pstmt.setInt(5, c.getTimeSlot().getDayOfWeek());
            pstmt.setInt(6, c.getTimeSlot().getStartPeriod());
            pstmt.setInt(7, c.getTimeSlot().getEndPeriod());
            
            // 把 Teacher 物件轉換成 uid 字串存入 (這就是 Foreign Key 外鍵)
            pstmt.setString(8, c.getTeacher().getUid());
            
            pstmt.executeUpdate();
            System.out.println("課程 [" + c.getCourseName() + "] 已成功開課並寫入資料庫！");
            
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println("課程代碼 " + c.getCourseId() + " 已存在，略過新增。");
            } else {
                System.err.println("寫入課程失敗: " + e.getMessage());
            }
        }
    }


    // 撈取全校課程 (SELECT)
    public List<Course> getAllCourses() {
        List<Course> courseList = new ArrayList<>();
        String sql = "SELECT * FROM courses";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            // 使用 while 迴圈，把資料庫裡的課程「一筆一筆」讀出來
            while (rs.next()) {
                String cId = rs.getString("course_id");
                String cName = rs.getString("course_name");
                int credits = rs.getInt("credits");
                int maxCap = rs.getInt("max_capacity");
                
                // 1. 重新組裝 TimeSlot 物件
                int day = rs.getInt("day_of_week");
                int start = rs.getInt("start_period");
                int end = rs.getInt("end_period");
                TimeSlot ts = new TimeSlot(day, start, end);
                
                // 2. 重新撈取 Teacher 物件 (直接呼叫我們剛剛寫好的 findTeacher！)
                String tId = rs.getString("teacher_id");
                Teacher t = findTeacher(tId);
                
                // 3. 把所有零件組裝回 Course 物件，並加入到 List 裡
                Course course = new Course(cId, cName, credits, maxCap, ts, t);
                courseList.add(course);
            }
            
        } catch (SQLException e) {
            System.err.println("查詢課程失敗: " + e.getMessage());
        }
        
        return courseList;
    }


    // 學生選課 (寫入 enrollments 表)
    public void saveEnrollment(Student student, Course course) {
        // 只需要填入學號和課程代碼
        String sql = "INSERT INTO enrollments (student_id, course_id) VALUES (?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, student.getUid());
            pstmt.setString(2, course.getCourseId());
            
            pstmt.executeUpdate();
            System.out.println(" 選課成功！已將 [" + student.getName() + "] 加入 [" + course.getCourseName() + "]");
            
        } catch (SQLException e) {
            // 還記得我們設計的 PRIMARY KEY (student_id, course_id) 嗎？它在這裡發揮作用了！
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println(" 選課失敗：這名學生已經選過這門課了！");
            } else {
                System.err.println(" 選課發生異常: " + e.getMessage());
            }
        }
    }

    // 查詢特定學生的所有選課 (SELECT + INNER JOIN)
    public List<Course> getStudentCourses(String studentUid) {
        List<Course> myCourses = new ArrayList<>();
        
        // 從 courses 拿資料，條件為 enrollments 裡面的 course_id 等於 courses 的 course_id 和特定學號。
        String sql = "SELECT c.* FROM courses c " +
                    "JOIN enrollments e ON c.course_id = e.course_id " +
                    "WHERE e.student_id = ?";
                    
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, studentUid);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // 跟 getAllCourses 一樣的還原流程
                    String cId = rs.getString("course_id");
                    String cName = rs.getString("course_name");
                    int credits = rs.getInt("credits");
                    int maxCap = rs.getInt("max_capacity");
                    
                    TimeSlot ts = new TimeSlot(rs.getInt("day_of_week"), rs.getInt("start_period"), rs.getInt("end_period"));
                    Teacher t = findTeacher(rs.getString("teacher_id"));
                    
                    Course course = new Course(cId, cName, credits, maxCap, ts, t);
                    myCourses.add(course);
                }
            }
        } catch (SQLException e) {
            System.err.println(" 查詢課表失敗: " + e.getMessage());
        }
        
        return myCourses;
    }
    // 老師登記成績 (UPDATE)

    public boolean updateGrade(Student student, Course course, double score) {
        String sql = "UPDATE enrollments SET score = ? WHERE student_id = ? AND course_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, score);
            pstmt.setString(2, student.getUid());
            pstmt.setString(3, course.getCourseId());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println(" 已將 [" + student.getName() + "] 的 [" + course.getCourseName() + "] 成績登記為: " + score);
                return true;
            } else {
                System.out.println(" 找不到該學生的選課紀錄。");
                return false;
            }
        } catch (SQLException e) {
            System.err.println(" 成績登記失敗: " + e.getMessage());
            return false;
        }
    }


    // 取得學生的所有課程與對應成績 (回傳 Map)
    public Map<Course, Double> getStudentGradesMap(String studentUid) {
        Map<Course, Double> courseGrades = new HashMap<>();
        
        // 把課程資料 (c.*) 跟成績 (e.score) 一起撈出來
        String sql = "SELECT c.*, e.score FROM courses c " +
                    "JOIN enrollments e ON c.course_id = e.course_id " +
                    "WHERE e.student_id = ?";
                    
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, studentUid);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // 1. 還原 Course 物件 (這部分跟之前一樣)
                    String cId = rs.getString("course_id");
                    String cName = rs.getString("course_name");
                    int credits = rs.getInt("credits");
                    int maxCap = rs.getInt("max_capacity");
                    TimeSlot ts = new TimeSlot(rs.getInt("day_of_week"), rs.getInt("start_period"), rs.getInt("end_period"));
                    Teacher t = findTeacher(rs.getString("teacher_id"));
                    
                    Course course = new Course(cId, cName, credits, maxCap, ts, t);
                    
                    double score = rs.getDouble("score");
                    if (rs.wasNull()) {
                        // 如果資料庫裡真的是 NULL，我們存 null 到 Map 裡，前端才會顯示「尚未評分」
                        courseGrades.put(course, null);
                    } else {
                        courseGrades.put(course, score);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println(" 查詢成績表失敗: " + e.getMessage());
        }
        
        return courseGrades;
    }

    public void hydrateCourseStudents(Course course) {
        String sql = "SELECT s.uid, s.name, s.password, e.score FROM students s " +
                     "JOIN enrollments e ON s.uid = e.student_id " +
                     "WHERE e.course_id = ?";
                     
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, course.getCourseId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Student s = new Student(rs.getString("uid"), rs.getString("name"), rs.getString("password"));
                    
                    double score = rs.getDouble("score");
                    if (!rs.wasNull()) {
                        s.setGrade(course, score);
                    }
                    
                    course.addStudent(s);
                }
            }
        } catch (SQLException e) {
            System.err.println(" 撈取修課名單失敗: " + e.getMessage());
        }
    }
}
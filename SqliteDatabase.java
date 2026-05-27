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
    // private static final String DB_URL = "jdbc:sqlite:test_my_system.db";
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

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(SqlQueries.ENABLE_FOREIGN_KEYS);
            stmt.execute(SqlQueries.CREATE_ADMINS);
            stmt.execute(SqlQueries.CREATE_TEACHERS);
            stmt.execute(SqlQueries.CREATE_STUDENTS);
            stmt.execute(SqlQueries.CREATE_COURSES);
            stmt.execute(SqlQueries.CREATE_AUTH_CODES);
            stmt.execute(SqlQueries.CREATE_ANNOUNCEMENTS);
            stmt.execute(SqlQueries.CREATE_ENROLLMENTS);
            stmt.execute(SqlQueries.CREATE_PENDING);

            stmt.execute(SqlQueries.INSERT_DEFAULT_ADMIN);
            
        } catch (SQLException e) {
            System.err.println("建表失敗: " + e.getMessage());
        }
    }

    // 2. 寫入資料 (INSERT)
    public void registerStudent(Student s) {

        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.INSERT_STUDENT)) {
            pstmt.setString(1, s.getUid());
            pstmt.setString(2, s.getName());
            pstmt.setString(3, s.getPassword());

            pstmt.executeUpdate(); // 把資料塞進資料庫
            System.out.println("學生新增成功!");
        } catch (SQLException e) {
            System.out.println(" 寫入學生失敗 (可能是帳號已存在): " + e.getMessage());
        }
    }

    public void registerTeacher(Teacher t) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.INSERT_TEACHER)) {
            pstmt.setString(1, t.getUid());
            pstmt.setString(2, t.getName());
            pstmt.setString(3, t.getPassword());
            pstmt.executeUpdate();
            System.out.println("教師新增成功!");
        } catch (SQLException e) {
            System.out.println(" 寫入教師失敗: " + e.getMessage());
        }
    }

    public void addCourseToSystem(Course c) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.INSERT_COURSE)) {
            pstmt.setString(1, c.getCourseId());
            pstmt.setString(2, c.getCourseName());
            pstmt.setInt(3, c.getCredits());
            pstmt.setInt(4, c.getMaxCapacity());
            pstmt.setInt(5, c.getTimeSlot().getDayOfWeek());
            pstmt.setInt(6, c.getTimeSlot().getStartPeriod());
            pstmt.setInt(7, c.getTimeSlot().getEndPeriod());
            pstmt.setString(8, c.getTeacher().getUid());

            pstmt.executeUpdate();
            System.out.println(" 課程 [" + c.getCourseName() + "] 已成功開課並寫入資料庫！");
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println(" 課程代碼 " + c.getCourseId() + " 已存在，略過新增。");
            } else {
                System.err.println(" 寫入課程失敗: " + e.getMessage());
            }
        }
    }

    public void insertAuthCode(String courseId, String code) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.INSERT_AUTH_CODE)) {
            pstmt.setString(1, code);
            pstmt.setString(2, courseId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println(" 寫入密碼卡失敗: " + e.getMessage());
        }
    }

    public void addAnnouncement(String courseId, String title, String content) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.INSERT_ANNOUNCEMENT)) {
            pstmt.setString(1, courseId);
            pstmt.setString(2, title);
            pstmt.setString(3, content);
            pstmt.executeUpdate();
            System.out.println(" 公告 [" + title + "] 已成功發布！");
        } catch (SQLException e) {
            System.err.println(" 發布公告失敗: " + e.getMessage());
        }
    }

    public void saveEnrollment(Student student, Course course) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.INSERT_ENROLLMENT)) {
            pstmt.setString(1, student.getUid());
            pstmt.setString(2, course.getCourseId());
            pstmt.executeUpdate();
            System.out.println(" 選課成功！已將 [" + student.getName() + "] 加入 [" + course.getCourseName() + "]");
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println(" 選課失敗：這名學生已經選過這門課了！");
            } else {
                System.err.println(" 選課發生異常: " + e.getMessage());
            }
        }
    }

    public void savePendingEnrollment(Student student, Course course) throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.INSERT_PENDING_ENROLLMENT)) {
            pstmt.setString(1, student.getUid());
            pstmt.setString(2, course.getCourseId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique")) {
                throw new Exception("您已經登記過此課程，請等待抽籤結果。");
            } else {
                throw new Exception("資料庫異常: " + e.getMessage());
            }
        }
    }

    // 查詢資料 SELECT
    public Student findStudent(String uid) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.FIND_STUDENT)) {
            pstmt.setString(1, uid);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) { 
                    return new Student(rs.getString("uid"), rs.getString("name"), rs.getString("password"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // 找不到就回傳 null
    }

    public Teacher findTeacher(String uid) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.FIND_TEACHER)) {
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

    public Admin findAdmin(String uid) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.FIND_ADMIN)) {
            pstmt.setString(1, uid);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Admin(rs.getString("uid"), rs.getString("name"), rs.getString("password"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Course> getAllCourses() {
        List<Course> courseList = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.GET_ALL_COURSES);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                TimeSlot ts = new TimeSlot(rs.getInt("day_of_week"), rs.getInt("start_period"), rs.getInt("end_period"));
                Teacher t = findTeacher(rs.getString("teacher_id"));
                
                Course course = new Course(rs.getString("course_id"), rs.getString("course_name"), 
                                         rs.getInt("credits"), rs.getInt("max_capacity"), ts, t);
                
                loadEnrolledStudentsForCourse(course);
                loadPendingStudentsForCourse(course);
                courseList.add(course);
            }
        } catch (SQLException e) { System.err.println(" 查詢課程失敗: " + e.getMessage()); }
        return courseList;
    }

    // 課程搜尋系統
    public List<Course> searchCourses(String keyword) {
        List<Course> searchResults = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.SEARCH_COURSES)) {
            String searchPattern = "%" + keyword + "%";
            
            // 對應 SQL 裡面的三個問號 (名稱、代碼、老師名)
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TimeSlot ts = new TimeSlot(rs.getInt("day_of_week"), rs.getInt("start_period"), rs.getInt("end_period"));
                    Teacher t = findTeacher(rs.getString("teacher_id"));
                    
                    Course course = new Course(rs.getString("course_id"), rs.getString("course_name"), 
                                             rs.getInt("credits"), rs.getInt("max_capacity"), ts, t);
                    

                    loadEnrolledStudentsForCourse(course);
                    loadPendingStudentsForCourse(course);
                    
                    searchResults.add(course);
                }
            }
        } catch (SQLException e) { 
            System.err.println(" 搜尋課程失敗: " + e.getMessage()); 
        }
        
        return searchResults;
    }

    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.GET_ALL_STUDENTS);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Student(rs.getString("uid"), rs.getString("name"), rs.getString("password")));
            }
        } catch (SQLException e) { System.err.println(" 查詢學生失敗: " + e.getMessage()); }
        return list;
    }

    public List<Teacher> getAllTeachers() {
        List<Teacher> list = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.GET_ALL_TEACHERS);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Teacher(rs.getString("uid"), rs.getString("name"), rs.getString("password")));
            }
        } catch (SQLException e) { System.err.println(" 查詢教師失敗: " + e.getMessage()); }
        return list;
    }


    // 查詢特定學生的所有選課 (SELECT + INNER JOIN)
    public List<Course> getStudentCourses(String studentUid) {
        List<Course> myCourses = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.GET_STUDENT_COURSES)) {
            pstmt.setString(1, studentUid);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TimeSlot ts = new TimeSlot(rs.getInt("day_of_week"), rs.getInt("start_period"), rs.getInt("end_period"));
                    Teacher t = findTeacher(rs.getString("teacher_id"));
                    myCourses.add(new Course(rs.getString("course_id"), rs.getString("course_name"), 
                                           rs.getInt("credits"), rs.getInt("max_capacity"), ts, t));
                }
            }
        } catch (SQLException e) { System.err.println(" 查詢課表失敗: " + e.getMessage()); }
        return myCourses;
    }

    // 取得學生的所有課程與對應成績 (回傳 Map)
    public Map<Course, Double> getStudentGradesMap(String studentUid) {
        Map<Course, Double> courseGrades = new HashMap<>();
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.GET_STUDENT_GRADES)) {
            pstmt.setString(1, studentUid);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TimeSlot ts = new TimeSlot(rs.getInt("day_of_week"), rs.getInt("start_period"), rs.getInt("end_period"));
                    Teacher t = findTeacher(rs.getString("teacher_id"));
                    Course course = new Course(rs.getString("course_id"), rs.getString("course_name"), 
                                             rs.getInt("credits"), rs.getInt("max_capacity"), ts, t);
                    
                    double score = rs.getDouble("score");
                    courseGrades.put(course, rs.wasNull() ? null : score);
                }
            }
        } catch (SQLException e) { System.err.println(" 查詢成績表失敗: " + e.getMessage()); }
        return courseGrades;
    }

    public List<Announcement> getCourseAnnouncements(String courseId) {
        List<Announcement> announcements = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.GET_COURSE_ANNOUNCEMENTS)) {
            pstmt.setString(1, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Announcement a = new Announcement(
                        rs.getInt("id"),
                        rs.getString("course_id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("post_time")
                    );
                    announcements.add(a);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ 查詢公告失敗: " + e.getMessage());
        }
        return announcements;
    }

    public void hydrateCourseStudents(Course course) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.GET_COURSE_STUDENTS_WITH_GRADES)) {
            pstmt.setString(1, course.getCourseId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Student s = new Student(rs.getString("uid"), rs.getString("name"), rs.getString("password"));
                    double score = rs.getDouble("score");
                    if (!rs.wasNull()) s.setGrade(course, score);
                    course.addStudent(s);
                }
            }
        } catch (SQLException e) { System.err.println(" 撈取修課名單失敗: " + e.getMessage()); }
    }

    private void loadPendingStudentsForCourse(Course course) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.GET_PENDING_STUDENTS)) {
            pstmt.setString(1, course.getCourseId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Student s = findStudent(rs.getString("student_id"));
                if (s != null) course.addPendingStudent(s);
            }
        } catch (SQLException e) { System.err.println(" 讀取排隊名單失敗: " + e.getMessage()); }
    }

    private void loadEnrolledStudentsForCourse(Course course) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.GET_ENROLLED_STUDENTS)) {
            pstmt.setString(1, course.getCourseId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Student s = findStudent(rs.getString("student_id"));
                if (s != null) course.addStudent(s);
            }
        } catch (SQLException e) { System.err.println(" 讀取正式名單失敗: " + e.getMessage()); }
    }


    //  更新與刪除 UPDATE & DELETE
    public boolean updateGrade(Student student, Course course, double score) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.UPDATE_GRADE)) {
            pstmt.setDouble(1, score);
            pstmt.setString(2, student.getUid());
            pstmt.setString(3, course.getCourseId());
            if (pstmt.executeUpdate() > 0) {
                System.out.println(" 已將 [" + student.getName() + "] 的 [" + course.getCourseName() + "] 成績登記為: " + score);
                return true;
            }
            System.out.println(" 找不到該學生的選課紀錄。");
            return false;
        } catch (SQLException e) {
            System.err.println(" 成績登記失敗: " + e.getMessage());
            return false;
        }
    }

    public void verifyAndConsumeAuthCode(Student student, Course course, String inputCode) throws Exception {
        // 使用 SqlQueries 字典檔中的常數
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.CHECK_AUTH_CODE)) {
            pstmt.setString(1, inputCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    throw new Exception("找不到這組密碼卡，請確認是否輸入錯誤！");
                }
                
                String targetCourseId = rs.getString("course_id");
                if (!targetCourseId.equals(course.getCourseId())) {
                    throw new Exception("這張密碼卡不屬於這門課！");
                }
                
                int isUsed = rs.getInt("is_used");
                if (isUsed == 1) {
                    throw new Exception("這張密碼卡已經被用掉囉！");
                }
            }
        } catch (SQLException e) {
            throw new Exception("資料庫查詢異常: " + e.getMessage());
        }
        
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.UPDATE_AUTH_CODE_USED)) {
            pstmt.setString(1, student.getUid()); 
            pstmt.setString(2, inputCode);        
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("更新密碼卡狀態失敗: " + e.getMessage());
        }
    }

    public void deletePendingEnrollment(Student student, Course course) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.DELETE_PENDING_ENROLLMENT)) {
            pstmt.setString(1, student.getUid());
            pstmt.setString(2, course.getCourseId());
            pstmt.executeUpdate();
        } catch (SQLException e) { System.err.println(" 取消登記異常: " + e.getMessage()); }
    }

    public boolean deleteEnrollment(Student student, Course course) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.DELETE_ENROLLMENT)) {
            pstmt.setString(1, student.getUid());
            pstmt.setString(2, course.getCourseId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(" 退選異常: " + e.getMessage());
            return false;
        }
    }
}
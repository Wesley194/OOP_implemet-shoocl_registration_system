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

    private void connect() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL;"); 
                stmt.execute("PRAGMA busy_timeout=5000;"); // 遇到別人佔用時，等待 5 秒鐘
            }
            
            System.out.println("Successfully connected to SQLite database!");
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
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
            stmt.execute(SqlQueries.CREATE_SYSTEM_SETTINGS);
            stmt.execute(SqlQueries.CREATE_COURSES);
            stmt.execute(SqlQueries.CREATE_AUTH_CODES);
            stmt.execute(SqlQueries.CREATE_ANNOUNCEMENTS);
            stmt.execute(SqlQueries.CREATE_ENROLLMENTS);
            stmt.execute(SqlQueries.CREATE_PENDING);

            stmt.execute(SqlQueries.INSERT_DEFAULT_ADMIN);
            stmt.execute(SqlQueries.INSERT_DEFAULT_PHASE);
            
        } catch (SQLException e) {
            System.err.println("Table creation failed: " + e.getMessage());
        }
    }


    // 2. 寫入資料 (INSERT)
    public void registerStudent(Student s) {

        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.INSERT_STUDENT)) {
            pstmt.setString(1, s.getUid());
            pstmt.setString(2, s.getName());
            pstmt.setString(3, s.getPassword());

            pstmt.executeUpdate();
            System.out.println("Student added successfully!");
        } catch (SQLException e) {
            System.out.println("Failed to add student (account may already exist): " + e.getMessage());
        }
    }

    public void registerTeacher(Teacher t) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.INSERT_TEACHER)) {
            pstmt.setString(1, t.getUid());
            pstmt.setString(2, t.getName());
            pstmt.setString(3, t.getPassword());
            pstmt.executeUpdate();
            System.out.println("Teacher added successfully!");
        } catch (SQLException e) {
            System.out.println("Failed to add teacher: " + e.getMessage());
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
            System.out.println("Course [" + c.getCourseName() + "] successfully created and saved to database!");
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println("Course ID " + c.getCourseId() + " already exists, skipping.");
            } else {
                System.err.println("Failed to add course: " + e.getMessage());
            }
        }
    }

    public void insertAuthCode(String courseId, String code) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.INSERT_AUTH_CODE)) {
            pstmt.setString(1, code);
            pstmt.setString(2, courseId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to add auth code: " + e.getMessage());
        }
    }

    public void addAnnouncement(String courseId, String professorId, String title, String content) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.INSERT_ANNOUNCEMENT)) {
            pstmt.setString(1, courseId);
            pstmt.setString(2, professorId);
            pstmt.setString(3, title);
            pstmt.setString(4, content);
            pstmt.executeUpdate();
            System.out.println(" Announcement <" + title + "> successfully published!");
        } catch (SQLException e) {
            System.err.println("Failed to publish announcement: " + e.getMessage());
        }
    }

    public void saveEnrollment(Student student, Course course) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.INSERT_ENROLLMENT)) {
            pstmt.setString(1, student.getUid());
            pstmt.setString(2, course.getCourseId());
            pstmt.executeUpdate();
            System.out.println("Enrollment successful! Added " + student.getName() + " to " + course.getCourseName() );
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println("Enrollment failed: This student is already enrolled in this course!");
            } else {
                System.err.println("Enrollment error: " + e.getMessage());
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
                throw new Exception("You have already registered for this course. Please wait for the lottery results.");
            } else {
                throw new Exception("Database error: " + e.getMessage());
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

    public String getSystemPhase() {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.GET_SYSTEM_PHASE);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getString("setting_value");
            }
        } catch (SQLException e) {
            System.err.println("Failed to get system phase: " + e.getMessage());
        }
        return "CLOSED";
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
        } catch (SQLException e) { System.err.println("Failed to query courses: " + e.getMessage()); }
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
            System.err.println("Failed to search courses: " + e.getMessage()); 
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
        } catch (SQLException e) { System.err.println("Failed to query students: " + e.getMessage()); }
        return list;
    }

    public List<Teacher> getAllTeachers() {
        List<Teacher> list = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.GET_ALL_TEACHERS);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Teacher(rs.getString("uid"), rs.getString("name"), rs.getString("password")));
            }
        } catch (SQLException e) { System.err.println("Failed to query teachers: " + e.getMessage()); }
        return list;
    }

    public List<AuthCode> getCourseAuthCodes(String courseId) {
        List<AuthCode> authCodes = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.GET_COURSE_AUTH_CODES)) {
            pstmt.setString(1, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AuthCode ac = new AuthCode(
                        rs.getString("code"),
                        rs.getInt("is_used") == 1, // 將 SQLite 的 1/0 轉換為 boolean
                        rs.getString("used_by")
                    );
                    authCodes.add(ac);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to query auth codes: " + e.getMessage());
        }
        return authCodes;
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
        } catch (SQLException e) { System.err.println("Failed to query schedule: " + e.getMessage()); }
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
        } catch (SQLException e) { System.err.println("Failed to query grades: " + e.getMessage()); }
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
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("course_id"),
                        rs.getString("course_name"),
                        rs.getString("professor_id"),
                        rs.getString("professor_name"),
                        rs.getString("created_at"),
                        rs.getString("updated_at")
                    );
                    announcements.add(a);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to query announcements: " + e.getMessage());
        }
        return announcements;
    }

    public boolean createAnnouncement(Announcement announcement) {
        if (isBlank(announcement.getTitle()) || isBlank(announcement.getContent())
                || isBlank(announcement.getCourseId()) || isBlank(announcement.getProfessorId())) {
            return false;
        }
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.INSERT_GENERAL_ANNOUNCEMENT,
                Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, announcement.getTitle().trim());
            pstmt.setString(2, announcement.getContent().trim());
            pstmt.setString(3, announcement.getCourseId());
            pstmt.setString(4, announcement.getProfessorId());
            if (pstmt.executeUpdate() > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        announcement.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Announcement creation failed: " + e.getMessage());
        }
        return false;
    }

    public List<Announcement> getAllAnnouncements() {
        List<Announcement> announcements = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.GET_ALL_ANNOUNCEMENTS);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                announcements.add(mapAnnouncement(rs));
            }
        } catch (SQLException e) {
            System.err.println("Announcement query failed: " + e.getMessage());
        }
        return announcements;
    }

    public List<Announcement> getAnnouncementsByProfessor(String professorId) {
        List<Announcement> announcements = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.GET_ANNOUNCEMENTS_BY_PROFESSOR)) {
            pstmt.setString(1, professorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    announcements.add(mapAnnouncement(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Professor announcement query failed: " + e.getMessage());
        }
        return announcements;
    }

    public List<Announcement> getAnnouncementsByProfessor(int professorId) {
        return getAnnouncementsByProfessor(String.valueOf(professorId));
    }

    public Announcement getAnnouncementById(int announcementId) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.GET_ANNOUNCEMENT_BY_ID)) {
            pstmt.setInt(1, announcementId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapAnnouncement(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Announcement lookup failed: " + e.getMessage());
        }
        return null;
    }

    public boolean updateAnnouncement(Announcement announcement) {
        if (isBlank(announcement.getTitle()) || isBlank(announcement.getContent())
                || isBlank(announcement.getProfessorId())) {
            return false;
        }
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.UPDATE_ANNOUNCEMENT)) {
            pstmt.setString(1, announcement.getTitle().trim());
            pstmt.setString(2, announcement.getContent().trim());
            pstmt.setInt(3, announcement.getId());
            pstmt.setString(4, announcement.getProfessorId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Announcement update failed: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteAnnouncement(int announcementId, String professorId) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.DELETE_ANNOUNCEMENT)) {
            pstmt.setInt(1, announcementId);
            pstmt.setString(2, professorId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Announcement deletion failed: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteAnnouncement(int announcementId, int professorId) {
        return deleteAnnouncement(announcementId, String.valueOf(professorId));
    }

    private Announcement mapAnnouncement(ResultSet rs) throws SQLException {
        return new Announcement(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getString("course_id"),
                rs.getString("course_name"),
                rs.getString("professor_id"),
                rs.getString("professor_name"),
                rs.getString("created_at"),
                rs.getString("updated_at"));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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
        } catch (SQLException e) { System.err.println("Failed to load enrolled students: " + e.getMessage()); }
    }

    private void loadPendingStudentsForCourse(Course course) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.GET_PENDING_STUDENTS)) {
            pstmt.setString(1, course.getCourseId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Student s = findStudent(rs.getString("student_id"));
                if (s != null) course.addPendingStudent(s);
            }
        } catch (SQLException e) { System.err.println("Failed to load pending students: " + e.getMessage()); }
    }

    private void loadEnrolledStudentsForCourse(Course course) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.GET_ENROLLED_STUDENTS)) {
            pstmt.setString(1, course.getCourseId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Student s = findStudent(rs.getString("student_id"));
                if (s != null) course.addStudent(s);
            }
        } catch (SQLException e) { System.err.println("Failed to load enrolled students: " + e.getMessage()); }
    }


    //  更新與刪除 UPDATE & DELETE
    public void updateSystemPhase(String phaseStr) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.UPDATE_SYSTEM_PHASE)) {
            pstmt.setString(1, phaseStr);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update system phase: " + e.getMessage());
        }
    }

    public boolean updateGrade(Student student, Course course, double score) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.UPDATE_GRADE)) {
            pstmt.setDouble(1, score);
            pstmt.setString(2, student.getUid());
            pstmt.setString(3, course.getCourseId());
            if (pstmt.executeUpdate() > 0) {
                System.out.println(" Registered grade for " + student.getName() + " in " + course.getCourseName() + "] 成績登記為: " + score);
                return true;
            }
            System.out.println("Enrollment record not found for this student.");
            return false;
        } catch (SQLException e) {
            System.err.println("Failed to register grade: " + e.getMessage());
            return false;
        }
    }

    public void verifyAndConsumeAuthCode(Student student, Course course, String inputCode) throws Exception {
        // 使用 SqlQueries 字典檔中的常數
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.CHECK_AUTH_CODE)) {
            pstmt.setString(1, inputCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    throw new Exception("Auth code not found. Please check your input!");
                }
                
                String targetCourseId = rs.getString("course_id");
                if (!targetCourseId.equals(course.getCourseId())) {
                    throw new Exception("This auth code does not belong to this course!");
                }
                
                int isUsed = rs.getInt("is_used");
                if (isUsed == 1) {
                    throw new Exception("This auth code has already been used!");
                }
            }
        } catch (SQLException e) {
            throw new Exception("Database query error: " + e.getMessage());
        }
        
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.UPDATE_AUTH_CODE_USED)) {
            pstmt.setString(1, student.getUid()); 
            pstmt.setString(2, inputCode);        
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Failed to update auth code status: " + e.getMessage());
        }
    }

    public void deletePendingEnrollment(Student student, Course course) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.DELETE_PENDING_ENROLLMENT)) {
            pstmt.setString(1, student.getUid());
            pstmt.setString(2, course.getCourseId());
            pstmt.executeUpdate();
        } catch (SQLException e) { System.err.println("Failed to cancel pending enrollment: " + e.getMessage()); }
    }

    public boolean deleteEnrollment(Student student, Course course) {
        try (PreparedStatement pstmt = connection.prepareStatement(SqlQueries.DELETE_ENROLLMENT)) {
            pstmt.setString(1, student.getUid());
            pstmt.setString(2, course.getCourseId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to drop course: " + e.getMessage());
            return false;
        }
    }
}

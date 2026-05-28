public class Announcement {
    private int id;
    private String title;
    private String content;
    private String courseId;
    private String courseName;
    private String professorId;
    private String professorName;
    private String createdAt;
    private String updatedAt;

    public Announcement(String title, String content, String professorId) {
        this(0, title, content, null, null, professorId, null, null, null);
    }

    public Announcement(String title, String content, String courseId, String professorId) {
        this(0, title, content, courseId, null, professorId, null, null, null);
    }

    public Announcement(int id, String title, String content, String professorId, String createdAt, String updatedAt) {
        this(id, title, content, null, null, professorId, null, createdAt, updatedAt);
    }

    public Announcement(int id, String title, String content, String professorId, String professorName,
            String createdAt, String updatedAt) {
        this(id, title, content, null, null, professorId, professorName, createdAt, updatedAt);
    }

    public Announcement(int id, String title, String content, String courseId, String courseName,
            String professorId, String professorName, String createdAt, String updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.courseId = courseId;
        this.courseName = courseName;
        this.professorId = professorId;
        this.professorName = professorName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Backward-compatible constructor for older course announcement code.
    public Announcement(int id, String courseId, String title, String content, String postTime) {
        this(id, title, content, courseId, null, postTime, postTime);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getProfessorId() { return professorId; }
    public void setProfessorId(String professorId) { this.professorId = professorId; }
    public String getProfessorName() { return professorName; }
    public void setProfessorName(String professorName) { this.professorName = professorName; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return id + " - " + title;
    }
}

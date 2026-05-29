import javax.swing.JFrame;
public interface MainFrameController {
    void switchToPanel(String panelName);
    SqliteDatabase getDatabase();
    RegistrationSystem getSystem();
    Student getCurrentStudent();
    void setCurrentStudent(Student student);
    Teacher getCurrentTeacher();
    void setCurrentTeacher(Teacher teacher);
    Admin getCurrentAdmin();
    void setCurrentAdmin(Admin admin);
    void logout();
    JFrame getFrame();
}
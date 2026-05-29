import javax.swing.JFrame;
public interface MainFrameController {
    void switchToPanel(String panelName);
    SqliteDatabase getDatabase();
    RegistrationSystem getSystem();
    Student getCurrentStudent();
    void setCurrentStudent(Student student);
    Teacher getCurrentTeacher();
    void setCurrentTeacher(Teacher teacher);
    void logout();
    JFrame getFrame();
}
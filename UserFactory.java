public class UserFactory {
    // 根據傳入的角色字串，自動產生對應的物件
    public static User createUser(String role, String uid, String name, String password) {
        if (role.equalsIgnoreCase("Student")) {
            return new Student(uid, name, password);
        } else if (role.equalsIgnoreCase("Teacher")) {
            return new Teacher(uid, name, password);
        } else if (role.equalsIgnoreCase("Admin")) {
            return new Admin(uid, name, password);
        }
        throw new IllegalArgumentException("未知的角色類型");
    }
}
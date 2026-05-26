public class Admin extends User {
    public Admin(String uid, String name, String password) {
        super(uid, name, password);
    }

    @Override
    public String getRole() {
        return "Admin";
    }
}

public abstract class User {
    protected String uid;
    protected String name;
    protected String password;

    public User(String uid, String name, String password) {
        this.uid = uid;
        this.name = name;
        this.password = password;
    }

    public boolean verifyPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    public String getUid() { return uid; }
    public String getName() { return name; }
    public abstract String getRole();
}
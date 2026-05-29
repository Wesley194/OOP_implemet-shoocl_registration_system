// 所有使用者共用的基本資料與登入驗證。
public abstract class User {
    protected String uid;
    protected String name;
    protected String password;

    // 建立使用者帳號。
    public User(String uid, String name, String password) {
        this.uid = uid;
        this.name = name;
        this.password = password;
    }

    // 驗證登入密碼。
    public boolean verifyPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    public String getPassword() {
        return password;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public abstract String getRole();

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

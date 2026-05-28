public class AuthCode {
    private String code;
    private boolean isUsed;
    private String usedBy; // 如果還沒被使用，這個值會是 null

    public AuthCode(String code, boolean isUsed, String usedBy) {
        this.code = code;
        this.isUsed = isUsed;
        this.usedBy = usedBy;
    }

    public String getCode() { return code; }
    public boolean isUsed() { return isUsed; }
    public String getUsedBy() { return usedBy; }
}

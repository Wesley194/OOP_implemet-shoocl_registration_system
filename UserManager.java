public class UserManager {
    
    private SqliteDatabase db;

    public UserManager(SqliteDatabase db) {
        this.db = db;
    }

    public boolean updateName(User currentUser, String currentPasswordInput, String newName) {
        // 驗證舊密碼
        if (!currentUser.verifyPassword(currentPasswordInput)) {
            System.out.println("Error: Current password is incorrect!");
            return false;
        }

        // 名字不能為空
        if (newName == null || newName.trim().isEmpty()) {
            System.out.println("Error: Name cannot be empty!");
            return false;
        }

        return db.updateUserName(currentUser, newName);
    }

    // 處理更改密碼的邏輯
    public boolean updatePassword(User currentUser, String currentPasswordInput, String newPassword, String confirmNewPassword) {
        // 驗證舊密碼
        if (!currentUser.verifyPassword(currentPasswordInput)) {
            System.out.println("Error: Current password is incorrect!");
            return false;
        }

        // 新密碼兩次輸入必須一致
        if (!newPassword.equals(confirmNewPassword)) {
            System.out.println("Error: New passwords do not match!");
            return false;
        }

        // 新密碼不能為空，且不能跟舊密碼一樣
        if (newPassword == null || newPassword.trim().isEmpty()) {
            System.out.println("Error: Password cannot be empty!");
            return false;
        }
        if (currentPasswordInput.equals(newPassword)) {
            System.out.println("Error: New password cannot be the same as the current password!");
            return false;
        }

        return db.updateUserPassword(currentUser, newPassword);
    }
}
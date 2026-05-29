import java.util.Scanner;

public class UserManagerTester {
    public static void main(String[] args) {
        
        SqliteDatabase db = new SqliteDatabase();
        UserManager userManager = new UserManager(db);
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== User Profile Management Tester ===");

        while (true) {
            System.out.println("\n--- System Login ---");
            System.out.print("Enter ID: ");
            String uid = scanner.nextLine();
            System.out.print("Enter Password: ");
            String pwd = scanner.nextLine();

            
            User currentUser = null;
            
            Admin admin = db.findAdmin(uid);
            Teacher teacher = db.findTeacher(uid);
            Student student = db.findStudent(uid);

            if (admin != null) currentUser = admin;
            else if (teacher != null) currentUser = teacher;
            else if (student != null) currentUser = student;

            // 驗證密碼
            if (currentUser != null && currentUser.verifyPassword(pwd)) {
                System.out.println("\nLogin successful! Welcome, " + currentUser.getName() + " (" + currentUser.getRole() + ")");
                runProfileMenu(scanner, userManager, currentUser);
            } else {
                System.out.println("Login failed. Invalid ID or password!");
            }
        }
    }

    // ================= 個人資料管理介面 =================
    private static void runProfileMenu(Scanner scanner, UserManager userManager, User currentUser) {
        while (true) {
            System.out.println("\n=== Profile Menu ===");
            System.out.println("1. View Profile");
            System.out.println("2. Change Name");
            System.out.println("3. Change Password");
            System.out.println("4. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            if (choice.equals("1")) {
                System.out.println("\n--- My Profile ---");
                System.out.println("ID: " + currentUser.getUid());
                System.out.println("Name: " + currentUser.getName());
                System.out.println("Role: " + currentUser.getRole());
                
            } else if (choice.equals("2")) {
                System.out.println("\n--- Change Name ---");
                System.out.print("Enter current password to verify: ");
                String currentPwd = scanner.nextLine();

                if (!currentUser.verifyPassword(currentPwd)) {
                    System.out.println(" Error: Incorrect current password! Action canceled.");
                    continue;
                }
                
                System.out.print("Enter NEW name: ");
                String newName = scanner.nextLine();

                boolean success = userManager.updateName(currentUser, currentPwd, newName);
                if (success) {
                    System.out.println("Name successfully changed to: " + currentUser.getName());
                }

            } else if (choice.equals("3")) {
                System.out.println("\n--- Change Password ---");
                System.out.print("Enter current password: ");
                String currentPwd = scanner.nextLine();
                
                if (!currentUser.verifyPassword(currentPwd)) {
                    System.out.println(" Error: Incorrect current password! Action canceled.");
                    continue; 
                }

                System.out.print("Enter NEW password: ");
                String newPwd = scanner.nextLine();
                
                System.out.print("Confirm NEW password: ");
                String confirmPwd = scanner.nextLine();

                boolean success = userManager.updatePassword(currentUser, currentPwd, newPwd, confirmPwd);
                if (success) {
                    System.out.println("Password successfully changed!");
                }

            } else if (choice.equals("4")) {
                System.out.println("Logging out...");
                break;
            } else {
                System.out.println("Invalid option. Please try again.");
            }
        }
    }
}
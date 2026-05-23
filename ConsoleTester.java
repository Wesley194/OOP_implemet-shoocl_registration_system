import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ConsoleTester {
    public static void main(String[] args) {
        // 系統初始化
        SqliteDatabase db = new SqliteDatabase();
        RegistrationSystem system = new RegistrationSystem(db);

        // 啟動終端機
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== 學校行政管理系統 ===");

        while (true) {
            System.out.println("\n--- 系統登入 ---");
            System.out.print("輸入帳號: ");
            String uid = scanner.nextLine();
            System.out.print("輸入密碼: ");
            String pwd = scanner.nextLine();

            Teacher currentTeacher = db.findTeacher(uid);
            Student currentStudent = db.findStudent(uid);

            if (currentTeacher != null && currentTeacher.verifyPassword(pwd)) {
                System.out.println("\n 教授 登入成功！" + currentTeacher.getName());
                runTeacherMenu(scanner, system, currentTeacher);
            } else if (currentStudent != null && currentStudent.verifyPassword(pwd)) {
                System.out.println("\n 學生 登入成功！" + currentStudent.getName());
                runStudentMenu(scanner, system, currentStudent, db);
            } else {
                System.out.println(" 帳號或密碼錯誤，請重新輸入！");
            }
        }
    }

    // =================老師介面=================
    private static void runTeacherMenu(Scanner scanner, RegistrationSystem system, Teacher teacher) {
        while (true) {
            System.out.println("\n=== 教授功能選單 ===");
            System.out.println("1. 新增課程");
            System.out.println("2. 查看開課清單與登記成績");
            System.out.println("3. 登出系統");
            System.out.print("請選擇功能: ");
            
            String choice = scanner.nextLine();

            if (choice.equals("1")) {
                System.out.println("\n--- 新增課程 ---");
                System.out.print("課程代碼 (例: CS102): ");
                String courseId = scanner.nextLine();
                System.out.print("課程名稱: ");
                String courseName = scanner.nextLine();
                System.out.print("學分數: ");
                int credits = Integer.parseInt(scanner.nextLine());
                
                System.out.print("上課星期 (1~5): ");
                int day = Integer.parseInt(scanner.nextLine());
                System.out.print("開始節次 (例如 2): ");
                int start = Integer.parseInt(scanner.nextLine());
                System.out.print("結束節次 (例如 4): ");
                int end = Integer.parseInt(scanner.nextLine());
                
                TimeSlot time = new TimeSlot(day, start, end); 
                system.createCourse(teacher, courseId, courseName, credits, 50, time);
                System.out.println("✅ 課程 [" + courseName + "] 新增成功！");

            } else if (choice.equals("2")) {
                System.out.println("\n--- 我的開課清單 ---");
                List<Course> myCourses = teacher.getTeachingCourses();
                if (myCourses.isEmpty()) {
                    System.out.println("您目前沒有開設任何課程。");
                    continue;
                }

                for (int i = 0; i < myCourses.size(); i++) {
                    Course c = myCourses.get(i);
                    System.out.println((i + 1) + ". " + c.getCourseName() + " (代碼: " + c.getCourseId() + ")");
                }
                System.out.print("請輸入要管理的課程編號 (例如 1): ");
                int courseIndex = Integer.parseInt(scanner.nextLine()) - 1;
                
                if (courseIndex >= 0 && courseIndex < myCourses.size()) {
                    Course selectedCourse = myCourses.get(courseIndex);
                    manageCourseGrades(scanner, system, teacher, selectedCourse);
                } else {
                    System.out.println(" 輸入無效！");
                }
            } else if (choice.equals("3")) {
                System.out.println(" 登出成功。");
                break; 
            } else {
                System.out.println(" 無效的選項。");
            }
        }
    }

    private static void manageCourseGrades(Scanner scanner, RegistrationSystem system, Teacher teacher, Course course) {
        System.out.println("\n--- [" + course.getCourseName() + "] 學生名單與成績 ---");
        List<Student> students = course.getEnrolledStudents();

        if (students.isEmpty()) {
            System.out.println("目前沒有學生選修這門課。");
            return;
        }

        for (int i = 0; i < students.size(); i++) {
            Student s = students.get(i);
            Double currentScore = s.getCourseGrades().get(course);
            String scoreDisplay = (currentScore == null) ? "尚未評分" : currentScore.toString() + " 分";
            System.out.println((i + 1) + ". " + s.getName() + " (" + s.getUid() + ") - 目前成績: " + scoreDisplay);
        }

        System.out.print("\n要為哪位學生登記成績？(輸入學生編號，例如 1，或輸入 0 返回): ");
        int studentIndex = Integer.parseInt(scanner.nextLine()) - 1;

        // 這是正確的老師打分數邏輯
        if (studentIndex >= 0 && studentIndex < students.size()) {
            Student targetStudent = students.get(studentIndex);
            System.out.print("請輸入 [" + targetStudent.getName() + "] 的分數 (0~100): ");
            double score = Double.parseDouble(scanner.nextLine());

            boolean success = system.gradeStudent(teacher, targetStudent, course, score);
            if (success) {
                System.out.println(" 成績登記成功！");
            } else {
                System.out.println(" 系統拒絕登記。");
            }
        }
    }

    // =================學生介面=================
    private static void runStudentMenu(Scanner scanner, RegistrationSystem system, Student student, SqliteDatabase db) {
        while (true) {
            System.out.println("\n=== 學生功能選單 ===");
            System.out.println("1. 瀏覽全校課程與選課");
            System.out.println("2. 查看我的課表與成績 (包含 GPA)");
            System.out.println("3. 登出系統");
            System.out.print("請選擇功能: ");

            String choice = scanner.nextLine();

            if (choice.equals("1")) {
                System.out.println("\n--- 全校課程總表 ---");
                List<Course> allCourses = db.getAllCourses();
                if (allCourses.isEmpty()) {
                    System.out.println("目前全校尚無開課資訊。");
                    continue;
                }

                for (int i = 0; i < allCourses.size(); i++) {
                    Course c = allCourses.get(i);
                    System.out.println((i + 1) + ". " + c.getCourseName() + " (代碼: " + c.getCourseId() + " | 老師: " + c.getTeacher().getName() + " | 時間: " + c.getTimeSlot() + ")");
                }
                
                System.out.print("請輸入欲選修的課程編號 (輸入 0 返回): ");
                int courseIndex = Integer.parseInt(scanner.nextLine()) - 1;
                
                if (courseIndex >= 0 && courseIndex < allCourses.size()) {
                    Course selectedCourse = allCourses.get(courseIndex);
                    
                    // 這是升級過後的 try-catch 選課邏輯
                    try {
                        system.enroll(student, selectedCourse);
                        System.out.println(" 選課成功！已將 [" + selectedCourse.getCourseName() + "] 加入您的課表。");
                    } catch (Exception e) {
                        // 精準捕捉並印出 RegistrationSystem 拋出的錯誤訊息
                        System.out.println(" " + e.getMessage());
                    }
                }
            } else if (choice.equals("2")) {
                System.out.println("\n--- 我的課表與歷年成績 ---");
                List<Course> myCourses = student.getMyCourses();
                Map<Course, Double> grades = student.getCourseGrades();

                if (myCourses.isEmpty()) {
                    System.out.println("您目前沒有選修任何課程。");
                } else {
                    for (Course c : myCourses) {
                        Double score = grades.get(c);
                        String scoreDisplay = (score == null) ? "老師尚未評分" : score.toString() + " 分";
                        System.out.println("- " + c.getCourseName() + " (" + c.getCredits() + "學分) | 成績: " + scoreDisplay);
                    }
                    System.out.println("===========================");
                    System.out.println(" 您目前的累計 GPA 為: " + system.calculateGPA(student));
                }
            } else if (choice.equals("3")) {
                System.out.println(" 登出成功。");
                break;
            } else {
                System.out.println(" 無效的選項。");
            }
        }
    }
}
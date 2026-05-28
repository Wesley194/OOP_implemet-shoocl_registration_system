import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DatabaseTester {
    public static void main(String[] args) {
        SqliteDatabase db = new SqliteDatabase();
        Random rand = new Random();

        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Garcia", "Wilson", "Taylor", "Anderson", "Thomas", "Moore", "Martin", "Jackson", "White", "Harris", "Clark", "Lewis", "Walker"};
        String[] firstNames = {"James", "John", "Robert", "Michael", "William", "David", "Richard", "Charles", "Joseph", "Thomas", "Mary", "Patricia", "Jennifer", "Linda", "Elizabeth", "Barbara", "Susan", "Jessica", "Sarah", "Karen"};
        
        // 生成教授
        Teacher[] teachers = new Teacher[5];
        for (int i = 0; i < 5; i++) {
            String lastName = lastNames[rand.nextInt(lastNames.length)];
            String firstName = firstNames[rand.nextInt(firstNames.length)];
            
            String fullName = firstName + " " + lastName;
            teachers[i] = new Teacher("T00" + (i + 1), fullName, "1234");
            db.registerTeacher(teachers[i]);
        }

        // 生成學生
        Student[] students = new Student[15];
        for (int i = 0; i < 15; i++) {

            String lastName = lastNames[rand.nextInt(lastNames.length)];
            String firstName = firstNames[rand.nextInt(firstNames.length)];
            
            String fullName = firstName + " " + lastName;

            String uid = String.format("B%03d", i + 1); 
            students[i] = new Student(uid, fullName, "0000");
            db.registerStudent(students[i]);
        }

        // 生成課程
        Course[] courses = new Course[5];
        courses[0] = new Course("CS170", "Digital Electronics", 3, 50, new TimeSlot(1, 2, 4), teachers[0]);
        courses[1] = new Course("CS215", "Data Structures", 3, 50, new TimeSlot(2, 5, 7), teachers[1]);
        courses[2] = new Course("CS260", "Computer Organization", 3, 50, new TimeSlot(3, 2, 4), teachers[2]);
        courses[3] = new Course("CS280", "Probability", 3, 50, new TimeSlot(4, 6, 8), teachers[3]);
        courses[4] = new Course("CS391", "OOP", 3, 50, new TimeSlot(5, 2, 4), teachers[4]);
        
        for (Course c : courses) {
            db.addCourseToSystem(c);
        }

        // 生成密碼卡
        for (Course c : courses) {
            System.out.print(" [" + c.getCourseName() + "] Auth Code: ");
            for (int i = 0; i < 5; i++) {
                String authCode = generateRandomCode(6);
                db.insertAuthCode(c.getCourseId(), authCode);
                System.out.print(authCode + "  ");
            }
            System.out.println();
            
            db.addAnnouncement(c.getCourseId(), "Welcome", "Please buy the textbook");
        }

        for (Student s : students) {
            // 每個學生隨機選 2~4 門課
            int enrollCount = rand.nextInt(3) + 2; 
            

            List<Course> shuffledCourses = Arrays.asList(courses.clone());
            Collections.shuffle(shuffledCourses, rand);
            
            for (int j = 0; j < enrollCount; j++) {
                Course randomCourse = shuffledCourses.get(j);
                
                // 模擬 50% 機率是「直接選上」，50% 機率是「進入抽籤排隊」
                if (rand.nextBoolean()) {
                    db.saveEnrollment(s, randomCourse);
                    
                } else {
                    try {
                        db.savePendingEnrollment(s, randomCourse);
                    } catch (Exception e) {

                    }
                }
            }
        }

        System.out.println("   testing account prompt:");
        System.out.println("   admin     | admin (passoword admin123)");
        System.out.println("   professor | T001 ~ T005 (passoword 1234)");
        System.out.println("   student   | B001 ~ B015 (passoword 0000)");
        
        db.closeConnection();
    }
    private static String generateRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
    
}
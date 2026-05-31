import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DatabaseTester {
    public static void main(String[] args) {
        SqliteDatabase db = new SqliteDatabase();
        Random rand = new Random();

        String[] lastNames = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Garcia", "Wilson", "Taylor", 
            "Anderson", "Thomas", "Moore", "Martin", "Jackson", "White", "Harris", "Clark", "Lewis", "Walker",
            "Hall", "Allen", "Young", "Hernandez", "King", "Wright", "Lopez", "Hill", "Scott", "Green",
            "Adams", "Baker", "Gonzalez", "Nelson", "Carter", "Mitchell", "Perez", "Roberts", "Turner", "Phillips"
        };
        
        String[] firstNames = {
            "James", "John", "Robert", "Michael", "William", "David", "Richard", "Charles", "Joseph", "Thomas", 
            "Mary", "Patricia", "Jennifer", "Linda", "Elizabeth", "Barbara", "Susan", "Jessica", "Sarah", "Karen",
            "Christopher", "Daniel", "Paul", "Mark", "Donald", "George", "Kenneth", "Steven", "Edward", "Brian",
            "Nancy", "Lisa", "Betty", "Margaret", "Sandra", "Ashley", "Kimberly", "Emily", "Donna", "Michelle"
        };

        int professor_num = 10;
        int student_num = 50;
        int cource_num = 17;
        // 生成教授
        Teacher[] teachers = new Teacher[professor_num];
        for (int i = 0; i < professor_num; i++) {
            String lastName = lastNames[rand.nextInt(lastNames.length)];
            String firstName = firstNames[rand.nextInt(firstNames.length)];
            
            String fullName = firstName + " " + lastName;
            teachers[i] = new Teacher("T00" + (i + 1), fullName, "1234");
            db.registerTeacher(teachers[i]);
        }

        // 生成學生
        Student[] students = new Student[student_num];
        for (int i = 0; i < student_num; i++) {

            String lastName = lastNames[rand.nextInt(lastNames.length)];
            String firstName = firstNames[rand.nextInt(firstNames.length)];
            
            String fullName = firstName + " " + lastName;

            String uid = String.format("B%03d", i + 1); 
            students[i] = new Student(uid, fullName, "0000");
            db.registerStudent(students[i]);
        }

        // 生成課程
        Course[] courses = new Course[cource_num];
        courses[0] = new Course("CS170", "Digital Electronics", 3, 50, new TimeSlot(1, 5, 7), teachers[0]);
        courses[1] = new Course("CS215", "Data Structures", 3, 50, new TimeSlot(4, 2, 4), teachers[1]);
        courses[2] = new Course("CS260", "Computer Organization", 3, 50, new TimeSlot(2, 2, 4), teachers[2]);
        courses[3] = new Course("CS280", "Probability", 3, 50, new TimeSlot(3, 2, 4), teachers[3]);
        courses[4] = new Course("CS391", "OOP", 3, 50, new TimeSlot(2, 6, 8), teachers[4]);
        courses[5] = new Course("CS350", "Computer Network", 3, 50, new TimeSlot(5, 6, 8), teachers[5]);
        courses[6] = new Course("CS365", "Unix System", 3, 50, new TimeSlot(1, 2, 4), teachers[6]);
        courses[7] = new Course("CS223", "Data Mining", 3, 50, new TimeSlot(4, 6, 8), teachers[7]);
        courses[8] = new Course("CS282", "PYTHON PROGRAMMING ", 3, 50, new TimeSlot(1, 6, 8), teachers[3]);
        courses[9] = new Course("CS445", "Wireless Internet", 3, 50, new TimeSlot(3, 5, 7), teachers[2]);
        courses[10] = new Course("CS466", "VLSI design", 3, 50, new TimeSlot(3, 5, 7), teachers[1]);
        courses[11] = new Course("CS480", "Information Security", 3, 50, new TimeSlot(4, 2, 4), teachers[5]);
        courses[12] = new Course("CS490", "Electronic Commerce", 3, 50, new TimeSlot(4, 2, 4), teachers[0]);
        courses[13] = new Course("CS491", "Network Applications", 3, 50, new TimeSlot(2, 2, 4), teachers[7]);
        courses[14] = new Course("EE1204", "Differential Equations", 3, 50, new TimeSlot(2, 2, 4), teachers[8]);
        courses[15] = new Course("EE2400", "Circuit Theory", 3, 50, new TimeSlot(2, 2, 4), teachers[9]);
        courses[16] = new Course("CS491", "Electromagetics", 3, 50, new TimeSlot(2, 2, 4), teachers[8]);

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
            
            db.addAnnouncement(c.getCourseId(), c.getTeacher().getUid(), "Welcome", "Please buy the textbook");
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
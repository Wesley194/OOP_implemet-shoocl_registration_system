import java.util.Random;

public class DatabaseTester {
    public static void main(String[] args) {
        SqliteDatabase db = new SqliteDatabase();
        Random rand = new Random();

        Teacher[] teachers = new Teacher[5];
        String[] teacherNames = {"G可驥", "楊惠F", "黃E哲", "陳嘉P", "克L迪"};
        for (int i = 0; i < 5; i++) {
            teachers[i] = new Teacher("T00" + (i + 1), teacherNames[i], "1234");
            db.registerTeacher(teachers[i]);
        }

        String[] lastNames = {"陳", "林", "黃", "張", "李", "王", "吳", "劉", "蔡", "楊", "許", "鄭", "謝", "洪", "郭", "邱", "曾", "廖", "賴", "徐"};
        String[] firstNameChars = {"冠", "宇", "宗", "翰", "柏", "廷", "承", "恩", "家", "豪", "雅", "婷", "欣", "妤", "詩", "涵", "品", "妍", "子", "晴", "建", "宏", "志", "明", "俊", "傑", "哲", "維", "佩", "珊", "芳", "如", "佳", "玲"};
        Student[] students = new Student[15];
        for (int i = 0; i < 15; i++) {

            String lastName = lastNames[rand.nextInt(lastNames.length)];
            
            int nameLength = (rand.nextInt(10) < 9) ? 2 : 1; 
            
    
            StringBuilder firstName = new StringBuilder();
            for (int j = 0; j < nameLength; j++) {
                firstName.append(firstNameChars[rand.nextInt(firstNameChars.length)]);
            }
            String fullName = lastName + firstName.toString();

            // 學號格式：B001 ~ B015
            String uid = String.format("B%03d", i + 1); 
            students[i] = new Student(uid, fullName, "0000");
            db.registerStudent(students[i]);
        }

        Course[] courses = new Course[5];
        courses[0] = new Course("CS170", "數位電子學", 3, 50, new TimeSlot(1, 2, 4), teachers[0]);
        courses[1] = new Course("CS215", "資料結構", 3, 50, new TimeSlot(2, 5, 7), teachers[1]);
        courses[2] = new Course("CS260", "計算機組織", 3, 50, new TimeSlot(3, 2, 4), teachers[2]);
        courses[3] = new Course("CS280", "機率學", 3, 50, new TimeSlot(4, 6, 8), teachers[3]);
        courses[4] = new Course("CS391", "物件導向程式設計", 3, 50, new TimeSlot(5, 2, 4), teachers[4]);

        for (Course c : courses) {
            db.addCourseToSystem(c);
        }

        for (Student s : students) {
            // 每個學生隨機選 2~4 門課
            int enrollCount = rand.nextInt(3) + 2; 
            for (int j = 0; j < enrollCount; j++) {
                Course randomCourse = courses[rand.nextInt(5)];
                db.saveEnrollment(s, randomCourse);
                
                if (rand.nextBoolean()) {
                    double randomScore = 60 + rand.nextInt(41);
                    db.updateGrade(s, randomCourse, randomScore);
                }
            }
        }

        System.out.println("   測試帳號提示：");
        System.out.println("   教授登入: T001 ~ T005 (密碼 1234)");
        System.out.println("   學生登入: B001 ~ B015 (密碼 0000)");
        
        db.closeConnection();
    }
}
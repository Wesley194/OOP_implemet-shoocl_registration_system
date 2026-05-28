// 課程滿員時使用的例外。
public class CourseFullException extends Exception {
    public CourseFullException(String message) {
        super(message);
    }
}

// 記錄課程的星期與上課節次。
public class TimeSlot {
    private int dayOfWeek;
    private int startPeriod;
    private int endPeriod;

    // 建立一個上課時段。
    public TimeSlot(int dayOfWeek, int startPeriod, int endPeriod) {
        this.dayOfWeek = dayOfWeek;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
    }

    // 檢查兩個時段是否衝堂。
    public boolean isConflictWith(TimeSlot other) {
        if (this.dayOfWeek != other.dayOfWeek) {
            return false;
        }
        return this.startPeriod <= other.endPeriod && this.endPeriod >= other.startPeriod;
    }

    // 提供時段資料給其他物件使用。
    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public int getStartPeriod() {
        return startPeriod;
    }

    public int getEndPeriod() {
        return endPeriod;
    }

    @Override
    public String toString() {
        return getDayName() + " (Periods " + startPeriod + "-" + endPeriod + ")";
    }

    // 把星期數字轉成英文顯示名稱。
    private String getDayName() {
        switch (dayOfWeek) {
            case 1:
                return "Monday";
            case 2:
                return "Tuesday";
            case 3:
                return "Wednesday";
            case 4:
                return "Thursday";
            case 5:
                return "Friday";
            default:
                return "Day " + dayOfWeek;
        }
    }
}

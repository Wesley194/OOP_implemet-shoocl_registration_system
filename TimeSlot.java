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
        return "星期" + dayOfWeek + " (" + startPeriod + "~" + endPeriod + "節)";
    }
}

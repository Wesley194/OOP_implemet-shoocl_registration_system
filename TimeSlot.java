public class TimeSlot {
    private int dayOfWeek; 
    private int startPeriod; 
    private int endPeriod;   

    public TimeSlot(int dayOfWeek, int startPeriod, int endPeriod) {
        this.dayOfWeek = dayOfWeek;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
    }

    public boolean isConflictWith(TimeSlot other) {
        if (this.dayOfWeek != other.dayOfWeek) return false;
        return this.startPeriod <= other.endPeriod && this.endPeriod >= other.startPeriod;
    }

    @Override
    public String toString() {
        return "星期" + dayOfWeek + " (" + startPeriod + "~" + endPeriod + "節)";
    }
    public int getDayOfWeek() { return dayOfWeek; }
    public int getStartPeriod() { return startPeriod; }
    public int getEndPeriod() { return endPeriod; }
}
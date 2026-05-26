public class SystemStateManager {
    public enum SystemPhase {
        CLOSED,
        PRE_ENROLL,
        LOTTERY_RUN,
        ADD_DROP
    }

    private SystemPhase currentPhase = SystemPhase.CLOSED;

    public void setCurrentPhase(SystemPhase phase) {
        this.currentPhase = phase;
        System.out.println("📢 系統廣播：目前選課階段已切換為 [" + phase + "]");
    }

    public SystemPhase getCurrentPhase() {
        return this.currentPhase;
    }

    public void requirePhase(SystemPhase expectedPhase, String actionName) throws Exception {
        if (this.currentPhase != expectedPhase) {
            throw new Exception("⛔ 目前不是「" + getPhaseName(expectedPhase) + "」時段，無法執行 " + actionName + "！");
        }
    }

    public void requireNotPhase(SystemPhase forbiddenPhase1, SystemPhase forbiddenPhase2, String actionName) throws Exception {
        if (this.currentPhase == forbiddenPhase1 || this.currentPhase == forbiddenPhase2) {
            throw new Exception("⛔ 抱歉，目前系統狀態禁止執行 " + actionName + "！");
        }
    }

    private String getPhaseName(SystemPhase phase) {
        switch (phase) {
            case CLOSED: return "系統關閉";
            case PRE_ENROLL: return "初選登記";
            case LOTTERY_RUN: return "抽籤分發";
            case ADD_DROP: return "加退選";
            default: return "未知";
        }
    }
}
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
        System.out.println("System broadcast: The course selection phase has now switched to [" + phase + "]");
    }

    public SystemPhase getCurrentPhase() {
        return this.currentPhase;
    }

    public void requirePhase(SystemPhase expectedPhase, String actionName) throws Exception {
        if (this.currentPhase != expectedPhase) {
            throw new Exception("It is not currently within the time frame of「" + getPhaseName(expectedPhase) + "」therefore " + actionName + " cannot be executed");
        }
    }

    public void requireNotPhase(SystemPhase forbiddenPhase1, SystemPhase forbiddenPhase2, String actionName) throws Exception {
        if (this.currentPhase == forbiddenPhase1 || this.currentPhase == forbiddenPhase2) {
            throw new Exception("Sorry, execution is currently disabled by the system! " + actionName + "！");
        }
    }

    // 改成public給前端用
    public String getPhaseName(SystemPhase phase) {
        switch (phase) {
            case CLOSED: return "System shutdown";
            case PRE_ENROLL: return "Preliminary registration";
            case LOTTERY_RUN: return "Distribution by lottery";
            case ADD_DROP: return "Add/Withdraw";
            default: return "unknown";
        }
    }
}